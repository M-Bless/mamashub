package com.kabarak.kabarakmhis.new_designs.chw.viewmodel

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
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.data_class.CodingObservation
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.data_class.QuantityObservation
import com.kabarak.kabarakmhis.new_designs.new_patient.FragmentConfirmPatient
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.*
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
        encounterId: String,
        dbServiceReferralRequest: DbServiceReferralRequest
        ){

        viewModelScope.launch {

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep

            CoroutineScope(Dispatchers.IO).launch {

                val patientId = dbPatientFhirInformation.id

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    //Create Patient
                    val patient = Patient()
                    patient.active = false

                    //Add organization from the kmfhl name
                    val organisationReference = Reference()
                    organisationReference.reference = dbServiceReferralRequest.chwDetails.loggedInChwUnit
                    patient.managingOrganization = organisationReference

                    //Patient name
                    val name = dbPatientFhirInformation.name

                    val nameList = getNames(name, name)
                    patient.name = nameList

                    //Patient dob
                    val birthDate = dbPatientFhirInformation.dob
                    patient.birthDate = FormatterClass().convertStringToDate(birthDate)

                    //Patient Address
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
                    DbResourceViews.PATIENT_INFO_CHV.name,
                    dbServiceReferralRequest
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
        encounterReason: String,
        dbServiceReferralRequest: DbServiceReferralRequest
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

            createServiceRequest(patientReference, encounterId, dbServiceReferralRequest)
            createCarePlan(patientReference, encounterId, encounterReason)
            saveResources(bundle, patientReference, encounterId, encounterReason)

        }

    }

    private suspend fun createServiceRequest(
        patientReference: Reference,
        encounterId: String,
        dbServiceReferralRequest: DbServiceReferralRequest) {

        //Service Request for the referral
        val serviceRequest = ServiceRequest()
        serviceRequest.id = FormatterClass().generateUuid() //Generate a random id
        serviceRequest.status = ServiceRequest.ServiceRequestStatus.ACTIVE

        //Add Referral Type
        val referralType = CodeableConcept()
        val referralFacilityCode = FormatterClass().getCodes(ReferralTypes.REFERRAL_TO_FACILITY.name)
        referralType.text = ReferralTypes.REFERRAL_TO_FACILITY.name
        referralType.addCoding()
            .setCode(referralFacilityCode)
            .setDisplay(ReferralTypes.REFERRAL_TO_FACILITY.name)
            .system = "http://snomed.info/sct" //Set the type of referral; Referral to facility or referral to chw
        serviceRequest.code = referralType

        serviceRequest.subject = patientReference //Set the patient reference
        serviceRequest.encounter = Reference("Encounter/$encounterId") //Set the encounter reference

        val todayDateStr = FormatterClass().getTodayDate()
        val todayDate = FormatterClass().convertDdMMyyyy(todayDateStr)

        serviceRequest.authoredOn = todayDate  //Set the date the referral was made

        //Add the referral reason, intervention given, comments

        val codeConceptList = ArrayList<CodeableConcept>()
        val reasonCodeList = dbServiceReferralRequest.referralDetails
        reasonCodeList.forEach {

            val text = it.text
            val code = it.code
            val display = it.display

            val codeableConcept = CodeableConcept()
            codeableConcept.text = text
            codeableConcept.addCoding()
                .setCode(code)
                .setDisplay(display)
                .system = "http://snomed.info/sct"

            codeConceptList.add(codeableConcept)
        }


        serviceRequest.reasonCode = codeConceptList

        //Add the Main reason for referral using supporting info

        val supportingReferenceList = ArrayList<Reference>()
        val supportingInfoList = dbServiceReferralRequest.supportingInfo
        supportingInfoList.forEach {

            val reference = it.reference
            val display = it.display

            val supportingInfo = Reference()
            supportingInfo.reference = reference
            supportingInfo.display = display
            supportingReferenceList.add(supportingInfo)
        }


        serviceRequest.supportingInfo = supportingReferenceList

        //Add the action taken using note
        val actionTaken = dbServiceReferralRequest.actionTaken
        val note = Annotation()
        note.text = actionTaken
        serviceRequest.note = listOf(note)

        //Who is requesting service

        val chwDetails = dbServiceReferralRequest.chwDetails

        val performerReference = Reference()
        performerReference.reference = chwDetails.loggedInChwUnit //TODO: Change to the logged in chw unit details
        performerReference.display = chwDetails.loggedInUserId //TODO: Change to the logged in chw's name/id
        serviceRequest.requester = performerReference

        //Person receiving the service

        val dbClinicianDetails = dbServiceReferralRequest.clinicianDetails

        val recipientReference = Reference()
        recipientReference.reference = dbClinicianDetails.clinicianRole //TODO: Change to the provider's role
        recipientReference.display = dbClinicianDetails.clinicianId //TODO: Change to the provider's name or id
        serviceRequest.performer = listOf(recipientReference)

        //Location receiving the service, which is the same as the one sending for now
        val dbLocation = dbServiceReferralRequest.locationDetails

        val locationReference = Reference()
        locationReference.reference = dbLocation.facilityCode //TODO: Change to the facility kmfl code
        locationReference.display = dbLocation.facilityName //TODO: Change to the facility name
        serviceRequest.locationReference = listOf(locationReference) //Set the facility details

        fhirEngine.create(serviceRequest)

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