package com.intellisoft.kabarakmhis.fhir.viewmodels

import android.app.Application
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
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

        val kabarakViewModel = KabarakViewModel(getApplication())
        val patientResource = getPatientResource()

        val patientId = if (patientResource.hasIdElement()) patientResource.idElement.idPart else ""
        val name = if (patientResource.hasName()) patientResource.name[0].nameAsSingleString else ""
        val dob =
            if (patientResource.hasBirthDateElement())
                LocalDate.parse(patientResource.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
            else null

        val kinName = if (patientResource.hasContact()) patientResource.contact[0].name.nameAsSingleString else ""
        val kinPhone = if (patientResource.hasContact()) patientResource.contact[0].telecom[0].value else ""
        val phone = if (patientResource.hasTelecom()) patientResource.telecom[0].value else ""

        val encountersList = getEncounterDetails()

        val dbEncounterList = mutableListOf<DbEncounterResult>()
        if (encountersList.isNotEmpty()) {

            val tetanusList = ArrayList<DbFhirEncounter>()

            for (encounter in encountersList){

                val encounterId = encounter.id
                val lastUpdated = encounter.effective
                val reasonCode = encounter.code
                val value = encounter.value

                FormatterClass().saveSharedPreference(getApplication<Application>().applicationContext,
                    value, encounterId)

                val observationsList = getPatientObservations(encounterId)

                val dbEncounter = DbEncounterResult(encounterId,value, lastUpdated, reasonCode, observationsList)
                dbEncounterList.add(dbEncounter)

                Log.e("encounterId", value)

                if (value == DbResourceViews.TETENUS_DIPTHERIA.name){

                    val dbFhirEncounter = DbFhirEncounter(encounterId, value, DbResourceViews.TETENUS_DIPTHERIA.name)
                    tetanusList.add(dbFhirEncounter)
                }

            }

            tetanusList.forEachIndexed { index, dbFhirEncounter ->

                val tetanusNo = "TT${index + 1}"
                val encounterId = dbFhirEncounter.id
                val encounterName = dbFhirEncounter.encounterName
                val encounterType = dbFhirEncounter.encounterType

                val dbFhirEncounterDetails = DbFhirEncounter(encounterId, tetanusNo, encounterType)
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
                phone = kinPhone
            )
        )

    }


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

//        Log.e("******* ", "*******")
//        println("--patientId--$patientId")
//        println(observations)

        return observations
    }

    private suspend fun observationsPerCode(key: String): List<ObservationItem> {
        val obs: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://snomed.info/sct"
                            code = key
                        })
                    })
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                sort(Observation.DATE, Order.DESCENDING)
            }
            .take(5)
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { obs.addAll(it) }
        return obs
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

        private fun createObservationItem(observation: Observation, resources: Resources): ObservationItem{

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

//            Log.e("******* ", "*******")
//            println("--id--$id")
//            println("--code--$code")
//            println("--text--$text")
//            println("--valueString--$valueString")

            return ObservationItem(
                id,
                code,
                text,
                valueString
            )
        }

        private fun createEncounterItem(encounter: Encounter, resources: Resources): EncounterItem{

            val encounterDateTimeString =
                if (encounter.hasPeriod()) {
                    encounter.period.start.time.toString()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }

            val encounterType = encounter.type.firstOrNull()?.coding?.firstOrNull()?.display ?: ""
            val encounterLocation = encounter.location.firstOrNull()?.location?.display ?: ""
            val encounterStatus = encounter.status.display

            var lastUpdatedValue = ""
            val lastUpdated = encounter.meta.lastUpdated
            lastUpdatedValue = lastUpdated?.toString() ?: ""

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

            Log.e("*_*_*_*_*","--------")
            Log.e("1",reasonCode)




            return EncounterItem(
                encounter.logicalId,
                textValue,
                lastUpdatedValue,
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

