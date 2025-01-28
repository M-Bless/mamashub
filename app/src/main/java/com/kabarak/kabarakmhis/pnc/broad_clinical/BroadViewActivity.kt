package com.kabarak.kabarakmhis.pnc.broad_clinical

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.BroadClinical
import com.kabarak.kabarakmhis.pnc.other_problems.OtherProblemsDetailsActivity
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd
import kotlinx.android.synthetic.main.activity_child_birth_view.tvANCID
import kotlinx.android.synthetic.main.activity_child_birth_view.tvAge
import kotlinx.android.synthetic.main.activity_child_birth_view.tvName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.QuestionnaireResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BroadViewActivity : AppCompatActivity() {

    private lateinit var broadRecyclerView: RecyclerView
    private lateinit var broadAdapter: BroadAdapter
    private var broads: MutableList<BroadClinical> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broad_view_activity)

        // Initialize FHIR engine and formatter
        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)

        // Retrieve patient ID from shared preferences
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        // Initialize ViewModel
        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        btnAdd.setOnClickListener {
            val intent = Intent(this, BroadAdd::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        broadRecyclerView = findViewById(R.id.recycler_view_child)
        broadRecyclerView.layoutManager = LinearLayoutManager(this)

        broadAdapter = BroadAdapter(broads) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, OtherProblemsDetailsActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        broadRecyclerView.adapter = broadAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch child data from FHIR server
        fetchChildrenFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }


    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@BroadViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@BroadViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@BroadViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@BroadViewActivity)
                        progressDialog.setTitle("Please wait...")
                        progressDialog.setMessage("Fetching patient details...")
                        progressDialog.show()

                        var patientName: String = ""
                        var dob: String = ""
                        var identifier: String = ""

                        val job = Job()
                        CoroutineScope(Dispatchers.IO + job).launch {
                            val patientData = getPatientDataFromFhirEngine()
                            patientName = patientData.first
                            dob = patientData.second

                            formatter.saveSharedPreference(this@BroadViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@BroadViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@BroadViewActivity, "identifier", identifier)
                            }
                        }.join()

                        showPatientDetails(patientName, dob, identifier)

                        progressDialog.dismiss()
                    }
                } else {
                    // Display the data from local storage
                    showPatientDetails(patientLocalName, patientLocalDob, patientLocalIdentifier)
                }
            } catch (e: Exception) {
                Log.e("BroadViewActivity", "Error fetching patient data: ${e.message}")
            }
        }
    }

    private fun showPatientDetails(patientName: String, dob: String?, identifier: String?) {
        tvName.text = patientName
        if (!identifier.isNullOrEmpty()) tvANCID.text = identifier
        if (!dob.isNullOrEmpty()) tvAge.text = "${formatter.calculateAge(dob)} years"
    }

    private fun getPatientDataFromFhirEngine(): Pair<String, String> {
        // Use FHIR engine to fetch patient data, then return the name and date of birth
        val patientData = patientDetailsViewModel.getPatientData()
        val patientName = patientData.name
        val dob = patientData.dob

        return Pair(patientName, dob)
    }

    private fun fetchChildrenFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("BroadViewActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    broads.clear()

                                    // Extract child data from the bundle
                                    extractChildrenFromBundle(bundle)

                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("BroadViewActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@BroadViewActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@BroadViewActivity, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@BroadViewActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("BroadViewActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@BroadViewActivity, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractChildrenFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {extractChildrenFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            broadAdapter.notifyDataSetChanged() // Call on the adapter instance
        }
    }


//    private fun extractChildrenFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
//        val responseId = questionnaireResponse.id
//
//        // Check if a child with this ID already exists to avoid duplicates
//        if (broads.any { it.id == responseId }) {
//            Log.d("BroadViewActivity", "Child with ID $responseId already exists. Skipping duplicate.")
//            return
//        }
//
//        var childAge : String? = null
//        var childWeight : String? = null
//        var childLength : String? = null
//
//
//        for (item in questionnaireResponse.item) {
//            if (item.linkId == "6bd4142d-16a0-4451-8e7c-53ea7391d3f7") {
//                childAge = item.answer.firstOrNull()?.valueCoding?.display.toString()
//            }else if(item.linkId == "58fac309-fed2-4652-86cc-3f81f56b35fa"){
//                childWeight = item.answer.firstOrNull()?.valueCoding?.display.toString()
//            }else if(item.linkId == "5efb7f2d-f48a-45a4-846e-18126cee96dd"){
//                childLength =  item.answer.firstOrNull()?.valueCoding?.display.toString()
//            }
//        }
//
//
//        // Add the child if both name and birth date are available
//        if (!childAge.isNullOrEmpty() && !childWeight.isNullOrEmpty()  && !childLength.isNullOrEmpty()) {
//            val broad = BroadClinical(id = responseId, age = childAge, length = childLength, weight =  childWeight)
//            broads.add(broad)
//            Log.d("BroadViewActivity", "Added Problem: $childAge, length: $childWeight, weight: $childLength, Response ID: $responseId")
//        }
//    }

    private fun extractChildrenFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a child with this ID already exists to avoid duplicates
        if (broads.any { it.id == responseId }) {
            Log.d("BroadViewActivity", "Child with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var childAge: String? = null
        var childWeight: String? = null
        var childLength: String? = null

//        for (item in questionnaireResponse.item) {
//            when (item.linkId) {
//                "6bd4142d-16a0-4451-8e7c-53ea7391d3f7" -> {
//                    childAge = item.answer.firstOrNull()?.valueCoding?.display.toString()
//                }
//                "58fac309-fed2-4652-86cc-3f81f56b35fa" -> {
//                    childWeight = item.answer.firstOrNull()?.valueCoding?.display.toString()
//                }
//                "5efb7f2d-f48a-45a4-846e-18126cee96dd" -> {
//                    childLength = item.answer.firstOrNull()?.valueCoding?.display.toString()
//                }
//            }
//        }

        for (item in questionnaireResponse.item) {
            if (item.linkId == "9c0f682e-e110-4ea9-8d9f-3265db02b990") {
                for (subItem in item.item) {
                    when (subItem.linkId) {
                        "6bd4142d-16a0-4451-8e7c-53ea7391d3f7"-> {
                           childAge = subItem.answer.firstOrNull()?.valueStringType?.value.toString()
                        }
                        "58fac309-fed2-4652-86cc-3f81f56b35fa" -> {
                            childWeight = subItem.answer.firstOrNull()?.valueStringType?.value.toString()
                        }
                        "5efb7f2d-f48a-45a4-846e-18126cee96dd" -> {
                            childLength = subItem.answer.firstOrNull()?.valueStringType?.value.toString()
                        }
                    }
                }
            }
        }

        // Add the child if age, weight, length, and Z score are available
        if (!childAge.isNullOrEmpty() && !childWeight.isNullOrEmpty() && !childLength.isNullOrEmpty()) {
            val broad = BroadClinical(id = responseId, age = childAge, length = childLength, weight = childWeight)
            broads.add(broad)
            Log.d("BroadViewActivity", "Added Problem: Age: $childAge, Weight: $childWeight, Length: $childLength, Response ID: $responseId")
        }
    }


    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (broads.isEmpty()) {
            broadRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            broadRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}


