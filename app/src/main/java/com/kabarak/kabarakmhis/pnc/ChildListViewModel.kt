package com.kabarak.kabarakmhis.pnc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient

class ChildListViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val identifier: String // The identifier passed from the Fragment
) : AndroidViewModel(application) {

    val liveSearchedChildren = MutableLiveData<List<ChildItem>>() // For displaying fetched patients
    val childCount = MutableLiveData<Long>()

    init {
        // Fetch patient data based on the identifier
        fetchPatientData()
    }

    private fun fetchPatientData() {
        viewModelScope.launch {
            // Fetch patients based on the identifier
            val patients = getPatients()
            liveSearchedChildren.value = patients
            childCount.value = patients.size.toLong() // Count number of patients
        }
    }

    private suspend fun getPatients(): List<ChildItem> {
        val children: MutableList<ChildItem> = mutableListOf()
        try {
            // Fetching all patients and filter them manually based on the identifier
            val patients = fhirEngine.search<Patient> {
                // Fetch all patients, no need to apply a filter in the search itself
                count = 100  // Adjust the count as per requirement
                from = 0
            }

            // Manually filter patients whose ID contains the identifier
            val filteredPatients = patients.filter { it.id.contains(identifier, ignoreCase = true) }

            // Map patients to ChildItem
            filteredPatients.mapIndexed { index, fhirPatient ->
                children.add(fhirPatient.toChildItem(index + 1)) // Using toChildItem to map Patient to ChildItem
            }
        } catch (e: Exception) {
            // Handle error, log it, or display a message to the user
        }
        return children
    }

    data class ChildItem(
        val id: String,
        val resourceId: String,
        val name: String,
        val gender: String,
        val dob: String
    )

    // Factory for creating the ViewModel
    class ChildListViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val identifier: String // Pass identifier to the factory
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChildListViewModel::class.java)) {
                return ChildListViewModel(application, fhirEngine, identifier) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Extension function to map a Patient to a ChildItem
private fun Patient.toChildItem(index: Int): ChildListViewModel.ChildItem {
    val name = if (hasName()) nameFirstRep.nameAsSingleString else "Unknown"
    val gender = if (hasGender()) gender.display else "Unknown"
    val dob = if (hasBirthDate()) birthDate.toString() else "Unknown"

    return ChildListViewModel.ChildItem(
        id = idElement.idPart ?: "Unknown",
        resourceId = idElement.idPart ?: "Unknown",
        name = name,
        gender = gender,
        dob = dob
    )
}
