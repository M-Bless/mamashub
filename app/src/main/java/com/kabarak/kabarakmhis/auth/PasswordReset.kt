package com.kabarak.kabarakmhis.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsAuthentication
import kotlinx.android.synthetic.main.activity_password_reset.*

class PasswordReset : AppCompatActivity() {

    private var formatterClass = FormatterClass()
    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        tvResendPassword.setOnClickListener {

            //Resend password

        }
        btnResetPassword.setOnClickListener {

            val verificationCode = etVerificationCode.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (!TextUtils.isEmpty(verificationCode) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){

            }else{



            }

        }

    }
}