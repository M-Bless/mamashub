package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_medical.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentMedical : Fragment(){

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_medical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        rootView.radioGrpBloodTransfusion.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearBloodTransfusionReaction, true)
                } else {
                    changeVisibility(rootView.linearBloodTransfusionReaction, false)
                }

            }
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

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(
                    requireContext(),
                    DbResourceViews.MEDICAL_HISTORY.name
                )

                if (encounterId != null){

                    val diabetes = patientDetailsViewModel.getObservationsPerCodeFromEncounter("405751000", encounterId)
                    val hypertension = patientDetailsViewModel.getObservationsPerCodeFromEncounter("38341003", encounterId)
                    val otherConditions = patientDetailsViewModel.getObservationsPerCodeFromEncounter("7867677", encounterId)
                    val specifyOtherCondition = patientDetailsViewModel.getObservationsPerCodeFromEncounter("7867677-S", encounterId)
                    val bloodTransfusion = patientDetailsViewModel.getObservationsPerCodeFromEncounter("116859006", encounterId)
                    val bloodTransfusionReaction = patientDetailsViewModel.getObservationsPerCodeFromEncounter("82545002", encounterId)
                    val tuberculosis = patientDetailsViewModel.getObservationsPerCodeFromEncounter("371569005", encounterId)

                    val drugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter("416098002", encounterId)
                    val specifyDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter("416098002-S", encounterId)
                    val nonDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter("609328004", encounterId)
                    val specifyNonDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter("609328004-S", encounterId)





                }

            }


        }catch (e: Exception){
            println(e)
        }


    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val isErrorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val diabetes = formatter.getRadioText(rootView.radioGrpDiabetes)
        val hypertension = formatter.getRadioText(rootView.radioGrpDiabetes)
        val tb = formatter.getRadioText(rootView.radioGrpDiabetes)
        val bloodTransfusion = formatter.getRadioText(rootView.radioGrpBloodTransfusion)

        if (diabetes != "" && hypertension != "" && tb != "" && bloodTransfusion != ""){
            addData("Diabetes",diabetes, DbObservationValues.DIABETES.name)
            addData("Hypertension",hypertension, DbObservationValues.HYPERTENSION.name)
            addData("Tuberculosis",tb, DbObservationValues.TUBERCULOSIS.name)
            addData("Blood Transfusion",bloodTransfusion, DbObservationValues.BLOOD_TRANSFUSION.name)

        }else{
            if (diabetes == "")isErrorList.add("Diabetes cannot be empty")
            if (hypertension == "")isErrorList.add("Hypertension cannot be empty")
            if (tb == "")isErrorList.add("Tuberculosis cannot be empty")
            if (bloodTransfusion == "")isErrorList.add("Blood transfusion cannot be empty")
        }

        if (rootView.linearBloodTransfusionReaction.visibility == View.VISIBLE){

            val bloodGrpReaction = formatter.getRadioText(rootView.radioGrpTransfusion)
            if (bloodGrpReaction != ""){

                if (rootView.linearBloodReaction.visibility == View.VISIBLE){
                    val bloodReaction = formatter.getRadioText(rootView.radioGrpDrugAllergies)
                    if (bloodReaction != ""){
                        addData("Blood Transfusion Reaction",bloodReaction, DbObservationValues.BLOOD_TRANSFUSION_REACTION.name)
                    }else{
                        isErrorList.add("Blood Transfusion Reaction cannot be empty")
                    }
                }

            }else{
                isErrorList.add("Blood transfusion reaction cannot be empty")
            }

        }

        val otherConditions = formatter.getRadioText(rootView.radioGrpOtherCondition)
        if (otherConditions != ""){

            addData("Other Medical Conditions",otherConditions, DbObservationValues.OTHER_CONDITIONS.name)

            if (rootView.layoutOtherCondition.visibility == View.VISIBLE){

                val otherConditionList = ArrayList<String>()

                if (rootView.checkBoxEpilepsy.isChecked)otherConditionList.add("Epilepsy")
                if (rootView.checkBoxMalariaPregnancy.isChecked)otherConditionList.add("Malaria in pregnancy")

                if (rootView.layoutOthers.visibility == View.VISIBLE){

                    val otherText = rootView.etOtherConditions.text.toString()
                    if (!TextUtils.isEmpty(otherText)){
                        otherConditionList.add(otherText)
                    }else{
                        isErrorList.add("You have selected other conditions but have not entered any other condition")
                    }
                }

                addData("Other Medical Conditions Information",
                    otherConditionList.joinToString(separator = ","), DbObservationValues.OTHER_CONDITIONS_SPECIFY.name)
            }

        }else{
            isErrorList.add("Other Conditions cannot be empty")
        }



        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.B_MEDICAL_HISTORY.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val drugAllergy = formatter.getRadioText(rootView.radioGrpDrugAllergies)
        if (drugAllergy != "") {
            addData("Drug Allergy", drugAllergy, DbObservationValues.DRUG_ALLERGY.name)

            if (rootView.linearDrug.visibility == View.VISIBLE){
                val text = rootView.etDrugAllergies.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("Specific Drug Allergy",text, DbObservationValues.SPECIFIC_DRUG_ALLERGY.name)
                }else{
                    isErrorList.add("You have selected drug allergy but have not entered any drug allergy")
                }
            }

        }else{
            isErrorList.add("Drug Allergy cannot be empty")
        }



        val otherDrugAllergy = formatter.getRadioText(rootView.radioGrpOtherAllergy)
        if (otherDrugAllergy != "") {

            addData("Other non drug allergies",otherDrugAllergy, DbObservationValues.NON_DRUG_ALLERGY.name)

            if (rootView.linearOtherNonDrugAllergy.visibility == View.VISIBLE){
                val text = rootView.etDrugOtherAllergies.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData(
                        "Other non drug allergies",
                        text,
                        DbObservationValues.SPECIFIC_NON_DRUG_ALLERGY.name
                    )
                }else{
                    isErrorList.add("You have selected other non drug allergy but have not entered any other non drug allergy")
                }

            }

        }else{
            isErrorList.add("Other Drug Allergy cannot be empty")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.C_DRUG_ALLERGIES.name, DbResourceType.Observation.name, label)
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

            formatter.showErrorDialog(isErrorList, requireContext())

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