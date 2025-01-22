package com.kabarak.kabarakmhis.pnc.familyplanning

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
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.FamilyPlanning
import com.kabarak.kabarakmhis.pnc.familyplannig.FamilyPlannigAdd
import com.kabarak.kabarakmhis.pnc.familyplannig.FamilyPlannigEdit
import com.kabarak.kabarakmhis.pnc.familyplannig.FamilyPlanningAdapter
import kotlinx.android.synthetic.main.activity_family_planning_view.btnAdd
import kotlinx.android.synthetic.main.activity_family_planning_view.tvANCID
import kotlinx.android.synthetic.main.activity_family_planning_view.tvAge
import kotlinx.android.synthetic.main.activity_family_planning_view.tvName
import org.hl7.fhir.r4.model.QuestionnaireResponse

class FamilyPlanningView: AppCompatActivity() {

    private lateinit var childRecyclerView: RecyclerView
    private lateinit var familyPlanningAdapter: FamilyPlanningAdapter
    private var familyPlanningList: MutableList<FamilyPlanning> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_planning_view)

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
            val intent = Intent(this, FamilyPlannigAdd::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        childRecyclerView = findViewById(R.id.recycler_view_child)
        childRecyclerView.layoutManager = LinearLayoutManager(this)

        familyPlanningAdapter = FamilyPlanningAdapter(familyPlanningList) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, FamilyPlanningDetails::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        childRecyclerView.adapter = familyPlanningAdapter

        // Initialize noRecordView (the include layout for "no records found")
        noRecordView = findViewById(R.id.no_record)

        // Initialize RetrofitCallsFhir
        retrofitCallsFhir = RetrofitCallsFhir()

        // Fetch child data from FHIR server
        fetchFamilyPlanningFromFHIR()

        // Fetch patient data
        fetchPatientData()
    }

    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@FamilyPlanningView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@FamilyPlanningView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@FamilyPlanningView, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@FamilyPlanningView)
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

                            formatter.saveSharedPreference(this@FamilyPlanningView, "patientName", patientName)
                            formatter.saveSharedPreference(this@FamilyPlanningView, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@FamilyPlanningView, "identifier", identifier)
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
                Log.e("ChildCivilRegistrationView", "Error fetching patient data: ${e.message}")
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

    private fun fetchFamilyPlanningFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("FamilyPlanningViewActivity", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    familyPlanningList.clear()

                                    // Extract child data from the bundle
                                    extractChildrenFromBundle(bundle)

                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("FamilyPlanningViewActivity", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@FamilyPlanningView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@FamilyPlanningView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@FamilyPlanningView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("FamilyPlanningViewActivity", "Error occurred while fetching data", t)
                        Toast.makeText(this@FamilyPlanningView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractChildrenFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                // Extract child from each QuestionnaireResponse
                extractFamilyPlanningFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            familyPlanningAdapter.notifyDataSetChanged()
        }
    }



    private fun extractFamilyPlanningFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a record with this ID already exists to avoid duplicates
        if (familyPlanningList.any { it.id == responseId }) {
            Log.d("FamilyPlanningViewActivity", "Record with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var familyPlanningDate: String? = null
        var familyPlanningMethod: String? = null
        var familyPlanningWeight: String? = null
        var familyPlanningBloodPressure: String? = null
        var familyPlanningRemarks: String? = null

        // Recursive function to traverse nested items
        fun traverseItems(items: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>) {
            for (item in items) {
                when (item.linkId) {
                    "33701d8e-d1af-42c1-d48b-542048c79933" -> {
                        familyPlanningDate = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueDateType() -> answer.valueDateType.value.toString()
                                else -> null
                            }
                        }
                    }
                    "43bb2c66-a1bb-43d0-8e3b-51c7c3ba8fcf" -> {
                        familyPlanningMethod = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueStringType() -> answer.valueStringType.value
                                else -> null
                            }
                        }
                    }
                    "479197ea-d3f3-4411-9810-0b0c64a85a8e" -> {
                        familyPlanningWeight = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueQuantity() -> answer.valueQuantity.value.toString()
                                else -> null
                            }
                        }
                    }
                    "a9ec0bc3-bb22-4c21-865b-c716b05263b1" -> {
                        familyPlanningBloodPressure = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueStringType() -> answer.valueStringType.value
                                else -> null
                            }
                        }
                    }
                    "71ff27f2-a22d-4765-8079-e2e6f46490bf" -> {
                        familyPlanningRemarks = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueStringType() -> answer.valueStringType.value
                                else -> null
                            }
                        }
                    }
                }
                // Recursively traverse nested items
                if (item.item.isNotEmpty()) {
                    traverseItems(item.item)
                }
            }
        }

        // Start traversing from the top-level items
        traverseItems(questionnaireResponse.item)

        // Use local variables to store the values before adding the record to the list
        val date = familyPlanningDate
        val method = familyPlanningMethod
        val weight = familyPlanningWeight
        val bloodPressure = familyPlanningBloodPressure
        val remarks = familyPlanningRemarks

        // Add the record if both date and method are available
        if (!date.isNullOrEmpty() && !method.isNullOrEmpty()) {
            val familyPlanning = FamilyPlanning(
                id = responseId,
                date = date,
                method = method,
                weight = weight ?: "",
                bloodPressure = bloodPressure ?: "",
                remarks = remarks ?: ""
            )
            familyPlanningList.add(familyPlanning)
            Log.d("FamilyPlanningViewActivity", "Added record: $method, Date: $date, Response ID: $responseId")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (familyPlanningList.isEmpty()) {
            childRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            childRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}