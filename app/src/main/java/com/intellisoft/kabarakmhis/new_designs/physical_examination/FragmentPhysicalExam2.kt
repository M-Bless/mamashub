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
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.*
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.navigation
import kotlinx.android.synthetic.main.fragment_physical_exam_2.view.radioGrpExternalExam
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentPhysicalExam2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel


    private lateinit var rootView: View
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_physical_exam_2, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]
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
        rootView.radioGrpFGM.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearFGM, true)
                } else {
                    changeVisibility(rootView.linearFGM, false)
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

            addData("Inspection Done",inspectionDoneValue, DbObservationValues.ABDOMINAL_INSPECTION.name)

            if(rootView.linearInspection.visibility == View.VISIBLE){

                val inspectionValue = rootView.etAbnomality.text.toString()
                if(!TextUtils.isEmpty(inspectionValue)){

                    Log.e("**** ", "saved inspect value : $inspectionValue")
                    addData("Specify the inspection done",inspectionValue , DbObservationValues.SPECIFY_ABDOMINAL_INSPECTION.name)
                }else{
                    errorList.add("If yes on an inspection, please specify")
                }

            }

        }else{
            errorList.add("Please make a selection on Inspection")
        }

        val palpationDoneValue = formatter.getRadioText(rootView.radioGrpPalpation)
        if (palpationDoneValue != ""){

            addData("Palpation Done",palpationDoneValue, DbObservationValues.ABDOMINAL_PALPATION.name)

            if(rootView.linearPalp.visibility == View.VISIBLE){

                val palpationValue = rootView.etPalpation.text.toString()
                if(!TextUtils.isEmpty(palpationValue)){
                    addData("Specify the palpation that was done",palpationValue, DbObservationValues.SPECIFY_ABDOMINAL_PALPATION.name)
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

            if (auscultationDoneValue == "Yes"){
                val auscultationValue = rootView.etAuscalation.text.toString()
                if(!TextUtils.isEmpty(auscultationValue)){
                    addData("Specify the Auscultation done",auscultationValue, DbObservationValues.SPECIFY_ABDOMINAL_AUSCALATION.name)
                }else{
                    errorList.add("If yes on Auscultation, please specify")
                }
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
                    addData("If yes, specify the Inspection", text, DbObservationValues.SPECIFY_EXTERNAL_INSPECTION.name)
                }else{
                    errorList.add("If yes on an external inspection, please specify")
                }
            }
        }else{
            errorList.add("Please make a selection of Inspection under External genitalia exam")
        }

        val externalPalpationValue = formatter.getRadioText(rootView.radioGrpExternalPalpation)
        if (externalPalpationValue != ""){
            addData("Palpation Done",externalPalpationValue, DbObservationValues.EXTERNAL_PALPATION.name)
            if(rootView.linearExternalPalp.visibility == View.VISIBLE){
                val text = rootView.etExternalPalpation.text.toString()
                if(!TextUtils.isEmpty(text)){
                    addData("If yes, specify the palpation done",text, DbObservationValues.SPECIFY_EXTERNAL_PALPATION.name)
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

            if (externalDischargeValue == "Yes"){
                val dischargeValue = rootView.etDischarge.text.toString()
                if(!TextUtils.isEmpty(dischargeValue)){
                    addData("If yes, specify the discharge done",dischargeValue, DbObservationValues.SPECIFY_EXTERNAL_DISCHARGE.name)
                }else{
                    errorList.add("If yes on discharge, please specify")
                }
            }

        }else{
            errorList.add("Please make a selection on Discharge under External genitalia exam")
        }

        val genitalUlcerValue = formatter.getRadioText(rootView.radioGrpGenital)
        if (genitalUlcerValue != "") {
            addData("Genital Ulcer Present", genitalUlcerValue, DbObservationValues.EXTERNAL_GENITAL_ULCER.name)
            if (rootView.linearGenital.visibility == View.VISIBLE) {
                val text = rootView.etGenital.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("If yes, specify the genital ulcer present", text, DbObservationValues.SPECIFY_EXTERNAL_GENITAL_ULCER.name)
                } else {
                    errorList.add("If yes on genital ulcer, please specify")
                }
            }
        } else {
            errorList.add("Please make a selection on Genital ulcer")
        }

        val fgmValue = formatter.getRadioText(rootView.radioGrpFGM)
        if (fgmValue != ""){

            addData("FGM Done", fgmValue, DbObservationValues.EXTERNAL_FGM.name)
            if (rootView.linearFGM.visibility == View.VISIBLE){

                val fgmList = ArrayList<String>()

                if (rootView.checkboxScarring.isChecked) fgmList.add("Scarring")
                if (rootView.checkboxDyspaneuria.isChecked) fgmList.add("Dyspareunia")
                if (rootView.checkboxKeloids.isChecked) fgmList.add("Keloids")
                if (rootView.checkboxUTI.isChecked) fgmList.add("UTI")

                if (fgmList.isNotEmpty()){
                    addData("FGM Complications ", fgmList.joinToString(","),
                        DbObservationValues.COMPLICATIONS_EXTERNAL_FGM.name)
                }

            }

        }else{
            errorList.add("FGM has not been selected")
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

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.PHYSICAL_EXAMINATION.name)

                if (encounterId != null){

                    val abdominalExamInspection = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABDOMINAL_INSPECTION.name), encounterId)

                    val specifyAbdominalExam = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_ABDOMINAL_INSPECTION.name), encounterId)

                    val abdominalExamPalpation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABDOMINAL_PALPATION.name), encounterId)

                    val specifyAbdominalExamPalpation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_ABDOMINAL_PALPATION.name), encounterId)

                    val abdominalExamAuscalation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABDOMINAL_AUSCALATION.name), encounterId)

                    val specifyAbdominalExamAuscalation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_ABDOMINAL_AUSCALATION.name), encounterId)

                    val externalGenitaliaExamInspection = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXTERNAL_INSPECTION.name), encounterId)

                    val externalGenitaliaExamInspectionResults = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_EXTERNAL_INSPECTION.name), encounterId)

                    val externalGenitaliaExamPalpation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXTERNAL_PALPATION.name), encounterId)

                    val externalGenitaliaExamPalpationResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_EXTERNAL_PALPATION.name), encounterId)

                    val externalGenitaliaExamDischarge = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXTERNAL_DISCHARGE.name), encounterId)

                    val externalGenitaliaExamDischargeResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_EXTERNAL_DISCHARGE.name), encounterId)

                    val externalGenitaliaExamGenitalUlcer = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXTERNAL_GENITAL_ULCER.name), encounterId)

                    val externalGenitaliaExamGenitalUlcerResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFY_EXTERNAL_GENITAL_ULCER.name), encounterId)

                    val fgmDone = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXTERNAL_FGM.name), encounterId)

                    val fgmDoneResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPLICATIONS_EXTERNAL_FGM.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (abdominalExamInspection.isNotEmpty()){
                            val value = abdominalExamInspection[0].value
                            if (value.contains("Yes")) rootView.radioGrpAbdominalExam.check(R.id.radioYesInspection)
                            if (value.contains("No")) rootView.radioGrpAbdominalExam.check(R.id.radioNoInspection)
                        }
                        if (specifyAbdominalExam.isNotEmpty()){
                            val value = specifyAbdominalExam[0].value
                            rootView.etAbnomality.setText(value)
                        }

                        Log.e("------- ", "------")
                        println(specifyAbdominalExam)

                        if (abdominalExamPalpation.isNotEmpty()){
                            val value = abdominalExamPalpation[0].value
                            if (value.contains("Yes")) rootView.radioGrpPalpation.check(R.id.radioYesPalpation)
                            if (value.contains("No")) rootView.radioGrpPalpation.check(R.id.radioNoPalpation)
                        }
                        if (specifyAbdominalExamPalpation.isNotEmpty()){
                            val value = specifyAbdominalExamPalpation[0].value
                            rootView.etPalpation.setText(value)
                        }

                        if (abdominalExamAuscalation.isNotEmpty()){
                            val value = abdominalExamAuscalation[0].value
                            if (value.contains("Yes")) rootView.radioGrpAuscalation.check(R.id.radioYesAuscalation)
                            if (value.contains("No")) rootView.radioGrpAuscalation.check(R.id.radioNoAuscalation)
                        }
                        if (specifyAbdominalExamAuscalation.isNotEmpty()){
                            val value = specifyAbdominalExamAuscalation[0].value
                            rootView.etAuscalation.setText(value)
                        }

                        if (externalGenitaliaExamInspection.isNotEmpty()){
                            val value = externalGenitaliaExamInspection[0].value
                            if (value.contains("Yes")) rootView.radioGrpExternalExam.check(R.id.radioYesExternalInspection)
                            if (value.contains("No")) rootView.radioGrpExternalExam.check(R.id.radioNoExternalInspection)
                        }
                        if (externalGenitaliaExamInspectionResults.isNotEmpty()){
                            val value = externalGenitaliaExamInspectionResults[0].value
                            rootView.etExternalAbnomality.setText(value)
                        }

                        if (externalGenitaliaExamPalpation.isNotEmpty()){
                            val value = externalGenitaliaExamPalpation[0].value
                            if (value.contains("Yes")) rootView.radioGrpExternalPalpation.check(R.id.radioYesExternalPalpation)
                            if (value.contains("No")) rootView.radioGrpExternalPalpation.check(R.id.radioNoExternalPalpation)
                        }
                        if (externalGenitaliaExamPalpationResult.isNotEmpty()){
                            val value = externalGenitaliaExamPalpationResult[0].value
                            rootView.etExternalPalpation.setText(value)
                        }

                        if (externalGenitaliaExamDischarge.isNotEmpty()){
                            val value = externalGenitaliaExamDischarge[0].value
                            if (value.contains("Yes")) rootView.radioGrpDischarge.check(R.id.radioYesDischarge)
                            if (value.contains("No")) rootView.radioGrpDischarge.check(R.id.radioNoDischarge)
                        }
                        if (externalGenitaliaExamDischargeResult.isNotEmpty()){
                            val value = externalGenitaliaExamDischargeResult[0].value
                            rootView.etDischarge.setText(value)
                        }

                        if (externalGenitaliaExamGenitalUlcer.isNotEmpty()){
                            val value = externalGenitaliaExamGenitalUlcer[0].value
                            if (value.contains("Yes")) rootView.radioGrpGenital.check(R.id.radioYesGenital)
                            if (value.contains("No")) rootView.radioGrpGenital.check(R.id.radioNoGenital)
                        }
                        if (externalGenitaliaExamGenitalUlcerResult.isNotEmpty()){
                            val value = externalGenitaliaExamGenitalUlcerResult[0].value
                            rootView.etGenital.setText(value)
                        }

                        if (fgmDone.isNotEmpty()){
                            val value = fgmDone[0].value
                            if (value.contains("Yes")) rootView.radioGrpFGM.check(R.id.radioYesFGM)
                            if (value.contains("No")) rootView.radioGrpFGM.check(R.id.radioNoFGM)
                        }
                        if (fgmDoneResult.isNotEmpty()){

                            val checkBoxList = mutableListOf<CheckBox>()
                            checkBoxList.addAll(listOf(
                                rootView.checkboxScarring,
                                rootView.checkboxDyspaneuria,
                                rootView.checkboxKeloids,
                                rootView.checkboxUTI,
                            ))

                            val value = fgmDoneResult[0].value
                            val valueList = formatter.stringToWords(value)
                            valueList.forEach {

                                checkBoxList.forEach { checkBox ->
                                    if (checkBox.text.toString() == it){
                                        checkBox.isChecked = true
                                    }
                                }

                            }
                        }




                    }




                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}