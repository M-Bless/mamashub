package com.kabarak.kabarakmhis.pnc

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
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildEdit : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private var questionnaireJsonString: String? = null
    private lateinit var responseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_edit)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Load the questionnaire JSON
        questionnaireJsonString = getStringFromAssets("new-patient-registration.json")
        responseId = intent.getStringExtra("responseId") ?: ""

        if (savedInstanceState == null && questionnaireJsonString != null) {
            // Render the initial questionnaire
            renderInitialQuestionnaire()

            // Fetch and populate the questionnaire response with the assigned responseId
            CoroutineScope(Dispatchers.IO).launch {
                fetchAndPopulateQuestionnaireResponse(responseId)
            }
        }

        // Listen for the submit request from the QuestionnaireFragment
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this
        ) { _, _ ->
            Log.d("ChildEdit", "Submit request received")
            updateQuestionnaireResponse() // Update function when the form is submitted
        }
    }

    private fun getStringFromAssets(fileName: String): String? {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("ChildEdit", "Error reading JSON from assets", e)
            null
        }
    }

    private fun renderInitialQuestionnaire() {
        // Render the questionnaire from the JSON file
        val questionnaireFragment = QuestionnaireFragment.builder()
            .setQuestionnaire(questionnaireJsonString!!)
            .build()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, questionnaireFragment, "initial-questionnaire-fragment")
        }
    }

    private suspend fun fetchAndPopulateQuestionnaireResponse(responseId: String) {
        retrofitCallsFhir.fetchQuestionnaireResponse(responseId, object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (response.isSuccessful) {
                        response.body()?.string()?.let { questionnaireResponseString ->
                            try {
                                val fhirContext = FhirContext.forR4()
                                val jsonParser = fhirContext.newJsonParser()
                                val questionnaireResponse = jsonParser.parseResource(QuestionnaireResponse::class.java, questionnaireResponseString)
                                populateQuestionnaireFragment(questionnaireResponse)
                            } catch (e: Exception) {
                                Log.e("ChildEdit", "Error parsing questionnaire response", e)
                                Toast.makeText(this@ChildEdit, "Error populating questionnaire", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            Toast.makeText(this@ChildEdit, "Failed to retrieve the response data.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("ChildEdit", "Failed to fetch response. Response code: ${response.code()}")
                        Toast.makeText(this@ChildEdit, "Failed to fetch the questionnaire response: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CoroutineScope(Dispatchers.Main).launch {
                    Log.e("Error", "Error occurred while fetching questionnaire response", t)
                    Toast.makeText(this@ChildEdit, "Error occurred while fetching: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun populateQuestionnaireFragment(questionnaireResponse: QuestionnaireResponse) {
        try {
            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser()
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)

            val questionnaireFragment = QuestionnaireFragment.builder()
                .setQuestionnaire(questionnaireJsonString!!)
                .setQuestionnaireResponse(questionnaireResponseString)
                .build()

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container_view, questionnaireFragment, "populated-questionnaire-fragment")
            }

            Log.d("ChildEdit", "Questionnaire response populated successfully.")
        } catch (e: Exception) {
            Log.e("ChildEdit", "Error initializing the questionnaire fragment", e)
            Toast.makeText(this, "Error initializing questionnaire", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateQuestionnaireResponse() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            val questionnaireResponse: QuestionnaireResponse = fragment.getQuestionnaireResponse()
            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser()

            // Initialize meta and increment versionId
            questionnaireResponse.meta = questionnaireResponse.meta ?: Meta()
            val currentVersionId = questionnaireResponse.meta.versionId?.toIntOrNull() ?: 0
            questionnaireResponse.meta.versionId = (currentVersionId + 1).toString()
            questionnaireResponse.id = responseId // Set the ID to responseId

            // Serialize the response to a JSON string
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)
            
            // Log the response (you can replace this with saving to a database or sending to a server)
            Log.d("submitUpdatedResponse", questionnaireResponseString)

            // Submit the updated response back to the server
            retrofitCallsFhir.updateQuestionnaireResponse(responseId, questionnaireResponseString, object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ChildEdit, "Successfully updated!", Toast.LENGTH_SHORT).show()
                            Log.d("ChildEdit", "Successfully updated the questionnaire response.")
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "No error body"
                            Log.e("Error", "Failed to update. Response code: ${response.code()}, Body: $errorBody")
                            Toast.makeText(this@ChildEdit, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@ChildEdit, "Error occurred while updating: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Error occurred while updating questionnaire response", t)
                    }
                }
            })
        } else {
            Log.e("updateQuestionnaireResponse", "QuestionnaireFragment not found or is null")
        }
    }


}
