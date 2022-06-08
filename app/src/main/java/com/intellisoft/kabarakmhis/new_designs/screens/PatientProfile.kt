package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
            val intent = Intent(this, PreviousPregnancyView::class.java)
            startActivity(intent)
        }
        relativeLyt.setOnClickListener {
            val intent = Intent(this, PhysicalExaminationView::class.java)
            startActivity(intent)
        }
        linearLayoutCall.setOnClickListener {

            val txtPhone = tvKinDetails.text.toString()
            if (!TextUtils.isEmpty(txtPhone)){
                calluser(txtPhone)
            }

        }

    }

    fun calluser(value: String){
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + value)
        startActivity(dialIntent)
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
            tvKinDetails.text = kinPhoneNumber

        }

        tvName.text = patientName
        tvAge.text = dob

    }
}