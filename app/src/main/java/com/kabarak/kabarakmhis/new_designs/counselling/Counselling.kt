package com.kabarak.kabarakmhis.new_designs.counselling

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile

class Counselling : AppCompatActivity() {

    private val formatter = FormatterClass()
    private val counselling1 = DbResourceViews.COUNSELLING1.name
    private val counselling2 = DbResourceViews.COUNSELLING2.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counselling)

        title = "Counselling"

        formatter.saveSharedPreference(this, "totalPages", "2")


        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                counselling1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentCounselling1())
                    formatter.saveCurrentPage("1", this)
                }
                counselling2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentCounselling2())
                    formatter.saveCurrentPage("2", this)
                }

                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentCounselling1())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean  {
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