package com.intellisoft.kabarakmhis.new_designs.preventive_service

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R

class PreventiveServiceList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preventive_service_list)

        title = "Preventive Service List"

    }
}