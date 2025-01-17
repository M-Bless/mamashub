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

class ChildProfileViewModel(application: Application, private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    val childProfile = MutableLiveData<ChildProfile?>()

    fun fetchChildProfile(childId: String) {
        viewModelScope.launch {
            try {
                val patient = fhirEngine.load(Patient::class.java, childId)
                childProfile.value = patient.toChildProfile()
            } catch (e: Exception) {
                childProfile.value = null
            }
        }
    }

    data class ChildProfile(
        val id: String,
        val name: String,
        val gender: String,
        val dob: String
    )

    class ChildProfileViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChildProfileViewModel::class.java)) {
                return ChildProfileViewModel(application, fhirEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private fun Patient.toChildProfile(): ChildProfileViewModel.ChildProfile {
    val name = if (hasName()) nameFirstRep.nameAsSingleString else "Unknown"
    val gender = if (hasGender()) gender.display else "Unknown"
    val dob = if (hasBirthDate()) birthDate.toString() else "Unknown"

    return ChildProfileViewModel.ChildProfile(
        id = idElement.idPart ?: "Unknown",
        name = name,
        gender = gender,
        dob = dob
    )
}
