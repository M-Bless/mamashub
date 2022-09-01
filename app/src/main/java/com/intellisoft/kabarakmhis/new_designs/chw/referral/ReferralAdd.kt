package com.intellisoft.kabarakmhis.new_designs.chw.referral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.dave.validations.PhoneNumberValidation
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
import kotlinx.android.synthetic.main.activity_birth_plan_view.*
import kotlinx.android.synthetic.main.activity_birth_plan_view.tvAncId
import kotlinx.android.synthetic.main.activity_birth_plan_view.tvPatient
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.navigation
import kotlinx.android.synthetic.main.activity_referral_add.*
import kotlinx.android.synthetic.main.navigation.view.*

class ReferralAdd : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private var observationList = mutableMapOf<String, DbObservationLabel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referral_add)

        title = "Referral Back to community"
        kabarakViewModel = KabarakViewModel(application)

        handleNavigation()

    }

    override fun onStart() {
        super.onStart()

        getUserDetails()
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Preview"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
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
        val signature = etSignature.text.toString()

        if (!TextUtils.isEmpty(officerName) && !TextUtils.isEmpty(officerNumber) &&
            !TextUtils.isEmpty(chvName) && !TextUtils.isEmpty(chvNumber) &&
            !TextUtils.isEmpty(describeServices) && !TextUtils.isEmpty(signature) &&
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
                addData("Signature ", signature, DbObservationValues.SIGNATURE.name)

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
            if (TextUtils.isEmpty(signature)) errorList.add("Please enter the signature")
            if (TextUtils.isEmpty(communityHealthWork)) errorList.add("Please enter the community health unit")
            if (referralOfficer == "") errorList.add("Please select the referral officer")

            formatter.showErrorDialog(errorList, this)
        }




    }

    private fun addData(key: String, value: String, codeLabel:String) {
        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
    }


    private fun getUserDetails() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        if (identifier != null && patientName != null) {
            tvPatient.text = patientName
            tvAncId.text = identifier
        }

    }
}