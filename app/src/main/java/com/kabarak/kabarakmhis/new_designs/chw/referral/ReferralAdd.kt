package com.kabarak.kabarakmhis.new_designs.chw.referral

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.dave.validations.PhoneNumberValidation
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmPage
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_birth_plan_view.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.navigation
import kotlinx.android.synthetic.main.activity_referral_add.*
import kotlinx.android.synthetic.main.activity_referral_add.tvANCID
import kotlinx.android.synthetic.main.activity_referral_add.tvAge
import kotlinx.android.synthetic.main.activity_referral_add.tvKinDetails
import kotlinx.android.synthetic.main.activity_referral_add.tvKinName
import kotlinx.android.synthetic.main.activity_referral_add.tvName
import kotlinx.android.synthetic.main.activity_referral_view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReferralAdd : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referral_add)

        title = "Referral Back to community"
        kabarakViewModel = KabarakViewModel(application)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        handleNavigation()

    }

    override fun onStart() {
        super.onStart()

        getPatientData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPatientData() {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                formatter.deleteSharedPreference(this@ReferralAdd, "savedEncounter")

                val patientLocalName = formatter.retrieveSharedPreference(this@ReferralAdd, "patientName")
                val patientLocalDob = formatter.retrieveSharedPreference(this@ReferralAdd, "dob")
                val patientLocalIdentifier = formatter.retrieveSharedPreference(this@ReferralAdd, "identifier")

                val patientLocalKinName = formatter.retrieveSharedPreference(this@ReferralAdd, "kinName")
                val patientLocalKinPhone = formatter.retrieveSharedPreference(this@ReferralAdd, "kinPhone")

                if (patientLocalName == null || patientLocalName == "") {

                    CoroutineScope(Dispatchers.Main).launch {

                        val progressDialog = ProgressDialog(this@ReferralAdd)
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
                                    formatter.saveSharedPreference(this@ReferralAdd, "edd", it)
                                }
                            }

                            formatter.saveSharedPreference(this@ReferralAdd, "patientName", patientName)
                            formatter.saveSharedPreference(this@ReferralAdd, "dob", dob)
                            formatter.saveSharedPreference(this@ReferralAdd, "identifier", identifier.toString())
                            formatter.saveSharedPreference(this@ReferralAdd, "kinName", kinName)
                            formatter.saveSharedPreference(this@ReferralAdd, "kinPhone", kinPhone)


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
                    formatter.saveSharedPreference(this@ReferralAdd, DbResourceViews.PATIENT_INFO.name, encounterId)
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

        formatter.retrieveSharedPreference(this, "edd")



    }

    override fun onResume() {
        super.onResume()
        getPatientData()
    }

    override fun onRestart() {
        super.onRestart()
        getPatientData()
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Preview"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener {

            val confirmCheck = checkbox.isChecked
            if (confirmCheck){
                saveData()
            }else{
                Toast.makeText(this, "Please confirm that this information is accurate.", Toast.LENGTH_SHORT).show()
            }



        }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun saveData(){

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val officerName = etOfficerName.text.toString()
        val officerNumber = etMobileNumber.text.toString()
        val chvName = etCHVName.text.toString()
        val chvNumber = etCHVNumber.text.toString()
        val communityHealthWork = etCommunityHealthUnit.text.toString()

        val referralOfficer = formatter.getRadioText(radioGrpReferringOfficer)
        val describeServices = etDescribe.text.toString()

        if (!TextUtils.isEmpty(officerName) && !TextUtils.isEmpty(officerNumber) &&
            !TextUtils.isEmpty(chvName) && !TextUtils.isEmpty(chvNumber) &&
            !TextUtils.isEmpty(describeServices) &&
            !TextUtils.isEmpty(communityHealthWork)&& referralOfficer != ""){

            val officerCheckNo = PhoneNumberValidation().getStandardPhoneNumber(officerNumber)
            val chvCheckNo = PhoneNumberValidation().getStandardPhoneNumber(chvNumber)

            if (officerCheckNo != null && chvCheckNo != null){

                addData("Name of referring officer ", officerName, DbObservationValues.OFFICER_NAME.name)
                addData("Mobile number ", officerCheckNo, DbObservationValues.OFFICER_NUMBER.name)
                addData("Name of receiving Community Health Volunteer (CHV) ", chvName, DbObservationValues.CHV_NAME.name)
                addData("Mobile number ", chvCheckNo, DbObservationValues.CHV_NUMBER.name)
                addData("Name of community health unit ", communityHealthWork, DbObservationValues.COMMUNITY_HEALTH_UNIT.name)


                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.A_REFERRAL_OFFICER_DETAILS.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()


                addData("Call made by referring officer ", referralOfficer, DbObservationValues.REFERRING_OFFICER.name)
                addData("Describe the services that CHV should provide for the client ", describeServices, DbObservationValues.CLIENT_SERVICE.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.B_REFERRAL_DETAILS.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()


                val dbDataDetailsList = ArrayList<DbDataDetails>()
                val dbDataDetails = DbDataDetails(dbDataList)
                dbDataDetailsList.add(dbDataDetails)
                val dbPatientData = DbPatientData(DbResourceViews.COMMUNITY_REFERRAL.name, dbDataDetailsList)

                kabarakViewModel.insertInfo(this, dbPatientData)

//                patientDetailsViewModel.updateCHWUpdate()

                formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.COMMUNITY_REFERRAL.name)

                val intent = Intent(this, ConfirmPage::class.java)
                startActivity(intent)

            }else{

                if (officerCheckNo == null) errorList.add("Invalid officer number")
                if (chvCheckNo == null) errorList.add("Invalid CHV number")

                formatter.showErrorDialog(errorList, this)

            }


        }else{
            if (TextUtils.isEmpty(officerName)) errorList.add("Please enter the name of the officer")
            if (TextUtils.isEmpty(officerNumber)) errorList.add("Please enter the mobile number of the officer")
            if (TextUtils.isEmpty(chvName)) errorList.add("Please enter the name of the CHV")
            if (TextUtils.isEmpty(chvNumber)) errorList.add("Please enter the mobile number of the CHV")
            if (TextUtils.isEmpty(describeServices)) errorList.add("Please enter the services provided")
            if (TextUtils.isEmpty(communityHealthWork)) errorList.add("Please enter the community health unit")
            if (referralOfficer == "") errorList.add("Please select the referral officer")

            formatter.showErrorDialog(errorList, this)
        }




    }

    private fun addData(key: String, value: String, codeLabel:String) {
        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
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