package com.kabarak.kabarakmhis.pnc.eye_problems

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.EyeProblems
import kotlinx.android.synthetic.main.activity_eye_problem_view.btnAdd
import org.hl7.fhir.r4.model.QuestionnaireResponse

class EyeProblemView : AppCompatActivity() {

    private lateinit var problemsRecyclerView: RecyclerView
    private lateinit var eyeProblemAdapter: EyeProblemAdapter
    private var problems: MutableList<EyeProblems> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View

    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eye_problem_view)

        initializeComponents()
        setupRecyclerView()
        setupAddButton()

        fetchProblemsFromFHIR()
        fetchPatientData()
    }

    private fun initializeComponents() {
        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        noRecordView = findViewById(R.id.no_record)
        retrofitCallsFhir = RetrofitCallsFhir()
    }

    private fun setupRecyclerView() {
        problemsRecyclerView = findViewById(R.id.recycler_view_eye_problem)
        problemsRecyclerView.layoutManager = LinearLayoutManager(this)

        eyeProblemAdapter = EyeProblemAdapter(problems) { responseId ->
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EyeProblemEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        problemsRecyclerView.adapter = eyeProblemAdapter
    }

    private fun setupAddButton() {
        btnAdd.setOnClickListener {
            val intent = Intent(this, EyeProblemAdd::class.java)
            startActivity(intent)
        }
    }

    private fun fetchPatientData() {
        // Implementation for fetching and displaying patient data
    }

    private fun fetchProblemsFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            processProblemsResponse(responseBody.string())
                        } ?: runOnUiThread {
                            Toast.makeText(this@EyeProblemView, "No data found", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    } else {
                        showToast("Failed to fetch data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
        }
    }

    private fun processProblemsResponse(rawResponse: String) {
        if (rawResponse.isNotEmpty()) {
            try {
                val fhirContext = FhirContext.forR4()
                val parser = fhirContext.newJsonParser()
                val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                problems.clear()
                extractProblemsFromBundle(bundle)
                runOnUiThread { updateUI() }
            } catch (e: Exception) {
                showToast("Failed to parse response")
                Log.e("EyeProblemView", "Error parsing response", e)
            }
        }
    }

    private fun extractProblemsFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                extractProblemsFromQuestionnaire(resource)
            }
        }
    }

    private fun extractProblemsFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id
        if (problems.any { it.id == responseId }) return

        var problemsType: String? = null
        var problemsDate: String? = null

        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                "fc352a37-796d-40b9-a06f-272d626b4a4d" -> {
                    problemsType = item.answer.firstOrNull()?.valueCoding?.display.toString()

                }
                "a0faf44a-aa7c-442c-aaa4-922894cbe60a" -> {
                    problemsDate = item.answer.firstOrNull()?.valueDateType?.value.toString()
                }
            }
        }

        if (!problemsType.isNullOrEmpty() && !problemsDate.isNullOrEmpty()) {
            val eyeproblem= EyeProblems(id = responseId, VisitType = problemsType, VisitDate = problemsDate)
            problems.add(eyeproblem)
            Log.d("EyeProblemView", "EyeProblem added: Type: $problemsType, Date: $problemsDate, Response ID: $responseId")
        }
    }

    private fun updateUI() {
        eyeProblemAdapter.notifyDataSetChanged()
        toggleViews()
    }

    private fun toggleViews() {
        if (problems.isEmpty()) {
            problemsRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            problemsRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@EyeProblemView, message, Toast.LENGTH_SHORT).show()
            toggleViews()
        }
    }
}
