package com.intellisoft.kabarakmhis.network_request.requests

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.widget.Toast
import com.intellisoft.kabarakmhis.network_request.builder.RetrofitBuilder
import com.intellisoft.kabarakmhis.network_request.interfaces.Interface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.kabarakmhis.MainActivity
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.helperclass.AuthResponse
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.UrlData
import com.intellisoft.kabarakmhis.helperclass.UserLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RetrofitCallsAuthentication {

    fun loginUser(context: Context, userLogin: UserLogin){

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                startLogin(context, userLogin)

            }.join()
        }

    }
    private suspend fun startLogin(context: Context, userLogin: UserLogin) {


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

                val baseUrl = context.getString(UrlData.BASE_URL.message)

                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
                val apiInterface = apiService.loginUser(userLogin)
                apiInterface.enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {

                        CoroutineScope(Dispatchers.Main).launch { progressDialog.dismiss() }

                        if (response.isSuccessful) {
                            messageToast = "User details verified successfully."

                            val responseData = response.body()

                            if (responseData != null){

                                val token = responseData.token
                                val expires = responseData.expires

                                formatter.saveSharedPreference(context, "token", token)
                                formatter.saveSharedPreference(context, "expires", expires)


                            }

                            FhirApplication.setLoggedIn(context, true)

                            val intent = Intent(context, MainActivity::class.java)
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

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
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


}

