package com.kabarak.kabarakmhis.new_designs.screens

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.auth.Login
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.NewMainActivity
import com.kabarak.kabarakmhis.new_designs.data_class.DbAncSchedule
import com.kabarak.kabarakmhis.new_designs.data_class.DbIdentifier
import com.kabarak.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class PatientProfile : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private val viewModel: MainActivityViewModel by viewModels()

    val modelList = ArrayList<Model>()
    private var isPatient = false


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

        initData()


    }


    private fun initData() {

        val dbMaternalProfileList = ArrayList<DbMaternalProfile>()

        //Clinical Information
        val dbMaternalClientInfoList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild1 = DbMaternalProfileChild(1.1,resources.getDrawable(R.drawable.register), "Maternal Profile")
        val dbMaternalProfileChild2 = DbMaternalProfileChild(1.2,resources.getDrawable(R.drawable.currentvisits), "Current Visit")

//        val dbMaternalProfileChild113 = DbMaternalProfileChild(1.3,resources.getDrawable(R.drawable.currentvisits), "BP Monitoring")
        val dbMaternalProfileChild114 = DbMaternalProfileChild(1.4,resources.getDrawable(R.drawable.upcoming_appointment), "Upcoming appointments")
        dbMaternalClientInfoList.addAll(listOf(dbMaternalProfileChild1, dbMaternalProfileChild2, dbMaternalProfileChild114))

        //History
        val dbMaternalHistoryList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild3 = DbMaternalProfileChild(2.1,resources.getDrawable(R.drawable.surgery), "Medical and Surgical History")
        val dbMaternalProfileChild4 = DbMaternalProfileChild(2.2,resources.getDrawable(R.drawable.pregnant), "Previous Pregnancy")
        dbMaternalHistoryList.addAll(listOf(dbMaternalProfileChild3, dbMaternalProfileChild4))

        //Mother and Fetal Assessment
        val dbMaternalAssessmentList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild5 = DbMaternalProfileChild(3.1,resources.getDrawable(R.drawable.check), "Physical Examination")
        val dbMaternalProfileChild6 = DbMaternalProfileChild(3.2,resources.getDrawable(R.drawable.antenatalprofile), "Antenatal Profile")
        val dbMaternalProfileChild7 = DbMaternalProfileChild(3.3,resources.getDrawable(R.drawable.present_pregnancy), "Present Pregnancy")
        val dbMaternalProfileChild8 = DbMaternalProfileChild(3.4,resources.getDrawable(R.drawable.weight_monitoring), "Weight Monitoring Chart")
        val dbMaternalProfileChild9 = DbMaternalProfileChild(3.5,resources.getDrawable(R.drawable.clinical_notes), "Clinical Notes")
        dbMaternalAssessmentList.addAll(listOf(dbMaternalProfileChild5, dbMaternalProfileChild6, dbMaternalProfileChild7, dbMaternalProfileChild8, dbMaternalProfileChild9))

        //Preventive Services
        val dbMaternalPreventiveServicesList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild10 = DbMaternalProfileChild(4.1,resources.getDrawable(R.drawable.tetanusinjection), "Tetanus Diphtheria")
        val dbMaternalProfileChild12 = DbMaternalProfileChild(4.2,resources.getDrawable(R.drawable.malaria_prophylaxis), "Malaria Prophylaxis")
        val dbMaternalProfileChild11 = DbMaternalProfileChild(4.3,resources.getDrawable(R.drawable.td), "IFAS")
        val dbMaternalProfileChild13 = DbMaternalProfileChild(4.4,resources.getDrawable(R.drawable.maternal_serology), "Maternal Serology")
        val dbMaternalProfileChild14 = DbMaternalProfileChild(4.5,resources.getDrawable(R.drawable.preventiveservices), "PMTCT Interventions")
        val dbMaternalProfileChild15 = DbMaternalProfileChild(4.6,resources.getDrawable(R.drawable.deworming), "Deworming")
        dbMaternalPreventiveServicesList.addAll(listOf(
            dbMaternalProfileChild10, dbMaternalProfileChild11, dbMaternalProfileChild12,
            dbMaternalProfileChild13, dbMaternalProfileChild14, dbMaternalProfileChild15))

        //Birth Plan
        val dbMaternalBirthPlanList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild16 = DbMaternalProfileChild(5.1,resources.getDrawable(R.drawable.birth_plan), "Birth Plan")
        dbMaternalBirthPlanList.addAll(listOf(dbMaternalProfileChild16))

        //Counseling
        val dbMaternalCounselingList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild17 = DbMaternalProfileChild(6.1,resources.getDrawable(R.drawable.counselling1), "Counseling")
        dbMaternalCounselingList.addAll(listOf(dbMaternalProfileChild17))

        //Referral to community
        val dbMaternalReferralList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild18 = DbMaternalProfileChild(7.1,resources.getDrawable(R.drawable.referral), "Referral")
        dbMaternalReferralList.addAll(listOf(dbMaternalProfileChild18))

        // PNC Visit
        val dbMaternalPncVisitList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild19 = DbMaternalProfileChild(8.1,resources.getDrawable(R.drawable.childbirth), "ChildBirth")
        val dbMaternalProfileChild20 = DbMaternalProfileChild(8.2,resources.getDrawable(R.drawable.postnatalcare), "Early Identification of Congenital Abnormalities")


        val dbMaternalProfileChild21 = DbMaternalProfileChild(8.3,resources.getDrawable(R.drawable.childbirth), "Reproductive Organs Cancer Screening")
        val dbMaternalProfileChild22 = DbMaternalProfileChild(8.4,resources.getDrawable(R.drawable.chm), "Family Planning")
        val dbMaternalProfileChild23 = DbMaternalProfileChild(8.5, resources.getDrawable(R.drawable.chm), "POSTNATAL CARE - Mother")
        val dbMaternalProfileChild24 = DbMaternalProfileChild(8.6, resources.getDrawable(R.drawable.chm), "POSTNATAL CARE - Child")

        // Child Health Monitoring
        val dbMaternalProfileChild25 = DbMaternalProfileChild(8.7, resources.getDrawable(R.drawable.chm), "Civil Registration")
        val dbMaternalProfileChild26 = DbMaternalProfileChild(8.8, resources.getDrawable(R.drawable.chm), "Reason for Special Care")
        val dbMaternalProfileChild27 = DbMaternalProfileChild(8.9, resources.getDrawable(R.drawable.chm), "Other Problems as Reported by Parent or Guardian")
        val dbMaternalProfileChild28 = DbMaternalProfileChild(9.1, resources.getDrawable(R.drawable.chm), "Record of Baby Teeth Development")
        val dbMaternalProfileChild29 = DbMaternalProfileChild(9.2, resources.getDrawable(R.drawable.chm), "Broad clinical review at first contact below 6 months")
        val dbMaternalProfileChild30 = DbMaternalProfileChild(9.3, resources.getDrawable(R.drawable.chm), "Feeding information from parent/guardian")
        val dbMaternalProfileChild31 = DbMaternalProfileChild(9.4, resources.getDrawable(R.drawable.chm), "Developmental Milestones")
        val dbMaternalProfileChild32 = DbMaternalProfileChild(9.5, resources.getDrawable(R.drawable.chm), "Identification of early eye problems in an infant")
        val dbMaternalProfileChild33 = DbMaternalProfileChild(9.6, resources.getDrawable(R.drawable.chm), "Record of baby’s teeth development")
        val dbMaternalProfileChild34 = DbMaternalProfileChild(9.7, resources.getDrawable(R.drawable.chm), "Reason for Special Care")

        dbMaternalPncVisitList.addAll(listOf(dbMaternalProfileChild19, dbMaternalProfileChild20, dbMaternalProfileChild21, dbMaternalProfileChild22, dbMaternalProfileChild23, dbMaternalProfileChild24, dbMaternalProfileChild25, dbMaternalProfileChild26, dbMaternalProfileChild27, dbMaternalProfileChild28, dbMaternalProfileChild29, dbMaternalProfileChild30, dbMaternalProfileChild31, dbMaternalProfileChild32, dbMaternalProfileChild33, dbMaternalProfileChild34))

        // Immunization
        val immunizationList = ArrayList<DbMaternalProfileChild>()
        val immunization1 = DbMaternalProfileChild(10.1, resources.getDrawable(R.drawable.chm), "BCG Vaccine")
        val immunization2 = DbMaternalProfileChild(10.2, resources.getDrawable(R.drawable.chm), "Polio vaccine")
        val immunization3 = DbMaternalProfileChild(10.3, resources.getDrawable(R.drawable.chm), "IPV (Inactivated Polio Vaccine)")
        val immunization4 = DbMaternalProfileChild(10.4, resources.getDrawable(R.drawable.chm), "Diphtheria/Pertussis/Tetanus/Hepatitis\n" +
                "B/Haemophilus Influenza Type B")
        val immunization5 = DbMaternalProfileChild(10.5, resources.getDrawable(R.drawable.chm), "Pneumococcal Conjugate Vaccine")
        val immunization6 = DbMaternalProfileChild(10.6, resources.getDrawable(R.drawable.chm), "Rotavirus Vaccine")
        val immunization7 = DbMaternalProfileChild(10.7, resources.getDrawable(R.drawable.chm), "Measles Vaccine (MR)")
        val immunization8 = DbMaternalProfileChild(10.8, resources.getDrawable(R.drawable.chm), "Yellow Fever Vaccine")
        val immunization9 = DbMaternalProfileChild(10.9, resources.getDrawable(R.drawable.chm), "Meningococcal Vaccine")
        val immunization10 = DbMaternalProfileChild(11.1, resources.getDrawable(R.drawable.chm), "Other Vaccines")
        val immunization11 = DbMaternalProfileChild(11.2, resources.getDrawable(R.drawable.chm), "Vitamin A")
        immunizationList.addAll(listOf(immunization1, immunization2, immunization3, immunization4, immunization5, immunization6, immunization7, immunization8, immunization9, immunization10, immunization11))



        val dbMaternalProfile = DbMaternalProfile("Client Information", dbMaternalClientInfoList, true)
        val dbMaternalMedicalHistory = DbMaternalProfile("History", dbMaternalHistoryList, isPatient)
        val dbMaternalAssessment = DbMaternalProfile("Mother and Fetal Assessment", dbMaternalAssessmentList, isPatient)
        val dbMaternalPreventiveServices = DbMaternalProfile("Preventive Services", dbMaternalPreventiveServicesList, isPatient)
        val dbMaternalBirthPlan = DbMaternalProfile("Birth Plan", dbMaternalBirthPlanList, isPatient)
        val dbMaternalCounseling = DbMaternalProfile("Counseling", dbMaternalCounselingList, isPatient)
        val dbMaternalReferral = DbMaternalProfile("Referral to Community", dbMaternalReferralList, isPatient)
        val dbMaternalPncVisit = DbMaternalProfile("PNC Visit", dbMaternalPncVisitList, isPatient)
        val dbImmunization = DbMaternalProfile("Immunization", immunizationList, isPatient)

        dbMaternalProfileList.addAll(listOf(
            dbMaternalProfile,dbMaternalMedicalHistory,dbMaternalAssessment,
            dbMaternalPreventiveServices,dbMaternalBirthPlan,dbMaternalCounseling,dbMaternalReferral, dbMaternalPncVisit, dbImmunization))

        val adapter = ExpandableRecyclerAdapter(dbMaternalProfileList, this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)


    }

    private fun getData() {

        val status = formatterClass.retrieveSharedPreference(this, "spinnerClientValue")
        isPatient = if (status != null && status != "") {
            status != ReferralTypes.REFERRED.name
        }else{
            false
        }

//        viewModel.poll()

    }

    private fun getAncSchedule(edd: String) {

        val ancSchedulingCalculator = AncSchedulingCalculator()

        val observationList = patientDetailsViewModel.getObservationFromEncounter(DbResourceViews.PRESENT_PREGNANCY.name)
        formatter.deleteSharedPreference(this@PatientProfile, DbResourceViews.PRESENT_PREGNANCY.name)

        if(observationList.isNotEmpty()){

            val encounterId = observationList[0].id
            val firstVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                formatter.getCodes(DbObservationValues.NEXT_CURRENT_VISIT.name), encounterId)

            /**
             * TODO: Get first present pregnancy encounter (This is assumed to be the first contact) and get the observation of the date
             * TODO: Get the date and calculate the anc schedule
             * TODO: Save the anc schedule in shared preferences
             * TODO: Get the anc schedule from shared preferences and display it
             */

            //Get first day of last menstrual period from EDD Change string to date and remove 280 days
            val firstDayLMP = formatterClass.convertStringToLocalDate(edd)
            //Get number of weeks from first day of last menstrual period to today

            if (firstVisit.isNotEmpty()){

                val firstVisitValue = firstVisit[0].value

                //Get the number of weeks from first day of lmp to first visit
                val firstContactWeek = formatter.getWeeksBetweenDates(firstDayLMP, firstVisitValue)

                val list = ancSchedulingCalculator.ancSchedule(firstContactWeek)
                list.add(0, firstContactWeek)
                val contactVisits = observationList.size

                val contactWeek = list[contactVisits]

                //Calculate the next contact date from today
                formatter.saveSharedPreference(this@PatientProfile, DbAncSchedule.CONTACT_WEEK.name, contactWeek.toString())


            }

        }else{

            //Get first day of last menstrual period from EDD Change string to date and remove 280 days
            val firstDayLMP = formatterClass.convertStringToLocalDate(edd)
            //Get number of weeks from first day of last menstrual period to today
            val contactWeek = formatter.getWeeksBetweenDates(firstDayLMP, formatterClass.getTodayDateNoTime())
            val list = ancSchedulingCalculator.ancSchedule(contactWeek)

            //Calculate the next contact date from today
            formatter.saveSharedPreference(this@PatientProfile, DbAncSchedule.CONTACT_WEEK.name, contactWeek.toString())

        }




    }

    private fun changeVisibility(){
//        navigateMedicalHistory.visibility = View.GONE
//        linearRow1.visibility = View.GONE
//        linearRow2.visibility = View.GONE
//        linearRow3.visibility = View.GONE
//        linearRow4.visibility = View.GONE
//        linearRow5.visibility = View.GONE
//        linearRow6.visibility = View.GONE
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

                            val observationList = patientDetailsViewModel.getObservationFromEncounter(
                                DbResourceViews.PATIENT_INFO.name)
                            if (observationList.isNotEmpty()){

                                val encounterId = observationList[0].id
                                val clinicalInfo = DbObservationFhirData(
                                    DbSummaryTitle.C_CLINICAL_INFORMATION.name,
                                    listOf(formatter.getCodes(DbObservationValues.GRAVIDA.name),
                                        formatter.getCodes(DbObservationValues.PARITY.name),
                                        formatter.getCodes(DbObservationValues.LMP.name),
                                        formatter.getCodes(DbObservationValues.EDD.name),
                                        formatter.getCodes(DbObservationValues.HEIGHT.name),
                                        formatter.getCodes(DbObservationValues.WEIGHT.name)))
                                val clinicalList = formatter.getObservationList(patientDetailsViewModel,clinicalInfo, encounterId)
                                clinicalList.forEach {

                                    val detailsList = it.detailsList
                                    detailsList.forEach { obs ->

                                        val code = obs.title.trim()
                                        val value = obs.value.trim()

                                        if (code == "Gravida"){
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.GRAVIDA.name, value)
                                        }
                                        if (code == "Parity"){
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.PARITY.name, value)
                                        }
                                        if (code == "Last Menstrual Date"){

                                            //Get the gestation age from LMP
                                            val firstDayLMP = formatterClass.convertYYYYMMDD(value)

                                            val gestationAge = formatter.getGestationWeeks( firstDayLMP, formatterClass.getTodayDateNoTime())

                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.LMP.name, value)
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.GESTATION.name, gestationAge.toString())

                                        }
                                        if (code == "Expected Date of Delivery"){
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.EDD.name, value)
                                        }
                                        if (code == "Height (cm)"){
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.HEIGHT.name, value)
                                        }
                                        if (code == "Weight (kg)"){
                                            formatter.saveSharedPreference(this@PatientProfile, DbObservationValues.WEIGHT.name, value)
                                        }



                                    }

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

        val edd = formatter.retrieveSharedPreference(this, "edd")
        if (edd != null){

            getAncSchedule(edd)


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