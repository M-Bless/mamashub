package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildPostnatalCare() : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var patientId: String
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private var questionnaireJsonString: String? = null
    private lateinit var kabarakViewModel: KabarakViewModel // Assuming you're using ViewModel for database operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_postnatal_care)

        // Initialize RetrofitCallsFhir and ViewModel
        retrofitCallsFhir = RetrofitCallsFhir()
        kabarakViewModel = KabarakViewModel(applicationContext as Application)

        // Retrieve patient ID from shared preferences
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        title = "Add Child PNC Visit"

        // Load the questionnaire JSON
        questionnaireJsonString = getStringFromAssets("baby-postnatal-care.json")

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
            Log.d("ChildPostnatalCare", "Submit request received")
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
            val questionnaireResponse = fragment.getQuestionnaireResponse()

            val fhirContext = FhirContext.forR4()
            val jsonParser = fhirContext.newJsonParser()
            val questionnaireResponseString = jsonParser.encodeResourceToString(questionnaireResponse)

            Log.d("submitQuestionnaire", questionnaireResponseString)
            saveQuestionnaireResponse(questionnaireResponseString)

            // Submit the QuestionnaireResponse to the server
            retrofitCallsFhir.submitQuestionnaireResponse(questionnaireResponseString, object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                        Toast.makeText(this@ChildPostnatalCare, "Successfully submitted and saved!", Toast.LENGTH_SHORT).show()
                        Log.d("ChildPostnatalCare", "Successfully submitted the questionnaire response.")
                        submitExtractedResources(questionnaireResponse)

                    } else {
                        Toast.makeText(this@ChildPostnatalCare, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", "Failed to submit. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ChildPostnatalCare, "Error occurred while submitting: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Error", "Error occurred while submitting questionnaire", t)
                }
            })
        } else {
            Log.e("submitQuestionnaire", "QuestionnaireFragment not found or is null")
        }
    }

    private fun submitExtractedResources(questionnaireResponse: QuestionnaireResponse) {
        lifecycleScope.launch {
            try {
                val fhirContext = FhirContext.forR4()
                val jsonParser = fhirContext.newJsonParser()

                // Parse the questionnaire JSON
                val questionnaire = jsonParser.parseResource(questionnaireJsonString) as Questionnaire
                // Extract resources to a Bundle
                val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse) as org.hl7.fhir.r4.model.Bundle

                bundle.type = org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION

                // Add the Patient resource with the ID from shared preferences
                val patientResource = org.hl7.fhir.r4.model.Patient().apply {
                    id = patientId // Use the patientId retrieved from shared preferences
                }

                val patientEntry = org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                    .setResource(patientResource)
                    .setRequest(
                        org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                            .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST)
                            .setUrl("Patient")
                    )
                bundle.entry.set(0,patientEntry)

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
                    subject = org.hl7.fhir.r4.model.Reference("Patient/$patientId") // Reference the Patient
                }

                val encounterEntry = org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                    .setResource(encounterResource)
                    .setRequest(
                        org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                            .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST)
                            .setUrl("Encounter")
                    )

                // Remove any invalid or empty entries
                bundle.entry.removeIf { it.resource == null }

                // Add the valid Encounter entry if needed
                bundle.addEntry(encounterEntry)

                // Convert Bundle to JSON
                val bundleJson = jsonParser.encodeResourceToString(bundle)
                Log.d("extraction result", bundleJson)

                // Submit the JSON string
                retrofitCallsFhir.submitExtractedBundle(bundleJson, object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ChildPostnatalCare, "Resources submitted successfully!", Toast.LENGTH_SHORT).show()
                            Log.d("ChildPostnatalCare", "Successfully submitted the extracted FHIR resources.")
                            finish()
                        } else {
                            Toast.makeText(this@ChildPostnatalCare, "Resource submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                            Log.e("Error", "Resource submission failed. Response code: ${response.code()}")
                            val errorBody = response.errorBody()?.string()
                            Log.e("FHIR Server Response", "Error: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@ChildPostnatalCare, "Resource submission failed: ${t.message}", Toast.LENGTH_SHORT).show()
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

