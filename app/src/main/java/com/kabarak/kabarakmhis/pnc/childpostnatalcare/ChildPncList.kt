package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.app.ProgressDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.pnc.data_class.ChildPnc
import kotlinx.android.synthetic.main.activity_child_pnc_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ChildPncList : AppCompatActivity() {
    private lateinit var childPncRecyclerView: RecyclerView
    private lateinit var childPncAdapter: ChildPncAdapter
    private var patients: MutableList<ChildPnc> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View

    private lateinit var kabarakViewModel: KabarakViewModel
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UI components
        setContentView(R.layout.activity_child_pnc_list)
        noRecordView = findViewById(R.id.no_record)

        title = "Child Postnatal Care"

        /// Initialize FHIR engine and formatter
        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)

        // Retrieve patient ID from shared preferences
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        // Initialize ViewModel
        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(
                application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        btnAdd.setOnClickListener {
            val intent = Intent(this, ChildPostnatalCare::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        childPncRecyclerView = findViewById(R.id.recyclerView)
        childPncRecyclerView.layoutManager = LinearLayoutManager(this)

        childPncAdapter = ChildPncAdapter(patients) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ChildPncViewActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        childPncRecyclerView.adapter = childPncAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch vaccines data from FHIR server
        fetchChildrenFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName =
                    formatter.retrieveSharedPreference(this@ChildPncList, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@ChildPncList, "dob")
                val patientLocalIdentifier =
                    formatter.retrieveSharedPreference(this@ChildPncList, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@ChildPncList)
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

                            formatter.saveSharedPreference(
                                this@ChildPncList,
                                "patientName",
                                patientName
                            )
                            formatter.saveSharedPreference(this@ChildPncList, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(
                                    this@ChildPncList,
                                    "identifier",
                                    identifier
                                )
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
                Log.e("ChildPncList", "Error fetching patient data: ${e.message}")
            }
        }
    }

    private fun showPatientDetails(patientName: String, dob: String?, identifier: String?) {
        tvPatient.text = patientName
        if (!identifier.isNullOrEmpty()) tvAncId.text = identifier
        //if (!dob.isNullOrEmpty()) tvAge.text = "${formatter.calculateAge(dob)} years"
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
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("ChildPncList", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(
                                        org.hl7.fhir.r4.model.Bundle::class.java,
                                        rawResponse
                                    )

                                    // Clear list before adding new items
                                    patients.clear()

                                    // Extract child data from the bundle
                                    extractChildrenPncFromBundle(bundle)

                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("ChildPncList", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@ChildPncList,
                                            "Failed to parse response",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@ChildPncList,
                                        "Received an empty response",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@ChildPncList,
                                "Failed to fetch data: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("ChildPncList", "Error occurred while fetching data", t)
                        Toast.makeText(
                            this@ChildPncList,
                            "Error occurred: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractChildrenPncFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                extractChildrenPncFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            toggleViews()
            childPncAdapter.notifyDataSetChanged()
        }

    }

    private fun extractChildrenPncFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        if (patients.any { it.visitTime == responseId }) {
            Log.d(
                "ChildPncList",
                "ChildPnc with ID $responseId already exists. Skipping duplicate."
            )
            return
        }

        // Variables to store parsed values
        var visitTime: String? = null
        var generalCondition: String? = null
        var nextVisit: String? = null

// Function to traverse top-level items in the QuestionnaireResponse and extract data based on linkIds
        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                "aa7fb496-8d17-4370-bc88-ddd0316eabf1" -> {
                    visitTime = item.answer.firstOrNull()?.valueCoding?.display ?: "Unknown"
                }

                "0590985e-8105-49ed-875b-3ca1c4807702" -> {
                    generalCondition = item.answer.firstOrNull()?.valueCoding?.display ?: "Unknown"
                }

                "03e1ed67-5a2e-4829-f0f0-22eb4a294b8b" -> {
                    nextVisit = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
            }
        }

// Check required fields before adding entry to patients list
        if (!generalCondition.isNullOrEmpty() && !nextVisit.isNullOrEmpty()) {
            val childPnc = ChildPnc(
                id = responseId,
                visitTime = visitTime ?: "Not specified",
                generalCondition = generalCondition ?: "Not specified",
                nextVisitDate = nextVisit ?: "Not scheduled"
            )
            patients.add(childPnc)

            Log.d(
                "ChildPncList",
                "Added PNC visit: $visitTime, Condition: $generalCondition, Next Visit: $nextVisit"
            )
        }

    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }


    private fun toggleViews() {
        if (patients.isEmpty()) {
            childPncRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            childPncRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}