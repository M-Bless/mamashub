package com.kabarak.kabarakmhis.new_designs.physical_examination

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.android.synthetic.main.activity_physical_examination_view.*
import kotlinx.android.synthetic.main.activity_physical_examination_view.no_record
import kotlinx.android.synthetic.main.activity_physical_examination_view.recycler_view
import kotlinx.android.synthetic.main.activity_physical_examination_view.tvAncId
import kotlinx.android.synthetic.main.activity_physical_examination_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream


class PhysicalExaminationView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()


    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_examination_view)

        title = "Physical Examination Details"

        btnAdd.setOnClickListener {
            val intent = Intent(this, PhysicalExamination::class.java)
            startActivity(intent)
        }
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)
        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
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

            val encounterId = formatter.retrieveSharedPreference(this@PhysicalExaminationView,
                DbResourceViews.PHYSICAL_EXAMINATION.name)

            if (encounterId != null) {

                formatter.saveSharedPreference(this@PhysicalExaminationView, "saveEncounterId", encounterId)


                val physicalExamination = DbObservationFhirData(DbSummaryTitle.A_PHYSICAL_EXAMINATION.name,
                    listOf("25656009","25656009-A"))
                val bloodPressure = DbObservationFhirData(DbSummaryTitle.B_PHYSICAL_BLOOD_PRESSURE.name,
                    listOf("271649006","271650006","78564009","703421000","267037003","267037003-A",
                        "53617003", "53617003-A","185712006","185712006-A","185712006-N"))
                val weightMonitor = DbObservationFhirData(DbSummaryTitle.C_WEIGHT_MONITORING.name,
                    listOf("77386006","726527001"))
                val abdominalExamination = DbObservationFhirData(DbSummaryTitle.D_ABDOMINAL_EXAMINATION.name,
                    listOf("163133003","163133003-A","113011001","113011001-A","37931006","37931006-A"))
                val externalGenitalia = DbObservationFhirData(DbSummaryTitle.E_EXTERNAL_GENITALIA_EXAM.name,
                    listOf("77142006","77142006-I","731273008","731273008-P","271939006","271939006-D","427788009","427788009-G","95041000119101","95041000119101-C"))

                val physicalExaminationList = formatter.getObservationList(patientDetailsViewModel, physicalExamination, encounterId)
                val bloodPressureList = formatter.getObservationList(patientDetailsViewModel,bloodPressure, encounterId)
                val weightMonitorList = formatter.getObservationList(patientDetailsViewModel,weightMonitor, encounterId)
                val abdominalExaminationList = formatter.getObservationList(patientDetailsViewModel,abdominalExamination, encounterId)
                val externalGenitaliaList = formatter.getObservationList(patientDetailsViewModel,externalGenitalia, encounterId)

                val observationDataList = merge(physicalExaminationList,bloodPressureList,weightMonitorList,abdominalExaminationList,externalGenitaliaList)


                CoroutineScope(Dispatchers.Main).launch {

                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,
                        this@PhysicalExaminationView)
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

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, four: List<T>, five: List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, four, five).forEach { item: List<T>? -> list.addAll(item!!) }
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