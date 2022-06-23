package com.intellisoft.kabarakmhis.new_designs.present_pregnancy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal1
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal2
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal3
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal4
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews

class PresentPregnancyAdd : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val presentPregnancy1 = DbResourceViews.PRESENT_PREGNANCY_1.name
    private val presentPregnancy2 = DbResourceViews.PRESENT_PREGNANCY_2.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present_pregnancy_add)

        title = "Present Pregnancy"

        formatter.saveSharedPreference(this, "totalPages", "2")

        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                presentPregnancy1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPresentPregnancy1())
                    formatter.saveCurrentPage("1" , this)
                }
                presentPregnancy2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPresentPregnancy2())
                    formatter.saveCurrentPage("2", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentPresentPregnancy1())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


        }

    }
}