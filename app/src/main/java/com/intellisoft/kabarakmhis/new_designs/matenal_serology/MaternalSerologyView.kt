package com.intellisoft.kabarakmhis.new_designs.matenal_serology

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R

class MaternalSerologyView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maternal_serology_view)

        title = "Maternal Serology Details"
    }
}