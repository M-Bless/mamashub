package com.kabarak.kabarakmhis.pnc.congenitalabnormalities

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
import com.kabarak.kabarakmhis.pnc.data_class.CongenitalAbnormality
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.pnc.ChildAdd
import com.kabarak.kabarakmhis.pnc.ChildDetailsActivity
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd
import kotlinx.android.synthetic.main.activity_child_birth_view.tvANCID
import kotlinx.android.synthetic.main.activity_child_birth_view.tvAge
import kotlinx.android.synthetic.main.activity_child_birth_view.tvName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.hl7.fhir.r4.model.QuestionnaireResponse

class CongenitalAbnormalitiesViewActivity : AppCompatActivity() {

    private lateinit var abnormalitiesRecyclerView: RecyclerView
    private lateinit var abnormalitiesAdapter: CongenitalAbnormalitiesAdapter
    private var abnormalities: MutableList<CongenitalAbnormality> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View


    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congenital_abnormalities_view)

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
            val intent = Intent(this, CongenitalAbnormalitiesAdd::class.java)
            startActivity(intent)
        }

        noRecordView = findViewById(R.id.no_record)

        abnormalitiesRecyclerView = findViewById(R.id.recycler_view_abnormalities)
        abnormalitiesRecyclerView.layoutManager = LinearLayoutManager(this)

        abnormalitiesAdapter = CongenitalAbnormalitiesAdapter(abnormalities) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CongenitalAbnormalitiesDetailsActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)        }
        abnormalitiesRecyclerView.adapter = abnormalitiesAdapter

        retrofitCallsFhir = RetrofitCallsFhir()

        fetchAbnormalitiesFromFHIR()
        fetchPatientData()

    }
    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@CongenitalAbnormalitiesViewActivity)
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

                            formatter.saveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@CongenitalAbnormalitiesViewActivity, "identifier", identifier)
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

    private fun fetchAbnormalitiesFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("AbnormalitiesActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    abnormalities.clear()
                                    extractAbnormalitiesFromBundle(bundle)
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("AbnormalitiesActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@CongenitalAbnormalitiesViewActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@CongenitalAbnormalitiesViewActivity, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@CongenitalAbnormalitiesViewActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("AbnormalitiesActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@CongenitalAbnormalitiesViewActivity, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractAbnormalitiesFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                extractAbnormalitiesFromQuestionnaire(resource)
            }
        }

        runOnUiThread {
            abnormalitiesAdapter.notifyDataSetChanged()
        }
    }

    private fun extractAbnormalitiesFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        var abnormalityDescription: String? = null
        var remarks: String? = null

        // Traverse items in the QuestionnaireResponse
        for (item in questionnaireResponse.item) {
            for (subItem in item.item) {
                when (subItem.linkId) {
                    "540079341102" -> {
                        // Attempt to extract abnormality description
                        abnormalityDescription = extractAnswerValue(subItem)
                    }
                    "5872260133687" -> {
                        // Attempt to extract remarks
                        remarks = extractAnswerValue(subItem)
                    }
                }
            }
        }

        // Add the abnormality only if abnormalityDescription is present
        if (!abnormalityDescription.isNullOrEmpty()) {
            val abnormality = CongenitalAbnormality(
                id = responseId,
                description = abnormalityDescription,
                remarks = remarks
            )
            abnormalities.add(abnormality)
            Log.d("AbnormalitiesActivity", "Added abnormality: $abnormalityDescription, Remarks: $remarks, Response ID: $responseId")
        } else {
            Log.d("AbnormalitiesActivity", "No valid abnormality found for response ID: $responseId")
        }
    }

    // Helper function to extract answer value from multiple possible FHIR types
    private fun extractAnswerValue(subItem: QuestionnaireResponse.QuestionnaireResponseItemComponent): String? {
        val answer = subItem.answer.firstOrNull()

        // Try each possible answer type
        return when {
            answer?.valueStringType != null -> answer.valueStringType.value
            answer?.valueCoding?.display != null -> answer.valueCoding.display
            answer?.valueDateType != null -> answer.valueDateType.value.toString()
            answer?.valueBooleanType != null -> answer.valueBooleanType.value.toString()
            else -> {
                Log.d("AbnormalitiesActivity", "Failed to extract value for linkId: ${subItem.linkId}")
                null
            }
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    private fun toggleViews() {
        if (abnormalities.isEmpty()) {
            abnormalitiesRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            abnormalitiesRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
