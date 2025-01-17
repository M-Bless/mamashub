package com.kabarak.kabarakmhis.pnc.poliovacination

import org.hl7.fhir.r4.model.QuestionnaireResponse


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
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.pnc.data_class.PolioVaccination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd
import kotlinx.android.synthetic.main.activity_child_birth_view.tvANCID
import kotlinx.android.synthetic.main.activity_child_birth_view.tvAge
import kotlinx.android.synthetic.main.activity_child_birth_view.tvName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class PolioVaccinationViewActivity : AppCompatActivity() {

    private lateinit var vaccinationRecyclerView: RecyclerView
    private lateinit var vaccinationAdapter: PolioVaccinationAdapter
    private var vaccinations: MutableList<PolioVaccination> = mutableListOf()
    private lateinit var retrofitCallsFhir: RetrofitCallsFhir
    private lateinit var noRecordView: View

    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatter: FormatterClass
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polio_vaccination_view)

        formatter = FormatterClass()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application, fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        btnAdd.setOnClickListener {
            val intent = Intent(this, PolioAddActivity::class.java)
            startActivity(intent)
        }

        noRecordView = findViewById(R.id.no_record)

        vaccinationRecyclerView = findViewById(R.id.recycler_view_vaccination)
        vaccinationRecyclerView.layoutManager = LinearLayoutManager(this)

        vaccinationAdapter = PolioVaccinationAdapter(vaccinations) { rawResponseId ->
            val responseId = extractResponseId(rawResponseId)
            Toast.makeText(this, "Response ID: $responseId", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, PolioVaccinationDetailsActivity::class.java)
            intent.putExtra("responseId", responseId)
            startActivity(intent)
        }
        vaccinationRecyclerView.adapter = vaccinationAdapter

        retrofitCallsFhir = RetrofitCallsFhir()

        fetchVaccinationsFromFHIR()
        fetchPatientData()

    }
    private fun fetchPatientData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientLocalName = formatter.retrieveSharedPreference(this@PolioVaccinationViewActivity, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@PolioVaccinationViewActivity, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@PolioVaccinationViewActivity, "identifier")

                if (patientLocalName.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val progressDialog = ProgressDialog(this@PolioVaccinationViewActivity)
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

                            formatter.saveSharedPreference(this@PolioVaccinationViewActivity, "patientName", patientName)
                            formatter.saveSharedPreference(this@PolioVaccinationViewActivity, "dob", dob)

                            if (identifier.isNotEmpty()) {
                                formatter.saveSharedPreference(this@PolioVaccinationViewActivity, "identifier", identifier)
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
                Log.e("ChildViewActivity", "Error fetching patient data: ${e.message}")
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

    private fun fetchVaccinationsFromFHIR() {
        lifecycleScope.launch(Dispatchers.IO) {
            retrofitCallsFhir.fetchAllQuestionnaireResponses(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val rawResponse = responseBody.string()
                            val fhirContext = FhirContext.forR4()
                            val parser = fhirContext.newJsonParser()
                            val bundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle::class.java, rawResponse)

                            vaccinations.clear()
                            extractVaccinationsFromBundle(bundle)
                            runOnUiThread { toggleViews() }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PolioVaccinationViewActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                            toggleViews()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    runOnUiThread {
                        Toast.makeText(this@PolioVaccinationViewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        toggleViews()
                    }
                }
            })
        }
    }

    private fun extractVaccinationsFromBundle(bundle: org.hl7.fhir.r4.model.Bundle) {
        for (entry in bundle.entry) {
            val fullUrl = entry.fullUrl
            val responseId = extractResponseId(fullUrl)

            val resource = entry.resource
            if (resource is QuestionnaireResponse) {
                parseVaccinationResponse(resource, responseId)
            }
        }

        runOnUiThread {
            vaccinationAdapter.notifyDataSetChanged()
        }
    }

    private fun parseVaccinationResponse(questionnaireResponse: QuestionnaireResponse, responseId: String) {
        var status: String? = null
        var dose: String? = null
        var dateGiven: String? = null
        var dateOfNextVisit: String? = null
        var batchNumber: Int? = null
        var lotNumber: Int? = null
        var manufacturer: String? = null
        var dateOfExpiry: String? = null

        questionnaireResponse.item.forEach { item ->
            if (item.text == "POLIO VACCINE: (Bivalent Oral Polio Vaccine(bOPV):") {
                // Get the status from the choice field
                item.answer.firstOrNull()?.valueCoding?.display?.let {
                    status = it
                }
                // Parse nested items
                item.answer.firstOrNull()?.item?.forEach { subItem ->
                    when (subItem.text) {
                        "Date Given" -> {
                            dateGiven = subItem.answer.firstOrNull()?.let { answer ->
                                when (val value = answer.value) {
                                    is org.hl7.fhir.r4.model.DateTimeType -> value.valueAsString
                                    is org.hl7.fhir.r4.model.DateType -> value.valueAsString
                                    else -> null
                                }
                            }
                        }
                        "Date of next visit" -> {
                            dateOfNextVisit = subItem.answer.firstOrNull()?.let { answer ->
                                when (val value = answer.value) {
                                    is org.hl7.fhir.r4.model.DateTimeType -> value.valueAsString
                                    is org.hl7.fhir.r4.model.DateType -> value.valueAsString
                                    else -> null
                                }
                            }
                        }
                        "Batch number" -> batchNumber = subItem.answer.firstOrNull()?.valueIntegerType?.value
                        "Lot number" -> lotNumber = subItem.answer.firstOrNull()?.valueIntegerType?.value
                        "Manufacturer" -> manufacturer = subItem.answer.firstOrNull()?.valueStringType?.value
                        "Date of expiry" -> {
                            dateOfExpiry = subItem.answer.firstOrNull()?.let { answer ->
                                when (val value = answer.value) {
                                    is org.hl7.fhir.r4.model.DateType -> value.valueAsString
                                    is org.hl7.fhir.r4.model.DateTimeType -> value.valueAsString
                                    else -> null
                                }
                            }
                        }
                    }
                }
            }
        }

        if (status != null) {
            vaccinations.add(
                PolioVaccination(
                    id = responseId,
                    status = status!!,
                    dose = dose,
                    dateGiven = dateGiven,
                    dateOfNextVisit = dateOfNextVisit,
                    batchNumber = batchNumber,
                    lotNumber = lotNumber,
                    manufacturer = manufacturer,
                    dateOfExpiry = dateOfExpiry
                )
            )
        }
    }



    private fun extractResponseId(fullUrl: String): String {
        val regex = Regex("QuestionnaireResponse/(\\d+)")
        val matchResult = regex.find(fullUrl)
        return matchResult?.groupValues?.get(1) ?: fullUrl
    }

    private fun toggleViews() {
        if (vaccinations.isEmpty()) {
            vaccinationRecyclerView.visibility = View.GONE
            noRecordView.visibility = View.VISIBLE
        } else {
            vaccinationRecyclerView.visibility = View.VISIBLE
            noRecordView.visibility = View.GONE
        }
    }
}
