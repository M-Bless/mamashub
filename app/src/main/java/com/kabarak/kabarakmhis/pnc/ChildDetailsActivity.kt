package com.kabarak.kabarakmhis.pnc

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
import com.kabarak.kabarakmhis.pnc.data_class.ChildDetail
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildDetailsActivity : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChildDetailsAdapter
    private var detailsList = mutableListOf<ChildDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_details)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.detailsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChildDetailsAdapter(detailsList)
        recyclerView.adapter = adapter

        // Get responseId from intent
        val responseId = intent.getStringExtra("responseId") ?: ""
        fetchChildDetails(responseId)

        val editButton: Button = findViewById(R.id.editButton)
        editButton.setOnClickListener {
            val intent = Intent(this, ChildEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
    }

    private fun fetchChildDetails(responseId: String) {
        retrofitCallsFhir.fetchQuestionnaireResponse(responseId, object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { jsonResponse ->
                        parseQuestionnaireResponse(jsonResponse)
                    } ?: run {
                        Toast.makeText(this@ChildDetailsActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ChildDetailsActivity", "Failed to fetch response. Code: ${response.code()}")
                    Toast.makeText(this@ChildDetailsActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ChildDetailsActivity", "Error occurred", t)
                Toast.makeText(this@ChildDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseQuestionnaireResponse(jsonResponse: String) {
        try {
            val fhirContext = FhirContext.forR4()
            val parser = fhirContext.newJsonParser()
            val questionnaireResponse = parser.parseResource(QuestionnaireResponse::class.java, jsonResponse)

            // Clear any existing data in the list
            detailsList.clear()

            // Extract details from each item in the QuestionnaireResponse
            questionnaireResponse.item.forEach { item ->
                processItem(item)
            }

            // Notify adapter about data change
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("ChildDetailsActivity", "Error parsing response", e)
            Toast.makeText(this, "Error displaying details", Toast.LENGTH_SHORT).show()
        }
    }

    // Recursive function to extract details from each item and its sub-items
    private fun processItem(item: QuestionnaireResponse.QuestionnaireResponseItemComponent) {
        val questionText = item.text ?: ""
        val answerText = extractAnswers(item)

        // Add extracted details to the list
        // If answerText is empty, only show the question text (title)
        detailsList.add(
            ChildDetail(
                question = questionText,
                answer = if (answerText.isNotBlank()) answerText else ""
            )
        )

        // Recursively process any nested items
        item.item.forEach { subItem ->
            processItem(subItem)
        }
    }


    // Extract answers from a QuestionnaireResponseItemComponent
    private fun extractAnswers(item: QuestionnaireResponse.QuestionnaireResponseItemComponent): String {
        val answers = StringBuilder()
        item.answer.forEach { answer ->
            when (val value = answer.value) {
                is org.hl7.fhir.r4.model.StringType -> answers.append(value.value).append("\n")
                is org.hl7.fhir.r4.model.Coding -> answers.append(value.display ?: "No display").append("\n")
                is org.hl7.fhir.r4.model.IntegerType -> answers.append(value.value.toString()).append("\n")
                is org.hl7.fhir.r4.model.BooleanType -> answers.append(value.value.toString()).append("\n")
                is org.hl7.fhir.r4.model.DateType -> answers.append(value.valueAsString).append("\n")
                else -> answers.append(value?.toString() ?: "No value").append("\n")
            }
        }
        return answers.toString().trim()
    }
}
