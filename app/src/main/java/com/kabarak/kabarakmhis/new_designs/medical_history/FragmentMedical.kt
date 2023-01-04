package com.kabarak.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_medical.*
import kotlinx.android.synthetic.main.fragment_medical.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


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

                    val diabetes = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.DIABETES.name), encounterId)
                    val hypertension = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.HYPERTENSION.name), encounterId)
                    val otherConditions = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.OTHER_CONDITIONS.name), encounterId)
                    val specifyOtherCondition = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.OTHER_CONDITIONS_SPECIFY.name), encounterId)
                    val bloodTransfusion = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.BLOOD_TRANSFUSION.name), encounterId)
                    val bloodTransfusionReaction = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.BLOOD_TRANSFUSION_REACTION.name), encounterId)
                    val specifyBloodTransfusionReaction = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.SPECIFY_BLOOD_TRANSFUSION_REACTION.name), encounterId)
                    val tuberculosis = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.TUBERCULOSIS.name), encounterId)

                    val drugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.DRUG_ALLERGY.name), encounterId)
                    val specifyDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.SPECIFIC_DRUG_ALLERGY.name), encounterId)
                    val nonDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.NON_DRUG_ALLERGY.name), encounterId)
                    val specifyNonDrugAllergy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.SPECIFIC_NON_DRUG_ALLERGY.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (diabetes.isNotEmpty()){

                            val value = diabetes[0].value
                            if (value.contains("Yes")) rootView.radioGrpDiabetes.check(R.id.radioYesDiabetes)
                            if (value.contains("No")) rootView.radioGrpDiabetes.check(R.id.radioNoDiabetes)

                        }
                        if (hypertension.isNotEmpty()){

                            val value = hypertension[0].value
                            if (value.contains("Yes")) rootView.radioGrpHypertension.check(R.id.radioYesHypertesnion)
                            if (value.contains("No")) rootView.radioGrpHypertension.check(R.id.radioNoHypertesnion)

                        }
                        if (otherConditions.isNotEmpty()){

                            val value = otherConditions[0].value
                            if (value.contains("Yes")) rootView.radioGrpOtherCondition.check(R.id.radioYesOtherCondition)
                            if (value.contains("No")) rootView.radioGrpOtherCondition.check(R.id.radioNoOtherCondition)

                        }

                        if (specifyOtherCondition.isNotEmpty()){

                            val checkBoxList = mutableListOf<CheckBox>()
                            checkBoxList.addAll(
                                listOf(
                                rootView.checkBoxEpilepsy,
                                rootView.checkBoxMalariaPregnancy,
                                rootView.checkBoxOthers))

                            val value = specifyOtherCondition[0].value
                            val valueList = formatter.stringToWords(value)

                            for (element in valueList){
                                for (j in 0 until checkBoxList.size){
                                    if (element == checkBoxList[j].text.toString()){
                                        checkBoxList[j].isChecked = true
                                    }
                                }
                            }

                            if (valueList.size > 2){
                                rootView.checkBoxOthers.isChecked = true
                                valueList.forEach {
                                    if (!it.contains("Epilepsy") && !it.contains(" Malaria in pregnancy")){
                                        rootView.etOtherConditions.setText(it)
                                    }
                                }


                            }



                        }

                        if (bloodTransfusion.isNotEmpty()){

                            val value = bloodTransfusion[0].value
                            if (value.contains("Yes")) rootView.radioGrpBloodTransfusion.check(R.id.radioYesBloodTransfusion)
                            if (value.contains("No")) rootView.radioGrpBloodTransfusion.check(R.id.radioNoBloodTransfusion)

                        }
                        if (bloodTransfusionReaction.isNotEmpty()){

                            val value = bloodTransfusionReaction[0].value
                            if (value.contains("Yes")) rootView.radioGrpTransfusion.check(R.id.radioYesReaction)
                            if (value.contains("No")) rootView.radioGrpTransfusion.check(R.id.radioNoReaction)

                        }
                        if (specifyBloodTransfusionReaction.isNotEmpty()){

                            val value = specifyBloodTransfusionReaction[0].value
                            rootView.etBloodTransReaction.setText(value)
                        }

                        if (tuberculosis.isNotEmpty()){

                            val value = tuberculosis[0].value
                            if (value.contains("Yes")) rootView.radioGrpTb.check(R.id.radioYesTb)
                            if (value.contains("No")) rootView.radioGrpTb.check(R.id.radioNoBloodTb)

                        }

                        if (drugAllergy.isNotEmpty()){
                            val value = drugAllergy[0].value
                            if (value.contains("Yes")) rootView.radioGrpDrugAllergies.check(R.id.radioYesAllergies)
                            if (value.contains("No")) rootView.radioGrpDrugAllergies.check(R.id.radioNoAllergies)
                        }
                        if (specifyDrugAllergy.isNotEmpty()){
                            val value = specifyDrugAllergy[0].value
                            rootView.etDrugAllergies.setText(value)
                        }

                        if (nonDrugAllergy.isNotEmpty()){
                            val value = drugAllergy[0].value
                            if (value.contains("Yes")) rootView.radioGrpOtherAllergy.check(R.id.radioYesOtherAllergy)
                            if (value.contains("No")) rootView.radioGrpOtherAllergy.check(R.id.radioNoOtherAllergy)
                        }
                        if (specifyNonDrugAllergy.isNotEmpty()){
                            val value = specifyNonDrugAllergy[0].value
                            rootView.etDrugOtherAllergies.setText(value)
                        }

                    }






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

                addData("Blood Transfusion Reaction",bloodGrpReaction, DbObservationValues.BLOOD_TRANSFUSION_REACTION.name)

                if (rootView.linearBloodReaction.visibility == View.VISIBLE){

                    val bloodReaction = etBloodTransReaction.text.toString()
                    if (!TextUtils.isEmpty(bloodReaction)){
                        addData("What was the blood reaction",bloodReaction, DbObservationValues.SPECIFY_BLOOD_TRANSFUSION_REACTION.name)
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
                        "What other non drug allergies do you have",
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

        CoroutineScope(Dispatchers.Main).launch {
            if (showLinear){
                linearLayout.visibility = View.VISIBLE
            }else{
                linearLayout.visibility = View.GONE
            }
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