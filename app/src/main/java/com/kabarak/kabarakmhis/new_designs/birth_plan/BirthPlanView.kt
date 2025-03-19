package com.kabarak.kabarakmhis.new_designs.birth_plan

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
import kotlinx.android.synthetic.main.activity_birth_plan_view.*
import kotlinx.android.synthetic.main.activity_birth_plan_view.no_record
import kotlinx.android.synthetic.main.activity_birth_plan_view.recycler_view
import kotlinx.android.synthetic.main.activity_birth_plan_view.tvAncId
import kotlinx.android.synthetic.main.activity_birth_plan_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class BirthPlanView : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birth_plan_view)

        title = "Birth Plan Details"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)


        btnAddBirthPlan.setOnClickListener {

            startActivity(Intent(this, BirthPlan::class.java))

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

        if (identifier != null && patientName != null) {
            tvPatient.text = patientName
            tvAncId.text = identifier
        }

    }
    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@BirthPlanView,
            DbResourceViews.BIRTH_PLAN.name)
        if (encounterId != null) {

            formatter.saveSharedPreference(this, "saveEncounterId", encounterId)

            val text1 = DbObservationFhirData(
                DbSummaryTitle.A_BIRTH_PLAN.name,
                listOf("161714006","257622000","257622000-N"))
            val text2 = DbObservationFhirData(
                DbSummaryTitle.B_BIRTH_ATTENDANT.name,
                listOf("308210000","308210000-N","308210000-D"))
            val text3 = DbObservationFhirData(
                DbSummaryTitle.C_ALTERNATIVE_BIRTH_ATTENDANT.name,
                listOf("308210000-A","308210000-AN","308210000-AD"))
            val text4 = DbObservationFhirData(
                DbSummaryTitle.D_BIRTH_COMPANION.name,
                listOf("62071000","359993007","263498003","360300001"))
            val text5 = DbObservationFhirData(
                DbSummaryTitle.E_ALTERNATIVE_BIRTH_COMPANION.name,
                listOf("62071000-AC","359993007-ACN","263498003-ACR","360300001-ACT"))
            val text6 = DbObservationFhirData(
                DbSummaryTitle.F_BLOOD_DONOR.name,
                listOf("308210000","359993007","365636006"))
            val text7 = DbObservationFhirData(
                DbSummaryTitle.E_FINANCIAL_PLAN.name,
                listOf("224164009"))


            val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
            val text2List = formatter.getObservationList(patientDetailsViewModel,text2, encounterId)
            val text3List = formatter.getObservationList(patientDetailsViewModel,text3, encounterId)
            val text4List = formatter.getObservationList(patientDetailsViewModel,text4, encounterId)
            val text5List = formatter.getObservationList(patientDetailsViewModel,text5, encounterId)
            val text6List = formatter.getObservationList(patientDetailsViewModel,text6, encounterId)
            val text7List = formatter.getObservationList(patientDetailsViewModel,text7, encounterId)


            val observationDataList = merge(text1List, text2List, text3List, text4List, text5List, text6List, text7List)

            CoroutineScope(Dispatchers.Main).launch {
                if (observationDataList.isNotEmpty()) {
                    no_record.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                } else {
                    no_record.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }

                val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@BirthPlanView)
                recyclerView.adapter = confirmParentAdapter
            }



        }


    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, four: List<T>,
                          five: List<T>, six: List<T>, seven: List<T>, ): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, four, five, six, seven).forEach { item: List<T>? -> list.addAll(item!!) }
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