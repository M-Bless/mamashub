package com.kabarak.kabarakmhis.pnc.child_civil_registration

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
import com.kabarak.kabarakmhis.pnc.data_class.CivilRegistration
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
import kotlinx.android.synthetic.main.activity_child_birth_view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse

class ChildCivilRegistrationView: AppCompatActivity() {

    private lateinit var childRecyclerView: RecyclerView
    private lateinit var childCivilRegistrationAdapter: ChildCivilRegistrationAdapter
    private var children: MutableList<CivilRegistration> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View  // View for the no_record layout

    // For fetching patient data
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_civil_registration_view)

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
            val intent = Intent(this, ChildCivilRegistration::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        childRecyclerView = findViewById(R.id.recycler_view_child)
        childRecyclerView.layoutManager = LinearLayoutManager(this)

        childCivilRegistrationAdapter = ChildCivilRegistrationAdapter(children) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CivilRegistrationDetails::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        childRecyclerView.adapter = childCivilRegistrationAdapter

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
                val patientLocalName = formatter.retrieveSharedPreference(this@ChildCivilRegistrationView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@ChildCivilRegistrationView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@ChildCivilRegistrationView, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@ChildCivilRegistrationView)
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

                            formatter.saveSharedPreference(this@ChildCivilRegistrationView, "patientName", patientName)
                            formatter.saveSharedPreference(this@ChildCivilRegistrationView, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@ChildCivilRegistrationView, "identifier", identifier)
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

    private fun fetchChildrenFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            Log.d("ChildCivilRegistrationView", "Raw Response body: $rawResponse")

                            if (rawResponse.isNotEmpty()) {
                                try {
                                    val fhirContext = FhirContext.forR4()
                                    val parser = fhirContext.newJsonParser()
                                    val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                                    // Clear list before adding new items
                                    children.clear()

                                    // Extract child data from the bundle
                                    extractChildrenFromBundle(bundle)

                                    // Show/hide views based on the presence of children
                                    runOnUiThread { toggleViews() }
                                } catch (e: Exception) {
                                    Log.e("ChildCivilRegistrationView", "Error parsing response", e)
                                    runOnUiThread {
                                        Toast.makeText(this@ChildCivilRegistrationView, "Failed to parse response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@ChildCivilRegistrationView, "Received an empty response", Toast.LENGTH_SHORT).show()
                                    toggleViews()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ChildCivilRegistrationView, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Log.e("ChildCivilRegistrationView", "Error occurred while fetching data", t)
                        Toast.makeText(this@ChildCivilRegistrationView, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
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
                extractChildrenFromQuestionnaire(resource)
            }
        }

        // Notify the adapter to update the UI with the new children data
        runOnUiThread {
            childCivilRegistrationAdapter.notifyDataSetChanged()
        }
    }


    private fun extractChildrenFromQuestionnaire(questionnaireResponse: QuestionnaireResponse) {
        val responseId = questionnaireResponse.id

        // Check if a child with this ID already exists to avoid duplicates
        if (children.any { it.id == responseId }) {
            Log.d("ChildCivilRegistrationView", "Child with ID $responseId already exists. Skipping duplicate.")
            return
        }

        var childName: String? = null
        var sexOfChild: String? = null
        var childBirthDate: String? = null

        // Recursive function to traverse nested items
        fun traverseItems(items: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>) {
            for (item in items) {
                when (item.linkId) {
                    "a6bb3331-f838-4704-8d25-1b16f82e10b2" -> {
                        childName = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueStringType() -> answer.valueStringType.value
                                else -> null
                            }
                        }
                    }
                    "0c2657aa-b3cf-4d56-8297-71e00c437776" -> {
                        sexOfChild = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueCoding() -> answer.valueCoding.display
                                else -> null
                            }
                        }
                    }
                    "a40daa32-c972-4330-8e89-125056db33ba" -> {
                        childBirthDate = item.answer.firstOrNull()?.let { answer ->
                            when {
                                answer.hasValueDateType() -> answer.valueDateType.value.toString()
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

        // Use local variables to store the values before adding the child to the list
        val name = childName
        val birthDate = childBirthDate
        val sex = sexOfChild

        // Add the child if both name and birth date are available
        if (!name.isNullOrEmpty() && !birthDate.isNullOrEmpty() && !sex.isNullOrEmpty()) {
            val child = CivilRegistration(id = responseId, name = name, birthDate = birthDate, sexOfChild = sex)
            children.add(child)
            Log.d("ChildCivilRegistrationView", "Added child: $name, Birth Date: $birthDate, gender: $sex Response ID: $responseId")
        }
    }

    private fun extractResponseId(rawResponseId: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(rawResponseId)
        return matchResult?.groupValues?.get(1) ?: rawResponseId
    }

    // Function to toggle visibility of the RecyclerView and noRecordView
    private fun toggleViews() {
        if (children.isEmpty()) {
            childRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            childRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
