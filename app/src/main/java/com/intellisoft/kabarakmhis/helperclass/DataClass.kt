package com.intellisoft.kabarakmhis.helperclass

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