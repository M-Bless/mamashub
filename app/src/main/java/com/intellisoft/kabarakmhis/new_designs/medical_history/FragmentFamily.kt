package com.intellisoft.kabarakmhis.new_designs.medical_history

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
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_family.view.*
import kotlinx.android.synthetic.main.fragment_family.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentFamily : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_family, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.radioGrpTwins.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTwins, true)
                } else {
                    changeVisibility(rootView.linearTwins, false)
                }

            }
        }
        rootView.radioGrpTb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTbRelation, true)
                } else {
                    changeVisibility(rootView.linearTbRelation, false)
                }

            }
        }
        rootView.radioGrpSameHouse.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReferTbScreening, true)
                } else {
                    changeVisibility(rootView.linearReferTbScreening, false)
                }

            }
        }

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()
        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()

        val twinsValue = formatter.getRadioText(rootView.radioGrpTwins)
        if (twinsValue != ""){

            if (rootView.linearTwins.visibility == View.VISIBLE){

                var twins = ""
                if (rootView.checkboxPreviousPregnancy.isChecked)twins = twins + "Previous Pregnancy" + ","
                if (rootView.checkboxMotherSide.isChecked)twins = twins + "Mother's side" + ","
                addData("Twins History",twins,DbObservationValues.TWINS.name)

            }else{
                addData("Twins History",twinsValue,DbObservationValues.TWINS.name)
            }

        }else{
            errorList.add("Twins selection is required")
        }

        val tbValue = formatter.getRadioText(rootView.radioGrpTb)
        if (tbValue != "") {

            if (rootView.linearTbRelation.visibility == View.VISIBLE){
                //Get Name of relative and relationship
                val relativeName = rootView.etRelativeTbName.text.toString()
                if (!TextUtils.isEmpty(relativeName)) {
                    addData("Family Member with TB ",relativeName,DbObservationValues.TUBERCULOSIS.name)
                } else {
                    errorList.add("TB Relative Name is required")
                }

                addData("Family Member with TB Relationship",spinnerRshpValue, DbObservationValues.RELATIONSHIP.name)

                val relativeHouseHold = formatter.getRadioText(rootView.radioGrpSameHouse)

                if (relativeHouseHold != "") {

                    if (rootView.linearReferTbScreening.visibility == View.VISIBLE){
                        //Refer for TB Screening
                        val text = rootView.etTbScreening.text.toString()
                        if (!TextUtils.isEmpty(text)){
                            addData("Tuberculosis Screening",text,DbObservationValues.TUBERCULOSIS.name)
                        }else{
                            errorList.add("Tuberculosis Screening value is required")
                        }

                    }else{
                        addData("Was the patient sharing residence with TB person? ",relativeHouseHold ,DbObservationValues.TUBERCULOSIS.name)
                    }

                } else {
                    errorList.add("TB Screening is required")
                }

            }else{
                addData("Tuberculosis History",tbValue,DbObservationValues.TUBERCULOSIS.name)
            }

        }else{
            errorList.add("TB selection is required")
        }


        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.D_FAMILY_HISTORY.name,
                DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }

        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
                DbResourceViews.MEDICAL_HISTORY.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{

            formatter.showErrorDialog(errorList, requireContext())
        }


    }

    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
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

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationshipList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerRelativeTbRshp!!.adapter = kinRshp

        rootView.spinnerRelativeTbRshp.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRelativeTbRshp -> { spinnerRshpValue = rootView.spinnerRelativeTbRshp.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }



}