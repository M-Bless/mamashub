package com.intellisoft.kabarakmhis.new_designs.chw.referral

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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_referral_view.*
import kotlinx.android.synthetic.main.activity_referral_view.btnAdd
import kotlinx.android.synthetic.main.activity_referral_view.no_record
import kotlinx.android.synthetic.main.activity_referral_view.recycler_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class ReferralView : AppCompatActivity() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatter = FormatterClass()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referral_view)

        title = "Referral Back to community"

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

        btnAdd.setOnClickListener {

            val intent = Intent(this, ReferralAdd::class.java)
            startActivity(intent)

        }
    }

    override fun onStart() {
        super.onStart()

        getData()
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

    private fun getData() {

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(this@ReferralView,
                DbResourceViews.COMMUNITY_REFERRAL.name)

            if (encounterId != null) {

                val text1 = DbObservationFhirData(
                    DbSummaryTitle.A_REFERRAL_OFFICER_DETAILS.name,
                    listOf("106292003", "408402003-1", "303119007", "408402003-2", "6827000"))
                val text2 = DbObservationFhirData(
                    DbSummaryTitle.B_REFERRAL_DETAILS.name,
                    listOf("420942008", "224930009", "700856009"))


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

                    val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@ReferralView)
                    recyclerView.adapter = confirmParentAdapter
                }

            }
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