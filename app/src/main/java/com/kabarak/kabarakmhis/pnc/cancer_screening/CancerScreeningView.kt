package com.kabarak.kabarakmhis.pnc.cancer_screening

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
import com.kabarak.kabarakmhis.pnc.data_class.CancerScreening
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.android.synthetic.main.activity_cancer_screening_view.btnAdd
import org.hl7.fhir.r4.model.QuestionnaireResponse

class CancerScreeningView : AppCompatActivity() {

    private lateinit var screeningRecyclerView: RecyclerView
    private lateinit var cancerScreeningAdapter: CancerScreeningAdapter
    private var screenings: MutableList<CancerScreening> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View

    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancer_screening_view)
        initializeComponents()
        setupRecyclerView()
        setupAddButton()

        fetchScreeningsFromFHIR()
        fetchPatientData()
    }

    private fun initializeComponents() {
        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)
        patientId = formatter.retrieveSharedPreference(this, "identifier").toString()
        Log.d("CancerScreeningView", "Initialized patient ID: $patientId")

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        noRecordView = findViewById(R.id.no_record)
        retrofitCallsFhir = RetrofitCallsFhir()
    }

    private fun setupRecyclerView() {
        screeningRecyclerView = findViewById(R.id.recycler_view_child)
        screeningRecyclerView.layoutManager = LinearLayoutManager(this)

        cancerScreeningAdapter = CancerScreeningAdapter(screenings) { responseId ->
            Log.d("CancerScreeningView", "Selected Response ID: $responseId")
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ViewCancerScreeningDetails::class.java)
            intent.putExtra("responseId", responseId) // Correctly pass responseId here
            startActivity(intent)
        }
        screeningRecyclerView.adapter = cancerScreeningAdapter
    }


    private fun setupAddButton() {
        btnAdd.setOnClickListener {
            val intent = Intent(this, CancerScreeningAdd::class.java)
            startActivity(intent)
        }
    }

    private fun fetchPatientData() {
        // Implementation for fetching and displaying patient data
    }

    private fun fetchScreeningsFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("CancerScreeningView", "Fetching questionnaire responses from FHIR...")
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            Log.d("CancerScreeningView", "Successfully fetched responses.")
                            processScreeningResponse(responseBody.string())
                        } ?: runOnUiThread {
                            Log.d("CancerScreeningView", "No data found in response body.")
                            Toast.makeText(this@CancerScreeningView, "No data found", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    } else {
                        Log.e("CancerScreeningView", "Failed to fetch data: ${response.message()}")
                        showToast("Failed to fetch data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("CancerScreeningView", "Network error: ${t.message}")
                    showToast("Network error: ${t.message}")
                }
            })
        }
    }

    private fun processScreeningResponse(rawResponse: String) {
        if (rawResponse.isNotEmpty()) {
            try {
                val fhirContext = FhirContext.forR4()
                val parser = fhirContext.newJsonParser()
                val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                screenings.clear()
                extractScreeningsFromBundle(bundle)
                Log.d("CancerScreeningView", "Processed screening responses, total count: ${screenings.size}")
                runOnUiThread { updateUI() }
            } catch (e: Exception) {
                Log.e("CancerScreeningView", "Error parsing response: ${e.localizedMessage}")
                showToast("Failed to parse response")
            }
        }
    }

    private fun extractScreeningsFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                Log.d("CancerScreeningView", "Extracting screening from QuestionnaireResponse ID: ${resource.id}")
                extractScreeningFromQuestionnaire(resource)
            }
        }
    }

    private fun extractScreeningFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id
        Log.d("CancerScreeningView", "Processing QuestionnaireResponse with ID: $responseId")

        if (screenings.any { it.id == responseId }) {
            Log.d("CancerScreeningView", "Screening already added for response ID: $responseId")
            return
        }

        var screeningType: String? = null
        var screeningDate: String? = null

        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                "394b61c6-6505-463b-88c7-84401139aeec" -> {
                    screeningType = item.answer.firstOrNull()?.valueCoding?.display.toString()
                    Log.d(
                        "CancerScreeningView",
                        "Found screening type: $screeningType for response ID: $responseId"
                    )
                }

                "ff18b0c1-f12b-410b-f6c5-4df23dfcc764" -> {
                    screeningDate = item.answer.firstOrNull()?.valueDateType?.value.toString()
                    Log.d(
                        "CancerScreeningView",
                        "Found screening date: $screeningDate for response ID: $responseId"
                    )
                }
            }
        }
        if (!screeningType.isNullOrEmpty() && !screeningDate.isNullOrEmpty()) {
            val screening = CancerScreening(
                id = responseId,
                type = screeningType,
                date = screeningDate,
                responseId = responseId // Pass responseId here
            )
            screenings.add(screening)
            Log.d(
                "CancerScreeningView",
                "Screening added: Type: $screeningType, Date: $screeningDate, Response ID: $responseId"
            )
        }
    }

    private fun updateUI() {
        cancerScreeningAdapter.notifyDataSetChanged()
        toggleViews()
        Log.d("CancerScreeningView", "UI updated, current screenings count: ${screenings.size}")
    }

    private fun toggleViews() {
        if (screenings.isEmpty()) {
            screeningRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
            Log.d("CancerScreeningView", "No screenings found, showing 'no record' view.")
        } else {
            screeningRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
            Log.d("CancerScreeningView", "Screenings found, showing recycler view.")
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@CancerScreeningView, message, Toast.LENGTH_SHORT).show()
            toggleViews()
        }
    }
}