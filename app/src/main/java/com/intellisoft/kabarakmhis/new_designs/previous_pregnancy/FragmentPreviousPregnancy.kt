package com.intellisoft.kabarakmhis.new_designs.previous_pregnancy

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
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.view.*
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentPreviousPregnancy : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var pregnancyOrderList = arrayOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th")
    private var spinnerPregnancyValue  = pregnancyOrderList[0]

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

        rootView = inflater.inflate(R.layout.fragment_prev_pregnancy, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        initSpinner()

        rootView.radioGrpPurperium.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearPurperium, true)
                } else {
                    changeVisibility(rootView.linearPurperium, false)
                }
            }
        }


        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }
    




    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val year = rootView.etYear.text.toString()
        val ancTime = rootView.etVisitTime.text.toString()
        val birthPlace = rootView.etPlaceOfChildBirth.text.toString()
        val gestation = rootView.etGestation.text.toString()
        val duration = rootView.etDuration.text.toString()

        val babyWeight = rootView.etBabyWeight.text.toString()

        if (!TextUtils.isEmpty(year) && !TextUtils.isEmpty(ancTime) && !TextUtils.isEmpty(birthPlace) &&
            !TextUtils.isEmpty(gestation) && !TextUtils.isEmpty(duration) && !TextUtils.isEmpty(babyWeight)){


            addData("Pregnancy Order",spinnerPregnancyValue, DbObservationValues.PREGNANCY_ORDER.name)
            addData("Year",year, DbObservationValues.YEAR.name)
            addData("ANC Time",ancTime, DbObservationValues.ANC_NO.name)
            addData("Child Birth Place",birthPlace, DbObservationValues.CHILDBIRTH_PLACE.name)
            addData("Gestation",gestation, DbObservationValues.GESTATION.name)
            addData("Duration",duration, DbObservationValues.LABOUR_DURATION.name)

            val deliveryMode = formatter.getRadioText(rootView.deliveryMode)
            if (deliveryMode != ""){
                addData("Delivery Mode",deliveryMode, DbObservationValues.DELIVERY_MODE.name)
            }else{
                errorList.add("Delivery Mode is required.")
            }

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_PREGNANCY_DETAILS.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }

            observationList.clear()

            addData("Baby Weight (grams)",babyWeight, DbObservationValues.BABY_WEIGHT.name)

            val radioGrpBabySex = formatter.getRadioText(rootView.radioGrpBabySex)
            if (radioGrpBabySex != ""){
                addData("Baby's Sex",radioGrpBabySex, DbObservationValues.BABY_SEX.name)
            }else{
                errorList.add("The sex of the baby is required")
            }

            val radioGrpOutcome = formatter.getRadioText(rootView.radioGrpOutcome)
            if (radioGrpOutcome != ""){
                addData("Outcome",radioGrpOutcome, DbObservationValues.BABY_OUTCOME.name)
            }else{
                errorList.add("Outcome is not selected")
            }

            val radioGrpPurperium = formatter.getRadioText(rootView.radioGrpPurperium)
            if (radioGrpPurperium != ""){
                addData("Purperium",radioGrpPurperium, DbObservationValues.BABY_PURPERIUM.name)

                if (rootView.linearPurperium.visibility == View.VISIBLE){
                    val text = rootView.etAbnormal.text.toString()
                    if (!TextUtils.isEmpty(text)){
                        addData("If Purperium is Abnormal, ",text, DbObservationValues.ABNORMAL_BABY_PURPERIUM.name)
                    }else{
                        errorList.add("If purperium is abnormal, please enter abnormal details")
                    }

                }

            }else{
                errorList.add("Please select purperium")
            }

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.B_BABY_DETAILS.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

        }else{

            if (TextUtils.isEmpty(year)) errorList.add("Please provide an year")
            if (TextUtils.isEmpty(ancTime)) errorList.add("Please provide an ANC Time")
            if (TextUtils.isEmpty(birthPlace)) errorList.add("Please provide a Birth Place")
            if (TextUtils.isEmpty(gestation)) errorList.add("Please provide a Gestation")
            if (TextUtils.isEmpty(duration)) errorList.add("Please provide a Duration")
            if (TextUtils.isEmpty(babyWeight)) errorList.add("Please provide Baby Weight")
        }

        if (errorList.size == 0) {

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PREVIOUS_PREGNANCY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
                DbResourceViews.PREVIOUS_PREGNANCY.name))
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pregnancyOrderList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerPregOrder!!.adapter = kinRshp

        rootView.spinnerPregOrder.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRshp -> { spinnerPregnancyValue = rootView.spinnerPregOrder.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.PREVIOUS_PREGNANCY.name)

                if (encounterId != null){

                    val pregnancyOrder = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PREGNANCY_ORDER.name), encounterId)

                    val year = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.YEAR.name), encounterId)

                    val ancNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ANC_NO.name), encounterId)

                    val birthPlace = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.CHILDBIRTH_PLACE.name), encounterId)

                    val gestation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.GESTATION.name), encounterId)

                    val duration = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.LABOUR_DURATION.name), encounterId)

                    val deliveryMode = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DELIVERY_MODE.name), encounterId)

                    val babyWeight = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BABY_WEIGHT.name), encounterId)

                    val babySex = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BABY_SEX.name), encounterId)

                    val babyOutcome = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BABY_OUTCOME.name), encounterId)

                    val babyPurperium = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BABY_PURPERIUM.name), encounterId)

                    val abnormalPurperium = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABNORMAL_BABY_PURPERIUM.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        Log.e("------", "deliveryMode: $deliveryMode")
                        Log.e("------", "babySex: $babySex")
                        Log.e("------", "babyOutcome: $babyOutcome")
                        Log.e("------", "babyPurperium: $babyPurperium")

                        if (pregnancyOrder.isNotEmpty()){
                            val value = pregnancyOrder[0].value
                            rootView.spinnerPregOrder.setSelection(pregnancyOrderList.indexOf(value))
                        }
                        if (year.isNotEmpty()){
                            rootView.etYear.setText(year[0].value)
                        }
                        if (ancNo.isNotEmpty()){
                            rootView.etVisitTime.setText(ancNo[0].value)
                        }
                        if (birthPlace.isNotEmpty()){
                            rootView.etPlaceOfChildBirth.setText(birthPlace[0].value)
                        }
                        if (gestation.isNotEmpty()){
                            rootView.etGestation.setText(gestation[0].value)
                        }
                        if (duration.isNotEmpty()){
                            rootView.etDuration.setText(duration[0].value)
                        }
                        if (deliveryMode.isNotEmpty()){
                            val value = deliveryMode[0].value
                            if (value.contains("Vaginal Deliver", ignoreCase = true)) rootView.deliveryMode.check(R.id.radioYesVaginal)
                            if (value.contains("Assisted Vaginal Delivery", ignoreCase = true)) rootView.deliveryMode.check(R.id.radioYesAssistedVaginal)
                            if (value.contains("Cesarean Section", ignoreCase = true)) rootView.deliveryMode.check(R.id.radioNoCs)
                        }
                        if (babyWeight.isNotEmpty()){
                            rootView.etBabyWeight.setText(babyWeight[0].value)
                        }
                        if (babySex.isNotEmpty()) {
                            val value = babySex[0].value
                            if (value.contains("Male", ignoreCase = true)) rootView.radioGrpBabySex.check(R.id.radioMaleBabySex)
                            if (value.contains("Female", ignoreCase = true)) rootView.radioGrpBabySex.check(R.id.radioFemaleBabySex)
                        }
                        if (babyOutcome.isNotEmpty()){
                            val value = babyOutcome[0].value
                            if (value.contains("Dead", ignoreCase = true)) rootView.radioGrpOutcome.check(R.id.radioDeadOutcome)
                            if (value.contains("Alive", ignoreCase = true)) rootView.radioGrpOutcome.check(R.id.radioAliveOutcome)
                        }
                        if (babyPurperium.isNotEmpty()){
                            val value = babyPurperium[0].value
                            if (value.contains("Normal", ignoreCase = true)) rootView.radioGrpPurperium.check(R.id.radioNormal)
                            if (value.contains("Abnormal", ignoreCase = true)) rootView.radioGrpPurperium.check(R.id.radioAbnormal)
                        }
                        if (abnormalPurperium.isNotEmpty()){
                            val value = abnormalPurperium[0].value
                            rootView.etAbnormal.setText(value)
                        }



                    }

                }


            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}