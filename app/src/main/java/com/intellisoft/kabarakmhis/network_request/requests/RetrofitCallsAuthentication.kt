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
import com.intellisoft.kabarakmhis.helperclass.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

                                getUserData(context)

                            }


                            FhirApplication.setLoggedIn(context, true)

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

    private fun getUserData(context: Context) {

        var formatter = FormatterClass()
        val stringStringMap = formatter.getHeaders(context)

        val baseUrl = context.getString(UrlData.BASE_URL.message)

        val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
        val apiInterface = apiService.getUserData(stringStringMap)
        apiInterface.enqueue(object : Callback<DbUserData> {
            override fun onResponse(
                call: Call<DbUserData>,
                response: Response<DbUserData>
            ) {


                if (response.isSuccessful) {

                    val responseData = response.body()

                    if (responseData != null){

                        val data = responseData.data
                        val id = data.id
                        val names = data.names
                        val email = data.email

                        formatter.saveSharedPreference(context, "id", id)
                        formatter.saveSharedPreference(context, "USERID", id)
                        formatter.saveSharedPreference(context, "names", names)
                        formatter.saveSharedPreference(context, "email", email)

                    }


                } else {

                    val code = response.code()
                    val message = response.errorBody().toString()

                    if (code != 500) {

                        val jObjError = JSONObject(response.errorBody()?.string())


                    } else {

                    }


                }


            }

            override fun onFailure(call: Call<DbUserData>, t: Throwable) {
                Log.e("-*-*error ", t.localizedMessage)

            }
        })
    }


}

