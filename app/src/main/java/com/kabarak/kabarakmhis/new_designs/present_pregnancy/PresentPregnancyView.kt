package com.kabarak.kabarakmhis.new_designs.present_pregnancy

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
import kotlinx.android.synthetic.main.activity_present_pregnancy_view.*
import kotlinx.android.synthetic.main.activity_present_pregnancy_view.no_record
import kotlinx.android.synthetic.main.activity_present_pregnancy_view.recycler_view
import kotlinx.android.synthetic.main.activity_present_pregnancy_view.tvAncId
import kotlinx.android.synthetic.main.activity_present_pregnancy_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class PresentPregnancyView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    
    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()


    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present_pregnancy_view)

        title = "Present Pregnancy View"

        btnAdd.setOnClickListener {
            val intent = Intent(this, PresentPregnancyAdd::class.java)
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

            val encounterId = formatter.retrieveSharedPreference(this@PresentPregnancyView,
                DbResourceViews.PRESENT_PREGNANCY.name)

            if (encounterId != null) {

                formatter.saveSharedPreference(this@PresentPregnancyView, "saveEncounterId", encounterId)


                val text1 = DbObservationFhirData(
                    DbSummaryTitle.A_CURRENT_PREGNANCY.name,
                    listOf("424525001","390840006","27171005","45295008","284473002"))
                val text2 = DbObservationFhirData(
                    DbSummaryTitle.B_PRESENT_BLOOD_PRESSURE.name,
                    listOf("271649006","271650006"))
                val text3 = DbObservationFhirData(
                    DbSummaryTitle.C_HB_TEST.name,
                    listOf("302763003","302763003-S","274643008","77386006","249016007"))

                val text5 = DbObservationFhirData(
                    DbSummaryTitle.D_PRESENTATION.name,
                    listOf("246105001","249062004","289438002","169731002", "390840006"))

                val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
                val text2List = formatter.getObservationList(patientDetailsViewModel,text2, encounterId)
                val text3List = formatter.getObservationList(patientDetailsViewModel,text3, encounterId)
                val text5List = formatter.getObservationList(patientDetailsViewModel,text5, encounterId)

                val observationDataList = merge(text1List, text2List, text3List, text5List)

                CoroutineScope(Dispatchers.Main).launch {
                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@PresentPregnancyView)
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