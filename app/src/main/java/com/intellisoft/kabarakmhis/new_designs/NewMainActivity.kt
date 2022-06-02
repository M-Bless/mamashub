package com.intellisoft.kabarakmhis.new_designs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.PatientsAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.RegisterNewPatient
import kotlinx.android.synthetic.main.activity_new_main.*

class NewMainActivity : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

        btnRegisterPatient.setOnClickListener {
            startActivity(Intent(this, RegisterNewPatient::class.java))
        }

        recyclerView = findViewById(R.id.patient_list);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

    }

    override fun onStart() {
        super.onStart()

        val patientData = retrofitCallsFhir.getPatients(this)
        val patientList = patientData.entry
        if (patientList != null){
            val configurationListingAdapter = PatientsAdapter(
                patientList,this@NewMainActivity)
            recyclerView.adapter = configurationListingAdapter
        }

    }
}