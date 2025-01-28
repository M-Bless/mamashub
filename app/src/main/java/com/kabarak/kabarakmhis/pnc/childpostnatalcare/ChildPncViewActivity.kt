package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.app.ProgressDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.ChildPncData
import kotlinx.android.synthetic.main.activity_child_pnc_view.*
import kotlinx.android.synthetic.main.pnc_navigator.view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.text.ParseException
import java.util.Locale

class ChildPncViewActivity : AppCompatActivity() {

    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val patients = mutableListOf<ChildPncData>()
    private lateinit var responseId: String // Define responseId globally
    private lateinit var currentId: String // Define responseId globally
    // Define the input format of the received date string
    private val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
    // Define the desired output format
    private val outputDateFormat = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_pnc_view)



        title = "Child Postnatal Care Details"

        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        // Assign questionnaireResponseId to responseId
        responseId = intent.getStringExtra("responseId") ?: ""

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        retrofitCallsFhir = RetrofitCallsFhir()
        fetchChildrenFromFHIR()
        fetchPatientData()
        handleNavigation()
    }


    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientName = formatter.retrieveSharedPreference(this@ChildPncViewActivity, "patientName") ?: ""
                val dob = formatter.retrieveSharedPreference(this@ChildPncViewActivity, "dob") ?: ""
                val identifier = formatter.retrieveSharedPreference(this@ChildPncViewActivity, "identifier") ?: ""

                if (patientName.isEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@ChildPncViewActivity)
                        progressDialog.setTitle("Please wait...")
                        progressDialog.setMessage("Fetching patient details...")
                        progressDialog.show()

                        val patientData = getPatientDataFromFhirEngine()
                        showPatientDetails(patientData.first, patientData.second, identifier)

                        progressDialog.dismiss()
                    }
                } else {
                    showPatientDetails(patientName, dob, identifier)
                }
            } catch (e: Exception) {
                Log.e("ChildPncViewActivity", "Error fetching patient data: ${e.message}")
            }
        }
    }

    private fun showPatientDetails(patientName: String, dob: String?, identifier: String?) {
        tvPatient.text = patientName
        tvANCID.text = identifier
        //tvAge.text = dob?.let { "${formatter.calculateAge(it)} years" } ?: "N/A"
    }

    private fun getPatientDataFromFhirEngine(): Pair<String, String> {
        val patientData = patientDetailsViewModel.getPatientData()
        return Pair(patientData.name, patientData.dob)
    }

    private fun fetchChildrenFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchQuestionnaireResponse(responseId, object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("ChildPncViewActivity", "Raw Response body: $rawResponse")

                            try {
                                val fhirContext = FhirContext.forR4()
                                val parser = fhirContext.newJsonParser()
                                val questionnaireResponse = parser.parseResource(QuestionnaireResponse::class.java, rawResponse)
                                patients.clear()
                                extractChildrenPncFromQuestionnaire(questionnaireResponse)
                            } catch (e: Exception) {
                                showToast("Failed to parse response")
                                Log.e("ChildPncViewActivity", "Error parsing response", e)
                            }
                        }
                    } else {
                        showToast("Failed to fetch data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showToast("Error occurred: ${t.message}")
                    Log.e("ChildPncViewActivity", "Error occurred while fetching data", t)
                }
            })
        }
    }

    private fun extractChildrenPncFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id
        if (patients.any { it.id == responseId }) return

        var timeOfVisit: String? = null
        var generalCondition: String? = null
        var temperature: String? = null
        var breathsPerMinute: String? = null
        var feedingMethod: String? = null
        var umbilicalCordStatus: String? = null
        var clinicalNotes: String? = null
        var nextVisitDate: String? = null

        for (item in questionnaireResponse.item) {
            when (item.linkId) {
                "aa7fb496-8d17-4370-bc88-ddd0316eabf1" -> timeOfVisit = item.answer.firstOrNull()?.valueCoding?.display
                "0590985e-8105-49ed-875b-3ca1c4807702" -> generalCondition = item.answer.firstOrNull()?.valueCoding?.display
                "d7bdda54-477e-43f5-85e6-6ab779382d42" -> temperature = item.answer.firstOrNull()?.valueIntegerType?.value.toString()
                "3e8437b9-1c69-4872-8636-efd095112fed" -> breathsPerMinute = item.answer.firstOrNull()?.valueIntegerType?.value.toString()
                "ace828ee-6239-4751-88fc-5d9ca2b16806" -> feedingMethod = item.answer.firstOrNull()?.valueCoding?.display
                "2b083308-eff3-4205-fa6a-002ffe78bc8f" -> umbilicalCordStatus = item.answer.firstOrNull()?.valueCoding?.display
                "9994db80-2095-4829-81f0-0d199dcfeb5d" -> clinicalNotes = item.answer.firstOrNull()?.valueStringType?.value
                "03e1ed67-5a2e-4829-f0f0-22eb4a294b8b" -> nextVisitDate = item.answer.firstOrNull()?.valueDateType?.value.toString()
            }
        }

        if (!generalCondition.isNullOrEmpty() && !nextVisitDate.isNullOrEmpty()) {
            patients.add(
                ChildPncData(
                    id = responseId,
                    visitTime = timeOfVisit ?: "N/A",
                    generalCondition = generalCondition,
                    temperature = temperature ?: "N/A",
                    breathsPerMinute = breathsPerMinute ?: "N/A",
                    feedingMethod = feedingMethod ?: "N/A",
                    umbilicalCordStatus = umbilicalCordStatus ?: "N/A",
                    clinicalNotes = clinicalNotes ?: "N/A",
                    nextVisitDate = nextVisitDate ?: "N/A"
                )
            )
        }

        runOnUiThread { displayQuestionnaireData() }
    }

    private fun displayQuestionnaireData() {
        // Retrieve the latest entry in the patients list
        val latestEntry = patients.lastOrNull() ?: return
        currentId = latestEntry.id
        // Display the details in the respective TextViews
        tvCondition.text = latestEntry.generalCondition ?: "N/A"
        tvTimeOfVisit.text = latestEntry.visitTime ?: "N/A"
        tvTemperature.text = latestEntry.temperature ?: "N/A"
        tvBreathsPerMinute.text = latestEntry.breathsPerMinute ?: "N/A"
        tvFeedingMethod.text = latestEntry.feedingMethod ?: "N/A"
        tvUmbilicalCordStatus.text = latestEntry.umbilicalCordStatus ?: "N/A"
        tvClinicalNotes.text = latestEntry.clinicalNotes ?: "N/A"
        // Parse and reformat the date
        tvNextVisitDate.text = try {
            val parsedDate = inputDateFormat.parse(latestEntry.nextVisitDate )
            "${outputDateFormat.format(parsedDate)}"
        } catch (e: ParseException) {
            "N/A"
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleNavigation() {
        pnc_navigator.btnEdit.text = "Edit Visit"
        pnc_navigator.btnCancel.text = "Cancel"

        pnc_navigator.btnEdit.setOnClickListener {
            val intent = Intent(this, ChildPncEdit::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)

        }
        pnc_navigator.btnCancel.setOnClickListener {
            startActivity(Intent(this, ChildPncList::class.java))
            finish()
        }
    }
}
