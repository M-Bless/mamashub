package com.intellisoft.kabarakmhis.network_request.interfaces

import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientSuccess
import retrofit2.Call
import retrofit2.http.*


interface Interface {

    @POST("auth/login/")
    fun loginUser(@Body user: UserLogin): Call<AuthResponse>

    @GET("auth/me")
    fun getUserData(@HeaderMap headers: Map<String, String>): Call<DbUserData>


    @POST("Patient")
    fun createFhirPatient(@Body dbPatient: DbPatient): Call<DbPatientSuccess>

    @GET("Patient")
    fun getPatientList(@Query("address-state") address_state:String): Call<DbPatientResult>

}