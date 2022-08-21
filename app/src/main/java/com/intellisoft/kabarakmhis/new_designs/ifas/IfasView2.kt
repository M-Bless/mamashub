package com.intellisoft.kabarakmhis.new_designs.ifas

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExamination
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_ifas_view2.*
import kotlinx.android.synthetic.main.activity_ifas_view2.no_record
import kotlinx.android.synthetic.main.activity_ifas_view2.recycler_view


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class IfasView2 : AppCompatActivity() {

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
        setContentView(R.layout.activity_ifas_view2)

        title = "IFAS"

//        btnPhysicalExamination.setOnClickListener {
//            val intent = Intent(this, PhysicalExamination::class.java)
//            startActivity(intent)
//        }
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

            val encounterId = formatter.retrieveSharedPreference(this@IfasView2,
                DbResourceViews.IFAS.name)

            if (encounterId != null) {

                val text1 = DbObservationFhirData(
                    DbSummaryTitle.A_SUPPLIMENTS_ISSUING_TO_CLIENT.name,
                    listOf("74935093","6709950","410666004","26462991"))
                val text2 = DbObservationFhirData(
                    DbSummaryTitle.B_ANC_CONTACT.name,
                    listOf("46645665", "39667636","76449731"))
                val text3 = DbObservationFhirData(
                    DbSummaryTitle.C_DOSAGE.name,
                    listOf("14420047","20726931","39234792","70346388"))

                val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
                val text2List = formatter.getObservationList(patientDetailsViewModel,text2, encounterId)
                val text3List = formatter.getObservationList(patientDetailsViewModel,text3, encounterId)

                val observationDataList = merge(text1List, text2List, text3List)


                CoroutineScope(Dispatchers.Main).launch {
                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@IfasView2)
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
    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third).forEach { item: List<T>? -> list.addAll(item!!) }
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