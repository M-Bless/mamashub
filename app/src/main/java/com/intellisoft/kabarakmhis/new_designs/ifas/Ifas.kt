package com.intellisoft.kabarakmhis.new_designs.ifas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.FragmentPresentPregnancy1
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.FragmentPresentPregnancy2
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile

class Ifas : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val ifas1 = DbResourceViews.IFAS1.name
    private val ifas2 = DbResourceViews.IFAS2.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifas)

        title = "IFAS"

        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                ifas1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentIfas1())
                    formatter.saveCurrentPage("1" , this)
                }
                ifas2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentIfas1())
                    formatter.saveCurrentPage("2", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentIfas1())
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