package com.intellisoft.kabarakmhis.new_designs.previous_pregnancy

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
import com.intellisoft.kabarakmhis.new_designs.adapter.ViewDetailsAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_previous_pregnancy_view.*
import kotlinx.android.synthetic.main.activity_previous_pregnancy_view.btnAdd
import kotlinx.android.synthetic.main.activity_previous_pregnancy_view.no_record
import kotlinx.android.synthetic.main.activity_previous_pregnancy_view.recycler_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class PreviousPregnancyView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previous_pregnancy_view)

        title = "Present Pregnancy View"

        btnAdd.setOnClickListener {
            val intent = Intent(this, PreviousPregnancy::class.java)
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

            val encounterId = formatter.retrieveSharedPreference(this@PreviousPregnancyView,
                DbResourceViews.PREVIOUS_PREGNANCY.name)

            if (encounterId != null) {

                val pregnancyDetails = DbObservationFhirData(DbSummaryTitle.A_PREGNANCY_DETAILS.name,
                    listOf("818602026","258707000","424525001","257557008","77386006","289248003","386216000"))
                val babyDetails = DbObservationFhirData(DbSummaryTitle.B_BABY_DETAILS.name,
                    listOf("47340003","268476009","364587008","289910000","289910000-A"))

                val pregnancyDetailsList = formatter.getObservationList(patientDetailsViewModel, pregnancyDetails, encounterId)
                val babyDetailsList = formatter.getObservationList(patientDetailsViewModel,babyDetails, encounterId)

                val observationDataList = merge(pregnancyDetailsList, babyDetailsList)

                CoroutineScope(Dispatchers.Main).launch {
                    if (observationDataList.isNotEmpty()) {
                        no_record.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                    } else {
                        no_record.visibility = View.VISIBLE
                        recycler_view.visibility = View.GONE
                    }

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@PreviousPregnancyView)
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

    private fun <T> merge(first: List<T>, second: List<T>): List<T> {
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