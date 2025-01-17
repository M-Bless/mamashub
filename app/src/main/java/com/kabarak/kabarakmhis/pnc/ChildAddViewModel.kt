package com.kabarak.kabarakmhis.pnc

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.pnc.extensions.readFileFromAssets
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import java.util.UUID

/** ViewModel for patient registration screen */
class ChildAddViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire_file_path_key"
    }

    private var _questionnaireJson: String? = null
    val questionnaireJson: String
        get() = fetchQuestionnaireJson()

    val isChildSaved = MutableLiveData<Boolean>()

    private val identifier: String
        get() = state.get<String>("identifier") ?: throw IllegalStateException("Identifier not found in SavedStateHandle")

    private val questionnaire: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaireJson)
                    as Questionnaire

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    fun saveChild(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            try {
                // Validate the response
                val validationErrors = QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaire,
                    questionnaireResponse,
                    getApplication()
                ).values.flatten()

                if (validationErrors.any { it is Invalid }) {
                    Log.e("saveChild", "Validation failed: $validationErrors")
                    isChildSaved.postValue(false)
                    return@launch
                }

                // Extract all resources from the questionnaire response
                val extractedResources = ResourceMapper.extract(questionnaire, questionnaireResponse)
                val resourcesToSave = mutableListOf<Resource>()

                // Process each resource in the extracted bundle
                extractedResources.entry.forEach { entry ->
                    entry.resource?.let { resource ->
                        resource.id = generateUuid(identifier)

                        // Dynamically set patient reference if applicable
                        val patientReference = "Patient/${generateUuid(identifier)}"
                        when (resource) {
                            is DomainResource -> {
                                setReferences(resource, patientReference, questionnaireResponse)
                                resourcesToSave.add(resource)
                            }
                            else -> Log.e("saveChild", "Unsupported resource type: ${resource::class.java.simpleName}")
                        }
                        Log.d("saveChild", "Resource to save: ${resource::class.java.simpleName}")
                    }
                }

                // Ensure there is at least one resource to save
                if (resourcesToSave.isEmpty()) {
                    Log.e("saveChild", "No valid resources extracted to save")
                    isChildSaved.postValue(false)
                    return@launch
                }

                // Save all extracted resources
                resourcesToSave.forEach { resource -> saveResource(resource) }

                // Resource(s) were successfully saved
                isChildSaved.postValue(true)
            } catch (e: Exception) {
                Log.e("saveChild", "Error saving resource", e)
                isChildSaved.postValue(false)
            }
        }
    }

    private suspend fun saveResource(resource: Resource) {
        try {
            // Log the resource being saved
            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser().setPrettyPrint(true)
            val resourceJson = jsonParser.encodeResourceToString(resource)
            Log.d("saveChild", "${resource::class.java.simpleName} resource being saved: $resourceJson")

            // Save the resource to the FHIR engine
            fhirEngine.create(resource)
        } catch (e: Exception) {
            Log.e("saveResource", "Error saving ${resource::class.java.simpleName}", e)
            isChildSaved.postValue(false)
        }
    }

    private fun setReferences(resource: DomainResource, patientReference: String, questionnaireResponse: QuestionnaireResponse) {
        // Check for birthDate in the questionnaire response and modify patient reference accordingly
        val birthDate = questionnaireResponse.item.find { it.linkId == "birthDate" }?.answer?.get(0)?.valueDateType?.value

        // Modify the patient ID if birthDate is present
        val patientReferenceWithIdentifier = if (birthDate != null) {
            // Ensure that the patient ID starts with the identifier if birthDate is present
            val newId = "$identifier-${UUID.randomUUID()}"
            resource.id = newId  // Set the new ID with the identifier prefix
            "Patient/$newId"
        } else {
            patientReference
        }

        // Set references for all relevant resources
        when (resource) {
            is Observation -> resource.subject.reference = patientReferenceWithIdentifier
            is Encounter -> resource.subject.reference = patientReferenceWithIdentifier
            is MedicationRequest -> resource.subject.reference = patientReferenceWithIdentifier
            is Condition -> resource.subject.reference = patientReferenceWithIdentifier
            is Immunization -> resource.patient.reference = patientReferenceWithIdentifier
            is Location -> {
                resource.managingOrganization = Reference(patientReferenceWithIdentifier)
            }
        }
    }

    private fun fetchQuestionnaireJson(): String {
        _questionnaireJson?.let {
            return it
        }
        val filePath: String? = state.get<String>(QUESTIONNAIRE_FILE_PATH_KEY)
        if (filePath.isNullOrEmpty()) {
            throw IllegalStateException("File path key is not set in SavedStateHandle")
        }
        _questionnaireJson = getApplication<Application>().readFileFromAssets(filePath)
        return _questionnaireJson!!
    }

    private fun generateUuid(prefix: String): String {
        return "${UUID.randomUUID()}"
    }
}
