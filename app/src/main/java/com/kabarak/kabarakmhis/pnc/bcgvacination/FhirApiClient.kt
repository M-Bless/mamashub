package com.kabarak.kabarakmhis.pnc.bcgvacination

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FhirApiClient {

    // Function to create and return the FhirService instance
    fun getFhirApiService(): FhirService {
        // Build the Retrofit instance
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://41.89.93.172/fhir/")  // Make sure this is your correct FHIR server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create and return the FhirService instance
        return retrofit.create(FhirService::class.java)
    }
}
