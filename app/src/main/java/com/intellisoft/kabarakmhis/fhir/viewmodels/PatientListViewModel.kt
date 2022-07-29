package com.intellisoft.kabarakmhis.fhir.viewmodels

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
import com.intellisoft.kabarakmhis.helperclass.DbPatientDetails
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.PatientItem
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays.sort
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
class PatientListViewModel (application: Application, private val fhirEngine: FhirEngine): AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<DbPatientDetails>>()

    init {
        updatePatientListAndPatientCount { getSearchResults() }
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount { getSearchResults(nameQuery) }
    }

    private fun updatePatientListAndPatientCount(search: suspend () -> List<DbPatientDetails>) {

        viewModelScope.launch {
            liveSearchedPatients.value = search()
        }
    }

    private suspend fun getSearchResults(nameQuery: String = ""): List<DbPatientDetails> {
        val patients: MutableList<DbPatientDetails> = mutableListOf()


        fhirEngine.search<Patient> {

            if (nameQuery.isNotEmpty()){
                filter(Patient.NAME, {
                    modifier = StringFilterModifier.CONTAINS
                    value = nameQuery
                })
            }
            filterCity(this)
            sort(Patient.FAMILY, Order.ASCENDING)
            count = 100
            from = 0

        }.mapIndexed { index, patient ->

            FormatterClass().patientData(patient, index + 1)}
            .let {

                patients.addAll(it)
            }

        return patients
    }

    private fun filterCity(search: Search) {
        search.filter(Patient.ADDRESS_COUNTRY, { value = "Kenya" })
    }

    class FhirFormatterClassViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PatientListViewModel(application, fhirEngine) as T
        }

    }


}
