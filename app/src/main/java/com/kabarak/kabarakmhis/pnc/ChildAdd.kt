package com.kabarak.kabarakmhis.pnc

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.QuestionnaireUtil
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire

class ChildAdd : AppCompatActivity() {

    private var questionnaireJsonString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure a QuestionnaireFragment
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
        } else {
            Log.e("ChildAdd", "Failed to load questionnaire JSON")
        }

        // Get a questionnaire response
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            val questionnaireResponse = fragment.getQuestionnaireResponse()

            // Print the response to the log
            val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)
            Log.d("response", questionnaireResponseString)
        } else {
            Log.e("ChildAdd", "QuestionnaireFragment not found")
        }
        // Submit button callback
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this,
        ) { _, _ ->
            Log.d("ChildAdd", "Submit request received")
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

    private fun enableEdgeToEdge() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    private fun submitQuestionnaire() {
        // Retrieve the QuestionnaireFragment
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            // Get the QuestionnaireResponse from the fragment
            val questionnaireResponse = fragment.getQuestionnaireResponse()

            // Use FHIR's JSON parser to convert QuestionnaireResponse into a JSON string
            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser()

            // Serialize the response to a JSON string
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)

            // Log the response (you can replace this with saving to a database or sending to a server)
            Log.d("submitQuestionnaire", questionnaireResponseString)

            // Optionally, save the response or send it to a server
            saveQuestionnaireResponse(questionnaireResponseString)

            lifecycleScope.launch {
                try {
                    val questionnaire =
                        jsonParser.parseResource(questionnaireJsonString) as Questionnaire
                    val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse)
                    Log.d("extraction result", jsonParser.encodeResourceToString(bundle))
                    // check bundle length
                    // with try catch block
                    try {
                        val bundleLength = bundle.entry.size
                        Log.d("bundleLength", bundleLength.toString())
                    } catch (e: Exception) {
                        Log.e("bundleLength", "Failed to get bundle length", e)
                    }

                } catch (e: Exception){
                    Log.e("submitQuestionnaire", "Failed to extract FHIR resources", e)
                }
            }

        } else {
            Log.e("submitQuestionnaire", "QuestionnaireFragment not found or is null")
        }
    }

    private fun saveQuestionnaireResponse(response: String) {
        try {
            val outputStream = openFileOutput("questionnaire_response.json", MODE_PRIVATE)
            outputStream.write(response.toByteArray(Charsets.UTF_8))
            outputStream.close()
            Log.d("saveQuestionnaireResponse", "Questionnaire response saved successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("saveQuestionnaireResponse", "Failed to save questionnaire response")
        }
    }
}