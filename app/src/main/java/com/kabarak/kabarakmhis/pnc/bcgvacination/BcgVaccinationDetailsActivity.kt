package com.kabarak.kabarakmhis.pnc.bcgvacination

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.uhn.fhir.context.FhirContext
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.Detail
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BcgVaccinationDetailsActivity : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BcgVaccinationDetailsAdapter
    private val detailsList = mutableListOf<Detail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bcg_vaccination_details)

        retrofitCallsFhir = RetrofitCallsFhir()
        recyclerView = findViewById(R.id.detailsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BcgVaccinationDetailsAdapter(detailsList)
        recyclerView.adapter = adapter

        val responseId = intent.getStringExtra("responseId") ?: ""
        fetchVaccinationDetails(responseId)

        val editButton: Button = findViewById(R.id.editButton)
        editButton.setOnClickListener {
            val intent = Intent(this, BcgVaccinationEditActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
    }

    private fun fetchVaccinationDetails(responseId: String) {
        retrofitCallsFhir.fetchQuestionnaireResponse(responseId, object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { jsonResponse ->
                        parseQuestionnaireResponse(jsonResponse)
                    } ?: showError("Failed to retrieve data")
                } else {
                    showError("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun parseQuestionnaireResponse(jsonResponse: String) {
        try {
            val fhirContext = FhirContext.forR4()
            val parser = fhirContext.newJsonParser()
            val questionnaireResponse = parser.parseResource(QuestionnaireResponse::class.java, jsonResponse)

            detailsList.clear()
            questionnaireResponse.item.forEach { item ->
                processItemRecursively(item)
            }
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            showError("Error displaying details")
            Log.e("BcgVaccinationDetailsActivity", "Error parsing response", e)
        }
    }

    private fun processItemRecursively(item: QuestionnaireResponse.QuestionnaireResponseItemComponent) {
        val detailQuestion = item.text ?: ""
        val detailAnswer = extractAnswers(item)

        if (detailAnswer.isNotBlank()) {
            detailsList.add(Detail(detailQuestion, detailAnswer))
        }

        item.item.forEach { subItem -> processItemRecursively(subItem) }
    }

    private fun extractAnswers(item: QuestionnaireResponse.QuestionnaireResponseItemComponent): String {
        val answers = StringBuilder()
        item.answer.forEach { answer ->
            when (val value = answer.value) {
                is org.hl7.fhir.r4.model.StringType -> answers.append("Manufacturer: ${value.value}").append("\n")
                is org.hl7.fhir.r4.model.Coding -> answers.append("Status: ${value.display ?: "No display"}").append("\n")
                is org.hl7.fhir.r4.model.IntegerType -> answers.append("Batch Number: ${value.value}").append("\n")
                is org.hl7.fhir.r4.model.BooleanType -> answers.append("Boolean Answer: ${value.value}").append("\n")
                is org.hl7.fhir.r4.model.DateType -> {
                    if (item.text == "Date of next visit" || item.text == "Date of expiry") {
                        answers.append("${item.text}: ${value.valueAsString}").append("\n")
                    } else {
                        answers.append("Date Given: ${value.valueAsString}").append("\n")
                    }
                }
                is org.hl7.fhir.r4.model.DateTimeType -> {
                    if (item.text == "Date Given") {
                        answers.append("Date Given: ${value.valueAsString}").append("\n")
                    }
                }
                else -> answers.append(value?.toString() ?: "No value").append("\n")
            }
        }
        return answers.toString().trim() // Remove extra whitespace
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
