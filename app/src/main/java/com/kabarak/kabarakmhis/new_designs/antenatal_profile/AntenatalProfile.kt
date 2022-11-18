package com.kabarak.kabarakmhis.new_designs.antenatal_profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile

class AntenatalProfile : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val antenatal1 = DbResourceViews.ANTENATAL_1.name
    private val antenatal2 = DbResourceViews.ANTENATAL_2.name
    private val antenatal3 = DbResourceViews.ANTENATAL_3.name
    private val antenatal4 = DbResourceViews.ANTENATAL_4.name


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenatal_profile)

        title = "Antenatal Profile"

        formatter.saveSharedPreference(this, "totalPages", "4")

        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                antenatal1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentAntenatal1())
                    formatter.saveCurrentPage("1" , this)
                }
                antenatal2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentAntenatal2())
                    formatter.saveCurrentPage("2", this)
                }
                antenatal3 -> {
                    ft.replace(R.id.fragmentHolder, FragmentAntenatal3())
                    formatter.saveCurrentPage("3", this)
                }
                antenatal4 -> {
                    ft.replace(R.id.fragmentHolder, FragmentAntenatal4())
                    formatter.saveCurrentPage("4", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentAntenatal1())
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