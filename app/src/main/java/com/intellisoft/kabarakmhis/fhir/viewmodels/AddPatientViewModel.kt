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
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientFhirInformation
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentPatientInfo
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import java.time.LocalDate
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
        questionnaireResponse: QuestionnaireResponse){

        viewModelScope.launch {

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient){
                return@launch
            }

            val patient = entry.resource as Patient

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

            val patientId = FormatterClass().retrieveSharedPreference(
                getApplication<Application>().applicationContext, "FHIRID")

            patient.id = patientId

            patient.name

            val id = fhirEngine.create(patient)

            isPatientSaved.value = true
        }

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

        questionnaireJson = readFileFromAssets(state[FragmentPatientInfo.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaire!!
    }

    private fun readFileFromAssets(fileName : String): String{
        return getApplication<Application>().assets.open(fileName).bufferedReader().use {
            it.readText()
        }

    }






}