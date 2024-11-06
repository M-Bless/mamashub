package com.kabarak.kabarakmhis.immunisation.Pconjugate


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
import com.kabarak.kabarakmhis.immunisation.data_class.Conjugate
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.android.synthetic.main.activity_pconjugate_view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse


class PconjugateView : AppCompatActivity() {


    private lateinit var conjugateRecyclerView: RecyclerView
    private lateinit var conjugateAdapter: PconjugateAdapter
    private var patients: MutableList<Conjugate> = mutableListOf()
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
        setContentView(R.layout.activity_pconjugate_view)
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
            val intent = Intent(this, PconjugateAdd::class.java)
            startActivity(intent)
        }


        // Initialize RecyclerView
        conjugateRecyclerView = findViewById(R.id.recycler_view_child)
        conjugateRecyclerView.layoutManager = LinearLayoutManager(this)


        conjugateAdapter = PconjugateAdapter(patients) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()


            val intent = Intent(this, PconjugateEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        conjugateRecyclerView.adapter = conjugateAdapter


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
                val patientLocalName = formatter.retrieveSharedPreference(this@PconjugateView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@PconjugateView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@PconjugateView, "identifier")


                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@PconjugateView)
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


                            formatter.saveSharedPreference(this@PconjugateView, "patientName", patientName)
                            formatter.saveSharedPreference(this@PconjugateView, "dob", dob)


                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@PconjugateView, "identifier", identifier)
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
                Log.e("PconjugateView", "Error fetching patient data: ${e.message}")
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
                            Log.d("PconjugateView", "Raw Response body: $rawResponse")


                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)


                                    // Clear list before adding new items
                                    patients.clear()


                                    // Extract child data from the bundle
                                    extractChildrenPncFromBundle(bundle)


                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("ChildViewActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@PconjugateView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@PconjugateView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PconjugateView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }


                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("ChildViewActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@PconjugateView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
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
            conjugateAdapter.notifyDataSetChanged()
        }


    }


    private fun extractChildrenPncFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id


        if (patients.any { it.id == responseId }) {
            Log.d("PconjugateView", "pConjugate with ID $responseId already exists. Skipping duplicate.")
            return
        }


        var dosename: String? = null
        var nextvisit: String? = null
        var dategiven: String? = null

        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                // Dose selection
                "8f12befa-41d8-4904-be57-d2b6cf574c71" -> {
                    dosename = item.answer.firstOrNull()?.valueCoding?.display.toString()
                }
                // Date given
                "3fc2494d-632e-4f85-9190-9627fcf788c6" -> {
                    dategiven = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
                // Next visit
                "e5d1198a-b5ae-4ac1-8d7d-4f09fa3cd508" -> {
                    nextvisit = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
            }
        }


        // Add the entry to patients list if required fields are present
        if (!dosename.isNullOrEmpty() && !nextvisit.isNullOrEmpty()) {
            val pConjugate = Conjugate(
                id = responseId,
                doseName = dosename.toString(),
                dateGiven = dategiven.toString(),
                nextVisit = nextvisit.toString()
            )
            patients.add(pConjugate)
            Log.d("PconjugateView", "Added PConjugate Vaccine: $dosename, Date Given: $dategiven, Next Visit: $nextvisit")
        }
    }


    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }




    private fun toggleViews() {
        if (patients.isEmpty()) {
            conjugateRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            conjugateRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
