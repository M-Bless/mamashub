package com.kabarak.kabarakmhis.new_designs.antenatal_profile

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
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.*
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.no_record
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.recycler_view
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.tvAncId
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.tvPatient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Stream

class AntenatalProfileView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()
    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenatal_profile_view)

        title = "Antenatal Profile Details"

        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

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

        btnAddAntenatal.setOnClickListener {

            startActivity(Intent(this, AntenatalProfile::class.java))

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

        val encounterId = formatter.retrieveSharedPreference(this@AntenatalProfileView,
            DbResourceViews.ANTENATAL_PROFILE.name)
        if (encounterId != null) {

            formatter.saveSharedPreference(this, "saveEncounterId", encounterId)

            val bloodTest = DbObservationFhirData(DbSummaryTitle.A_BLOOD_TESTS.name,
                listOf("302763003","302763003-S","365636006","365636006-S","169676009","169676009-S","33747003","33747003-S"))
            val urineTest = DbObservationFhirData(DbSummaryTitle.B_URINE_TESTS.name,
                listOf("27171005","45295008","45295008-A","390840006"))
            val tbScreen = DbObservationFhirData(DbSummaryTitle.C_TB_SCREEN.name,
                listOf("171126009","371569005","148264888-P","148264888-N","521195552","384813511","423337059"))
            val obstetricSound = DbObservationFhirData(DbSummaryTitle.D_OBSTETRIC_ULTRASOUND.name,
                listOf("268445003-1","410672004-1","268445003-2","410672004-2"))
            val multipleBaby = DbObservationFhirData(DbSummaryTitle.D_MULTIPLE_BABIES.name,
                listOf("45384004","45384004-N"))
            val hivStatus = DbObservationFhirData(DbSummaryTitle.E_HIV_STATUS.name,
                listOf("19030005-ANC","860046068","278977008-P"))
            val maternalHaart = DbObservationFhirData(DbSummaryTitle.F_MATERNAL_HAART.name,
                listOf("120841000", "416234007","5111197"))
            val hivTesting = DbObservationFhirData(DbSummaryTitle.G_HIV_TESTING.name,
                listOf("31676001","31676001-Y","31676001-NO","278977008","31676001-NR"))
            val syphilisTest = DbObservationFhirData(DbSummaryTitle.H_SYPHILIS_TESTING.name,
                listOf("76272004","76272004-Y","76272004-N","10759921000119107"))
            val hepatitisTest = DbObservationFhirData(DbSummaryTitle.I_HEPATITIS_TESTING.name,
                listOf("128241005", "128241005-R", "128241005-N", "10759151000119101"))
            val coupleCounselling = DbObservationFhirData(DbSummaryTitle.J_COUPLE_COUNSELLING_TESTING.name,
                listOf("31676001","31676001-S", "31676001-R", "31676001-RRD"))


            val bloodTestList = formatter.getObservationList(patientDetailsViewModel, bloodTest, encounterId)
            val urineTestList = formatter.getObservationList(patientDetailsViewModel,urineTest, encounterId)
            val tbScreenList = formatter.getObservationList(patientDetailsViewModel,tbScreen, encounterId)
            val obstetricSoundList = formatter.getObservationList(patientDetailsViewModel,obstetricSound, encounterId)
            val multipleBabyList = formatter.getObservationList(patientDetailsViewModel,multipleBaby, encounterId)
            val hivStatusList = formatter.getObservationList(patientDetailsViewModel,hivStatus, encounterId)
            val maternalHaartList = formatter.getObservationList(patientDetailsViewModel,maternalHaart, encounterId)
            val hivTestingList = formatter.getObservationList(patientDetailsViewModel,hivTesting, encounterId)
            val syphilisTestList = formatter.getObservationList(patientDetailsViewModel,syphilisTest, encounterId)
            val hepatitisTestList = formatter.getObservationList(patientDetailsViewModel,hepatitisTest, encounterId)
            val coupleCounsellingList = formatter.getObservationList(patientDetailsViewModel,coupleCounselling, encounterId)

            val observationDataList = merge(bloodTestList, urineTestList, tbScreenList, obstetricSoundList,
                multipleBabyList, hivStatusList, maternalHaartList, hivTestingList, syphilisTestList, hepatitisTestList, coupleCounsellingList)

            CoroutineScope(Dispatchers.Main).launch {
                if (observationDataList.isNotEmpty()) {
                    no_record.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                } else {
                    no_record.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }

                val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@AntenatalProfileView)
                recyclerView.adapter = confirmParentAdapter
            }



        }


    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, four: List<T>,
                          five: List<T>, six: List<T>, seven: List<T>, eight: List<T>,
                          nine: List<T>, ten: List<T>, eleven: List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, four, five, six, seven, eight, nine, ten, eleven).forEach { item: List<T>? -> list.addAll(item!!) }
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