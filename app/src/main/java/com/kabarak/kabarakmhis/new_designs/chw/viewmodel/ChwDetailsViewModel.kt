package com.kabarak.kabarakmhis.new_designs.chw.viewmodel

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.get
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.data_class.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*
import java.util.*
import java.util.stream.Stream
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class ChwDetailsViewModel(
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

    private suspend fun getPatientResource(): Patient {
        return fhirEngine.get(patientId)
    }

    private suspend fun getPatientDetailDataModel(): List<DbConfirmDetails> {

        val patient = getPatientResource()
        val patientData = FormatterClass().patientData(patient, 1)

        patientData.name
        patientData.dob
        val id = patientData.id

        return getChwDetails(id)
    }

    private fun getObservationData(dbObservationFhirData:DbObservationFhirData, encounterId: String):ArrayList<DbConfirmDetails>{

        val observationDataList = ArrayList<DbConfirmDetails>()
        val detailsList = ArrayList<DbObserveValue>()

        dbObservationFhirData.codeList.forEach {

            val list = getObservationsPerCodeFromEncounter(it, encounterId)
            list.forEach { obs ->
                val text = obs.text
                val value = obs.value
                val dbObserveValue = DbObserveValue(text, value)
                detailsList.add(dbObserveValue)
            }

        }
        val dbConfirmDetails = DbConfirmDetails(dbObservationFhirData.title, detailsList)
        observationDataList.add(dbConfirmDetails)

        return observationDataList

    }



    private suspend fun getChwDetails(id: String): List<DbConfirmDetails> {

        val formatter = FormatterClass()

        val text1 = DbObservationFhirData(
            DbSummaryTitle.A_PATIENT_DATA.name, listOf(
                formatter.getCodes(DbObservationValues.DATE_STARTED.name),
                formatter.getCodes(DbObservationValues.BABY_SEX.name),
                formatter.getCodes(DbObservationValues.TIMING_CONTACT.name)))

        val text2 = DbObservationFhirData(
            DbSummaryTitle.B_COMMUNITY_HEALTH_FACILITY_DETAILS.name,
            listOf(
                formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_UNIT.name),formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_LINK.name),
                formatter.getCodes(DbObservationValues.REFERRAL_REASON.name),formatter.getCodes(DbObservationValues.MAIN_PROBLEM.name),
                formatter.getCodes(DbObservationValues.CHW_INTERVENTION_GIVEN.name),formatter.getCodes(DbObservationValues.CHW_COMMENTS.name),
            ))

        val text3 = DbObservationFhirData(
            DbSummaryTitle.C_CHV_REFERRING_THE_PATIENT.name,
            listOf(
                formatter.getCodes(DbObservationValues.OFFICER_NAME.name),formatter.getCodes(DbObservationValues.OFFICER_NUMBER.name),
                formatter.getCodes(DbObservationValues.TOWN_NAME.name),formatter.getCodes(DbObservationValues.SUB_COUNTY_NAME.name),
                formatter.getCodes(DbObservationValues.COUNTY_NAME.name),formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_UNIT.name),
            )
        )

        val encounter = mutableListOf<EncounterItem>()
        fhirEngine.search<Encounter>{
            filter(Encounter.REASON_CODE, {value = of(Coding().apply { code = DbResourceViews.PATIENT_INFO_CHV.name })})
            filter(Encounter.SUBJECT, {value = "Patient/$id"})
            sort(Encounter.DATE, Order.ASCENDING)
        }.take(Int.MAX_VALUE)
            .map { PatientDetailsViewModel.createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        var observationDataList = listOf<DbConfirmDetails>()

        if (encounter.isNotEmpty()){

            val encounterId = encounter[0].id

            val text1List = getObservationData(text1, encounterId)
            val text2List = getObservationData(text2, encounterId)
            val text3List = getObservationData(text3, encounterId)

            observationDataList = merge(text1List, text2List, text3List)

        }


        return observationDataList
    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third).forEach { item: List<T>? -> list.addAll(item!!) }
        return list
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
            var issuedDate = ""
            if (observation.hasIssued()){
                issuedDate = observation.issued.toString()
            }else{

                if (observation.hasMeta()){
                    if (observation.meta.hasLastUpdated()){
                        issuedDate = observation.meta.lastUpdated.toString()
                    }else{
                        ""
                    }
                }else{
                    ""
                }

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

            if (issuedDate != ""){
                val newDate = FormatterClass().convertFhirDate(issuedDate)
                if (newDate != null){
                    issuedDate = newDate
                }
            }

            return ObservationItem(
                id,
                code,
                text,
                valueString,
                issuedDate
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
            return ChwDetailsViewModel(application, fhirEngine, patientId) as T
        }

    }


}

