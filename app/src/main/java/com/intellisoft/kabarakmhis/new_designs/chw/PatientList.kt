package com.intellisoft.kabarakmhis.new_designs.chw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_patient_list.*

class PatientList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        title = "Patients"

        btnRegisterPatient.setOnClickListener {

            startActivity(Intent(this@PatientList, CommunityHealthWorkerForm::class.java))

        }
    }
}