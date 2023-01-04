package com.kabarak.kabarakmhis.new_designs.counselling

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
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_counselling_view.*
import kotlinx.android.synthetic.main.activity_counselling_view.no_record
import kotlinx.android.synthetic.main.activity_counselling_view.recycler_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class CounsellingView : AppCompatActivity() {
    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counselling_view)

        title = "Counselling"

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

        btnAddCounselling.setOnClickListener {
            startActivity(Intent(this, Counselling::class.java))
        }

        CoroutineScope(Dispatchers.IO).launch { getObservationDetails() }

    }

    override fun onStart() {
        super.onStart()

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

        val encounterId = formatter.retrieveSharedPreference(this@CounsellingView,
            DbResourceViews.COUNSELLING.name)
        if (encounterId != null) {
            formatter.saveSharedPreference(this@CounsellingView, "saveEncounterId", encounterId)

            val text1 = DbObservationFhirData(
                DbSummaryTitle.A_COUNSELLING_DONE.name,
                listOf("50206362","69475666","97129423"))
            val text2 = DbObservationFhirData(
                DbSummaryTitle.B_PREGNANCY_COUNSELLING.name,
                listOf("22723167","43183900","96204638","30822033","55268779","51049855","96888625","1528937"))
            val text3 = DbObservationFhirData(
                DbSummaryTitle.C_INFANT_COUNSELLING.name,
                listOf("91232116","80142304"))
            val text4 = DbObservationFhirData(
                DbSummaryTitle.D_PREGNANCY_COUNSELLING_DETAILS.name,
                listOf("16673572","5745006","55623893","46209251", "83359346","25865687","15859810", "77178232","35317232"))

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

                val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@CounsellingView)
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