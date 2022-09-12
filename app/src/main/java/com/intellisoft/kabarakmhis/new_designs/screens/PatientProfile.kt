package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.NewMainActivity
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.AntenatalProfileView
import com.intellisoft.kabarakmhis.new_designs.birth_plan.BirthPlanView
import com.intellisoft.kabarakmhis.new_designs.chw.referral.ReferralView
import com.intellisoft.kabarakmhis.new_designs.clinical_notes.ClinicalNotesList
import com.intellisoft.kabarakmhis.new_designs.counselling.CounsellingView
import com.intellisoft.kabarakmhis.new_designs.data_class.DbIdentifier
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.deworming.DewormingView
import com.intellisoft.kabarakmhis.new_designs.ifas.IfasList
import com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis.MalariaProphylaxisList
import com.intellisoft.kabarakmhis.new_designs.matenal_serology.MaternalSerologyView
import com.intellisoft.kabarakmhis.new_designs.medical_history.MedicalSurgicalHistoryView
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExaminationList
import com.intellisoft.kabarakmhis.new_designs.pmtct.PMTCTInterventionsView
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyList
import com.intellisoft.kabarakmhis.new_designs.tetanus_diptheria.PreventiveServiceList
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancyList
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.weight_monitoring.WeightMonitoringChart
import kotlinx.android.synthetic.main.activity_new_main.*
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.ArrayList

class PatientProfile : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        kabarakViewModel = KabarakViewModel(this.application)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        title = "Client Details"

        linearCall.setOnClickListener {
            val txtCall = tvKinDetails.text.toString()
            calluser(txtCall)
        }
        imgViewCall.setOnClickListener {
            val txtCall = tvKinDetails.text.toString()
            calluser(txtCall)
        }
        getData()

        navigate()

    }

    private fun getData() {

        val status = formatterClass.retrieveSharedPreference(this, "status")
        if (status != null && status != "") {

            if (status=="referred"){
                changeVisibility()
            }

        }

//        viewModel.poll()

    }

    private fun changeVisibility(){
        navigateMedicalHistory.visibility = View.GONE
        linearRow1.visibility = View.GONE
        linearRow2.visibility = View.GONE
        linearRow3.visibility = View.GONE
        linearRow4.visibility = View.GONE
        linearRow5.visibility = View.GONE
    }

    private fun calluser(value: String){
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$value")
        startActivity(dialIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        getPatientData()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPatientData() {

        CoroutineScope(Dispatchers.IO).launch {

            try {
                getData()

                formatter.deleteSharedPreference(this@PatientProfile, "savedEncounter")

                val patientLocalName = formatter.retrieveSharedPreference(this@PatientProfile, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@PatientProfile, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@PatientProfile, "identifier")

                val patientLocalKinName = formatter.retrieveSharedPreference(this@PatientProfile, "kinName")
                val patientLocalKinPhone = formatter.retrieveSharedPreference(this@PatientProfile, "kinPhone")

                if (patientLocalName == null || patientLocalName == "") {

                    CoroutineScope(Dispatchers.Main).launch {

                        val progressDialog = ProgressDialog(this@PatientProfile)
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
                                    formatter.saveSharedPreference(this@PatientProfile, "edd", it)
                                }
                            }

                            formatter.saveSharedPreference(this@PatientProfile, "patientName", patientName)
                            formatter.saveSharedPreference(this@PatientProfile, "dob", dob)
                            formatter.saveSharedPreference(this@PatientProfile, "identifier", identifier.toString())
                            formatter.saveSharedPreference(this@PatientProfile, "kinName", kinName)
                            formatter.saveSharedPreference(this@PatientProfile, "kinPhone", kinPhone)


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
                    formatter.saveSharedPreference(this@PatientProfile, DbResourceViews.PATIENT_INFO.name, encounterId)
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


    }

    override fun onResume() {
        super.onResume()
        getPatientData()
    }

    override fun onRestart() {
        super.onRestart()
        getPatientData()
    }



    private fun navigate() {
        navigateClinicalNotes.setOnClickListener { startActivity(Intent(this, ClinicalNotesList::class.java))}
        navigateBirthPlan.setOnClickListener { startActivity(Intent(this, BirthPlanView::class.java))}
        navigatePresent.setOnClickListener { startActivity(Intent(this, PresentPregnancyList::class.java))}
        navigatePhysicalExam.setOnClickListener { startActivity(Intent(this, PhysicalExaminationList::class.java))}
        navigateWeight.setOnClickListener { startActivity(Intent(this, WeightMonitoringChart::class.java))}
        navigateMedicalHistory.setOnClickListener { startActivity(Intent(this, MedicalSurgicalHistoryView::class.java))}
        navigatePreviousPreg.setOnClickListener { startActivity(Intent(this, PreviousPregnancyList::class.java))}
        navigateAntenatal.setOnClickListener { startActivity(Intent(this, AntenatalProfileView::class.java))}
        navigatePreventiveService.setOnClickListener { startActivity(Intent(this, PreventiveServiceList::class.java))}
        navigateMalariaProphylaxis.setOnClickListener { startActivity(Intent(this, MalariaProphylaxisList::class.java))}
        navigateMaternalSerology.setOnClickListener { startActivity(Intent(this, MaternalSerologyView::class.java))}

        navigateIfas.setOnClickListener { startActivity(Intent(this, IfasList::class.java))}
        navigatePmtct.setOnClickListener { startActivity(Intent(this, PMTCTInterventionsView::class.java))}
        navigateDeworming.setOnClickListener { startActivity(Intent(this, DewormingView::class.java))}
        navigateCounselling.setOnClickListener { startActivity(Intent(this, CounsellingView::class.java))}
        navigateReferral.setOnClickListener { startActivity(Intent(this, ReferralView::class.java))}
        navigatePatientDetails.setOnClickListener { startActivity(Intent(this, PatientDetails::class.java))}

        navigateSummary.setOnClickListener { startActivity(Intent(this, Summary::class.java))}

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, NewMainActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {

                startActivity(Intent(this, Login::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}