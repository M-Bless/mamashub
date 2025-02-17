package com.kabarak.kabarakmhis.pnc.bcgvacination

import org.hl7.fhir.r4.model.Observation
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface FhirService {

    @POST("Observation")  // This endpoint will save the Observation resource
    fun saveObservation(@Body observation: Observation): Call<Observation>
}
