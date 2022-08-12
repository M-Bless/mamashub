package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentPatientDetails

class ConfirmPage : AppCompatActivity() {

    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_page)

        if (savedInstanceState == null) {

            val pageConfirmDetails = formatter.retrieveSharedPreference(this, "pageConfirmDetails")

            if (pageConfirmDetails != null){
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(this, pageConfirmDetails))
                ft.commit()
            }else{
                startActivity(Intent(this, PatientProfile::class.java))
                finish()
            }


        }
    }
}