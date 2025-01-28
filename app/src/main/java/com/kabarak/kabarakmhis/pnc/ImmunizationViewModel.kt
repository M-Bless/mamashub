package com.kabarak.kabarakmhis.pnc
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.kabarak.kabarakmhis.fhir.FhirApplication
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.DateTimeType
import java.util.UUID

class ImmunizationViewModel(application: Application) : AndroidViewModel(application) {

    val isImmunizationSaved = MutableLiveData<Boolean>()
    private val fhirEngine: FhirEngine = FhirApplication.fhirEngine(application)

    fun saveImmunization(questionnaireJson: String, questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            try {
                val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)
                val questionnaire = fhirContext.newJsonParser()
                    .parseResource(Questionnaire::class.java, questionnaireJson)

                // Validate the response
                val validationErrors = QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaire,
                    questionnaireResponse,
                    getApplication()
                ).values.flatten()

                if (validationErrors.any { it is Invalid }) {
                    Log.e("saveImmunization", "Validation failed: $validationErrors")
                    isImmunizationSaved.postValue(false)
                    return@launch
                }

                // Extract resources
                val extractedResources = ResourceMapper.extract(questionnaire, questionnaireResponse)
                val immunizationResources = extractedResources.entry.mapNotNull {
                    it.resource as? Immunization
                }

                if (immunizationResources.isEmpty()) {
                    Log.e("saveImmunization", "No Immunization resources extracted")
                    isImmunizationSaved.postValue(false)
                    return@launch
                }

                // Save Immunization resources
                immunizationResources.forEach { immunization ->
                    immunization.id = UUID.randomUUID().toString()

                    // Log the resource in JSON format
                    logResource(immunization)

                    saveResource(immunization)
                }

                isImmunizationSaved.postValue(true)
            } catch (e: Exception) {
                Log.e("saveImmunization", "Error saving Immunization resource", e)
                isImmunizationSaved.postValue(false)
            }
        }
    }

    private suspend fun saveResource(resource: Immunization) {
        try {
            fhirEngine.create(resource)
            Log.d("saveResource", "Immunization saved: ${resource.id}")
        } catch (e: Exception) {
            Log.e("saveResource", "Error saving resource: ${resource.id}", e)
            isImmunizationSaved.postValue(false)
        }
    }

    private fun logResource(resource: Immunization) {
        try {
            val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)
            val jsonParser = fhirContext.newJsonParser().setPrettyPrint(true)
            val resourceJson = jsonParser.encodeResourceToString(resource)

            // Log the resource in JSON format
            Log.d("logResource", "Resource JSON:\n$resourceJson")
        } catch (e: Exception) {
            Log.e("logResource", "Error converting resource to JSON", e)
        }
    }
}
