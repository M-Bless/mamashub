package com.kabarak.kabarakmhis.pnc.diphtheria

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
import com.kabarak.kabarakmhis.pnc.data_class.Diphtheria
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
import kotlinx.android.synthetic.main.activity_diphtheria_view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse

class DiphtheriaView : AppCompatActivity() {

    private lateinit var diphtheriaRecyclerView: RecyclerView
    private lateinit var diphtheriaAdapter: DiphtheriaAdapter
    private var diphtherias: MutableList<Diphtheria> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diphtheria_view)

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
            val intent = Intent(this, DiphtheriaAdd::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        diphtheriaRecyclerView = findViewById(R.id.recycler_view_diphtheria)
        diphtheriaRecyclerView.layoutManager = LinearLayoutManager(this)

        diphtheriaAdapter = DiphtheriaAdapter(diphtherias) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DiphtheriaDetails::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        diphtheriaRecyclerView.adapter = diphtheriaAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch diphtheria data from FHIR server
        fetchDiphtheriaDataFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@DiphtheriaView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@DiphtheriaView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@DiphtheriaView, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@DiphtheriaView)
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

                            formatter.saveSharedPreference(this@DiphtheriaView, "patientName", patientName)
                            formatter.saveSharedPreference(this@DiphtheriaView, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@DiphtheriaView, "identifier", identifier)
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
                Log.e("DiphtheriaView", "Error fetching patient data: ${e.message}")
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

    private fun fetchDiphtheriaDataFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("DiphtheriaView", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    diphtherias.clear()

                                    // Extract diphtheria data from the bundle
                                    extractDiphtheriaFromBundle(bundle)

                                    // Show/hide views based on the presence of diphtheria records
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("DiphtheriaView", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@DiphtheriaView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@DiphtheriaView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DiphtheriaView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("DiphtheriaView", "Error occurred while fetching data", t)
                        Toast.makeText(this@DiphtheriaView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractDiphtheriaFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                // Extract diphtheria from each QuestionnaireResponse
                extractDiphtheriaFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new diphtheria data
        runOnUiThread {
            diphtheriaAdapter.notifyDataSetChanged()
        }
    }

    private fun extractDiphtheriaFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a diphtheria record with this ID already exists to avoid duplicates
        if (diphtherias.any { it.id == responseId }) {
            Log.d("DiphtheriaView", "Diphtheria record with ID $responseId already exists. Skipping duplicate.")
            return
        }
        var dose: String? = null
        var dateGiven: String? = null
        var nextVisitDate: String? = null
        var batch: String? = null
        var lotNumber: String? = null
        var manufacturer: String? = null
        var expiryDate: String? = null

        // Iterate over each item in the questionnaire response
        for (item in questionnaireResponse.item) {
            // Check the linkId to assign values to the correct variables
            when (item.linkId) {
                "7245469372851" -> dose = item.answer.firstOrNull()?.valueCoding?.display
                "1706243778715", "326494514561", "964674084101" -> dateGiven = item.answer.firstOrNull()?.valueDateType?.valueAsString
                "837211414406", "217550121148" -> nextVisitDate = item.answer.firstOrNull()?.valueDateType?.valueAsString
                "623710444302", "670034935729", "988275944803" -> batch = item.answer.firstOrNull()?.valueCoding.toString()
                "5130109776129", "149322105195", "766906389085" -> lotNumber = item.answer.firstOrNull()?.valueCoding.toString()
                "5096787115259", "427047554544", "561522366666" -> manufacturer = item.answer.firstOrNull()?.valueCoding.toString()
                "130764843510", "989835392078", "939052861601" -> expiryDate = item.answer.firstOrNull()?.valueDateType?.valueAsString
            }
        }

        // Display parsed values
        Log.d("DiphtheriaView", "Dose: $dose")
        Log.d("DiphtheriaView", "Date Given: $dateGiven")
        Log.d("DiphtheriaView", "Next Visit Date: $nextVisitDate")
        Log.d("DiphtheriaView", "Batch Number: $batch")
        Log.d("DiphtheriaView", "Lot Number: $lotNumber")
        Log.d("DiphtheriaView", "Manufacturer: $manufacturer")
        Log.d("DiphtheriaView", "Expiry Date: $expiryDate")

        // Check required fields and add Diphtheria record if valid
        if (!dose.isNullOrEmpty() && !dateGiven.isNullOrEmpty() && !nextVisitDate.isNullOrEmpty()) {
            val diphtheria = Diphtheria(
                id = responseId,
                dose = dose,
                date = dateGiven,
                nextDate = nextVisitDate,
                batch = batch,
                lotnumber = lotNumber,
                manufacturer = manufacturer,
                expiryDate = expiryDate
            )
            diphtherias.add(diphtheria)
            Log.d("DiphtheriaView", "Added diphtheria record: Dose: $dose, Date Given: $dateGiven, Next Visit Date: $nextVisitDate, Response ID: $responseId")
        } else {
            Log.d("DiphtheriaView", "Incomplete data, Diphtheria record not added.")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (diphtherias.isEmpty()) {
            diphtheriaRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            diphtheriaRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}