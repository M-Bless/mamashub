package com.intellisoft.kabarakmhis.helperclass

import com.google.gson.annotations.SerializedName
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailData

enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url)
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
    val dob: String,
    val phone: String,
    val city: String,
    val country: String,
    val isActive: Boolean,
    val html: String,
    var risk: String? = "",
    var riskItem: RiskAssessmentItem? = null,
    var state: String,
    var district: String,
    var region: String
) {
    override fun toString(): String = name
}
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
    val effective: String,
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
data class PatientDetailEncounter(
    val encounter: EncounterItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

enum class ObservationViewTypes {
    OBSERVATION,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ObservationViewTypes {
            return values()[ordinal]
        }
    }

}