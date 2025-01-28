package com.kabarak.kabarakmhis.pnc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.activity_child_birth_view.*

class ChildViewActivity : AppCompatActivity() {

    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_birth_view)

        // Initialize formatter and fetch patient ID from shared preferences
        formatter = FormatterClass()
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        // Load patient details
        fetchPatientData()

        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = ChildListFragment()

            // Pass the identifier as an argument to the fragment
            val identifier = formatter.retrieveSharedPreference(this, "identifier")
            val bundle = Bundle().apply {
                putString("identifier", identifier) // Pass identifier to fragment
            }
            fragment.arguments = bundle

            // Add fragment to the container
            fragmentTransaction.replace(R.id.fragment_container, fragment)
            fragmentTransaction.commit()
        }

        // Set up the Add button to navigate to ChildAdd activity
        btnAdd.setOnClickListener {
            val identifier = formatter.retrieveSharedPreference(this, "identifier") // Retrieve the identifier
            if (!identifier.isNullOrEmpty()) {
                val intent = Intent(this, ChildAdd::class.java)
                intent.putExtra("identifier", identifier) // Pass the identifier to the next activity
                startActivity(intent)
            } else {
                Toast.makeText(this, "Identifier not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPatientData() {
        val patientName = formatter.retrieveSharedPreference(this, "patientName") ?: "Unknown"
        val dob = formatter.retrieveSharedPreference(this, "dob")
        val identifier = formatter.retrieveSharedPreference(this, "identifier")

        showPatientDetails(patientName, dob, identifier)
    }

    private fun showPatientDetails(patientName: String, dob: String?, identifier: String?) {
        tvName.text = patientName
        if (!identifier.isNullOrEmpty()) tvANCID.text = identifier
        if (!dob.isNullOrEmpty()) tvAge.text = "${formatter.calculateAge(dob)} years"
    }

    // Method to show or hide the no_record layout based on data availability
    fun setNoRecordVisibility(isDataAvailable: Boolean) {
        val noRecordView = findViewById<View>(R.id.no_record)
        if (isDataAvailable) {
            noRecordView.visibility = View.GONE // Hide if data is available
        } else {
            noRecordView.visibility = View.VISIBLE // Show if no data is available
        }
    }
}
