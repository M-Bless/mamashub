package com.intellisoft.kabarakmhis.fhir.viewmodels

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
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.QuestionnaireHelper
import com.intellisoft.kabarakmhis.new_designs.data_class.CodingObservation
import com.intellisoft.kabarakmhis.new_designs.data_class.QuantityObservation
import com.intellisoft.kabarakmhis.new_designs.screens.FragmentConfirmDetails
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import java.util.*
import kotlin.collections.ArrayList

class AddPatientDetailsViewModel(application: Application, private val state: SavedStateHandle) :AndroidViewModel(application){

    val questionnaire : String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaireResource : Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire) as
                    Questionnaire
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private var questionnaireJson : String? = null

    fun savePatient(questionnaireResponse: QuestionnaireResponse){
        Log.e("******* ", "questionnaireResponse")
        println(questionnaireResponse)

        viewModelScope.launch {
            if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaireResource, questionnaireResponse, getApplication())
                    .values.flatten().any{
                        Log.e("*******2 ", "questionnaireResponse")
                        println(it)
                        !it.isValid})
            {


                isPatientSaved.value = false
                return@launch
            }


            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient){
                return@launch
            }

            val patient = entry.resource as Patient
            patient.id = FormatterClass().generateUuid()
            fhirEngine.create(patient)
            isPatientSaved.value = true
        }

    }

    private fun getQuestionnaireJson():String{
        questionnaireJson?.let { return it!! }

        questionnaireJson = readFileFromAssets(state[FragmentConfirmDetails.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaire!!
    }

    private fun readFileFromAssets(fileName : String): String{
        return getApplication<Application>().assets.open(fileName).bufferedReader().use {
            it.readText()
        }

    }

    fun updateEncounter(
        patientReference: Reference,
        encounterId: String,
        questionnaireResponse: QuestionnaireResponse,
        dataCodeList: ArrayList<CodingObservation>,
        dataQuantityList: ArrayList<QuantityObservation>,
    ){

        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse)

            val questionnaireHelper = QuestionnaireHelper()
            dataCodeList.forEach {
                bundle.addEntry()
                    .setResource(
                        questionnaireHelper.codingQuestionnaire(
                            it.code,
                            it.display,
                            it.value
                        )
                    )
                    .request.url = "Observation"

                Log.e("-----1 Obse ", dataCodeList.toString())
            }
            dataQuantityList.forEach {
                bundle.addEntry()
                    .setResource(
                        questionnaireHelper.quantityQuestionnaire(
                            it.code,
                            it.display,
                            it.display,
                            it.value,
                            it.unit,
                        )
                    )
                    .request.url = "Observation"
                Log.e("-----2 Obse ", dataQuantityList.toString())
            }

            updateEncounterResource(
                patientReference,
                encounterId,
                bundle
            )



        }

    }

    private suspend fun updateEncounterResource(
        patientReference: Reference,
        encounterId: String,
        bundle: Bundle
    ) {

        val encounterReference = Reference("Encounter/$encounterId")

        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = FormatterClass().generateUuid()
                        resource.subject = patientReference
                        resource.encounter = encounterReference
                        resource.issued = Date()
                        saveResourceToDatabase(resource)
                    }
                }


            }
        }
    }


    fun createEncounter(
        patientReference: Reference,
        encounterId: String,
        questionnaireResponse: QuestionnaireResponse,
        dataCodeList: ArrayList<CodingObservation>,
        dataQuantityList: ArrayList<QuantityObservation>,
        encounterReason: String
    ) {

        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse)

            val questionnaireHelper = QuestionnaireHelper()
            dataCodeList.forEach {
                bundle.addEntry()
                    .setResource(
                        questionnaireHelper.codingQuestionnaire(
                            it.code,
                            it.display,
                            it.value
                        )
                    )
                    .request.url = "Observation"

                Log.e("-----1 Obse ", dataCodeList.toString())
            }
            dataQuantityList.forEach {
                bundle.addEntry()
                    .setResource(
                        questionnaireHelper.quantityQuestionnaire(
                            it.code,
                            it.display,
                            it.display,
                            it.value,
                            it.unit,
                        )
                    )
                    .request.url = "Observation"
                Log.e("-----2 Obse ", dataQuantityList.toString())
            }

            saveResources(bundle, patientReference, encounterId, encounterReason)

        }

    }

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        reason: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")

        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = FormatterClass().generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        resource.issued = Date()
                        saveResourceToDatabase(resource)
                    }
                }
                is Encounter -> {
                    resource.subject = subjectReference
                    resource.id = encounterId
                    resource.reasonCodeFirstRep.text = reason
                    resource.reasonCodeFirstRep.codingFirstRep.code = reason
                    resource.status = Encounter.EncounterStatus.INPROGRESS
                    saveResourceToDatabase(resource)
                }

            }
        }
    }

    private suspend fun saveResourceToDatabase(resource: Resource) {
        val saved = fhirEngine.create(resource)
        Log.e("****Observations ", saved.toString())

    }
    private suspend fun updateResourceToDatabase(resource: Resource) {
        val update = fhirEngine.create(resource)
    }


}