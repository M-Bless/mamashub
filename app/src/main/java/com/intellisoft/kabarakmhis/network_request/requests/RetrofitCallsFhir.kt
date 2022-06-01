package com.intellisoft.kabarakmhis.network_request.requests

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.intellisoft.kabarakmhis.network_request.builder.RetrofitBuilder
import com.intellisoft.kabarakmhis.network_request.interfaces.Interface
import com.intellisoft.kabarakmhis.new_designs.NewMainActivity
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.data.SYNC_VALUE
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatient
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientResult
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientSuccess
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RetrofitCallsFhir {

    fun createPatient(context: Context, dbPatient: DbPatient){

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
            progressDialog.setMessage("Authentication in progress..")
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

                            if (responseData != null){

                                Log.e("*** ", responseData.toString())

                            }

                            val intent = Intent(context, NewMainActivity::class.java)
                            context.startActivity(intent)

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
                                        Toast.makeText(context, messageToast, Toast.LENGTH_SHORT).show()
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

    fun getPatients(context: Context) = runBlocking{

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
                if (response.isSuccessful){

                    val list = response.body()
                    if (list != null){
                        confList = list
                    }

                }


            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }.join()

        return confList

    }



}

