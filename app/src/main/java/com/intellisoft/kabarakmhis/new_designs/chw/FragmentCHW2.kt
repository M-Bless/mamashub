package com.intellisoft.kabarakmhis.new_designs.chw

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_chw2.view.*
import kotlinx.android.synthetic.main.fragment_chw2.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentCHW2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_chw2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()



        return rootView
    }

    override fun onStart() {
        super.onStart()

        handleNavigation()
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val chvName = rootView.etPatientName.text.toString()
        val mobileNumber = rootView.etNumber.text.toString()
        val village = rootView.etEstate.text.toString()
        val subLocation = rootView.etSubCounty.text.toString()
        val location = rootView.etLocation.text.toString()
        val communityUnit = rootView.etCommunityName.text.toString()

        val date = rootView.tvDate.text.toString()
        val time = rootView.tvTime.text.toString()
        val officerName = rootView.etOfficerName.text.toString()
        val professionName = rootView.etProfession.text.toString()
        val facilityName = rootView.etHealthFacility.text.toString()
        val actionTaken = rootView.etActionTaken.text.toString()

        if (
            !TextUtils.isEmpty(chvName) && !TextUtils.isEmpty(mobileNumber) &&
            !TextUtils.isEmpty(village) && !TextUtils.isEmpty(subLocation) &&
            !TextUtils.isEmpty(location) && !TextUtils.isEmpty(communityUnit) &&
            !TextUtils.isEmpty(date) && !TextUtils.isEmpty(time) &&
            !TextUtils.isEmpty(officerName) && !TextUtils.isEmpty(professionName) &&
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(actionTaken)
        ){

            addData("Name:",chvName, DbObservationValues.OFFICER_NAME.name)
            addData("Mobile Number:",mobileNumber, DbObservationValues.OFFICER_NUMBER.name)
            addData("Village/Estate:",village, DbObservationValues.TOWN_NAME.name)
            addData("Sub-location:",subLocation, DbObservationValues.SUB_COUNTY_NAME.name)
            addData("Location",location, DbObservationValues.COUNTY_NAME.name)
            addData("Name of community health unit",communityUnit, DbObservationValues.COMMUNITY_HEALTH_UNIT.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.C_CHV_REFERRING_THE_PATIENT.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            addData("Date:",date, DbObservationValues.NEXT_VISIT_DATE.name)
            addData("Time:",time, DbObservationValues.TIMING_CONTACT.name)
            addData("Name of officer:",officerName, DbObservationValues.OFFICER_NAME.name)
            addData("Profession:",professionName, DbObservationValues.PROFESSION.name)
            addData("Name of health facility:",facilityName, DbObservationValues.FACILITY_NAME.name)
            addData("Action taken:",actionTaken, DbObservationValues.ACTION_TAKEN.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.D_RECEIVING_OFFICER.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.COMMUNITY_REFERRAL_WORKER.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
                DbResourceViews.COMMUNITY_REFERRAL_WORKER.name))
            ft.addToBackStack(null)
            ft.commit()


        }else{

            if (TextUtils.isEmpty(chvName)) errorList.add("Patient Name is required")
            if (TextUtils.isEmpty(mobileNumber)) errorList.add("Mobile Number is required)")
            if (TextUtils.isEmpty(village)) errorList.add("Village is required")
            if (TextUtils.isEmpty(subLocation)) errorList.add("Sub-County is required")
            if (TextUtils.isEmpty(location)) errorList.add("Location is required")
            if (TextUtils.isEmpty(communityUnit)) errorList.add("Community Unit is required")
            if (TextUtils.isEmpty(date)) errorList.add("Date is required")
            if (TextUtils.isEmpty(time)) errorList.add("Time is required")
            if (TextUtils.isEmpty(officerName)) errorList.add("Officer Name is required")
            if (TextUtils.isEmpty(professionName)) errorList.add("Profession is required")
            if (TextUtils.isEmpty(facilityName)) errorList.add("Health Facility is required")
            if (TextUtils.isEmpty(actionTaken)) errorList.add("Action Taken is required")

            formatter.showErrorDialog(errorList, requireContext())



        }



    }

    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

}