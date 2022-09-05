package com.intellisoft.kabarakmhis

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.NewMainActivity
import com.intellisoft.kabarakmhis.new_designs.chw.PatientList
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {

    private var isLoggedIn: Boolean = false
    private lateinit var kabarakViewModel: KabarakViewModel
    private var formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        CoroutineScope(Dispatchers.IO).launch {
            kabarakViewModel = KabarakViewModel(applicationContext as Application)
            kabarakViewModel.insertCounty(this@Splash)
        }

        isLoggedIn = FhirApplication.isLoggedIn(this)

        Handler().postDelayed({
            if (isLoggedIn) {

                val role = formatter.retrieveSharedPreference(this, "role")
                if (role == "CHW"){
                    val intent = Intent(this, PatientList::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, NewMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            } else {
                startActivity(Intent(this@Splash, Login::class.java))

            }
            finish()
        }, 5000)
    }
}