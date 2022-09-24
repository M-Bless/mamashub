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
import com.intellisoft.kabarakmhis.new_designs.chw.PatientList
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

                val formatter = FormatterClass()
                val baseUrl = context.getString(UrlData.BASE_URL.message)
                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
                try {

                    val apiInterface = apiService.loginUser(userLogin)
                    if (apiInterface.isSuccessful){

                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()

                        if (statusCode == 200 || statusCode == 201){

                            if (body != null){

                                val token = body.token
                                val expires = body.expires

                                formatter.saveSharedPreference(context, "token", token)
                                formatter.saveSharedPreference(context, "expires", expires)

                                getUserData(context)

                            }else{
                                messageToast = "Error: Body is null"
                            }

                        }else{
                            messageToast = "Error: The request was not successful"
                        }



                    }else{
                        apiInterface.errorBody()?.let {
                            val errorBody = JSONObject(it.string())
                            messageToast = errorBody.getString("message")
                        }
                    }


                }catch (e: Exception){

                    messageToast = "There was an issue with the server"
                }


            }.join()
            CoroutineScope(Dispatchers.Main).launch{

                progressDialog.dismiss()
                Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()

            }

        }

    }

    private suspend fun getUserData(context: Context) {

        var messageToast = ""
        val formatter = FormatterClass()
        val stringStringMap = formatter.getHeaders(context)

        val baseUrl = context.getString(UrlData.BASE_URL.message)

        val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
        try {

            val apiInterface = apiService.getUserData(stringStringMap)

            if (apiInterface.isSuccessful){
                val statusCode = apiInterface.code()
                val body = apiInterface.body()

                if (statusCode == 200 || statusCode == 201){

                    if (body != null){

                        messageToast = "Login successful"

                        val data = body.data
                        val id = data.id
                        val names = data.names
                        val email = data.email
                        val role = data.role

                        val kmhflCode = data.kmhflCode
                        val facilityName = data.facilityName

                        Log.e("TAG", "getUserData: $kmhflCode", )

                        formatter.saveSharedPreference(context, "id", id)
                        formatter.saveSharedPreference(context, "USERID", id)
                        formatter.saveSharedPreference(context, "names", names)
                        formatter.saveSharedPreference(context, "email", email)
                        formatter.saveSharedPreference(context, "role", role)

                        if (kmhflCode != null) formatter.saveSharedPreference(context, "kmhflCode", kmhflCode)
                        if (facilityName != null) formatter.saveSharedPreference(context, "facilityName", facilityName)

                        FhirApplication.setLoggedIn(context, true)

                        when (role) {
                            "CHW" -> {
                                val intent = Intent(context, PatientList::class.java)
                                context.startActivity(intent)
                            }
                            "CLINICIAN" -> {
                                val intent = Intent(context, NewMainActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                FhirApplication.setLoggedIn(context, false)
                                messageToast = "Error: Role not recognized"
                            }
                        }

                    }else{
                        messageToast = "Error: Body is null"
                    }

                }else{

                    messageToast = if (statusCode == 401){
                        "Error: Invalid credentials"
                    }else{
                        "Error: Status code is $statusCode"
                    }
                }
            }else{
                apiInterface.errorBody()?.let {
                    val errorBody = JSONObject(it.string())
                    messageToast = errorBody.getString("message")
                }

            }

        }catch (e: Exception){

            messageToast = "There was an issue with the server"
        }

        CoroutineScope(Dispatchers.Main).launch{

                Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()

        }


    }


}

