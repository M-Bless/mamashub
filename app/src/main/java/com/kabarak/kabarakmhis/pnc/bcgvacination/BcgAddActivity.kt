package com.kabarak.kabarakmhis.pnc.bcgvacination

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

class BcgAddActivity : AppCompatActivity() {

    private lateinit var viewModel: ImmunizationViewModel
    private var questionnaireJsonString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bcg_vaccination)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[ImmunizationViewModel::class.java]

        // Load the BCG questionnaire JSON from assets
        questionnaireJsonString = readFileFromAssets("bcgpolio.json")

        if (questionnaireJsonString == null) {
            Toast.makeText(this, "Failed to load BCG questionnaire.", Toast.LENGTH_SHORT).show()
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

        // Observe save status
        viewModel.isImmunizationSaved.observe(this) { isSaved ->
            if (isSaved) {
                Toast.makeText(this, "BCG data saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save BCG data.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle submit button action
        supportFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this
        ) { _, _ -> handleSubmit() }
    }

    private fun handleSubmit() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (fragment is QuestionnaireFragment) {
            val questionnaireResponse: QuestionnaireResponse = fragment.getQuestionnaireResponse()

            // Log the QuestionnaireResponse as JSON
            val gson = Gson()
            val jsonResponse = gson.toJson(questionnaireResponse)
            Log.d("BcgAddActivity", "QuestionnaireResponse (JSON): $jsonResponse")

            // Save the response using ImmunizationViewModel
            questionnaireJsonString?.let { questionnaireJson ->
                viewModel.saveImmunization(questionnaireJson, questionnaireResponse)
            } ?: Log.e("BcgAddActivity", "Questionnaire JSON is null")
        } else {
            Log.e("BcgAddActivity", "QuestionnaireFragment not found")
        }
    }
}
