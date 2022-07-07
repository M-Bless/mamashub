package com.intellisoft.kabarakmhis.new_designs.pmtct

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.ifas.FragmentIfas1
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile

class PMTCTInterventions : AppCompatActivity() {

    private val formatter = FormatterClass()
    private val PMTCT1 = DbResourceViews.PMTCT1.name
    private val PMTCT2 = DbResourceViews.PMTCT2.name
    private val PMTCT3 = DbResourceViews.PMTCT3.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pmtctinterventions)

        title = "PMTCT Interventions"

        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                PMTCT1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPmtct1())
                    formatter.saveCurrentPage("1" , this)
                }
                PMTCT2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPmtct2())
                    formatter.saveCurrentPage("2", this)
                }
                PMTCT3 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPmtct3())
                    formatter.saveCurrentPage("2", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentPmtct1())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}