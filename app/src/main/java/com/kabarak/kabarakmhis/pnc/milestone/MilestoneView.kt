package com.kabarak.kabarakmhis.pnc.milestone

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.pnc.data_class.Milestone
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.android.synthetic.main.activity_milestone_view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse

class MilestoneView : AppCompatActivity() {

    private lateinit var milestoneRecyclerView: RecyclerView
    private lateinit var milestoneAdapter: MilestoneAdapter
    private var milestones: MutableList<Milestone> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milestone_view)

        // Initialize FHIR engine and formatter
        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)

        // Retrieve patient ID from shared preferences
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        // Initialize ViewModel
        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        btnAdd.setOnClickListener {
            val intent = Intent(this, MilestoneAdd::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        milestoneRecyclerView = findViewById(R.id.recycler_view_milestone)
        milestoneRecyclerView.layoutManager = LinearLayoutManager(this)

        milestoneAdapter = MilestoneAdapter(milestones) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MilestoneDetails::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        milestoneRecyclerView.adapter = milestoneAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch milestone data from FHIR server
        fetchMilestonesFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@MilestoneView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@MilestoneView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@MilestoneView, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@MilestoneView)
                        progressDialog.setTitle("Please wait...")
                        progressDialog.setMessage("Fetching patient details...")
                        progressDialog.show()

                        var patientName: String = ""
                        var dob: String = ""
                        var identifier: String = ""

                        val job = Job()
                        CoroutineScope(Dispatchers.IO + job).launch {
                            val patientData = getPatientDataFromFhirEngine()
                            patientName = patientData.first
                            dob = patientData.second

                            formatter.saveSharedPreference(this@MilestoneView, "patientName", patientName)
                            formatter.saveSharedPreference(this@MilestoneView, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@MilestoneView, "identifier", identifier)
                            }
                        }.join()

                        showPatientDetails(patientName, dob, identifier)

                        progressDialog.dismiss()
                    }
                } else {
                    // Display the data from local storage
                    showPatientDetails(patientLocalName, patientLocalDob, patientLocalIdentifier)
                }
            } catch (e: Exception) {
                Log.e("MilestoneViewActivity", "Error fetching patient data: ${e.message}")
            }
        }
    }

    private fun showPatientDetails(patientName: String, dob: String?, identifier: String?) {
        tvName.text = patientName
        if (!identifier.isNullOrEmpty()) tvANCID.text = identifier
        if (!dob.isNullOrEmpty()) tvAge.text = "${formatter.calculateAge(dob)} years"
    }

    private fun getPatientDataFromFhirEngine(): Pair<String, String> {
        // Use FHIR engine to fetch patient data, then return the name and date of birth
        val patientData = patientDetailsViewModel.getPatientData()
        val patientName = patientData.name
        val dob = patientData.dob

        return Pair(patientName, dob)
    }

    private fun fetchMilestonesFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("MilestoneViewActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    milestones.clear()

                                    // Extract milestone data from the bundle
                                    extractMilestonesFromBundle(bundle)

                                    // Show/hide views based on the presence of milestones
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("MilestoneView", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@MilestoneView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@MilestoneView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MilestoneView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("MilestoneView", "Error occurred while fetching data", t)
                        Toast.makeText(this@MilestoneView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractMilestonesFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                // Extract milestone from each QuestionnaireResponse
                extractMilestonesFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new milestone data
        runOnUiThread {
            milestoneAdapter.notifyDataSetChanged()
        }
    }

    private fun extractMilestonesFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a milestone with this ID already exists to avoid duplicates
        if (milestones.any { it.id == responseId }) {
            Log.d("MilestoneView", "Milestone with ID $responseId already exists. Skipping duplicate.")
            return
        }
        var milestonevisit: String? = null
        var milestoneage: String? = null
        var milestonetime: String? = null

        for (item in questionnaireResponse.item) {
            Log.d("MilestoneViewActivity", "Item linkId: ${item.linkId}")
            if (item.linkId == "78bb5435-e38b-412f-8434-a007373053a5") {
                for (subItem in item.item) {
                    Log.d("MilestoneView", "SubItem linkId: ${subItem.linkId}")
                    when (subItem.linkId) {
                        "4b735f95-8e24-42ff-9259-c1b8d92a5efb" -> {
                            milestonevisit = subItem.answer.firstOrNull()?.valueCoding?.display
                            Log.d("MilestoneView", "Milestone Visit: $milestonevisit")
                        }
                        "cb763ec8-d1b9-4b14-ad03-822a37ab9161" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "c470e52b-5814-4897-cf5e-3821d9798ae1" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneViewActivity", "Milestone Age: $milestoneage")
                                    }
                                    "ff0e59b5-13ca-4ce7-9899-bfa4495586e2" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneViewActivity", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        "8fab7061-93aa-447c-c528-3643ecef52c1" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneViewActivity", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "abf6fb60-d7a6-4c12-e9b8-fae30bd50a12" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "90013af0-4d77-4833-be2b-026303adbf4d" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        "f0c96adf-b0d1-4f85-9223-4bf90a53a43c" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "3bef4c4c-0a79-415f-9958-5407a0898a7c" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "61c70430-da71-4a80-81ec-a45a6dacfe6d" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        // Add other cases here if needed
                        "0eb13eaf-ad5d-44f0-8c99-d9e4990404a6" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "4d4c2e7f-a037-4cef-8b2a-3fd477a89306" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "0bedeacd-5af7-47a8-a069-3ed6adc5ad4f" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        "c21908af-2a59-4400-d273-a9d6780115c7" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "9a0e373d-0e8d-4b30-82b6-fdc4de49d891" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "6d630e50-eefd-4e4c-8074-036b573305d4" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        "70aa20c3-2340-410a-8f83-861d83df1413" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "c6df25d9-3d2e-47eb-88e6-25bddc095403" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "4182748e-dd58-4422-932b-97a9a45129e7" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                        "e3802e7d-6874-4b0c-a834-2ec7cd473460" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "95845e66-3964-47f7-8b15-45a86a43e032" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "93c50a96-f3be-406a-89f0-029eb4a38cf4" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
//                        // Last item in the list
                        "86bb9431-e687-4bd6-a500-ba4b966313c0" -> {
                            for (nameItem in subItem.item) {
                                Log.d("MilestoneView", "NameItem linkId: ${nameItem.linkId}")
                                when (nameItem.linkId) {
                                    "d7fdc869-120f-4611-9091-7272a4201d6e" -> {
                                        milestoneage = nameItem.answer.firstOrNull()?.valueIntegerType?.value.toString()
                                        Log.d("MilestoneView", "Milestone Age: $milestoneage")
                                    }
                                    "b5b6aa98-9d65-4938-92d8-46fafd204a8d" -> {
                                        milestonetime = nameItem.answer.firstOrNull()?.valueCoding?.display.toString()
                                        Log.d("MilestoneView", "Milestone Time: $milestonetime")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

// Add the milestone if both name and date are available
        if (!milestoneage.isNullOrEmpty() && !milestonetime.isNullOrEmpty()) {
            val milestone = milestonevisit?.let { Milestone(id = responseId, visit = it, age = milestoneage, time = milestonetime) }
            if (milestone != null) {
                milestones.add(milestone)
            }
            Log.d("MilestoneView", "Added milestone: $milestoneage, Date: $milestonetime, Response ID: $responseId")
        }
        // Add the milestone if both name and date are available
        if (!milestoneage.isNullOrEmpty() && !milestonetime.isNullOrEmpty()) {
            val milestone =
                milestonevisit?.let { Milestone(id = responseId,visit= it, age = milestoneage, time = milestonetime) }
            if (milestone != null) {
                milestones.add(milestone)
            }
            Log.d("MilestoneView", "Added milestone: $milestoneage, Date: $milestonetime, Response ID: $responseId")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (milestones.isEmpty()) {
            milestoneRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            milestoneRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}