package com.kabarak.kabarakmhis.pnc

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.gson.Gson
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.ImmunizationViewModel
import com.kabarak.kabarakmhis.pnc.extensions.readFileFromAssets
import org.hl7.fhir.r4.model.QuestionnaireResponse

class ImmunizationAdd : AppCompatActivity() {

    private lateinit var viewModel: ImmunizationViewModel
    private var questionnaireJsonString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_immunization_add)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[ImmunizationViewModel::class.java]

        // Load the questionnaire JSON from assets
        questionnaireJsonString = readFileFromAssets("bcgpolio.json")

        if (questionnaireJsonString == null) {
            Toast.makeText(this, "Failed to load questionnaire.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (savedInstanceState == null) {
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

        // Observe the ViewModel's LiveData for save status
        viewModel.isImmunizationSaved.observe(this) { isSaved ->
            if (isSaved) {
                Toast.makeText(this, "Immunization saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save immunization.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle submit button action from the questionnaire fragment
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this
        ) { _, _ ->
            Log.d("ImmunizationAdd", "Submit request received")
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            val questionnaireResponse: QuestionnaireResponse = fragment.getQuestionnaireResponse()

            // Log the QuestionnaireResponse as JSON
            val gson = Gson()
            val jsonResponse = gson.toJson(questionnaireResponse)
            Log.d("ImmunizationAdd", "QuestionnaireResponse (JSON): $jsonResponse")

            // Send the response to the ViewModel for processing
            questionnaireJsonString?.let { questionnaireJson ->
                viewModel.saveImmunization(questionnaireJson, questionnaireResponse)
            } ?: run {
                Log.e("ImmunizationAdd", "Questionnaire JSON is null")
            }
        } else {
            Log.e("ImmunizationAdd", "QuestionnaireFragment not found")
        }
    }
}
