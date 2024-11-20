package com.kabarak.kabarakmhis.pnc.cancer_screening

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancerScreeningEdit : AppCompatActivity() {
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private var questionnaireJsonString: String? = null
    private lateinit var responseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_edit)

        retrofitCallsFhir = RetrofitCallsFhir()
        questionnaireJsonString = getStringFromAssets("cancer_screening.json")
        responseId = intent.getStringExtra("responseId") ?: ""

        if (savedInstanceState == null && questionnaireJsonString != null) {
            renderInitialQuestionnaire()
            fetchAndPopulateQuestionnaireResponse(responseId)
        }

        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this,
        ) { _, _ ->
            Log.d("CancerScreeningEdit", "Submit request received")
            submitUpdatedResponse()
        }
    }

    private fun getStringFromAssets(fileName: String): String? {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("CancerScreeningEdit", "Error loading JSON from assets", e)
            null
        }
    }

    private fun renderInitialQuestionnaire() {
        val questionnaireFragment = QuestionnaireFragment.builder()
            .setQuestionnaire(questionnaireJsonString!!)
            .build()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, questionnaireFragment, "initial-questionnaire-fragment")
        }
    }

    private fun fetchAndPopulateQuestionnaireResponse(responseId: String) {
        retrofitCallsFhir.fetchQuestionnaireResponse(responseId, object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { jsonResponse ->
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val fhirContext = FhirContext.forR4()
                                val parser = fhirContext.newJsonParser()
                                val questionnaireResponse = parser.parseResource(QuestionnaireResponse::class.java, jsonResponse)
                                populateQuestionnaireFragment(questionnaireResponse)
                            } catch (e: Exception) {
                                Log.e("CancerScreeningEdit", "Error parsing and populating questionnaire", e)
                                Toast.makeText(this@CancerScreeningEdit, "Failed to parse response", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        showToastAndLog("Empty response received from server")
                    }
                } else {
                    showToastAndLog("Failed to fetch response: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToastAndLog("Error fetching response: ${t.message}")
            }
        })
    }

    private fun populateQuestionnaireFragment(questionnaireResponse: QuestionnaireResponse) {
        try {
            val fhirContext = FhirContext.forR4()
            val parser = fhirContext.newJsonParser()
            val questionnaireResponseJson = parser.encodeResourceToString(questionnaireResponse)

            val questionnaireFragment = QuestionnaireFragment.builder()
                .setQuestionnaire(questionnaireJsonString!!)
                .setQuestionnaireResponse(questionnaireResponseJson)
                .build()

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container_view, questionnaireFragment, "populated-questionnaire-fragment")
            }
            Log.d("CancerScreeningEdit", "Questionnaire populated successfully")
        } catch (e: Exception) {
            Log.e("CancerScreeningEdit", "Error loading questionnaire with response", e)
            Toast.makeText(this, "Error loading questionnaire", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitUpdatedResponse() {
        val fragment = supportFragmentManager.findFragmentByTag("populated-questionnaire-fragment") as? QuestionnaireFragment
        val updatedQuestionnaireResponse = fragment?.getQuestionnaireResponse()

        updatedQuestionnaireResponse?.let {
            val fhirContext = FhirContext.forR4()
            val parser = fhirContext.newJsonParser()
            val updatedResponseJson = parser.encodeResourceToString(it)

            Log.d("submitUpdatedResponse", "Submitting: $updatedResponseJson")

            retrofitCallsFhir.updateQuestionnaireResponse(responseId, updatedResponseJson, object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        showToast("Data updated successfully")
                        finish()
                    } else {
                        showToastAndLog("Failed to update data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showToastAndLog("Error updating data: ${t.message}")
                }
            })
        } ?: showToast("Failed to retrieve updated response")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastAndLog(message: String) {
        Log.e("CancerScreeningEdit", message)
        showToast(message)
    }
}
