package com.intellisoft.kabarakmhis.helperclass

import com.google.gson.annotations.SerializedName
import com.intellisoft.kabarakmhis.R

enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url)
}
data class SuccessLogin(
    val details: String
)
data class UserLogin(
    val emailAddress: String,
    val password: String
)
data class UserResetPassword(
    val emailAddress: String,

    )
data class AuthResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("issued") val issued: String?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("newUser") val newUser: Boolean?,
    @SerializedName("_reset_url") val _reset_url: String?,

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