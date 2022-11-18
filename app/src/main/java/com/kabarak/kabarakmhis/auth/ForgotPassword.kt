package com.kabarak.kabarakmhis.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.helperclass.UserResetPassword
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsAuthentication
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPassword : AppCompatActivity() {

    private var formatterClass = FormatterClass()
    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btnRequestCode.setOnClickListener {

            val emailAddress = etEmailAddress.text.toString()
            if (!TextUtils.isEmpty(emailAddress)){

                val isEmailAddress = formatterClass.validateEmail(emailAddress)
                if (isEmailAddress){

                    val userLogin = UserResetPassword(emailAddress)
//                    retrofitCallsAuthentication.loginUser(this, userLogin)

                }else{
                    etEmailAddress.error = "Provide a valid email address."
                }

            }else{
                etEmailAddress.error = "Field cannot be empty!.."
            }


        }

    }
}