package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.os.Build
import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_medical.view.*
import kotlinx.android.synthetic.main.fragment_medical.view.btnNext

class FragmentMedical : Fragment(){

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_medical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.btnNext.setOnClickListener {

            saveData()
        }

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

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()


        return rootView
    }

    private fun saveData() {

        if (rootView.linearBloodReaction.visibility == View.VISIBLE){
            val text = rootView.etClientName.text.toString()
            addData("Blood Transfusion Reaction",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpTransfusion)
            addData("Blood Transfusion Reaction",text)
        }
        if (rootView.linearDrug.visibility == View.VISIBLE){
            val text = rootView.etDrugAllergies.text.toString()
            addData("Drug Allergy",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpDrugAllergies)
            addData("Drug Allergy",text)
        }
        if (rootView.linearOtherNonDrugAllergy.visibility == View.VISIBLE){
            val text = rootView.etDrugOtherAllergies.text.toString()
            addData("Other non drug allergies",text)
        }

        val diabetes = formatter.getRadioText(rootView.radioGrpDiabetes)
        val hypertension = formatter.getRadioText(rootView.radioGrpDiabetes)
        val tb = formatter.getRadioText(rootView.radioGrpDiabetes)
        val bloodTransfusion = formatter.getRadioText(rootView.radioGrpBloodTransfer)

        if (diabetes != "" && hypertension != "" && tb != "" && bloodTransfusion != ""){
            addData("Diabetes",diabetes)
            addData("Hypertension",hypertension)
            addData("Tuberculosis",tb)
            addData("Blood Transfusion",bloodTransfusion)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Medical History and Allergies", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentFamily())
        ft.addToBackStack(null)
        ft.commit()

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