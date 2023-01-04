package com.kabarak.kabarakmhis.new_designs.deworming

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
import kotlinx.android.synthetic.main.activity_deworming_view.*
import kotlinx.android.synthetic.main.activity_deworming_view.no_record
import kotlinx.android.synthetic.main.activity_deworming_view.recycler_view
import kotlinx.android.synthetic.main.activity_deworming_view.tvAncId
import kotlinx.android.synthetic.main.activity_deworming_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class DewormingView : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deworming_view)

        title = "Deworming"

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

        btnAddDeworming.setOnClickListener {
            startActivity(Intent(this, Deworming::class.java))

        }
    }
    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            getObservationDetails()

        }

        getUserDetails()

    }

    private fun getUserDetails() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        tvPatient.text = patientName
        tvAncId.text = identifier


    }

    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@DewormingView,
            DbResourceViews.DEWORMING.name)
        if (encounterId != null) {

            formatter.saveSharedPreference(this@DewormingView, "saveEncounterId", encounterId)

            val text1 = DbObservationFhirData(
                DbSummaryTitle.A_DEWORMING.name,
                listOf("14369007","410671006"))

            val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
            val observationDataList = merge(text1List)


            CoroutineScope(Dispatchers.Main).launch {
                if (observationDataList.isNotEmpty()) {
                    no_record.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                } else {
                    no_record.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }

                val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@DewormingView)
                recyclerView.adapter = confirmParentAdapter
            }



        }


    }

    private fun <T> merge(first: List<T> ): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first).forEach { item: List<T>? -> list.addAll(item!!) }
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