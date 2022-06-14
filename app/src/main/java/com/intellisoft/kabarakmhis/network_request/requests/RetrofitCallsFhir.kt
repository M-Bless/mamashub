package com.intellisoft.kabarakmhis.network_request.requests

import android.app.ProgressDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.intellisoft.kabarakmhis.fhir.data.SYNC_VALUE
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.network_request.builder.RetrofitBuilder
import com.intellisoft.kabarakmhis.network_request.interfaces.Interface
import com.intellisoft.kabarakmhis.new_designs.NewMainActivity
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RetrofitCallsFhir {

    fun createPatient(context: Context, dbPatient: DbPatient) {

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                createPatientBac(context, dbPatient)

            }.join()
        }

    }

    private suspend fun createPatientBac(context: Context, dbPatient: DbPatient) {


        val job1 = Job()
        CoroutineScope(Dispatchers.Main + job1).launch {

            var progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait..")
            progressDialog.setMessage("Patient creation in progress..")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()


            var messageToast = ""
            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                var formatter = FormatterClass()

                val baseUrl = context.getString(UrlData.FHIR_URL.message)

                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
                val apiInterface = apiService.createFhirPatient(dbPatient)
                apiInterface.enqueue(object : Callback<DbPatientSuccess> {
                    override fun onResponse(
                        call: Call<DbPatientSuccess>,
                        response: Response<DbPatientSuccess>
                    ) {

                        CoroutineScope(Dispatchers.Main).launch { progressDialog.dismiss() }

                        if (response.isSuccessful) {
                            messageToast = "User details added successfully."

                            val responseData = response.body()

                            if (responseData != null) {

                                Log.e("*** ", responseData.toString())

                            }

//                            val intent = Intent(context, NewMainActivity::class.java)
//                            context.startActivity(intent)

                            CoroutineScope(Dispatchers.Main).launch {

                                Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()

                            }

                        } else {

                            progressDialog.dismiss()

                            val code = response.code()
                            val message = response.errorBody().toString()

                            if (code != 500) {

                                val jObjError = JSONObject(response.errorBody()?.string())

                                CoroutineScope(Dispatchers.IO).launch {

                                    messageToast = "There was an issue."

//                                    messageToast = Formatter().getObjectiveKeys(
//                                        jObjError
//                                    ).toString()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, messageToast, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                }

                            } else {
                                messageToast =
                                    "We are experiencing some server issues. Please try again later"

                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                                }
                            }


                        }


                    }

                    override fun onFailure(call: Call<DbPatientSuccess>, t: Throwable) {
                        Log.e("-*-*error ", t.localizedMessage)
                        messageToast = "There is something wrong. Please try again"
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                        }

                        progressDialog.dismiss()
                    }
                })


            }.join()

        }

    }

    fun getPatients(context: Context) = runBlocking {

        getPatientsBac(context)

    }

    private suspend fun getPatientsBac(context: Context): DbPatientResult {

        var confList = DbPatientResult(0, ArrayList())
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {


            val baseUrl = context.getString(UrlData.FHIR_URL.message)

            val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
            val callSync: Call<DbPatientResult> = apiService.getPatientList(SYNC_VALUE)

            try {
                val response: Response<DbPatientResult> = callSync.execute()
                if (response.isSuccessful) {

                    val list = response.body()
                    if (list != null) {
                        confList = list
                    }

                }


            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }.join()

        return confList

    }

    fun createFhirEncounter(
        context: Context,
        dbCode: DbCode,
        encounterType: String
    ) {

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                createFhirEncounterBac(context, dbCode, encounterType)

            }.join()
        }

    }

    private suspend fun createFhirEncounterBac(
        context: Context,
        dbCode: DbCode,
        encounterType: String
    ) {


        val job1 = Job()
        CoroutineScope(Dispatchers.Main + job1).launch {

            var progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait..")
            progressDialog.setMessage("Saving in progress..")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()


            var messageToast = ""
            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                val formatter = FormatterClass()
                val patientId = formatter.retrieveSharedPreference(context, "patientId")

                val id = formatter.generateUuid()
                val subject = DbSubject("Patient/$patientId")
                val reasonCodeList = ArrayList<DbReasonCode>()
                val dbReasonCode = DbReasonCode(encounterType)
                reasonCodeList.add(dbReasonCode)

                val dbEncounter =
                    DbEncounter(DbResourceType.Encounter.name, id, subject, reasonCodeList)

                val baseUrl = context.getString(UrlData.FHIR_URL.message)

                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
                val apiInterface = apiService.createFhirEncounter(dbEncounter)
                apiInterface.enqueue(object : Callback<DbEncounter> {
                    override fun onResponse(
                        call: Call<DbEncounter>,
                        response: Response<DbEncounter>
                    ) {

                        CoroutineScope(Dispatchers.Main).launch { progressDialog.dismiss() }

                        if (response.isSuccessful) {
                            messageToast = "User details added successfully."

                            val responseData = response.body()

                            if (responseData != null) {
                                val encounterId = responseData.id
                                createObservation(encounterId,context, dbCode)

                                Log.e("*** ", responseData.toString())

                            }



                            CoroutineScope(Dispatchers.Main).launch {

                                Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()

                            }

                        } else {

                            progressDialog.dismiss()

                            val code = response.code()
                            val message = response.errorBody().toString()

                            if (code != 500) {

                                val jObjError = JSONObject(response.errorBody()?.string())

                                CoroutineScope(Dispatchers.IO).launch {

                                    messageToast = "There was an issue."

//                                    messageToast = Formatter().getObjectiveKeys(
//                                        jObjError
//                                    ).toString()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, messageToast, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                }

                            } else {
                                messageToast =
                                    "We are experiencing some server issues. Please try again later"

                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                                }
                            }


                        }


                    }

                    override fun onFailure(call: Call<DbEncounter>, t: Throwable) {
                        Log.e("-*-*error ", t.localizedMessage)
                        messageToast = "There is something wrong. Please try again"
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                        }

                        progressDialog.dismiss()
                    }
                })


            }.join()

        }

    }

    fun createObservation(encounterId: String, context: Context, dbCode: DbCode) {

        CoroutineScope(Dispatchers.IO).launch {

            val formatter = FormatterClass()

            val patientId = formatter.retrieveSharedPreference(context, "patientId")
            val id = formatter.generateUuid()
            val subject = DbSubject("Patient/$patientId")
            val encounter = DbEncounterData("Encounter/$encounterId")

            val dbObservation =
                DbObservation(DbResourceType.Observation.name, id, subject, encounter, dbCode)

            val baseUrl = context.getString(UrlData.FHIR_URL.message)

            val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
            val apiInterface = apiService.createFhirObservation(dbObservation)

            apiInterface.enqueue(object : Callback<DbObservation> {
                override fun onResponse(
                    call: Call<DbObservation>,
                    response: Response<DbObservation>
                ) {

                    if (response.isSuccessful) {

                        val responseData = response.body()

                        if (responseData != null) {

                            Log.e("***1 ", responseData.toString())

                        }



                        CoroutineScope(Dispatchers.Main).launch {

                            Toast.makeText(
                                context,
                                "User data has been saved successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(context, NewMainActivity::class.java)
                            context.startActivity(intent)

                        }

                    } else {

                        val code = response.code()
                        val message = response.errorBody().toString()
                        Log.e("***2 ", response.toString())

                        if (code != 500) {

//                            val jObjError = JSONObject(response.errorBody()?.string())

                            CoroutineScope(Dispatchers.Main).launch {

                                Toast.makeText(
                                    context,
                                    "There was an issue. Please try again after some time.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        } else {
                            val messageToast =
                                "We are experiencing some server issues. Please try again later"

                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                            }
                        }


                    }


                }

                override fun onFailure(call: Call<DbObservation>, t: Throwable) {
                    Log.e("-*-*error ", t.localizedMessage)
                    val messageToast = "There is something wrong. Please try again"
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
                    }

//                    progressDialog.dismiss()
                }
            })

        }

    }


    fun getPatientEncounters(context: Context) = runBlocking {
        getPatientEncountersBac(context)
    }

    private suspend fun getPatientEncountersBac(context: Context) {

        coroutineScope {

            launch(Dispatchers.IO) {
                val formatter = FormatterClass()
                val patientId = formatter.retrieveSharedPreference(context, "patientId")

                nukeEncounters(context)

                val baseUrl = context.getString(UrlData.FHIR_URL.message)

                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)

                if (patientId != null) {

                    val callSync: Call<DbEncounterList> = apiService.getEncounterList(patientId)

                    try {
                        val response: Response<DbEncounterList> = callSync.execute()
                        if (response.isSuccessful) {

                            val list = response.body()
                            if (list != null) {

                                val encounterList = list.entry
                                if (!encounterList.isNullOrEmpty()) {

                                    for (items in encounterList) {

                                        val encounterId = items.resource.id
                                        val reasonCode = items.resource.reasonCode[0].text

                                        formatter.saveSharedPreference(
                                            context,
                                            reasonCode,
                                            encounterId
                                        )

                                    }

                                }

                            }

                        }


                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                }

            }

        }


    }

    private fun nukeEncounters(context: Context) {

        val formatter = FormatterClass()

        val encounterList = ArrayList<String>()
        encounterList.addAll(listOf(
            DbResourceViews.MEDICAL_HISTORY.name,
            DbResourceViews.PREVIOUS_PREGNANCY.name,
            DbResourceViews.PHYSICAL_EXAMINATION.name,
            DbResourceViews.CLINICAL_NOTES.name,
            DbResourceViews.BIRTH_PLAN.name,
            DbResourceViews.SURGICAL_HISTORY.name,
            DbResourceViews.MEDICAL_DRUG_HISTORY.name,
            DbResourceViews.FAMILY_HISTORY.name,
            DbResourceViews.ANTENATAL_PROFILE.name))

        for (items in encounterList){
            formatter.deleteSharedPreference(context, items)
        }

    }

    fun getEncounterDetails(context: Context, encounterId: String) = runBlocking {
        getEncounterDetailsBac(context, encounterId)
    }

    private fun getEncounterDetailsBac(context: Context, encounterId: String) : ArrayList<DbObserveValue>{

        val simpleEncounterList = ArrayList<DbObserveValue>()

        val baseUrl = context.getString(UrlData.FHIR_URL.message)

        val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)

        val callEncounterSync: Call<DbEncounterDetailsList> =
            apiService.getEncounterDetails(encounterId)
        val responseEncounter: Response<DbEncounterDetailsList> = callEncounterSync.execute()
        if (responseEncounter.isSuccessful) {

            val reasonBody = responseEncounter.body()
            if (reasonBody != null) {

                val entryList = reasonBody.entry
                if (!entryList.isNullOrEmpty()) {

                    for (observations in entryList) {

                        val id = observations.resource.id
                        val code = observations.resource.code
                        if (code != null) {

                            val codingList = code.coding
                            for (items in codingList){

                                val codeValue = items.code
                                val display = items.display


                                if (codeValue == "Next Appointment"){
                                    val dbObserveValue = DbObserveValue(id, display)
                                    simpleEncounterList.add(dbObserveValue)
                                }

                            }

                        }

                    }

                }
            }

        }
        return simpleEncounterList
    }

    fun getObservationDetails(context: Context, observationId: String) = runBlocking {
        getObservationDetailsBac(context, observationId)
    }

    private suspend fun getObservationDetailsBac(context: Context, observationId: String):ArrayList<DbCodingData>{

        val observationList = ArrayList<DbCodingData>()
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {

            val baseUrl = context.getString(UrlData.FHIR_URL.message)

            val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)

            val callSync: Call<DbEncounterDataResourceData> = apiService.getObservationDetails(observationId)

            try {
                val response: Response<DbEncounterDataResourceData> = callSync.execute()
                if (response.isSuccessful){

                    val responseBody = response.body()

                    if (responseBody != null){

                        val codeData = responseBody.code
                        if (codeData != null){

                            val encounterList = codeData.coding
                            if (!encounterList.isNullOrEmpty()){

                                for (items in encounterList){

                                    val code = items.code
                                    val display = items.display
                                    val system = items.system

                                    val dbCodingData = DbCodingData(system, code, display)
                                    observationList.add(dbCodingData)

                                    Log.e("+++++code ", code)
                                    Log.e("+++++display ", display)
                                    Log.e("+++++++++", "+++++")

                                }

                            }

                        }

                    }

                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }.join()

        return observationList

    }

}
