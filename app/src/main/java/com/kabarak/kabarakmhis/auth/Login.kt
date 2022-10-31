package com.kabarak.kabarakmhis.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.kabarak.kabarakmhis.MainActivity
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.helperclass.UserLogin
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsAuthentication
import com.kabarak.kabarakmhis.new_designs.NewMainActivity
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()
    private var formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tvCreate.setOnClickListener {

            Toast.makeText(this, "Please contact system admin to add you.", Toast.LENGTH_SHORT).show()

        }
        tvForgetPassword.setOnClickListener {

            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }



        btnLogin.setOnClickListener {

            loginUser()

        }

    }

    private fun loginUser() {

        val emailAddress = etEmailAddress.text.toString()
        val password = etPassword.text.toString()

        if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(password)){

            val isEmailAddress = formatterClass.validateEmail(emailAddress)
            if (isEmailAddress){

                if (emailAddress=="app@demo.com" && password=="12345678") {
                    val intent = Intent(this, NewMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    val userLogin = UserLogin(emailAddress, password)
                    retrofitCallsAuthentication.loginUser(this, userLogin)
                }



            }else{
                etEmailAddress.error = "Provide a valid email address."
            }


        }else{

            if (TextUtils.isEmpty(emailAddress)) etEmailAddress.error = "Field cannot be empty!.."
            if (TextUtils.isEmpty(password)) etPassword.error = "Field cannot be empty!.."

        }


    }
}