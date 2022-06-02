package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*

class MedicalSurgicalHistoryView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_surgical_history_view)

        btnAddHistory.setOnClickListener {
            val intent = Intent(this, MedicalHistory::class.java)
            startActivity(intent)
        }
    }
}