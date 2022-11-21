package com.kabarak.kabarakmhis.fhir.viewmodels

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
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.helperclass.QuestionnaireHelper
import com.kabarak.kabarakmhis.new_designs.data_class.CodingObservation
import com.kabarak.kabarakmhis.new_designs.data_class.DbPatientFhirInformation
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.data_class.QuantityObservation
import com.kabarak.kabarakmhis.new_designs.new_patient.FragmentConfirmPatient
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.*
import java.util.*
import kotlin.collections.ArrayList

class AddPatientViewModel(application: Application, private val state: SavedStateHandle) :AndroidViewModel(application){

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
        dbPatientFhirInformation: DbPatientFhirInformation,
        questionnaireResponse: QuestionnaireResponse,
        encounterId: String
        ){

        viewModelScope.launch {

            CoroutineScope(Dispatchers.IO).launch {

                val patientId = dbPatientFhirInformation.id

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    val patient = Patient()

                    patient.active = true

                    val name = dbPatientFhirInformation.name

                    val nameList = getNames(name, name)
                    patient.name = nameList

                    val birthDate = dbPatientFhirInformation.birthDate
                    patient.birthDate = FormatterClass().convertStringToDate(birthDate)

                    val addressList = ArrayList<Address>()

                    val dbAddressList = dbPatientFhirInformation.addressList
                    dbAddressList.forEach {

                        val text = it.text
                        val line = it.line
                        val city = it.city
                        val district = it.district
                        val state = it.state
                        val country = it.country

                        val address = Address()
                        address.state = state
                        address.city = city
                        address.district = district
                        address.country = country
                        address.text = text

                        addressList.add(address)

                    }

                    patient.address = addressList

                    val kinDetailsList = ArrayList<Patient.ContactComponent>()
                    val dbKinList = dbPatientFhirInformation.kinList
                    dbKinList.forEach {

                        val relationShp = it.relationship
                        val kinName = it.name
                        val kinPhoneList = it.telecom

                        val contact = Patient.ContactComponent()
                        val humanName = HumanName()
                        humanName.family = kinName
                        contact.name = humanName

                        val rshpList = ArrayList<CodeableConcept>()
                        val rshp = CodeableConcept()
                        rshp.text = relationShp
                        rshpList.add(rshp)
                        contact.relationship = rshpList

                        val phoneList = ArrayList<ContactPoint>()
                        kinPhoneList.forEach {kinNo ->
                            val phone = ContactPoint()
                            phone.value = kinNo.value
                            phoneList.add(phone)
                        }
                        contact.telecom = phoneList
                        kinDetailsList.add(contact)

                    }

                    patient.contact = kinDetailsList

                    val userContactList = dbPatientFhirInformation.telecomList
                    val contactList = ArrayList<ContactPoint>()
                    userContactList.forEach {

                        val contact = ContactPoint()
                        contact.value = it.value
                        contact.system = ContactPoint.ContactPointSystem.PHONE
                        contactList.add(contact)
                    }

                    patient.telecom = contactList

                    val dbMaritalStatus = dbPatientFhirInformation.maritalStatus
                    val maritalStatus = CodeableConcept()
                    maritalStatus.text = dbMaritalStatus
                    patient.maritalStatus = maritalStatus

                    patient.id = patientId

                    val ancCode = dbPatientFhirInformation.identifier
                    val nationalId = dbPatientFhirInformation.nationalId

                    val identifierList = ArrayList<Identifier>()


                    /**
                     * TODO: Leave identifierNumber.value empty for now.
                     * TODO: Identifier value should be generated from the server
                     * TODO: Identifier assigner should be the KMFL CODE
                     *
                     * TODO: The KMFL CODE should be Attached to the encounter as a Location
                     */

                    val identifierNumber = Identifier()
                    identifierNumber.id = "NATIONAL_ID"
                    identifierNumber.value = nationalId
                    identifierList.add(identifierNumber)

                    val identifier = Identifier()
                    identifier.id = "ANC_NUMBER"
                    identifier.value = ancCode
                    identifierList.add(identifier)

                    //Get the KMFL CODE from the shared preferences
                    val kmhflCode = FormatterClass().retrieveSharedPreference(getApplication(), "kmhflCode")

                    val identifierKmflCode = Identifier()
                    identifier.id = "KMHFL_CODE"
                    identifier.value = kmhflCode
                    identifierList.add(identifierKmflCode)

                    patient.identifier = identifierList

                    fhirEngine.create(patient)

                }.join()
                delay(3500)

                val patientReference = Reference("Patient/$patientId")

                val dataCodeList = dbPatientFhirInformation.dataCodeList
                val dataQuantityList = dbPatientFhirInformation.dataQuantityList

                createEncounter(
                    patientReference,
                    encounterId,
                    questionnaireResponse,
                    dataCodeList,
                    dataQuantityList,
                    DbResourceViews.PATIENT_INFO.name
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

            saveResources(bundle, patientReference, encounterId, encounterReason)
            createCarePlan(patientReference, encounterId, encounterReason)
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
        fhirEngine.create(resource)
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