package com.kabarak.kabarakmhis.pnc.milestone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MilestoneAdd : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private var questionnaireJsonString: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milestone_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Load the questionnaire JSON
        questionnaireJsonString = getStringFromAssets("milestone.json")

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
            Log.d("Milestone Planning", "Submit request received")
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
            retrofitCallsFhir.submitQuestionnaireResponse(questionnaireResponseString, object :
                Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MilestoneAdd, "Successfully submitted!", Toast.LENGTH_SHORT).show()
                        Log.d("Milestone", "Successfully submitted the questionnaire response.")
                        finish()
                    } else {
                        Toast.makeText(this@MilestoneAdd, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Failed to submit. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@MilestoneAdd, "Error occurred while submitting: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Error", "Error occurred while submitting questionnaire", t)
                }
            })
        } else {
            Log.e("submitQuestionnaire", "QuestionnaireFragment not found or is null")
        }
    }
    private fun submitExtractedResources(questionnaireResponse: org.hl7.fhir.r4.model.QuestionnaireResponse) {
        lifecycleScope.launch {
            try {
                val fhirContext = FhirContext.forR4()
                val jsonParser = fhirContext.newJsonParser()

                // Parse the questionnaire JSON
                val questionnaire = jsonParser.parseResource(questionnaireJsonString) as Questionnaire
                // Extract resources to a Bundle
                val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse) as org.hl7.fhir.r4.model.Bundle
                bundle.type = org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION

                // Set HTTP method and URL for each entry
                bundle.entry.forEach { entry ->
                    entry.request = org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                        .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST)
                        .setUrl(entry.resource.fhirType())
                }

                // Ensure only one valid Encounter entry
                val encounterResource = org.hl7.fhir.r4.model.Encounter().apply {
                    status = org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED
                    class_ = org.hl7.fhir.r4.model.Coding().apply {
                        system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
                        code = "AMB"
                        display = "ambulatory"
                    }
                }
                val encounterEntry = org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                    .setResource(encounterResource)
                    .setRequest(
                        org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                            .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST)
                            .setUrl("Encounter")
                    )

                // Remove any extra or empty Encounter entries
                bundle.entry.removeAll { it.request?.url == "Encounter" && it.resource == null }

                // Add the valid Encounter entry only if needed
                bundle.addEntry(encounterEntry)

                // Convert Bundle to JSON
                val bundleJson = jsonParser.encodeResourceToString(bundle)
                Log.d("extraction result", bundleJson)

                // Submit the JSON string
                retrofitCallsFhir.submitExtractedBundle(bundleJson, object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MilestoneAdd, "Resources submitted successfully!", Toast.LENGTH_SHORT).show()
                            Log.d("ChildCivilRegistration", "Successfully submitted the extracted FHIR resources.")
                            finish()
                        } else {
                            Toast.makeText(this@MilestoneAdd, "Resource submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                            Log.e("Error", "Resource submission failed. Response code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MilestoneAdd, "Resource submission failed: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Failed to submit extracted resources. Error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("submitExtractedResources", "Failed to extract and submit FHIR resources", e)
            }
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