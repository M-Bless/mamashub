package com.intellisoft.kabarakmhis.new_designs.birth_plan

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.dave.validations.PhoneNumberValidation
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_birthplan2.view.*
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etCompanionName
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etCompanionPhone
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etTransportMeans
import kotlinx.android.synthetic.main.fragment_birthplan2.view.navigation

import kotlinx.android.synthetic.main.navigation.view.*


class FragmentBirthPlan2 : Fragment() , AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    var designationList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerDesignationValue1  = designationList[0]

    var bloodGroupList = arrayOf("","A", "AB", "B", "O")
    private var spinnerBloodGroupValue  = bloodGroupList[0]

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_birthplan2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())

        getPageDetails()
        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun initSpinner() {

        val designation = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, designationList)
        designation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rootView.spinnerCompanionDesignation!!.adapter = designation
        rootView.spinnerCompanionDesignation.onItemSelectedListener = this

        val bloodGroup = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroupList)
        bloodGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerDonorGroup!!.adapter = bloodGroup
        rootView.spinnerDonorGroup.onItemSelectedListener = this




    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerCompanionDesignation -> { spinnerDesignationValue1 = rootView.spinnerCompanionDesignation.selectedItem.toString() }
            R.id.spinnerDonorGroup -> { spinnerBloodGroupValue = rootView.spinnerDonorGroup.selectedItem.toString() }


            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()

        val companionName = rootView.etCompanionName.text.toString()
        val companionPhone = rootView.etCompanionPhone.text.toString()
        val companionMeans = rootView.etTransportMeans.text.toString()

        if (!TextUtils.isEmpty(companionName) && !TextUtils.isEmpty(companionPhone) && !TextUtils.isEmpty(companionMeans)) {

            addData("Name",companionName, DbObservationValues.COMPANION_NAME.name)
            addData("Telephone Number",companionPhone ,DbObservationValues.COMPANION_NUMBER.name)
            addData("Transport",companionMeans, DbObservationValues.COMPANION_TRANSPORT.name)
            addData("Designation",spinnerDesignationValue1, DbObservationValues.ATTENDANT_DESIGNATION.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.E_ALTERNATIVE_BIRTH_COMPANION.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

        }else{

        }




        val donorName = rootView.etDonorName.text.toString()
        val donorPhone = rootView.etDonorPhone.text.toString()

        addData("Blood Donor Name",donorName, DbObservationValues.DONOR_NAME.name)
        addData("Blood Donor Phone Number",donorPhone, DbObservationValues.DONOR_NUMBER.name)
        addData("Blood Group",spinnerBloodGroupValue, DbObservationValues.DONOR_BLOOD_GROUP.name)
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.F_BLOOD_DONOR.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()


        val financialPlan = rootView.etFinancialPlan.text.toString()
        addData("Financial plan for childbirth",financialPlan, DbObservationValues.FINANCIAL_PLAN.name)

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.E_FINANCIAL_PLAN.name, DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()

        val companionPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(companionPhone)
        val donorPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(donorPhone)

        if (companionPhoneNo != null && donorPhoneNo != null) {

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.BIRTH_PLAN.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.BIRTH_PLAN.name))
            ft.addToBackStack(null)
            ft.commit()


        }else{

            if (companionPhoneNo == null) rootView.etCompanionPhone.error = "Invalid Phone Number"
            if (donorPhoneNo == null) rootView.etDonorPhone.error = "Invalid Phone Number"

        }


    }



    private fun addData(key: String, value: String, codeLabel: String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
        }

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