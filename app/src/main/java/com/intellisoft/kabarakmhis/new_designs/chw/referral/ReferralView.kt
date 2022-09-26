package com.intellisoft.kabarakmhis.new_designs.chw.referral

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbIdentifier
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.android.synthetic.main.activity_referral_view.*
import kotlinx.android.synthetic.main.activity_referral_view.btnAdd
import kotlinx.android.synthetic.main.activity_referral_view.no_record
import kotlinx.android.synthetic.main.activity_referral_view.recycler_view
import kotlinx.android.synthetic.main.activity_referral_view.tvANCID
import kotlinx.android.synthetic.main.activity_referral_view.tvAge
import kotlinx.android.synthetic.main.activity_referral_view.tvKinDetails
import kotlinx.android.synthetic.main.activity_referral_view.tvKinName
import kotlinx.android.synthetic.main.activity_referral_view.tvName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
        getPatientData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPatientData() {

        CoroutineScope(Dispatchers.IO).launch {

            try {
                getData()

                formatter.deleteSharedPreference(this@ReferralView, "savedEncounter")

                val patientLocalName = formatter.retrieveSharedPreference(this@ReferralView, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@ReferralView, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@ReferralView, "identifier")

                val patientLocalKinName = formatter.retrieveSharedPreference(this@ReferralView, "kinName")
                val patientLocalKinPhone = formatter.retrieveSharedPreference(this@ReferralView, "kinPhone")

                if (patientLocalName == null || patientLocalName == "") {

                    CoroutineScope(Dispatchers.Main).launch {

                        val progressDialog = ProgressDialog(this@ReferralView)
                        progressDialog.setTitle("Please wait...")
                        progressDialog.setMessage("Getting user data...")
                        progressDialog.show()

                        var patientName = ""
                        var dob = ""
                        var kinName = ""
                        var kinPhone = ""
                        var identifierList: ArrayList<DbIdentifier>
                        var identifier = ""

                        val job = Job()
                        CoroutineScope(Dispatchers.IO + job).launch {

                            val patientData = patientDetailsViewModel.getPatientData()

                            patientName = patientData.name
                            dob = patientData.dob

                            kinName = patientData.kinData.name
                            kinPhone = patientData.kinData.phone
                            identifierList = patientData.identifier


                            identifierList.forEach {
                                if (it.id == "ANC_NUMBER"){
                                    identifier = it.value
                                }
                            }


                            val edd = patientDetailsViewModel.getObservationsPerCode("161714006")
                            if (edd.isNotEmpty()){
                                edd[0].value.let {
                                    formatter.saveSharedPreference(this@ReferralView, "edd", it)
                                }
                            }

                            formatter.saveSharedPreference(this@ReferralView, "patientName", patientName)
                            formatter.saveSharedPreference(this@ReferralView, "dob", dob)
                            formatter.saveSharedPreference(this@ReferralView, "identifier", identifier.toString())
                            formatter.saveSharedPreference(this@ReferralView, "kinName", kinName)
                            formatter.saveSharedPreference(this@ReferralView, "kinPhone", kinPhone)


                        }.join()

                        showClientDetails(patientName, dob, identifier, kinName, kinPhone)

                        progressDialog.dismiss()

                    }.join()


                } else {

                    //Patient details has been retrieved from the local database
                    showClientDetails(patientLocalName, patientLocalDob, patientLocalIdentifier, patientLocalKinName, patientLocalKinPhone)

                }

                val observationList = patientDetailsViewModel.getObservationFromEncounter(
                    DbResourceViews.PATIENT_INFO.name)
                if (observationList.isNotEmpty()) {
                    val encounterId = observationList[0].id
                    formatter.saveSharedPreference(this@ReferralView, DbResourceViews.PATIENT_INFO.name, encounterId)
                }

//                viewModel.poll()

            }catch (e: Exception){
                Log.e("Error", e.message.toString())
            }



        }


    }

    private fun showClientDetails(
        patientLocalName: String,
        patientLocalDob: String?,
        patientLocalIdentifier: String?,
        kinName: String?,
        kinPhone: String?
    ) {

        tvName.text = patientLocalName

        if (patientLocalIdentifier != null) tvANCID.text = patientLocalIdentifier
        if (kinName != null) tvKinName.text = kinName
        if (kinPhone != null) tvKinDetails.text = kinPhone

        if (patientLocalDob != null) {
            val age = "${formatter.calculateAge(patientLocalDob)} years"
            tvAge.text = age
        }

        val edd = formatter.retrieveSharedPreference(this, "edd")



    }

    override fun onResume() {
        super.onResume()
        getPatientData()
    }

    override fun onRestart() {
        super.onRestart()
        getPatientData()
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

                startActivity(Intent(this, ReferralView::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}