package com.intellisoft.kabarakmhis.new_designs.medical_history

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
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_medical.view.*
import kotlinx.android.synthetic.main.fragment_medical.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*

class FragmentMedical : Fragment(){

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_medical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        rootView.radioGrpTransfusion.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearBloodReaction, true)
                } else {
                    changeVisibility(rootView.linearBloodReaction, false)
                }

            }
        }
        rootView.radioGrpDrugAllergies.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearDrug, true)
                } else {
                    changeVisibility(rootView.linearDrug, false)
                }

            }
        }
        rootView.radioGrpOtherAllergy.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearOtherNonDrugAllergy, true)
                } else {
                    changeVisibility(rootView.linearOtherNonDrugAllergy, false)
                }

            }
        }
        rootView.radioGrpOtherCondition.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.layoutOtherCondition, true)
                } else {
                    changeVisibility(rootView.layoutOtherCondition, false)
                }

            }
        }
        rootView.checkBoxOthers.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked){
                rootView.layoutOthers.visibility = View.VISIBLE
            }else{
                rootView.layoutOthers.visibility = View.GONE
            }
        }

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()


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

        val isErrorList = ArrayList<Any>()
        val dbDataList = ArrayList<DbDataList>()

        val diabetes = formatter.getRadioText(rootView.radioGrpDiabetes)
        val hypertension = formatter.getRadioText(rootView.radioGrpDiabetes)
        val tb = formatter.getRadioText(rootView.radioGrpDiabetes)
        val bloodTransfusion = formatter.getRadioText(rootView.radioGrpTransfusion)

        if (diabetes != "" && hypertension != "" && tb != "" && bloodTransfusion != ""){
            addData("Diabetes",diabetes, DbObservationValues.DIABETES.name)
            addData("Hypertension",hypertension, DbObservationValues.HYPERTENSION.name)
            addData("Tuberculosis",tb, DbObservationValues.TUBERCULOSIS.name)
            addData("Blood Transfusion",bloodTransfusion, DbObservationValues.BLOOD_TRANSFUSION.name)

        }else{
            if (diabetes == "")isErrorList.add(rootView.radioGrpDiabetes)
            if (hypertension == "")isErrorList.add(rootView.radioGrpDiabetes)
            if (tb == "")isErrorList.add(rootView.radioGrpDiabetes)
            if (bloodTransfusion == "")isErrorList.add(rootView.radioGrpTransfusion)
        }
        if (rootView.layoutOtherCondition.visibility == View.VISIBLE){

            val otherConditionList = ArrayList<String>()
            if (rootView.checkBoxEpilepsy.isChecked)otherConditionList.add("Epilepsy")
            if (rootView.checkBoxMalariaPregnancy.isChecked)otherConditionList.add("Epilepsy")
            if (rootView.layoutOthers.visibility == View.VISIBLE){

                val otherText = rootView.etOtherConditions.text.toString()
                if (!TextUtils.isEmpty(otherText)){
                    otherConditionList.add(otherText)
                }else{
                    isErrorList.add(rootView.etOtherConditions)
                }
            }

            addData("Other Medical History",otherConditionList.toString(), DbObservationValues.MEDICAL_HISTORY.name)
        }
        if (rootView.linearBloodReaction.visibility == View.VISIBLE){
            val text = rootView.etClientName.text.toString()
            addData("Blood Transfusion Reaction",text, DbObservationValues.BLOOD_TRANSFUSION.name)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpTransfusion)
            addData("Blood Transfusion Reaction",text, DbObservationValues.BLOOD_TRANSFUSION.name)
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Medical History", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        if (rootView.linearDrug.visibility == View.VISIBLE){
            val text = rootView.etDrugAllergies.text.toString()
            addData("Drug Allergy",text, DbObservationValues.DRUG_ALLERGY.name)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpDrugAllergies)
            addData("Drug Allergy",text, DbObservationValues.DRUG_ALLERGY.name)
        }
        if (rootView.linearOtherNonDrugAllergy.visibility == View.VISIBLE){
            val text = rootView.etDrugOtherAllergies.text.toString()
            addData("Other non drug allergies",text, DbObservationValues.DRUG_ALLERGY.name)
        }
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Drug Allergies", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        if (isErrorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentFamily())
            ft.addToBackStack(null)
            ft.commit()

        }else{

            formatter.validate(isErrorList, requireContext())

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