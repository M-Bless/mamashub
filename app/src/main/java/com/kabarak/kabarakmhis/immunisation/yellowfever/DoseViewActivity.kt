package com.kabarak.kabarakmhis.immunisation.yellowfever

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.kabarak.kabarakmhis.immunisation.data_class.DoseClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.android.synthetic.main.activity_dose_view.*
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoseViewActivity : AppCompatActivity() {
    private lateinit var doseRecyclerView: RecyclerView
    private lateinit var doseAdapter: DoseAdapter
    private var dose: MutableList<DoseClass> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View
    private lateinit var btnAdd: Button


    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UI components
        setContentView(R.layout.activity_dose_view)
        noRecordView = findViewById(R.id.no_record)
        btnAdd = findViewById(R.id.btnAdd)



        /// Initialize FHIR engine and formatter
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
            val intent = Intent(this, DoseAddActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        doseRecyclerView = findViewById(R.id.recycler_view_child)
        doseRecyclerView.layoutManager = LinearLayoutManager(this)

        doseAdapter = DoseAdapter(dose) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DoseEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        doseRecyclerView.adapter = doseAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch vaccines data from FHIR server
        fetchVaccinationsFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@DoseViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@DoseViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@DoseViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@DoseViewActivity)
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

                            formatter.saveSharedPreference(this@DoseViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@DoseViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@DoseViewActivity, "identifier", identifier)
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
                Log.e("DoseViewActivity", "Error fetching patient data: ${e.message}")
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

    private fun fetchVaccinationsFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("DoseViewActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    dose.clear()

                                    // Extract vaccinations from the bundle
                                    extractVaccinationsFromBundle(bundle)

                                    // Show/hide views based on the presence of doses
                                    runOnUiThread {
                                        toggleViews()

                                    }
                                } catch (e: Exception) {
                                    Log.e("DoseViewActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@DoseViewActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@DoseViewActivity, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DoseViewActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("DoseViewActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@DoseViewActivity, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractVaccinationsFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                extractVaccinationsFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            doseAdapter.notifyDataSetChanged()
        }

    }

    private fun extractVaccinationsFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        if (dose.any { it.id == responseId }) {
            Log.d("DoseViewActivity", "Vaccine with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var vaccine: String? = null
        var dateIssued: String? = null


        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                "4f8bd8bf-a7b6-4217-8e2a-06cdc7299f97" -> {
                    // Vaccine Name
                    vaccine = item.answer.firstOrNull()?.valueStringType?.value.toString()
                }
                "2d0efc67-a114-4094-801e-8243bafcff59" -> {
                    // Vaccination Date
                    dateIssued = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
            }
        }

        // Add the entry to dose list if required fields are present
        if (!vaccine.isNullOrEmpty() && !dateIssued.isNullOrEmpty()) {
            val doseClass = DoseClass(
                id = responseId,
                vaccineName = vaccine.toString(),
                issuedDate = dateIssued.toString()
            )
            dose.add(doseClass)
            Log.d("DoseViewActivity", "Added vaccination: $vaccine, Date: $dateIssued")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }


    private fun toggleViews() {
        if (dose.isEmpty()) {
            doseRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
            btnAdd.visibility = View.VISIBLE  // Show the Add button when there are no doses
        } else {
            doseRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
            //btnAdd.visibility = View.GONE  // Hide the Add button when there are doses
        }
    }
}