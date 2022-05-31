package com.intellisoft.kabarakmhis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication

class Splash : AppCompatActivity() {

    private var isLoggedIn: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        isLoggedIn = FhirApplication.isLoggedIn(this)

        Handler().postDelayed({
            if (isLoggedIn) {
                startActivity(Intent(this@Splash, MainActivity::class.java))
            } else {
                startActivity(Intent(this@Splash, Login::class.java))
            }
            finish()
        }, 3000)
    }
}