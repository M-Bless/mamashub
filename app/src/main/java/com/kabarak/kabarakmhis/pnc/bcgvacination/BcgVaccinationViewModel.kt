package com.kabarak.kabarakmhis.pnc.bcgvacination



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.kabarak.kabarakmhis.fhir.FhirApplication
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
class BcgVaccinationViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    val isResourcesSaved = MutableLiveData<Boolean>()

    // The questionnaireJson should not be null, handle it properly
    private var questionnaireJson: String? = null

    private val fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    init {
        // Ensure the questionnaireJson is initialized properly
        questionnaireJson = state["questionnaire_json"]
    }

    private val questionnaireResource: Questionnaire
        get() {
            // If questionnaireJson is null, we can throw an exception or log it
            requireNotNull(questionnaireJson) { "Questionnaire JSON is null!" }
            return FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaireJson) as Questionnaire
        }

    /**
     * Saves BCG vaccination questionnaire response into the application database and FHIR server.
     *
     * @param questionnaireResponse BCG vaccination questionnaire response
     * @param patientId patient ID for reference
     */
    fun saveBcgVaccinationResponse(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()

            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }
            saveResources(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String
    ) {
        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach { entry ->
            when (val resource = entry.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference

                        // Save to remote FHIR server using Retrofit
                        saveObservationToServer(resource)
                    }
                }
            }
        }
    }

    private fun saveObservationToServer(observation: Observation) {
        val fhirApiClient = FhirApiClient() // Get the client with the base URL pointing to your server
        val fhirApiService = fhirApiClient.getFhirApiService()

        // Send the Observation resource to the FHIR server
        val call = fhirApiService.saveObservation(observation)

        // Make the network request
        call.enqueue(object : Callback<Observation> {
            override fun onResponse(call: Call<Observation>, response: Response<Observation>) {
                if (response.isSuccessful) {
                    // Successfully saved to the FHIR server
                    viewModelScope.launch {
                        saveResourceToDatabase(observation)
                    }
                } else {
                    // Handle error (response.errorBody() could provide more details)
                    println("Error saving to FHIR server: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Observation>, t: Throwable) {
                // Handle failure (network issue, server down, etc.)
                t.printStackTrace()
            }
        })
    }

    private suspend fun saveResourceToDatabase(resource: Resource) {
        // Save the resource locally in the FHIR Engine
        fhirEngine.create(resource)
    }

    private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }
}
