package com.kabarak.kabarakmhis.immunisation.vitamin_a_supplimentary

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
import com.kabarak.kabarakmhis.pnc.data_class.VitaminASup
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.android.synthetic.main.activity_vitamin_asupplimentary_view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse


class VitaminAsupplimentaryView : AppCompatActivity() {


    private lateinit var vitaminASupRecyclerView: RecyclerView
    private lateinit var vitaminASupAdapter: VitaminASupplimentaryAdapter
    private var vitaminDosage: MutableList<VitaminASup> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View


    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize UI components
        setContentView(R.layout.activity_vitamin_asupplimentary_view)
        noRecordView = findViewById(R.id.no_record)




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
            val intent = Intent(this, VitaminAsupplimentaryAdd::class.java)
            startActivity(intent)
        }


        // Initialize RecyclerView
        vitaminASupRecyclerView = findViewById(R.id.recycler_view_sup)
        vitaminASupRecyclerView.layoutManager = LinearLayoutManager(this)


        vitaminASupAdapter = VitaminASupplimentaryAdapter(vitaminDosage) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()


            val intent = Intent(this, VitaminAsupplimentaryEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        vitaminASupRecyclerView.adapter = vitaminASupAdapter


        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)


        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()


        // Fetch child data from FHIR server
        fetchVitaminASupFromFHIR()


        // Fetch patient data
        fetchPatientData()
    }


    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@VitaminAsupplimentaryView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@VitaminAsupplimentaryView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@VitaminAsupplimentaryView, "identifier")


                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@VitaminAsupplimentaryView)
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


                            formatter.saveSharedPreference(this@VitaminAsupplimentaryView, "patientName", patientName)
                            formatter.saveSharedPreference(this@VitaminAsupplimentaryView, "dob", dob)


                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@VitaminAsupplimentaryView, "identifier", identifier)
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
                Log.e("VitaminAsupplimentaryView", "Error fetching patient data: ${e.message}")
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


    private fun fetchVitaminASupFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("VitaminAsupplimentaryView", "Raw Response body: $rawResponse")


                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)


                                    // Clear list before adding new items
                                    vitaminDosage.clear()


                                    // Extract child data from the bundle
                                    extractVitaminAFromBundle(bundle)


                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("VitaminAsupplimentaryView", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@VitaminAsupplimentaryView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@VitaminAsupplimentaryView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@VitaminAsupplimentaryView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }


                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("VitaminAsupplimentaryView", "Error occurred while fetching data", t)
                        Toast.makeText(this@VitaminAsupplimentaryView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }


    private fun extractVitaminAFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                extractVitaminARecordsFromQuestionnaire(resource)
            }
        }


        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            vitaminASupAdapter.notifyDataSetChanged()
        }


    }


    private fun extractVitaminARecordsFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        if (vitaminDosage.any { it.id == responseId }) {
            Log.d("VitaminAsupplimentaryView", "Vitamin A supplementary with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var dosename: String? = null
        var dategiven: String? = null
        var nextvisit: String? = null
        var agegiven: String? = null

        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                // Dose selection based on age
                "3064e91f-d908-4557-aa4a-4a1adc223a56", "01128d61-4f6c-4cc4-8850-516563ee8f80" -> {
                    dosename = item.answer.firstOrNull()?.valueStringType?.value.toString()
                }
                // Date given
                "e720967b-f434-48c8-856c-9b70f63d11df" -> {
                    dategiven = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
                // Next visit
                "e1e5e113-efad-4cd8-a3e1-99cdd24ebc4c" -> {
                    nextvisit = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
                // Age given (if this is part of the form)
                "5d33188b-f609-40a8-f5a8-218b61afc8d2" -> {
                    agegiven = item.answer.firstOrNull()?.valueStringType?.value.toString()
                }
            }
        }

        // Add the entry to vitaminDosage list if required fields are present
        if (!dosename.isNullOrEmpty() && !nextvisit.isNullOrEmpty()) {
            val vitaminASup = VitaminASup(
                id = responseId,
                dose = dosename.toString(),
                age = agegiven.toString(),
                nextVisit = nextvisit.toString(),
                dateGiven = dategiven.toString()
            )
            vitaminDosage.add(vitaminASup)
            Log.d("VitaminAsupplimentaryView", "Added VitaminASup Vaccine: $dosename, Date Given: $agegiven, Next Visit: $nextvisit")
        }
    }



    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }




    private fun toggleViews() {
        if (vitaminDosage.isEmpty()) {
            vitaminASupRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            vitaminASupRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
