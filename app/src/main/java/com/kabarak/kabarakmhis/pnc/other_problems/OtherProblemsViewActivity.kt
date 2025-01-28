package com.kabarak.kabarakmhis.pnc.other_problems

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
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.OtherProblems
import com.kabarak.kabarakmhis.pnc.other_problems.OtherProblemsDetailsActivity
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd
import kotlinx.android.synthetic.main.activity_child_birth_view.tvANCID
import kotlinx.android.synthetic.main.activity_child_birth_view.tvAge
import kotlinx.android.synthetic.main.activity_child_birth_view.tvName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtherProblemsViewActivity : AppCompatActivity() {

    private lateinit var problemRecyclerView: RecyclerView
    private lateinit var otherProblemsAdapter: OtherProblemsAdapter
    private var problems: MutableList<OtherProblems> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_problems_view)

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
            val intent = Intent(this, OtherProblemsReported::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        problemRecyclerView = findViewById(R.id.recycler_view_child)
        problemRecyclerView.layoutManager = LinearLayoutManager(this)

//        otherProblemsAdapter = OtherProblemsAdapter(problems) { rawResponseId ->
//            val responseId = extractResponseId(rawResponseId)
//            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()
//
//            val intent = Intent(this, OtherProblemsEdit::class.java)
//            intent.putExtra("responseId", responseId)
//            startActivity(intent)
//        }
//        problemRecyclerView.adapter = otherProblemsAdapter
//
//        // Initialize noRecordView (the include layout for "no records found")
//        noRecordView = findViewById(R.id.no_record)
//
//        // Initialize RetrofitCallsFhir
//        retrofitCallsFhir = RetrofitCallsFhir()
//
//        // Fetch child data from FHIR server
//        fetchChildrenFromFHIR()
//
//        // Fetch patient data
//        fetchPatientData()
//    }
        otherProblemsAdapter = OtherProblemsAdapter(problems) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, OtherProblemsDetailsActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        problemRecyclerView.adapter = otherProblemsAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch child data from FHIR server
        fetchChildrenFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@OtherProblemsViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@OtherProblemsViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@OtherProblemsViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@OtherProblemsViewActivity)
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

                            formatter.saveSharedPreference(this@OtherProblemsViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@OtherProblemsViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@OtherProblemsViewActivity, "identifier", identifier)
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
                Log.e("OtherProblemsViewActivity", "Error fetching patient data: ${e.message}")
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

    private fun fetchChildrenFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("OtherProblemsViewActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    problems.clear()

                                    // Extract child data from the bundle
                                    extractChildrenFromBundle(bundle)

                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("OtherProblemsViewActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@OtherProblemsViewActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@OtherProblemsViewActivity, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@OtherProblemsViewActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("OtherProblemsViewActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@OtherProblemsViewActivity, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractChildrenFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {extractChildrenFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            otherProblemsAdapter.notifyDataSetChanged() // Call on the adapter instance
        }
    }


    private fun extractChildrenFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a child with this ID already exists to avoid duplicates
        if (problems.any { it.id == responseId }) {
            Log.d("OtherProblemsViewActivity", "Child with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var sleepingProblem: String? = null
        var irritability: String? = null
        var othersSpecify: String? = null

        for (item in questionnaireResponse.item) {
            if (item.linkId == "fbbb030e-f52f-4e8d-8347-f5be6b7f0863") {
               sleepingProblem = item.answer.firstOrNull()?.valueCoding?.display.toString()
            }else if (item.linkId == "f0b53fbd-4226-43a7-8331-aa1002912485"){
                irritability = item.answer.firstOrNull()?.valueCoding?.display.toString()
            } else if (item.linkId == "1e7e68af-bbc5-4591-eeaf-23ca447c9992"){
                othersSpecify = item.answer.firstOrNull()?.valueStringType?.value.toString()
            }
        }

        // Add the child if both name and birth date are available
        if (!sleepingProblem.isNullOrEmpty() && !irritability.isNullOrEmpty()) {
            val problem = OtherProblems(
                id = responseId,
                sleepingProblems = sleepingProblem,
                irritability = irritability,
                othersSpecify = othersSpecify.toString()
            )
            problems.add(problem)
            Log.d("OtherProblemsViewActivity", "Added Problem: $sleepingProblem, Irritability: $irritability, Other problems: $othersSpecify Response ID: $responseId")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (problems.isEmpty()) {
            problemRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            problemRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}

