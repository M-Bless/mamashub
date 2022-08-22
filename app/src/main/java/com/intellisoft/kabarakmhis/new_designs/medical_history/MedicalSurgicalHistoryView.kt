package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.ObservationAdapter
import com.intellisoft.kabarakmhis.new_designs.adapter.ViewDetailsAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbConfirmDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.no_record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Stream
import kotlin.collections.ArrayList

class MedicalSurgicalHistoryView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_surgical_history_view)
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        title = "Medical & Surgical History Details"

        btnAddHistory.setOnClickListener {
            val intent = Intent(this, MedicalHistory::class.java)
            startActivity(intent)
        }

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)


    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(
                this@MedicalSurgicalHistoryView,
                DbResourceViews.MEDICAL_HISTORY.name
            )
            if (encounterId != null) {

                val surgical = DbObservationFhirData(DbSummaryTitle.A_SURGICAL_HISTORY.name,
                    listOf("161615003","12658000","12658000"))
                val medical = DbObservationFhirData(DbSummaryTitle.B_MEDICAL_HISTORY.name,
                    listOf("405751000","38341003","7867677","7867677-S","116859006","82545002","371569005"))
                val drug = DbObservationFhirData(DbSummaryTitle.C_DRUG_ALLERGIES.name,
                    listOf("416098002","416098002-S","609328004","609328004-S"))
                val family = DbObservationFhirData(DbSummaryTitle.D_FAMILY_HISTORY.name,
                    listOf("169828005","169828005-S","161414005","161414005-N", "161414005-R","161414005-H", "171126009"))


                val surgicalList = formatter.getObservationList(patientDetailsViewModel, surgical, encounterId)
                val medicalList = formatter.getObservationList(patientDetailsViewModel,medical, encounterId)
                val drugList = formatter.getObservationList(patientDetailsViewModel,drug, encounterId)
                val familyList = formatter.getObservationList(patientDetailsViewModel,family, encounterId)

                val observationDataList = merge(surgicalList, medicalList, drugList, familyList)

                CoroutineScope(Dispatchers.Main).launch {

                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@MedicalSurgicalHistoryView)
                    recyclerView.adapter = confirmParentAdapter

                }


            }

        }

        getUserDetails()
    }

    private fun getUserDetails() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        if (identifier != null && patientName != null) {
            tvPatient.text = patientName
            tvAncId.text = identifier
        }

    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, four: List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, four).forEach { item: List<T>? -> list.addAll(item!!) }
        return list
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}