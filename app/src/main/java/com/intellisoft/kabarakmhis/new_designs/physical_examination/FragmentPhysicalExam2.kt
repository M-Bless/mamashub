package com.intellisoft.kabarakmhis.new_designs.physical_examination

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
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

    private var observationList = mutableMapOf<String, String>()
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

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        if(rootView.linearInspection.visibility == View.VISIBLE){
            val text = rootView.etAbnomality.text.toString()
            addData("Abdominal Examination",text)
        }
        if(rootView.linearPalp.visibility == View.VISIBLE){
            val text = rootView.etPalpation.text.toString()
            addData("Palpation Done",text)
        }
        if(rootView.linearAusc.visibility == View.VISIBLE){
            val text = rootView.etAuscalation.text.toString()
            addData("Auscultation Done",text)
        }


        if(rootView.linearExternalInspection.visibility == View.VISIBLE){
            val text = rootView.etExternalAbnomality.text.toString()
            addData("Inspection Done",text)
        }

        if(rootView.linearExternalPalp.visibility == View.VISIBLE){
            val text = rootView.etExternalPalpation.text.toString()
            addData("Palpation Done",text)
        }
        if(rootView.linearDischarge.visibility == View.VISIBLE){
            val text = rootView.etDischarge.text.toString()
            addData("Discharge Present",text)
        }
        if(rootView.linearGenital.visibility == View.VISIBLE){
            val text = rootView.etGenital.text.toString()
            addData("Genital Ulcer Present",text)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Physical Exam", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.PHYSICAL_EXAMINATION.name, dbDataDetailsList)

        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.PHYSICAL_EXAMINATION.name)


        startActivity(Intent(requireContext(), PatientProfile::class.java))

    }

    private fun addData(key: String, value: String) {
        observationList[key] = value
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