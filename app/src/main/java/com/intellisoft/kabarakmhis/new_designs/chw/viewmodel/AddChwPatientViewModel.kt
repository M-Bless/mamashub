package com.intellisoft.kabarakmhis.new_designs.chw.viewmodel

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
import com.intellisoft.kabarakmhis.helperclass.DbChwData
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.QuestionnaireHelper
import com.intellisoft.kabarakmhis.new_designs.data_class.CodingObservation
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientFhirInformation
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.data_class.QuantityObservation
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentConfirmPatient
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentPatientInfo
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.*
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class AddChwPatientViewModel(application: Application, private val state: SavedStateHandle) :AndroidViewModel(application){

    val questionnaire : String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaireResource : Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire) as
                    Questionnaire
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private var questionnaireJson : String? = null

    fun savePatient(
        dbPatientFhirInformation: DbChwData,
        questionnaireResponse: QuestionnaireResponse,
        encounterId: String
        ){

        viewModelScope.launch {

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep

            CoroutineScope(Dispatchers.IO).launch {

                val patientId = dbPatientFhirInformation.id

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    val patient = Patient()
                    val name = dbPatientFhirInformation.name

                    val nameList = getNames(name, name)
                    patient.name = nameList

                    val birthDate = dbPatientFhirInformation.dob
                    patient.birthDate = FormatterClass().convertStringToDate(birthDate)

                    val identifierList = ArrayList<Identifier>()
                    val identifierNumber = Identifier()
                    identifierNumber.system = "CHV-REFERRAL"
                    identifierList.add(identifierNumber)
                    patient.identifier = identifierList


                    val addressList = ArrayList<Address>()

                    val address = Address()
                    address.country = "KENYA-KABARAK-MHIS-CHW"
                    addressList.add(address)

                    patient.address = addressList


                    patient.id = patientId

                    fhirEngine.create(patient)

                }.join()
                delay(2000)

                val patientReference = Reference("Patient/$patientId")


                val dataCodeList = dbPatientFhirInformation.dataCodeList
                val dataQuantityList = dbPatientFhirInformation.dataQuantityList

                createEncounter(
                    patientReference,
                    encounterId,
                    questionnaireResponse,
                    dataCodeList,
                    dataQuantityList,
                    DbResourceViews.PATIENT_INFO_CHV.name
                )

            }


            isPatientSaved.value = true
        }

    }

    //Create CarePlan
    private suspend fun createCarePlan(
        patientReference: Reference,
        encounterId: String,
        encounterReason: String
    ) {

        val encounterReference = Reference("Encounter/$encounterId")

        val carePlan = CarePlan()
        carePlan.id = FormatterClass().generateUuid()
        carePlan.subject = patientReference
        carePlan.status = CarePlan.CarePlanStatus.ACTIVE
        carePlan.intent = CarePlan.CarePlanIntent.PLAN
        carePlan.encounter = encounterReference
        carePlan.title = encounterReason

        fhirEngine.create(carePlan)

    }

    private fun createEncounter(
        patientReference: Reference,
        encounterId: String,
        questionnaireResponse: QuestionnaireResponse,
        dataCodeList: ArrayList<CodingObservation>,
        dataQuantityList: ArrayList<QuantityObservation>,
        encounterReason: String
    ) {

        viewModelScope.launch {

            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
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
            }

            createCarePlan(patientReference, encounterId, encounterReason)
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
        Log.e("++++ ", "4")
        val saved = fhirEngine.create(resource)
        Log.e("****Observations ", saved.toString())

    }

    fun getNames(
        firstname: String,
        other_name: String): List<HumanName> {
        return listOf(
            HumanName()
                .addGiven(firstname)
                .setFamily(other_name)
                .setUse(HumanName.NameUse.OFFICIAL)
        )
    }

    private fun getQuestionnaireJson():String{
        questionnaireJson?.let { return it!! }

        questionnaireJson = readFileFromAssets(state[FragmentConfirmPatient.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaire!!
    }

    private fun readFileFromAssets(fileName : String): String{
        return getApplication<Application>().assets.open(fileName).bufferedReader().use {
            it.readText()
        }

    }






}