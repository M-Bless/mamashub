package com.kabarak.kabarakmhis.new_designs.chw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews

class CommunityHealthWorkerForm : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val chw1 = DbResourceViews.CHW_1.name
    private val chw2 = DbResourceViews.CHW_2.name


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_health_worker_form)

        title = "Community Health Worker"

        formatter.saveSharedPreference(this, "totalPages", "2")


        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                chw1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentCHW1())
                    formatter.saveCurrentPage("1", this)
                }
                chw2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentCHW2())
                    formatter.saveCurrentPage("2", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentCHW1())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


        }

    }
}