package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.activity_patient_profile.*

class PatientProfile : AppCompatActivity() {

    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        rltMedicalHistory.setOnClickListener {
            val intent = Intent(this, MedicalSurgicalHistoryView::class.java)
            startActivity(intent)
        }
        previousPregnancy.setOnClickListener {
            val intent = Intent(this, PreviousPregnancy::class.java)
            startActivity(intent)
        }
        relativeLyt.setOnClickListener {
            val intent = Intent(this, PhysicalExamination::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()

        getPatientData()
    }

    private fun getPatientData() {

        val patientName = formatter.retrieveSharedPreference(this, "name")
        val dob = formatter.retrieveSharedPreference(this, "dob")

        val kinRelationShip = formatter.retrieveSharedPreference(this, "kinRelationShip")
        val kinName = formatter.retrieveSharedPreference(this, "kinName")
        val kinPhoneNumber = formatter.retrieveSharedPreference(this, "kinPhoneNumber")

        if (kinRelationShip != null && kinName != null && kinPhoneNumber != null){

            val kinDetails = "$kinName \n$kinPhoneNumber"
            tvKinDetails.text = kinDetails

        }

        tvName.text = patientName
        tvAge.text = dob

    }
}