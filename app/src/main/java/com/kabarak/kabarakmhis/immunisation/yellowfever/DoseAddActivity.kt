package com.kabarak.kabarakmhis.immunisation.yellowfever

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.immunisation.yellowfever.DoseAddActivity
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoseAddActivity : AppCompatActivity() {
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private var questionnaireJsonString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dose_add)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Load the questionnaire JSON
        questionnaireJsonString = getStringFromAssets("YellowFeverVaccine-nb-NO-v1.json")

        if (savedInstanceState == null && questionnaireJsonString != null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(
                    R.id.fragment_container_view,
                    QuestionnaireFragment.builder()
                        .setQuestionnaire(questionnaireJsonString!!)
                        .build()
                )
            }
        }

        // Submit button listener
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this,
        ) { _, _ ->
            Log.d("DoseAddActivity", "Submit request received")
            submitQuestionnaire()
        }
    }

    private fun getStringFromAssets(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun submitQuestionnaire() {
        // Retrieve the QuestionnaireFragment
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            // Get the QuestionnaireResponse from the fragment
            val questionnaireResponse: QuestionnaireResponse = fragment.getQuestionnaireResponse()

            // Use FHIR's JSON parser to convert QuestionnaireResponse into a JSON string
            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser()

            // Serialize the response to a JSON string
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)

            // Log the response (you can replace this with saving to a database or sending to a server)
            Log.d("submitQuestionnaire", questionnaireResponseString)

            // Submit the QuestionnaireResponse to the server using RetrofitCallsFhir
            retrofitCallsFhir.submitQuestionnaireResponse(questionnaireResponseString, object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DoseAddActivity, "Successfully submitted!", Toast.LENGTH_SHORT).show()
                        Log.d("DoseAddActivity", "Successfully submitted the questionnaire response.")

                    } else {
                        Toast.makeText(this@DoseAddActivity, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Failed to submit. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@DoseAddActivity, "Error occurred while submitting: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Error", "Error occurred while submitting questionnaire", t)
                }
            })
        } else {
            Log.e("submitQuestionnaire", "QuestionnaireFragment not found or is null")
        }
    }
}