package com.kabarak.kabarakmhis.fhir.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.get
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.Search
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*


@RequiresApi(Build.VERSION_CODES.O)
class PatientListViewModel (application: Application, private val fhirEngine: FhirEngine): AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<DbPatientDetails>>()

    init {
        updatePatientListAndPatientCount { getSearchResults() }
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount { getSearchResults(nameQuery) }
    }

    fun getPatientList() = runBlocking{
        getSearchResults()
    }

    private fun updatePatientListAndPatientCount(search: suspend () -> List<DbPatientDetails>) {

        viewModelScope.launch {
            liveSearchedPatients.value = search()
        }
    }

    private suspend fun getSearchResults(nameQuery: String = ""): List<DbPatientDetails> {

        val clientList = ArrayList<DbPatientDetails>()

        val formatterClass = FormatterClass()
        val spinnerClientValue = formatterClass.retrieveSharedPreference(
            getApplication<Application>().applicationContext, "spinnerClientValue")

        val kmflCode = formatterClass.retrieveSharedPreference(
            getApplication<Application>().applicationContext, "kmhflCode")

        if (spinnerClientValue == "CLIENT_RECORDS"){

            val patientsList: MutableList<DbPatientDetails> = mutableListOf()
            //Get all records
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
                FormatterClass().patientData(patient, index + 1)
            }.let { patientsList.addAll(it) }

            patientsList.forEach {patient ->

                val id = patient.id
                val name = patient.name
                val kmflDbCode = patient.kmflCode

                val appointmentsList = getAppointmentEncounters(id)
                val appointment =if (appointmentsList.isNotEmpty()){
                    appointmentsList[0].value.trim()
                }else{
                    "-"
                }

                if (kmflDbCode != null && kmflDbCode != ""){
                    if (kmflDbCode == kmflCode){
                        val dbPatientDetails = DbPatientDetails(id, name, appointment)
                        clientList.add(dbPatientDetails)

                    }
                }

            }


        }else if (spinnerClientValue == "REFERRED"){

            val referralList: MutableList<DbServiceReferralRequest> = mutableListOf()

            //Get referred records from the service request
            val searchValue = FormatterClass().getCodes(ReferralTypes.REFERRAL_TO_FACILITY.name)
            fhirEngine.search<ServiceRequest>{

                filter(ServiceRequest.CODE, {value = of(Coding().apply { system = "http://snomed.info/sct"; code = searchValue })})
                sort(ServiceRequest.AUTHORED, Order.DESCENDING)

                count = 100
                from = 0
            }.mapIndexed { index, serviceRequest ->
                FormatterClass().serviceReferralRequest(serviceRequest, index + 1)
            }.let { referralList.addAll(it) }

            Log.e("referralList", referralList.toString())

            //Remove any referrals that are not from the current facility
//            referralList.removeAll { referral -> referral.locationDetails.facilityCode != kmflCode }

            referralList.forEach {

                val id = it.patient
                val patient = getPatientResource(id)
                val dbPatientDetailsData = FormatterClass().patientData(patient, 0)

                val patientId = dbPatientDetailsData.id
                val patientName = dbPatientDetailsData.name
                dbPatientDetailsData.dob

                val dbPatientDetails = DbPatientDetails(patientId, patientName, "-")
                clientList.add(dbPatientDetails)

            }


        }


        return clientList
    }

    private suspend fun getPatientResource(patientId: String): Patient {
        return fhirEngine.get(patientId)
    }

    private suspend fun getAppointmentEncounters(patientId: String): List<ObservationItem> {

        val formatter = FormatterClass()
        val encounter = mutableListOf<EncounterItem>()
        fhirEngine.search<Encounter>{
            filter(Encounter.REASON_CODE, {value = of(Coding().apply { code = DbResourceViews.PRESENT_PREGNANCY.name })})
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
                    filter(Observation.CODE, {value = of(Coding().apply { system = "http://snomed.info/sct"
                        code = formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name) })})
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

    private fun filterChvCity(search: Search) {

        FormatterClass().saveSharedPreference(getApplication<Application>().applicationContext,
            "status", "referred")
        search.filter(Patient.ORGANIZATION, {value = "CHW-TO-ORGANISATION"})
        search.filter(Patient.ACTIVE, {value = of(false)})
    }

    private fun filterCity(search: Search) {
        FormatterClass().saveSharedPreference(getApplication<Application>().applicationContext,
            "status", "all")
        search.filter(Patient.ADDRESS_COUNTRY, { value = "KENYA-KABARAK-MHIS6" })

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
