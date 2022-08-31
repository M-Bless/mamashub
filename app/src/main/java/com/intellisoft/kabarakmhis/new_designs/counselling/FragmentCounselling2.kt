package com.intellisoft.kabarakmhis.new_designs.counselling

import android.app.Application
import android.content.Intent
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
import kotlinx.android.synthetic.main.fragment_couselling2.view.*
import kotlinx.android.synthetic.main.fragment_couselling2.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentCounselling2 : Fragment() {

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

        rootView = inflater.inflate(R.layout.fragment_couselling2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

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

        val dbDataList = ArrayList<DbDataList>()

        val text1 = formatter.getRadioText(rootView.radioGrpInfantFeeding)
        val text2 = formatter.getRadioText(rootView.radioGrpColostrum)

        val paleMother = formatter.getRadioText(rootView.radioGrpMotherPale)
        val headAche = formatter.getRadioText(rootView.radioGrpHeadAche)
        val vaginalBleeding = formatter.getRadioText(rootView.radioGrpVaginalBleeding)
        val abdominalPain = formatter.getRadioText(rootView.radioGrpAbdominalPain)
        val babyMovement = formatter.getRadioText(rootView.radioGrpBabyMovement)
        val convulsions = formatter.getRadioText(rootView.radioGrpConvulsions)
        val waterBreaking = formatter.getRadioText(rootView.radioGrpWaterBreaking)
        val swollenFace = formatter.getRadioText(rootView.radioGrpSwollenFace)
        val motherFever = formatter.getRadioText(rootView.radioGrpMotherFever)

        if (text1 != "" && text2 != "" && paleMother != "" && headAche != "" && vaginalBleeding != ""
            && abdominalPain != "" && babyMovement != "" && convulsions != "" && waterBreaking != ""
            && swollenFace != "" && motherFever != "") {

            addData("Was infant feeding counselling done",text1, DbObservationValues.INFANT_FEEDING.name)
            addData("Was counselling on exclusive breastfeeding and benefits of colostrum done",text2, DbObservationValues.EXCLUSIVE_BREASTFEEDING.name)
            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.C_INFANT_COUNSELLING.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            addData("Was the mother pale:",paleMother, DbObservationValues.MOTHER_PALE.name)
            addData("Does the mother have severe headache:",headAche, DbObservationValues.SEVERE_HEADACHE.name)
            addData("Did the mother have vaginal bleeding:",vaginalBleeding, DbObservationValues.VAGINAL_BLEEDING.name)
            addData("Did the mother have abdominal pain:",abdominalPain, DbObservationValues.ABDOMINAL_PAIN.name)
            addData("Did the mother have reduced or no movement of the unborn baby:",babyMovement, DbObservationValues.REDUCED_MOVEMENT.name)
            addData("Did the mother have convulsions/fits:",convulsions, DbObservationValues.MOTHER_FITS.name)
            addData("Was the mother's water breaking:",waterBreaking, DbObservationValues.WATER_BREAKING.name)
            addData("Did the mother have swollen face and hands:",swollenFace, DbObservationValues.SWOLLEN_FACE.name)
            addData("Did the mother have a fever:",motherFever, DbObservationValues.FEVER.name)
            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.D_PREGNANCY_COUNSELLING_DETAILS.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.COUNSELLING.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.COUNSELLING.name))
            ft.addToBackStack(null)
            ft.commit()

        } else {
            Toast.makeText(requireContext(), "Please make a selection on all the fields", Toast.LENGTH_SHORT).show()
        }

    }



    private fun addData(key: String, value: String, codeLabel:String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
        }

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

                val encounterId = formatter.retrieveSharedPreference(
                    requireContext(),
                    DbResourceViews.COUNSELLING.name
                )
                if (encounterId != null) {

                    val infantFeeding = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.INFANT_FEEDING.name), encounterId)
                    val breast = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EXCLUSIVE_BREASTFEEDING.name), encounterId)
                    val motherPale = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.MOTHER_PALE.name), encounterId)
                    val severeHeadache = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SEVERE_HEADACHE.name), encounterId)
                    val vaginalBleeding = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.VAGINAL_BLEEDING.name), encounterId)
                    val abdominalPain = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABDOMINAL_PAIN.name), encounterId)
                    val reducedMovement = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REDUCED_MOVEMENT.name), encounterId)
                    val motherFits = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.MOTHER_FITS.name), encounterId)
                    val waterBreaking = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.WATER_BREAKING.name), encounterId)
                    val swollenFace = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SWOLLEN_FACE.name), encounterId)
                    val fever = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FEVER.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {
                        if (infantFeeding.isNotEmpty()){
                            val value = infantFeeding[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpInfantFeeding.check(R.id.radioYesInfantFeeding)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpInfantFeeding.check(R.id.radioNoInfantFeeding)
                        }
                        if (breast.isNotEmpty()){
                            val value = breast[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpColostrum.check(R.id.radioYesColostrum)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpColostrum.check(R.id.radioNoColostrum)
                        }
                        if (motherPale.isNotEmpty()){
                            val value = motherPale[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpMotherPale.check(R.id.radioYesMotherPale)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpMotherPale.check(R.id.radioNoMotherPale)
                        }
                        if (severeHeadache.isNotEmpty()){
                            val value = severeHeadache[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHeadAche.check(R.id.radioYesHeadAche)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHeadAche.check(R.id.radioNoHeadAche)
                        }
                        if (vaginalBleeding.isNotEmpty()){
                            val value = vaginalBleeding[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpVaginalBleeding.check(R.id.radioYesVaginalBleeding)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpVaginalBleeding.check(R.id.radioNoVaginalBleeding)
                        }
                        if (abdominalPain.isNotEmpty()){
                            val value = abdominalPain[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpAbdominalPain.check(R.id.radioYesAbdominalPain)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpAbdominalPain.check(R.id.radioNoAbdominalPain)
                        }
                        if (reducedMovement.isNotEmpty()){
                            val value = reducedMovement[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpBabyMovement.check(R.id.radioYesBabyMovement)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpBabyMovement.check(R.id.radioNoBabyMovement)
                        }
                        if (motherFits.isNotEmpty()){
                            val value = motherFits[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpConvulsions.check(R.id.radioYesConvulsions)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpConvulsions.check(R.id.radioNoConvulsions)
                        }
                        if (waterBreaking.isNotEmpty()){
                            val value = waterBreaking[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpWaterBreaking.check(R.id.radioYesWaterBreaking)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpWaterBreaking.check(R.id.radioNoWaterBreaking)
                        }
                        if (swollenFace.isNotEmpty()){
                            val value = swollenFace[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpSwollenFace.check(R.id.radioYesSwollenFace)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpSwollenFace.check(R.id.radioNoWaterSwollenFace)
                        }
                        if (fever.isNotEmpty()){
                            val value = fever[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpMotherFever.check(R.id.radioYesMotherFever)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpMotherFever.check(R.id.radioNoMotherFever)
                        }
                    }



                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}