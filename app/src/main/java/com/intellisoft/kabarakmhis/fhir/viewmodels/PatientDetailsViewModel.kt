package com.intellisoft.kabarakmhis.fhir.viewmodels

import android.app.Application
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import ca.uhn.fhir.parser.RDFParser.ID
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.get
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.FhirEncounter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.EnumSet.of
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String): AndroidViewModel(application) {

    var livePatientData = MutableLiveData<DbPatientRecord>()

    init {
        updatePatientData()
    }

    private fun updatePatientData() {

        viewModelScope.launch {
            getPatientDetailData()
        }
    }

    /** Emits list of [PatientDetailData]. */
    private fun getPatientDetailData() {
//        viewModelScope.launch { livePatientData = getPatientDetailDataModel() }
    }

    fun getPatientData() = runBlocking{
        getPatientDetailDataModel()
    }

    fun getObservationsEncounter(encounterId: String) = runBlocking{
        getPatientObservations(encounterId)
    }

    private suspend fun getPatient(): DbPatientRecord? {

        val patient = getPatientResource()

        return null
//        return FormatterClass().patientData(patient, 0)
    }

    private suspend fun getPatientResource(): Patient {
        return fhirEngine.get(patientId)
    }

    private suspend fun getPatientDetailDataModel():DbPatientRecord{

        val formatter = FormatterClass()

        val kabarakViewModel = KabarakViewModel(getApplication())
        val patientResource = getPatientResource()

        val patientId = if (patientResource.hasIdElement()) patientResource.idElement.idPart else ""
        val name = if (patientResource.hasName()) patientResource.name[0].family else ""

        val dob =
            if (patientResource.hasBirthDateElement())
                LocalDate.parse(patientResource.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
            else null

        val kinName = if (patientResource.hasContact()) patientResource.contact[0].name.nameAsSingleString else ""
        val kinPhone = if (patientResource.hasContact()){

            if (patientResource.contact[0].hasTelecom())
                patientResource.contact[0].telecom[0].value
            else
                ""
        } else ""
        val kinRelationship = if (patientResource.hasContact()){

            if (patientResource.contact[0].hasRelationship())
                patientResource.contact[0].relationship[0].text
            else
                ""
        } else ""

        val phone = if (patientResource.hasTelecom()) patientResource.telecom[0].value else ""

        val maritalStatus = if (patientResource.hasMaritalStatus()) patientResource.maritalStatus.text else ""

        val addressDataList = ArrayList<DbAddressData>()
        if (patientResource.hasAddress()){

            val address = patientResource.address
            if (!address.isNullOrEmpty()){

                val addressList = patientResource.address[0]

                val text = if (addressList.hasText()) addressList.text else ""
                val city = if (addressList.hasCity()) addressList.city else ""
                val district = if (addressList.hasDistrict()) addressList.district else ""
                val state = if (addressList.hasState()) addressList.state else ""

                val dbAddressData = DbAddressData(text, city, district, state)
                addressDataList.add(dbAddressData)
            }

        }


        val identifierList = ArrayList<DbIdentifier>()
        if (patientResource.hasIdentifier()){
            for (identifier in patientResource.identifier){
                val system = identifier.id
                val value = identifier.value
                if (value != null){
                    identifierList.add(DbIdentifier(system, value))
                }

            }
        }


        val encountersList = getEncounterDetails()

        val dbEncounterList = mutableListOf<DbEncounterResult>()
        if (encountersList.isNotEmpty()) {

            val tetanusList = ArrayList<DbFhirEncounter>()
            val physicalExamList = ArrayList<DbFhirEncounter>()
            val previousPregList = ArrayList<DbFhirEncounter>()
            val clinicalNoteList = ArrayList<DbFhirEncounter>()
            val presentPregList = ArrayList<DbFhirEncounter>()
            val malariaProphylaxisList = ArrayList<DbFhirEncounter>()
            val ifasList = ArrayList<DbFhirEncounter>()

            for (encounter in encountersList){

                val encounterId = encounter.id
                val encounterDate = encounter.effective
                val reasonCode = encounter.code
                val value = encounter.value

                FormatterClass().saveSharedPreference(getApplication<Application>().applicationContext,
                    value, encounterId)

                val observationsList = getPatientObservations(encounterId)

                val dbEncounter = DbEncounterResult(encounterId,value, encounterDate, reasonCode, observationsList)
                dbEncounterList.add(dbEncounter)

                Log.e("value", value)

                when (value) {
                    DbResourceViews.TETENUS_DIPTHERIA.name -> {
                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.TETENUS_DIPTHERIA.name, encounterDate)
                        tetanusList.add(dbFhirEncounter)
                    }
                    DbResourceViews.PHYSICAL_EXAMINATION.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.PHYSICAL_EXAMINATION.name, encounterDate)
                        physicalExamList.add(dbFhirEncounter)
                    }
                    DbResourceViews.PREVIOUS_PREGNANCY.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.PREVIOUS_PREGNANCY.name, encounterDate)
                        previousPregList.add(dbFhirEncounter)
                    }
                    DbResourceViews.CLINICAL_NOTES.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.CLINICAL_NOTES.name, encounterDate)
                        clinicalNoteList.add(dbFhirEncounter)
                    }
                    DbResourceViews.PRESENT_PREGNANCY.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.PRESENT_PREGNANCY.name, encounterDate)
                        presentPregList.add(dbFhirEncounter)
                    }
                    DbResourceViews.MALARIA_PROPHYLAXIS.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.MALARIA_PROPHYLAXIS.name, encounterDate)
                        malariaProphylaxisList.add(dbFhirEncounter)
                    }
                    DbResourceViews.IFAS.name -> {

                        val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.IFAS.name, encounterDate)
                        ifasList.add(dbFhirEncounter)
                    }

                }

            }

            //Tetanus Diphtheria
            var tetanusNoValue = 0
            tetanusList.forEachIndexed { index, dbFhirEncounter ->

                tetanusNoValue += 1

                val tetanusNo = "TT${index + 1}"
                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, tetanusNo, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }
            val tetanus = "$tetanusNoValue of 5"
            formatter.saveSharedPreference(getApplication<Application>().applicationContext,
                "${DbResourceViews.TETENUS_DIPTHERIA.name}_SUMMARY", tetanus)

            //Physical Examination
            var physicalNo = 0
            physicalExamList.forEachIndexed { index, dbFhirEncounter ->

                physicalNo += 1

                val encounterNameDetail = when (index + 1) {
                    1 -> {
                        "First Visit"
                    }
                    2 -> {
                        "Second Visit"
                    }
                    3 -> {
                        "Third Visit"
                    }
                    4 -> {
                        "Fourth Visit"
                    }
                    5 -> {
                        "Fifth Visit"
                    }
                    6 -> {
                        "Sixth Visit"
                    }
                    7 -> {
                        "Seventh Visit"
                    }
                    8 -> {
                        "Eighth Visit"
                    }
                    9 -> {
                        "Ninth Visit"
                    }
                    10 -> {
                        "Tenth Visit"
                    }
                    else -> {
                        "Nth Visit"
                    }
                }

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }
            val physicalExam = "$physicalNo of 8"
            formatter.saveSharedPreference(getApplication<Application>().applicationContext,
                "${DbResourceViews.PHYSICAL_EXAMINATION.name}_SUMMARY", physicalExam)

            //Previous Pregnancy
            previousPregList.forEachIndexed { index, dbFhirEncounter ->

                val encounterNameDetail = when (index + 1) {
                    1 -> {
                        "First pregnancy"
                    }
                    2 -> {
                        "Second pregnancy"
                    }
                    3 -> {
                        "Third pregnancy"
                    }
                    4 -> {
                        "Fourth pregnancy"
                    }
                    5 -> {
                        "Fifth pregnancy"
                    }
                    6 -> {
                        "Sixth pregnancy"
                    }
                    7 -> {
                        "Seventh pregnancy"
                    }
                    8 -> {
                        "Eighth pregnancy"
                    }
                    9 -> {
                        "Ninth pregnancy"
                    }
                    10 -> {
                        "Tenth pregnancy"
                    }
                    else -> {
                        "Nth pregnancy"
                    }
                }

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }

            //Present Pregnancy
            var presentPregNo = 0
            presentPregList.forEachIndexed { index, dbFhirEncounter ->

                presentPregNo += 1
                val encounterNameDetail = when (index + 1) {
                    1 -> {
                        "1st Contact"
                    }
                    2 -> {
                        "2nd Contact"
                    }
                    3 -> {
                        "3rd Contact"
                    }
                    4 -> {
                        "4th Contact"
                    }
                    5 -> {
                        "5th Contact"
                    }
                    6 -> {
                        "6th Contact"
                    }
                    7 -> {
                        "7th Contact"
                    }
                    8 -> {
                        "8th Contact"
                    }
                    9 -> {
                        "9th Contact"
                    }
                    10 -> {
                        "10th Contact"
                    }
                    else -> {
                        "Nth Contact"
                    }
                }

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }
            val presentPreg = "$presentPregNo of 8"
            formatter.saveSharedPreference(getApplication<Application>().applicationContext,
                "${DbResourceViews.PRESENT_PREGNANCY.name}_SUMMARY", presentPreg)

            //Malaria Prophylaxis
            var malariaProphylaxisNo = 0
            malariaProphylaxisList.forEachIndexed { index, dbFhirEncounter ->

                malariaProphylaxisNo += 1

                val encounterNameDetail = "ANC Contact ${index + 1}"

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }
            val malariaProphylaxis = "$malariaProphylaxisNo of 8"
            formatter.saveSharedPreference(getApplication<Application>().applicationContext,
                "${DbResourceViews.MALARIA_PROPHYLAXIS.name}_SUMMARY", malariaProphylaxis)

            //IFAS
            var ifasNo = 0
            ifasList.forEachIndexed { index, dbFhirEncounter ->

                ifasNo += 1

                val encounterNameDetail = if (index == 0) {
                     "First Contact Before ANC"
                } else {
                    "ANC Contact ${index + 1}"
                }

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)

            }
            val ifas = "$ifasNo of 9"
            formatter.saveSharedPreference(getApplication<Application>().applicationContext,
                "${DbResourceViews.IFAS.name}_SUMMARY", ifas)

            //Clinical Notes
            clinicalNoteList.forEachIndexed { index, dbFhirEncounter ->

                val encounterNameDetail = "Clinical Note ${index + 1}"

                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType
                val encounterDate = dbFhirEncounter.encounterDate

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, encounterNameDetail, encounterType, encounterDate)
                kabarakViewModel.insertFhirEncounter(getApplication<Application>().applicationContext, dbFhirEncounterDetails)



            }


        }

        return DbPatientRecord(
            id = patientId,
            name = name,
            dob = dob.toString(),
            phone = phone,
            kinData = DbKinData(
                name = kinName,
                phone = kinPhone,
                relationship = kinRelationship
            ),
            identifier = identifierList,
            maritalStatus = maritalStatus,
            address = addressDataList,
        )

    }

    fun getClientAppointment(dbClientDetails: DbPatientDetails, encounterName: String) = runBlocking {
        clientAppointments(dbClientDetails, encounterName)
    }

    private suspend fun clientAppointments(dbClientDetails: DbPatientDetails, encounterName: String): List<DbPatientDetails>{

        val dbPatientDetailsList = mutableListOf<DbPatientDetails>()

        val patientId = dbClientDetails.id
        val name = dbClientDetails.name

        val formatter = FormatterClass()

        //Get the encounter details
        val encounter = mutableListOf<EncounterItem>()
        fhirEngine.search<Encounter>{
            filter(Encounter.REASON_CODE, {value = of(Coding().apply { code = encounterName })})
            filter(Encounter.SUBJECT, {value = "Patient/$patientId"})
            sort(Encounter.DATE, Order.ASCENDING)
        }.take(Int.MAX_VALUE)
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        encounter.forEach { encounterData->

            //Get Observation Data on next visit from the encounter
            val observationValues = getAppointmentData(patientId, encounterData.id, formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name))
            observationValues.forEach {

                val value = it.value

                val dbPatientDetails = DbPatientDetails(
                    id = patientId,
                    name = name,
                    lastUpdated = value
                )

                dbPatientDetailsList.add(dbPatientDetails)

            }

        }

        return dbPatientDetailsList
    }

    private suspend fun getAppointmentData(patientId: String, encounterId: String, codeValue: String): List<ObservationItem>{

        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.CODE, {value = of(Coding().apply { system = "http://snomed.info/sct"; code = codeValue })})
                filter(Observation.ENCOUNTER, {value = "Encounter/$encounterId"})
                filter(Observation.SUBJECT, {value = "Patient/$patientId"})
                sort(Observation.DATE, Order.ASCENDING)
            }
            .take(1)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }
        return observations

    }

    fun getObservationFromEncounter(encounterName: String) = runBlocking {
        observationFromEncounter(encounterName)
    }

    private suspend fun observationFromEncounter(encounterName: String) : List<EncounterItem>{

        val encounter = mutableListOf<EncounterItem>()

        /**
         * Filter encounters by Location which will be the KMFL Code from the logged in person
         */

        fhirEngine.search<Encounter>{
            filter(Encounter.REASON_CODE, {value = of(Coding().apply { code = encounterName })})
            filter(Encounter.SUBJECT, {value = "Patient/$patientId"})
            sort(Encounter.DATE, Order.ASCENDING)
        }.take(Int.MAX_VALUE)
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        encounter.sortBy { it.effective}

        return encounter
    }

    //Get encounter details from the encounter id



    fun getObservationsFromEncounter(encounterId: String) = runBlocking{
        getPatientObservations(encounterId)
    }

    //Get all observations for patient under the selected encounter
    private suspend fun getPatientObservations(encounterId: String): List<ObservationItem> {



        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.ENCOUNTER, {value = "Encounter/$encounterId"})
            }
            .take(Int.MAX_VALUE)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }


        return observations
    }

    fun getObservationsPerCode(codeValue: String) = runBlocking{
        observationsPerCode(codeValue)
    }

    private suspend fun observationsPerCode(codeValue: String): List<ObservationItem>{

        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.CODE, {value = of(Coding().apply {
                    system = "http://snomed.info/sct"; code = codeValue
                })})
                filter(Observation.SUBJECT, {value = "Patient/$patientId"})
            }
            .take(1)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }

        return observations

    }

    fun getObservationsPerCodeFromEncounter(codeValue: String, encounterId: String) = runBlocking{
        observationsPerCodeFromEncounter(codeValue, encounterId)
    }

    private suspend fun observationsPerCodeFromEncounter(codeValue: String, encounterId: String): List<ObservationItem>{

        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.CODE, {value = of(Coding().apply { system = "http://snomed.info/sct"; code = codeValue })})
                filter(Observation.ENCOUNTER, {value = "Encounter/$encounterId"})
                filter(Observation.SUBJECT, {value = "Patient/$patientId"})
                sort(Observation.DATE, Order.ASCENDING)
            }
            .take(1)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }

        return observations

    }

    //Get all encounters under this patient
    private suspend fun getEncounterDetails():List<EncounterItem>{

        val encounter = mutableListOf<EncounterItem>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        return encounter
    }



    companion object{

        fun createObservationItem(observation: Observation, resources: Resources): ObservationItem{


            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasEffectiveDateTimeType()) {
                    observation.effectiveDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }


            val id = observation.logicalId
            val text = observation.code.text ?: observation.code.codingFirstRep.display
            val code = observation.code.coding[0].code
            val value =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.value.toString()
                } else if (observation.hasValueCodeableConcept()) {
                    observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                }else if (observation.hasValueStringType()) {
                    observation.valueStringType.asStringValue().toString() ?: ""
                }else {
                    ""
                }
            val valueUnit =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.unit ?: observation.valueQuantity.code
                } else {
                    ""
                }
            val valueString = "$value $valueUnit"

            return ObservationItem(
                id,
                code,
                text,
                valueString
            )
        }

        fun createEncounterItem(encounter: Encounter, resources: Resources): EncounterItem{

            val encounterDate =
                if (encounter.hasPeriod()) {
                    if (encounter.period.hasStart()) {
                        encounter.period.start
                    } else {
                        ""
                    }
                } else {
                    ""
                }

            var lastUpdatedValue = ""

            if (encounter.hasMeta()){
                if (encounter.meta.hasLastUpdated()){
                    lastUpdatedValue = encounter.meta.lastUpdated.toString()
                }
            }

            val reasonCode = encounter.reasonCode.firstOrNull()?.text ?: ""

            var textValue = ""

            if(encounter.reasonCode.size > 0){

                val text = encounter.reasonCode[0].text
                val textString = encounter.reasonCode[0].text?.toString() ?: ""
                val textStringValue = encounter.reasonCode[0].coding[0].code ?: ""

                textValue = if (textString != "") {
                    textString
                }else if (textStringValue != ""){
                    textStringValue
                }else text ?: ""

            }

            val encounterDateStr = if (encounterDate != "") {
                encounterDate.toString()
            } else {
                lastUpdatedValue
            }

            return EncounterItem(
                encounter.logicalId,
                textValue,
                encounterDateStr,
                reasonCode
            )
        }
    }

    class PatientDetailsViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val patientId: String
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PatientDetailsViewModel(application, fhirEngine, patientId) as T
        }

    }


}

