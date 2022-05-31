package com.intellisoft.kabarakmhis.network_request.interfaces

import com.intellisoft.kabarakmhis.helperclass.*
import retrofit2.Call
import retrofit2.http.*


interface Interface {

    @POST("auth/login/")
    fun loginUser(@Body user: UserLogin): Call<AuthResponse>

}