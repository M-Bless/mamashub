package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.QuestionnaireUtil

class ChildPostnatalCare : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_child_postnatal_care)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure a QuestionnaireFragment
        val questionnaireJsonString = getStringFromAssets("BabyPostnatalCare-nb-NO-vv1.0.2.json")

        if (savedInstanceState == null && questionnaireJsonString != null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                val fragment = QuestionnaireFragment()
                fragment.arguments = bundleOf(QuestionnaireUtil.getExtraQuestionnaireJsonString() to questionnaireJsonString)
                add(R.id.fragment_container_view, fragment)
            }
        } else {
            Log.e("ChildPostanalRecord", "Failed to load questionnaire JSON")
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
            Log.e("ChildPostnatalDetails", "QuestionnaireFragment not found")
        }
        // Submit button callback
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this,
        ) { _, _ ->
            Log.d("ChildPostnatalDetails", "Submit request received")
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