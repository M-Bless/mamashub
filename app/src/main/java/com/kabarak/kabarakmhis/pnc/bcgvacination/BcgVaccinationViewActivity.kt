package com.kabarak.kabarakmhis.pnc.bcgvacination

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer

import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import com.kabarak.kabarakmhis.R

import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.helperclass.Model
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.pnc.ChildAdd
import com.kabarak.kabarakmhis.pnc.ChildProfileViewModel
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd

import kotlinx.android.synthetic.main.activity_patient_profile.imgViewCall
import kotlinx.android.synthetic.main.activity_patient_profile.linearCall


class BcgVaccinationViewActivity : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var fhirEngine: FhirEngine // Initialize later in onCreate
    private val childProfileViewModel: ChildProfileViewModel by viewModels {
        ChildProfileViewModel.ChildProfileViewModelFactory(application, fhirEngine)
    }
    private val formatterClass = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private val viewModel: MainActivityViewModel by viewModels()

    val modelList = ArrayList<Model>()
    private var isPatient = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bcg_vaccination_view)


        fhirEngine = FhirApplication.fhirEngine(this)

        kabarakViewModel = KabarakViewModel(this.application)

        // Get the CHILD_ID from the intent
        val childId = intent.getStringExtra("CHILD_ID")
        val childIdTextView: TextView = findViewById(R.id.text_bn_no)
        val childNameTextView: TextView = findViewById(R.id.text_child_name)
        val childGenderTextView: TextView = findViewById(R.id.text_child_gender)
        val childDobTextView: TextView = findViewById(R.id.text_child_dob) // New TextView for DOB


        // Set up the Add button to navigate to ChildAdd activity
        btnAdd.setOnClickListener {
            val intent = Intent(this, BcgAddActivity::class.java)
            startActivity(intent)
        }


        // Display the ID temporarily while loading
        childIdTextView.text = "Child ID: $childId"

// Display the ID temporarily while loading
        childIdTextView.text = "Child ID: $childId"

        if (childId != null) {
            // Fetch the child's profile
            childProfileViewModel.fetchChildProfile(childId)

            // Observe changes to the child's profile
            childProfileViewModel.childProfile.observe(this, Observer { childProfile ->
                if (childProfile != null) {
                    // Update the UI with the fetched details
                    childNameTextView.text = "Name: ${childProfile.name}"
                    childGenderTextView.text = "Gender: ${childProfile.gender}"


                    childDobTextView.text = "Date of Birth: ${childProfile.dob}" // Update DOB TextView


                } else {
                    // Display a message if the profile couldn't be loaded
                    childNameTextView.text = "Error: Child profile not found."
                }
            })
        } else {
            // Handle the case where no CHILD_ID is provided
            childNameTextView.text = "Error: No Child ID provided."
        }



        title = "Child Details"

        linearCall.setOnClickListener {
        }
        imgViewCall.setOnClickListener {

        }



    }}