package com.intellisoft.kabarakmhis.new_designs.chw

import android.app.Application
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
import kotlinx.android.synthetic.main.fragment_chw1.view.*
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentCHW1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_chw1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()


        return rootView
    }

    override fun onStart() {
        super.onStart()

        handleNavigation()
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val clientName = rootView.etPatientName.text.toString()
        val age = rootView.etPatientName.text.toString()

        val communityHealthUnit = rootView.etCommunityHealthUnit.text.toString()
        val healthUnit = rootView.etHealthFacility.text.toString()
        val reason = rootView.etReferralReason.text.toString()
        val mainProblem = rootView.etMainProblem.text.toString()
        val interventionGiven = rootView.etIntervention.text.toString()

        val comments = rootView.etComments.text.toString()

        val isFemaleChecked = rootView.checkboxNoPast.isChecked

        if (
            !TextUtils.isEmpty(clientName) && !TextUtils.isEmpty(age) &&
            !TextUtils.isEmpty(communityHealthUnit) && !TextUtils.isEmpty(healthUnit) &&
            !TextUtils.isEmpty(reason) && !TextUtils.isEmpty(mainProblem) &&
            !TextUtils.isEmpty(interventionGiven) && !TextUtils.isEmpty(comments) && isFemaleChecked) {

            val date = formatter.getTodayDateNoTime()
            val time = formatter.getTodayTimeNoDate()

            addData("Name of client",clientName, DbObservationValues.CLIENT_NAME.name)
            addData("Sex","Female", DbObservationValues.BABY_SEX.name)
            addData("Age",age, DbObservationValues.DATE_OF_BIRTH.name)
            addData("Date",date, DbObservationValues.DATE_STARTED.name)
            addData("Time of referral",time, DbObservationValues.TIMING_CONTACT.name)


            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_PATIENT_DATA.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()


            addData("Name of community health unit",communityHealthUnit, DbObservationValues.CLIENT_NAME.name)
            addData("Name of link health facility",healthUnit, DbObservationValues.CLIENT_NAME.name)
            addData("Reason(s) for referral",reason, DbObservationValues.CLIENT_NAME.name)
            addData("Main problem(s)",mainProblem, DbObservationValues.CLIENT_NAME.name)
            addData("Intervention given",interventionGiven, DbObservationValues.CLIENT_NAME.name)
            addData("Comments",comments, DbObservationValues.CLIENT_NAME.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.B_COMMUNITY_HEALTH_FACILITY_DETAILS.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.COMMUNITY_REFERRAL_WORKER.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentCHW2())
            ft.addToBackStack(null)
            ft.commit()


        }else{

            if (TextUtils.isEmpty(clientName)) errorList.add("Client Name is required")
            if (TextUtils.isEmpty(age)) errorList.add("Age is required")
            if (TextUtils.isEmpty(communityHealthUnit)) errorList.add("Community Health Unit is required")
            if (TextUtils.isEmpty(healthUnit)) errorList.add("Health Facility is required")
            if (TextUtils.isEmpty(reason)) errorList.add("Referral Reason is required")
            if (TextUtils.isEmpty(mainProblem)) errorList.add("Main Problem is required")
            if (TextUtils.isEmpty(interventionGiven)) errorList.add("Intervention Given is required")
            if (TextUtils.isEmpty(comments)) errorList.add("Comments is required")
            if (!isFemaleChecked) errorList.add("Please make a selection")

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