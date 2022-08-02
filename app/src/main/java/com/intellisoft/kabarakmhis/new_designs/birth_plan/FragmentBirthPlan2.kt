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
import com.intellisoft.kabarakmhis.R
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

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    var designationList = arrayOf("","Midwife", "Obstetrician")
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

        addData("Name",companionName)
        addData("Telephone Number",companionPhone)
        addData("Transport",companionMeans)
        addData("Designation",spinnerDesignationValue1)

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Alternative Birth Companion", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()


        val donorName = rootView.etDonorName.text.toString()
        val donorPhone = rootView.etDonorPhone.text.toString()

        addData("Blood Donor Name",donorName)
        addData("Blood Donor Phone Number",donorPhone)
        addData("Blood Group",spinnerBloodGroupValue)
        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Blood Donor", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()

        val financialPlan = rootView.etFinancialPlan.text.toString()
        addData("Financial plan for childbirth",financialPlan)


        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Counselling", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.BIRTH_PLAN.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.BIRTH_PLAN.name))
        ft.addToBackStack(null)
        ft.commit()

    }



    private fun addData(key: String, value: String) {
        if (key != ""){
            observationList[key] = value
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