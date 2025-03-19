package com.kabarak.kabarakmhis.pnc

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.gson.Gson
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.extensions.readFileFromAssets
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class ChildAdd : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var viewModel: ChildAddViewModel
    private var questionnaireJsonString: String? = null
    private var identifier: String? = null // Store the identifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_add)

        // Retrieve the identifier from the intent
        identifier = intent.getStringExtra("identifier")

        if (identifier.isNullOrEmpty()) {
            Toast.makeText(this, "Identifiear not found", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if no identifier is found
            return
        }

        // Pass the identifier in the SavedStateHandle
        val savedStateHandle = SavedStateHandle(
            mapOf(
                ChildAddViewModel.QUESTIONNAIRE_FILE_PATH_KEY to "new-patient-registration.json",
                "identifier" to identifier!! // Add identifier to SavedStateHandle
            )
        )

        // Initialize the ViewModel
        viewModel = ChildAddViewModel(application, savedStateHandle)

        // Load the questionnaire JSON

        questionnaireJsonString = getStringFromAssets("new-patient-registration.json")


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

        // Observe ViewModel LiveData for save status
        viewModel.isChildSaved.observe(this) { isSaved ->
            if (isSaved) {
                Toast.makeText(this, "Child registration saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save child registration.", Toast.LENGTH_SHORT).show()
            }
        }

        // Submit button listener
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this,
        ) { _, _ ->
            Log.d("ChildAdd", "Submit request received")
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
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
                        Toast.makeText(this@ChildAdd, "Successfully submitted!", Toast.LENGTH_SHORT).show()
                        finish()
                        Log.d("ChildAdd", "Successfully submitted the questionnaire response.")
                    } else {
                        Toast.makeText(this@ChildAdd, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Failed to submit. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ChildAdd, "Error occurred while submitting: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Error", "Error occurred while submitting questionnaire", t)
                }
            })

        } else {
            Log.e("ChildAdd", "QuestionnaireFragment not found or is null")
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


}
