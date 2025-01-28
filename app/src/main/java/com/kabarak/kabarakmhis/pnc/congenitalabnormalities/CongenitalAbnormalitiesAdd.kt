package com.kabarak.kabarakmhis.pnc.congenitalabnormalities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.gson.Gson
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.ChildAddViewModel
import com.kabarak.kabarakmhis.pnc.extensions.readFileFromAssets
import org.hl7.fhir.r4.model.QuestionnaireResponse

class CongenitalAbnormalitiesAdd : AppCompatActivity() {

    private lateinit var viewModel: ChildAddViewModel
    private var questionnaireJsonString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congenital_abnormalities_add)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[ChildAddViewModel::class.java]

        // Load the congenital abnormalities questionnaire JSON from assets
        questionnaireJsonString = readFileFromAssets("congenital_abnormalities.json")

        if (questionnaireJsonString == null) {
            Toast.makeText(this, "Failed to load Congenital Abnormalities questionnaire.", Toast.LENGTH_SHORT).show()
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
        viewModel.isChildSaved.observe(this) { isSaved ->
            if (isSaved) {
                Toast.makeText(this, "Congenital Abnormalities data saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save Congenital Abnormalities data.", Toast.LENGTH_SHORT).show()
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
            Log.d("CongenitalAbnormalitiesAdd", "QuestionnaireResponse (JSON): $jsonResponse")

            // Save the response using ChildAddViewModel
            questionnaireJsonString?.let { questionnaireJson ->
                viewModel.saveChild(questionnaireResponse)
            } ?: Log.e("CongenitalAbnormalitiesAdd", "Questionnaire JSON is null")
        } else {
            Log.e("CongenitalAbnormalitiesAdd", "QuestionnaireFragment not found")
        }
    }
}
