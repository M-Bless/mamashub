package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.auth.Login
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {

                startActivity(Intent(this, Login::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}