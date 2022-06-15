package com.intellisoft.kabarakmhis.network_request.interfaces

import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientSuccess
import retrofit2.Call
import retrofit2.http.*


val param = "$ everything"

interface Interface {


    @POST("auth/login/")
    fun loginUser(@Body user: UserLogin): Call<AuthResponse>

    @GET("auth/me")
    fun getUserData(@HeaderMap headers: Map<String, String>): Call<DbUserData>


    @POST("Patient")
    fun createFhirPatient(@Body dbPatient: DbPatient): Call<DbPatientSuccess>

    @GET("Patient")
    fun getPatientList(@Query("address-state") address_state:String): Call<DbPatientResult>

    @POST("Encounter")
    fun createFhirEncounter(@Body dbEncounter: DbEncounter): Call<DbEncounter>

    @PUT("Encounter/{encounterId}")
    fun updateFhirEncounter(@Body dbEncounter: DbEncounter): Call<DbEncounter>

    @GET("Encounter/")
    fun getEncounterList(@Query("patient") patient:String): Call<DbEncounterList>

    @GET("Encounter/{encounterId}/"+"$"+"everything")
    fun getEncounterDetails(
        @Path("encounterId") encounterId:String): Call<DbEncounterDetailsList>

    @POST("Observation")
    fun createFhirObservation(@Body dbObservation: DbObservation): Call<DbObservation>

    //This should be looked at
    @GET("Observation/{observationId}")
    fun getObservationDetails(
        @Path("observationId") observationId:String
    ): Call<DbEncounterDataResourceData>


}