package com.intellisoft.kabarakmhis.new_designs.pmtct

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_pmtctinterventions_view.*
import kotlinx.android.synthetic.main.activity_pmtctinterventions_view.no_record
import kotlinx.android.synthetic.main.activity_pmtctinterventions_view.tvAncId
import kotlinx.android.synthetic.main.activity_pmtctinterventions_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class PMTCTInterventionsView : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pmtctinterventions_view)

        title = "PMTCT Interventions"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        btnAddPMTCT.setOnClickListener {
            startActivity(Intent(this, PMTCTInterventions::class.java))
        }

        getObservationDetails()
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
    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@PMTCTInterventionsView,
            DbResourceViews.PMTCT.name)
        if (encounterId != null) {

            val text1 = DbObservationFhirData(
                DbSummaryTitle.A_INTERVENTION_GIVEN.name,
                listOf("82261064"))
            val text2 = DbObservationFhirData(
                DbSummaryTitle.B_ART_FOR_LIFE.name,
                listOf("54840574","16714043","54840574-O"))
            val text3 = DbObservationFhirData(
                DbSummaryTitle.C_PMTCT_DOSAGE.name,
                listOf("69335547","9697869","7676996"))
            val text4 = DbObservationFhirData(
                DbSummaryTitle.D_VL_SAMPLE.name,
                listOf("98046364","93778367"))

            val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
            val text2List = formatter.getObservationList(patientDetailsViewModel,text2, encounterId)
            val text3List = formatter.getObservationList(patientDetailsViewModel,text3, encounterId)
            val text4List = formatter.getObservationList(patientDetailsViewModel,text4, encounterId)

            val observationDataList = merge(text1List, text2List, text3List, text4List)


            CoroutineScope(Dispatchers.Main).launch {
                if (observationDataList.isNotEmpty()) {
                    no_record.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                } else {
                    no_record.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }

                val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@PMTCTInterventionsView)
                recycler_view.adapter = confirmParentAdapter
            }




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