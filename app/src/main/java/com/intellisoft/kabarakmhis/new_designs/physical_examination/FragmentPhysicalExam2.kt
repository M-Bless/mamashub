package com.intellisoft.kabarakmhis.new_designs.physical_examination

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
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
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.*
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.navigation
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.radioGrpExternalExam
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentPhysicalExam2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel


    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_physical_exam_2, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.radioGrpAbdominalExam.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearInspection, true)
                } else {
                    changeVisibility(rootView.linearInspection, false)
                }

            }
        }
        rootView.radioGrpPalpation.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearPalp, true)
                } else {
                    changeVisibility(rootView.linearPalp, false)
                }

            }
        }
        rootView.radioGrpAuscalation.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearAusc, true)
                } else {
                    changeVisibility(rootView.linearAusc, false)
                }

            }
        }
        rootView.radioGrpExternalExam.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearExternalInspection, true)
                } else {
                    changeVisibility(rootView.linearExternalInspection, false)
                }

            }
        }
        rootView.radioGrpExternalPalpation.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearExternalPalp, true)
                } else {
                    changeVisibility(rootView.linearExternalPalp, false)
                }

            }
        }
        rootView.radioGrpDischarge.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearDischarge, true)
                } else {
                    changeVisibility(rootView.linearDischarge, false)
                }

            }
        }
        rootView.radioGrpGenital.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearGenital, true)
                } else {
                    changeVisibility(rootView.linearGenital, false)
                }

            }
        }



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
        val dbDataList = ArrayList<DbDataList>()

        val inspectionDoneValue = formatter.getRadioText(rootView.radioGrpAbdominalExam)
        if (inspectionDoneValue != ""){

            addData("Inspection Done","Yes", DbObservationValues.ABDOMINAL_INSPECTION.name)

            if(rootView.linearInspection.visibility == View.VISIBLE){

                val inspectionValue = rootView.etAbnomality.text.toString()
                if(!TextUtils.isEmpty(inspectionValue)){
                    addData("If yes, specify",inspectionValue , DbObservationValues.ABDOMINAL_INSPECTION.name)
                }else{
                    errorList.add("If yes on an inspection, please specify")
                }

            }

        }else{
            errorList.add("Please make a selection on Inspection")
        }

        val palpationDoneValue = formatter.getRadioText(rootView.radioGrpPalpation)
        if (palpationDoneValue != ""){

            if(rootView.linearPalp.visibility == View.VISIBLE){

                val palpationValue = rootView.etPalpation.text.toString()
                if(!TextUtils.isEmpty(palpationValue)){
                    addData("If yes, specify",palpationValue, DbObservationValues.ABDOMINAL_PALPATION.name)
                }else{
                    errorList.add("If yes on palpation, please specify")
                }
            }

        }else{
            errorList.add("Please make a selection on Palpation")
        }

        val auscultationDoneValue = formatter.getRadioText(rootView.radioGrpPalpation)
        if (auscultationDoneValue != ""){

            addData("Auscultation Done",auscultationDoneValue, DbObservationValues.ABDOMINAL_AUSCALATION.name)

            val auscultationValue = rootView.etAuscalation.text.toString()
            if(!TextUtils.isEmpty(auscultationValue)){
                addData("If yes, specify",auscultationValue, DbObservationValues.ABDOMINAL_AUSCALATION.name)
            }else{
                errorList.add("If yes on auscultation, please specify")
            }

        }else{
            errorList.add("Please make a selection on auscultation")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.D_ABDOMINAL_EXAMINATION.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()


        val externalGenitaliaExamValue = formatter.getRadioText(rootView.radioGrpExternalExam)
        if (externalGenitaliaExamValue != ""){
            addData("Inspection Done",externalGenitaliaExamValue, DbObservationValues.EXTERNAL_INSPECTION.name)
            if(rootView.linearExternalPalp.visibility == View.VISIBLE){
                val text = rootView.etExternalAbnomality.text.toString()
                if(!TextUtils.isEmpty(text)) {
                    addData("If yes, specify", text, DbObservationValues.EXTERNAL_INSPECTION.name)
                }else{
                    errorList.add("If yes on an external inspection, please specify")
                }
            }
        }else{
            errorList.add("Please make a selection of Inspection under External genitalia exam")
        }

        val externalPalpationValue = formatter.getRadioText(rootView.radioGrpExternalPalpation)
        if (externalPalpationValue != ""){
            addData("Palpation Done","Yes", DbObservationValues.EXTERNAL_PALPATION.name)
            if(rootView.linearExternalPalp.visibility == View.VISIBLE){
                val text = rootView.etExternalPalpation.text.toString()
                if(!TextUtils.isEmpty(text)){
                    addData("If yes, specify",text, DbObservationValues.EXTERNAL_PALPATION.name)
                }else{
                    errorList.add("If yes on external palpation, please specify")
                }
            }

        }else{
            errorList.add("Please make a selection on Palpation under External genitalia exam")
        }

        val externalDischargeValue = formatter.getRadioText(rootView.radioGrpDischarge)
        if (externalDischargeValue != ""){

            addData("Discharge Done", externalDischargeValue, DbObservationValues.EXTERNAL_DISCHARGE.name)

            val text = rootView.etDischarge.text.toString()
            if(!TextUtils.isEmpty(text)) {
                addData("If yes, specify", text, DbObservationValues.EXTERNAL_DISCHARGE.name)
            }else{
                errorList.add("If yes on discharge, please specify")
            }

        }else{
            errorList.add("Please make a selection on Discharge under External genitalia exam")
        }

        val genitalUlcerValue = formatter.getRadioText(rootView.radioGrpGenital)
        if (genitalUlcerValue != "") {
            addData("Genital Ulcer Present", "Yes", DbObservationValues.EXTERNAL_GENITAL_ULCER.name)
            if (rootView.linearGenital.visibility == View.VISIBLE) {
                val text = rootView.etGenital.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("If yes, specify", text, DbObservationValues.EXTERNAL_GENITAL_ULCER.name)
                } else {
                    errorList.add("If yes on genital ulcer, please specify")
                }
            }
        } else {
            errorList.add("Please make a selection on Genital ulcer")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.E_EXTERNAL_GENITALIA_EXAM.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()


        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PHYSICAL_EXAMINATION.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.PHYSICAL_EXAMINATION.name))
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

}