package com.kabarak.kabarakmhis.new_designs.malaria_propylaxis

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
import kotlinx.android.synthetic.main.activity_birth_plan_view.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_view.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_view.no_record
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_view.recycler_view
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_view.tvAncId
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_view.tvPatient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class MalariaProphylaxisView : AppCompatActivity() {

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
        setContentView(R.layout.activity_malaria_prophylaxis_view)

        title = "Malaria Prophylaxis Details"

        btnAdd.setOnClickListener {
            val intent = Intent(this, MalariaProphylaxis::class.java)
            startActivity(intent)
        }

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

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

            val encounterId = formatter.retrieveSharedPreference(this@MalariaProphylaxisView,
                DbResourceViews.MALARIA_PROPHYLAXIS.name)

            if (encounterId != null) {

                formatter.saveSharedPreference(this@MalariaProphylaxisView, "saveEncounterId", encounterId)

                val text1 = DbObservationFhirData(
                    DbSummaryTitle.A_ANC_VISIT.name,
                    listOf("46645665","39234792","520474952","388435640-Y","388435640-N","390840006"))
                val text2 = DbObservationFhirData(
                    DbSummaryTitle.B_LLITN_GIVEN.name,
                    listOf("412894909","784030374-Y","784030374-N"))


                val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
                val text2List = formatter.getObservationList(patientDetailsViewModel,text2, encounterId)

                val observationDataList = merge(text1List, text2List)

                CoroutineScope(Dispatchers.Main).launch {
                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@MalariaProphylaxisView)
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
    private fun <T> merge(first: List<T>, second: List<T> ): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second).forEach { item: List<T>? -> list.addAll(item!!) }
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