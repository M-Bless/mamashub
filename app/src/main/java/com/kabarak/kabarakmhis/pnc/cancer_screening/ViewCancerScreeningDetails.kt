package com.kabarak.kabarakmhis.pnc.cancer_screening

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.CancerScreening
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.Bundle

class ViewCancerScreeningDetails : AppCompatActivity() {

    private lateinit var screeningRecyclerView: RecyclerView
    private lateinit var cancerScreeningAdapter: CancerScreeningAdapter
    private var screenings: MutableList<CancerScreening> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_cancer_screening_details)

        patientId = intent.getStringExtra("identifier") ?: ""
        Toast.makeText(this, if (patientId.isNotEmpty()) "Received patient ID: $patientId" else "Patient ID not found", Toast.LENGTH_SHORT).show()

        formatter = FormatterClass()
        retrofitCallsFhir = RetrofitCallsFhir()

        screeningRecyclerView = findViewById(R.id.recycler_view_child)
        screeningRecyclerView.layoutManager = LinearLayoutManager(this)
        cancerScreeningAdapter = CancerScreeningAdapter(screenings) { responseId ->
            val intent = Intent(this, CancerScreeningEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        screeningRecyclerView.adapter = cancerScreeningAdapter

        fetchCancerScreeningResponses()
    }

    private fun fetchCancerScreeningResponses() {
        CoroutineScope(Dispatchers.IO).launch {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            try {
                                val fhirContext = FhirContext.forR4()
                                val parser = fhirContext.newJsonParser()
                                val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                screenings.clear()
                                extractScreeningDataFromBundle(bundle)

                                runOnUiThread {
                                    if (screenings.isEmpty()) {
                                        Toast.makeText(this@ViewCancerScreeningDetails, "No cancer screening data found", Toast.LENGTH_SHORT).show()
                                    } else {
                                        cancerScreeningAdapter.notifyDataSetChanged()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("CancerScreening", "Error parsing response", e)
                                runOnUiThread {
                                    Toast.makeText(this@ViewCancerScreeningDetails, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ViewCancerScreeningDetails, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("CancerScreening", "Error occurred while fetching data", t)
                        Toast.makeText(this@ViewCancerScreeningDetails, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun extractScreeningDataFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                val extractedPatientId = resource.subject.reference.split("/").lastOrNull() ?: ""
                if (extractedPatientId == patientId) {
                    val responseId = resource.id
                    resource.item.forEach { item ->
                        screenings.add(
                            CancerScreening(
                                item.linkId,
                                item.text,
                                extractAnswers(item),
                                responseId
                            )
                        )
                    }
                    Log.d("CancerScreening", "Added screening data for patient: $extractedPatientId with response ID: $responseId")
                }
            }
        }
    }

    private fun extractAnswers(item: QuestionnaireResponse.QuestionnaireResponseItemComponent): String {
        val answers = StringBuilder()
        item.answer.forEach { answer ->
            answers.append(
                when (val value = answer.value) {
                    is org.hl7.fhir.r4.model.StringType -> value.value
                    is org.hl7.fhir.r4.model.Coding -> value.display ?: "No display"
                    else -> value?.toString() ?: "No value"
                }
            ).append("\n")
        }
        return answers.toString().trim()
    }
}
