package com.intellisoft.kabarakmhis.new_designs.chw.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.Search
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays.sort
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*


@RequiresApi(Build.VERSION_CODES.O)
class ChwPatientListViewModel (application: Application, private val fhirEngine: FhirEngine): AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<DbChwPatientData>>()

    init {
        updatePatientListAndPatientCount { getSearchResults() }
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount { getSearchResults(nameQuery) }
    }

    fun getPatientList() = runBlocking{
        getSearchResults()
    }

    private fun updatePatientListAndPatientCount(search: suspend () -> List<DbChwPatientData>) {

        viewModelScope.launch {
            liveSearchedPatients.value = search()
        }
    }

    private suspend fun getSearchResults(nameQuery: String = "", spinnerClientValue: String=""): List<DbChwPatientData> {
        val patientsList: MutableList<DbPatientDetails> = mutableListOf()

        fhirEngine.search<Patient> {

            if (nameQuery.isNotEmpty()){
                filter(Patient.NAME, {
                    modifier = StringFilterModifier.CONTAINS
                    value = nameQuery
                })
            }

            if (spinnerClientValue.isNotEmpty()){
                if (spinnerClientValue =="Referred from"){
                    filterFrom(this)
                }else{
                    filterTo(this)
                }
            }else{
                filterTo(this)
            }

            filterReferTo(this)
            sort(Patient.FAMILY, Order.ASCENDING)
            count = 100
            from = 0

        }.mapIndexed { index, patient ->
            FormatterClass().patientData(patient, index + 1)
        }.let { patientsList.addAll(it) }

        val clientList = ArrayList<DbChwPatientData>()
        patientsList.forEach {patient ->

            val id = patient.id
            val name = patient.name
            val dob = patient.dob

            val referralList = getReferralDate(id)
            val appointment =if (referralList.isNotEmpty()){
                referralList[0].value.trim()
            }else{
                "----/--/--"
            }

            val dbPatientDetails = DbChwPatientData(id, name, dob, appointment, "")
            clientList.add(dbPatientDetails)

        }

        return clientList
    }

    private fun filterTo(search: Search) {
        search.filter(Patient.ORGANIZATION, {value = "CHW-TO-ORGANISATION"})
        search.filter(Patient.ACTIVE, {value = of(false)})
    }

    private fun filterFrom(search: Search) {
        search.filter(Patient.ORGANIZATION, {value = "ORGANISATION-TO-CHW"})
        search.filter(Patient.ACTIVE, {value = of(true)})
    }

    private suspend fun getReferralDate(patientId: String):List<ObservationItem>  {

        val formatter = FormatterClass()
        val encounter = mutableListOf<EncounterItem>()
        fhirEngine.search<Encounter>{
            filter(Encounter.REASON_CODE, {value = of(Coding().apply { code = DbResourceViews.PATIENT_INFO_CHV.name })})
            filter(Encounter.SUBJECT, {value = "Patient/$patientId"})
            sort(Encounter.DATE, Order.ASCENDING)
        }.take(Int.MAX_VALUE)
            .map { PatientDetailsViewModel.createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        val observations = mutableListOf<ObservationItem>()

        //Get next date observations from encounter
        encounter.forEach { encounterItem ->

            val encounterId = encounterItem.id

            fhirEngine
                .search<Observation> {
                    filter(Observation.CODE, {value = of(Coding().apply { system = "http://snomed.info/sct"; code = formatter.getCodes(DbObservationValues.DATE_STARTED.name) })})
                    filter(Observation.ENCOUNTER, {value = "Encounter/$encounterId"})
                    filter(Observation.SUBJECT, {value = "Patient/$patientId"})
                    sort(Observation.DATE, Order.ASCENDING)
                }
                .take(1)
                .map { PatientDetailsViewModel.createObservationItem(it, getApplication<Application>().resources)
                }
                .let { observations.addAll(it) }

        }

        return observations

    }

    private fun filterReferTo(search: Search) {

        FormatterClass().saveSharedPreference(getApplication<Application>().applicationContext,
            "chw-status", "Referred to")

        search.filter(Patient.ACTIVE, {value = of(false)})
        search.filter(Patient.ORGANIZATION, {value = "CHW-TO-ORGANISATION"})
    }

    class FhirFormatterClassViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChwPatientListViewModel(application, fhirEngine) as T
        }

    }


}
