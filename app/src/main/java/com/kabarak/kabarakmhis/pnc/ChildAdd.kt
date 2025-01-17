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
        questionnaireJsonString = readFileFromAssets("new-patient-registration.json")

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

            // Convert the QuestionnaireResponse to JSON using Gson
            val gson = Gson()
            val jsonResponse = gson.toJson(questionnaireResponse)

            // Log the JSON response
            Log.d("ChildAdd", "QuestionnaireResponse (JSON): $jsonResponse")

            // Optionally, display the JSON response in a Toast or TextView
            Toast.makeText(this, "QuestionnaireResponse (JSON): $jsonResponse", Toast.LENGTH_LONG).show()


            // Send the response to the ViewModel for further processing
            viewModel.saveChild(questionnaireResponse)
        } else {
            Log.e("ChildAdd", "QuestionnaireFragment not found or is null")
        }
    }


}
