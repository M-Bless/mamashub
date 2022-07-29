package com.intellisoft.kabarakmhis.helperclass

import com.google.gson.annotations.SerializedName
import com.intellisoft.kabarakmhis.R
import java.time.LocalDate

enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url),
    FHIR_URL(R.string.fhir_url)
}
data class SuccessLogin(
    val details: String
)
data class UserLogin(
    val email: String,
    val password: String
)
data class UserResetPassword(
    val emailAddress: String,

    )
data class AuthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("token") val token: String,
    @SerializedName("issued") val issued: String,
    @SerializedName("expires") val expires: String,
    @SerializedName("newUser") val newUser: Boolean
    )
data class PatientItem(
    val id: String,
    val resourceId: String,
    val name: String,
    val gender: String,
    val dob: LocalDate? = null,
    val phone: String,
    val city: String,
    val country: String,
    val isActive: Boolean,
    val html: String,
    var risk: String? = "",
) {
    override fun toString(): String = name
}

data class DbPatientDetails(
    val id : String,
    val name : String,
)

data class RiskAssessmentItem(
    var riskStatusColor: Int,
    var riskStatus: String,
    var lastContacted: String,
    var patientCardColor: Int
)
data class RelatedPersonItem(
    val id: String,
    val name: String,
    val gender: String,
    val dob: String,
    var riskItem: RiskAssessmentItem? = null,
) {
    override fun toString(): String = name
}
data class ObservationItem(
    val id: String,
    val code: String,
    val text: String,
    val value: String
) {
    override fun toString(): String = code
}
data class ConditionItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}
enum class ViewTypes {
    HEADER,
    PATIENT,
    PATIENT_PROPERTY,
    ENCOUNTER,
    OBSERVATION,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ViewTypes {
            return values()[ordinal]
        }
    }

}
data class EncounterItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}
data class Steps(
    val fistIn: String?,
    val lastIn: String?,
    val secondButton: Boolean?
)
enum class ViewType {
    HEADER,
    PATIENT,
    CHILD,
    PATIENT_PROPERTY,
    RELATION,
    OBSERVATION,
    ENCOUNTER,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ViewType {
            return values()[ordinal]
        }
    }

}
/**
 * Encounter
 */


enum class ObservationViewTypes {
    OBSERVATION,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ObservationViewTypes {
            return values()[ordinal]
        }
    }

}

enum class Navigation{
    FRAGMENT,
    ACTIVITY
}



data class DbUserData(
    val data: DbData,
    val status: String
)
data class DbData(
    val id: String,
    val names: String,
    val email: String,
    val role: String
)
data class DbPatientRecord(
    val id: String,
    val name: String,
    val dob: String,
    val phone: String?,
    val kinData: DbKinData,
)
data class DbKinData(
    val name: String,
    val phone: String
)