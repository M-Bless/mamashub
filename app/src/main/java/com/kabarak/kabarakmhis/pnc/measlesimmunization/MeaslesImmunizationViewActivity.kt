package com.kabarak.kabarakmhis.pnc.measlesimmunization

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.congenitalabnormalities.CongenitalAbnormalitiesAdd
import com.kabarak.kabarakmhis.pnc.data_class.MeaslesImmunization
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

class MeaslesImmunizationViewActivity : AppCompatActivity() {

    private lateinit var immunizationRecyclerView: RecyclerView
    private lateinit var immunizationAdapter: MeaslesImmunizationAdapter
    private var immunizations: MutableList<MeaslesImmunization> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View

    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measles_immunization_view)

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
            val intent = Intent(this, MeaslesImmunizationAddActivity::class.java)
            startActivity(intent)
        }

        noRecordView = findViewById(R.id.no_record)
        immunizationRecyclerView = findViewById(R.id.recycler_view_immunization)
        immunizationRecyclerView.layoutManager = LinearLayoutManager(this)

        immunizationAdapter = MeaslesImmunizationAdapter(immunizations) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            val intent = Intent(this, MeaslesImmunizationDetailsActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        immunizationRecyclerView.adapter = immunizationAdapter

        retrofitCallsFhir = RetrofitCallsFhir()
        fetchImmunizationsFromFHIR()
        fetchPatientData()

    }
    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@MeaslesImmunizationViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@MeaslesImmunizationViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@MeaslesImmunizationViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@MeaslesImmunizationViewActivity)
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

                            formatter.saveSharedPreference(this@MeaslesImmunizationViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@MeaslesImmunizationViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@MeaslesImmunizationViewActivity, "identifier", identifier)
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
                Log.e("ChildViewActivity", "Error fetching patient data: ${e.message}")
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


    private fun fetchImmunizationsFromFHIR() {
        retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val rawResponse = responseBody.string()
                        val fhirContext = FhirContext.forR4()
                        val parser = fhirContext.newJsonParser()
                        val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                        immunizations.clear()
                        extractImmunizationsFromBundle(bundle)
                        runOnUiThread { toggleViews() }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MeaslesImmunizationViewActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@MeaslesImmunizationViewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    toggleViews()
                }
            }
        })
    }

    private fun extractImmunizationsFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val fullUrl = entry.fullUrl
            val responseId = extractResponseId(fullUrl)
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                parseImmunizationResponse(resource, responseId)
            }
        }

        runOnUiThread {
            immunizationAdapter.notifyDataSetChanged()
        }
    }

    private fun parseImmunizationResponse(questionnaireResponse: QuestionnaireResponse, responseId: String) {
        var visit = ""
        var dose: String? = null
        var batchNumber: String? = null
        var lotNumber: String? = null
        var manufacturer: String? = null
        var dateOfExpiry: String? = null

        questionnaireResponse.item.forEach { item ->
            when (item.text) {
                "First visit" -> visit = "First visit"
                "second visit" -> visit = "Second visit"
                "third visit" -> visit = "Third visit"
                "Dose 0.5ml, deep subcutaneous injection into the right upper arm deltoidmuscle." -> {
                    dose = item.answer.firstOrNull()?.let { answer ->
                        when (val value = answer.value) {
                            is org.hl7.fhir.r4.model.DateType -> value.valueAsString
                            is org.hl7.fhir.r4.model.DateTimeType -> value.valueAsString
                            else -> null
                        }
                    }
                }
                "Batch number" -> batchNumber = item.answer.firstOrNull()?.let { answer ->
                    when (val value = answer.value) {
                        is org.hl7.fhir.r4.model.IntegerType -> value.value.toString()
                        is org.hl7.fhir.r4.model.StringType -> value.value
                        else -> null
                    }
                }
                "Lot number" -> lotNumber = item.answer.firstOrNull()?.let { answer ->
                    when (val value = answer.value) {
                        is org.hl7.fhir.r4.model.IntegerType -> value.value.toString()
                        is org.hl7.fhir.r4.model.StringType -> value.value
                        else -> null
                    }
                }
                "Manufacturer" -> manufacturer = item.answer.firstOrNull()?.let { answer ->
                    when (val value = answer.value) {
                        is org.hl7.fhir.r4.model.IntegerType -> value.value.toString()
                        is org.hl7.fhir.r4.model.StringType -> value.value
                        else -> null
                    }
                }
                "Date of expiry" -> dateOfExpiry = item.answer.firstOrNull()?.let { answer ->
                    when (val value = answer.value) {
                        is org.hl7.fhir.r4.model.DateType -> value.valueAsString
                        is org.hl7.fhir.r4.model.DateTimeType -> value.valueAsString
                        else -> null
                    }
                }
            }
        }

        if (visit.isNotEmpty()) {
            immunizations.add(MeaslesImmunization(responseId, visit, dose, batchNumber, lotNumber, manufacturer, dateOfExpiry))
        }
    }

    private fun extractResponseId(fullUrl: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(fullUrl)
        return matchResult?.groupValues?.get(1) ?: fullUrl
    }

    private fun toggleViews() {
        if (immunizations.isEmpty()) {
            immunizationRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            immunizationRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
