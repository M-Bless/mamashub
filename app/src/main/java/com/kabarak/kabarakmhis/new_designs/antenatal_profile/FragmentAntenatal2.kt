package com.kabarak.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.fragment_antenatal2.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.navigation
import kotlinx.android.synthetic.main.fragment_birthplan2.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.*
import kotlin.collections.ArrayList


class FragmentAntenatal2 : Fragment() , AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    var artEligibilityList = arrayOf("","I", "II", "III", "IV")
    private var spinnerArtElligibilityValue  = artEligibilityList[0]

    var partnerHivList = arrayOf("","N", "U", "Kp")
    private var spinnerPartnerHivValue  = partnerHivList[0]

    var arvBeforeFirstVisitList = arrayOf("","Y", "N", "NA", "Revisit")
    private var spinnerBeforeFirstVisitValue  = arvBeforeFirstVisitList[0]

    var startedHaartList = arrayOf("","Y", "N", "NA", "Revisit")
    private var spinnerStartedHaartValue  = startedHaartList[0]

    var cotrimoxazoleList = arrayOf("","Y", "B")
    private var spinnerCotrimoxazoleValue  = cotrimoxazoleList[0]

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal2, container, false)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "P" || checkedBtn == "Kp") {
                    formatter.saveSharedPreference(requireContext(), "hivStatus", "true")
                    changeVisibility(rootView.linearARTEligibility, true)
                    changeVisibility(rootView.linearMaternalHAART, true)
                } else {
                    formatter.saveSharedPreference(requireContext(), "hivStatus", "false")
                    changeVisibility(rootView.linearARTEligibility, false)
                    changeVisibility(rootView.linearMaternalHAART, false)
                }
            }
        }
        rootView.radioGrpTb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTB, true)
                } else {
                    changeVisibility(rootView.linearTB, false)

                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpTbResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Positive") {
                    changeVisibility(rootView.linearPositive, true)
                    changeVisibility(rootView.linearNegative, false)
                } else if (checkedBtn == "Negative")  {
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, true)
                }else{
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpUltrasound1.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearDate, true)
                } else {
                    changeVisibility(rootView.linearDate, false)
                }
            }
        }
        rootView.radioGrpUltrasound2.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linear2ndUltra, true)
                } else {
                    changeVisibility(rootView.linear2ndUltra, false)
                }
            }
        }

        rootView.radioGrpMultipleBaby.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearMultipleBaby, true)
                } else {
                    changeVisibility(rootView.linearMultipleBaby, false)
                }
            }
        }

        rootView.tvIPTDateGiven.setOnClickListener { onCreateDialog(999) }
        rootView.tvIPTNextVisit.setOnClickListener { onCreateDialog(998) }
        rootView.tvUltraSound1.setOnClickListener { onCreateDialog(997) }
        rootView.tvUltraSound2.setOnClickListener { onCreateDialog(996) }
        rootView.tvLastCCC.setOnClickListener { onCreateDialog(995) }

        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun initSpinner() {

        val artEligibility = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, artEligibilityList)
        val partnerHivStatus = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partnerHivList)
        val onArvBeforeFirstAnc = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arvBeforeFirstVisitList)
        val startedHaartAnc = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, startedHaartList)
        val cotrimoxazole = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cotrimoxazoleList)

        artEligibility.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        partnerHivStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        onArvBeforeFirstAnc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        startedHaartAnc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cotrimoxazole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rootView.spinnerEligibility!!.adapter = artEligibility
        rootView.spinnerPartnerHIVStatus!!.adapter = partnerHivStatus
        rootView.spinnerOnARVBeforeANCVisit!!.adapter = onArvBeforeFirstAnc
        rootView.spinnerStartedHaartInANC!!.adapter = startedHaartAnc
        rootView.spinnerCotrimoxazole!!.adapter = cotrimoxazole

        rootView.spinnerEligibility.onItemSelectedListener = this
        rootView.spinnerPartnerHIVStatus.onItemSelectedListener = this
        rootView.spinnerOnARVBeforeANCVisit.onItemSelectedListener = this
        rootView.spinnerStartedHaartInANC.onItemSelectedListener = this
        rootView.spinnerCotrimoxazole.onItemSelectedListener = this



    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerEligibility -> { spinnerArtElligibilityValue = rootView.spinnerEligibility.selectedItem.toString() }
            R.id.spinnerPartnerHIVStatus -> { spinnerPartnerHivValue = rootView.spinnerPartnerHIVStatus.selectedItem.toString() }
            R.id.spinnerOnARVBeforeANCVisit -> { spinnerBeforeFirstVisitValue = rootView.spinnerOnARVBeforeANCVisit.selectedItem.toString() }
            R.id.spinnerStartedHaartInANC -> { spinnerStartedHaartValue = rootView.spinnerStartedHaartInANC.selectedItem.toString() }
            R.id.spinnerCotrimoxazole -> { spinnerCotrimoxazoleValue = rootView.spinnerCotrimoxazole.selectedItem.toString() }


            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val tbTest = formatter.getRadioText(rootView.radioGrpTb)
        if (tbTest != "") {
            addData("Tb Test", tbTest , DbObservationValues.TB_SCREENING.name)

            if (rootView.linearTB.visibility == View.VISIBLE){
                val text = formatter.getRadioText(rootView.radioGrpTbResults)
                if (text != "") {
                    addData("Tb Test Results", text, DbObservationValues.TB_SCREENING_RESULTS.name)

                    if (rootView.linearPositive.visibility == View.VISIBLE){

                        val data = rootView.etTb.text.toString()
                        if(!TextUtils.isEmpty(data)){
                            addData("TB diagnosis",data , DbObservationValues.POSITIVE_TB_DIAGNOSIS.name)
                        }else{
                            errorList.add("Please enter TB diagnosis")
                        }

                    }
                    if (rootView.linearNegative.visibility == View.VISIBLE){
                        val iptGvn = rootView.etIpt.text.toString()
                        val dateGvn = rootView.tvIPTDateGiven.text.toString()
                        val nextGive = rootView.tvIPTNextVisit.text.toString()

                        if (!TextUtils.isEmpty(iptGvn)) {
                            addData("IPT Given", iptGvn , DbObservationValues.NEGATIVE_TB_DIAGNOSIS.name)
                        } else {
                            errorList.add("Please enter IPT Given")
                        }
                        if (!TextUtils.isEmpty(dateGvn)) {
                            addData("IPT Date Given", dateGvn, DbObservationValues.IPT_DATE.name)
                        } else {
                            errorList.add("Please enter IPT Date Given")
                        }
                        if (!TextUtils.isEmpty(nextGive)) {
                            addData("IPT Next Visit", nextGive, DbObservationValues.IPT_VISIT.name)
                        } else {
                            errorList.add("Please enter IPT Next Visit")
                        }

                    }

                }else{
                    errorList.add("Please select TB Test Results")
                }
            }

        }else{
            errorList.add("Please select TB Test")
        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.C_TB_SCREEN.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val multipleBabies = formatter.getRadioText(rootView.radioGrpMultipleBaby)
        if (multipleBabies != "") {
            addData("Multiple babies", multipleBabies, DbObservationValues.MULTIPLE_BABIES.name)
            if (rootView.linearMultipleBaby.visibility == View.VISIBLE){
                val text = rootView.etMultipleBaby.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("Multiple babies results", text , DbObservationValues.NO_MULTIPLE_BABIES.name)
                }else{
                    errorList.add("Please enter Multiple babies results")
                }
            }
        }else{
            errorList.add("Please select Multiple babies")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.D_MULTIPLE_BABIES.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val obstetricUltraSound1 = formatter.getRadioText(rootView.radioGrpUltrasound1)
        if (obstetricUltraSound1 != "") {
            addData("1st Obstetric Sound", obstetricUltraSound1 , DbObservationValues.OBSTERIC_ULTRASOUND_1.name)
            if (rootView.linearDate.visibility == View.VISIBLE){
                val text = rootView.tvUltraSound1.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("1st Obstetric Sound results", text , DbObservationValues.OBSTERIC_ULTRASOUND_1_DATE.name)
                }else{
                    errorList.add("Please enter 1st Obstetric Sound results")
                }
            }
        }else{
            errorList.add("Please select 1st Obstetric Sound")
        }

        val obstetricUltraSound2 = formatter.getRadioText(rootView.radioGrpUltrasound2)
        if (obstetricUltraSound2 != "") {
            addData("2nd Obstetric Sound", obstetricUltraSound2, DbObservationValues.OBSTERIC_ULTRASOUND_2.name)
            if (rootView.linear2ndUltra.visibility == View.VISIBLE){
                val text = rootView.tvUltraSound2.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("2nd Obstetric Sound results", text, DbObservationValues.OBSTERIC_ULTRASOUND_2_DATE.name)
                }else{
                    errorList.add("Please enter 2nd Obstetric Sound results")
                }
            }
        }else{
            errorList.add("Please select 2nd Obstetric Sound")
        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.D_OBSTETRIC_ULTRASOUND.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val hivStatus = formatter.getRadioText(rootView.radioGrpHIVStatus)
        if (hivStatus != "") {

            addData("HIV status before 1st ANC", hivStatus, DbObservationValues.HIV_STATUS_BEFORE_1_ANC.name)

            if (rootView.linearARTEligibility.visibility == View.VISIBLE){

                if (spinnerArtElligibilityValue != "") {
                    addData("ART Eligibility (WHO Stage)", spinnerArtElligibilityValue, DbObservationValues.ART_ELIGIBILITY.name)
                }else{
                    errorList.add("Please select ART Eligibility (WHO Stage)")
                }

                val lastCCC = rootView.tvLastCCC.text.toString()
                if (!TextUtils.isEmpty(lastCCC)) {
                    addData("Last CCC", lastCCC, DbObservationValues.LAST_CCC.name)
                }else{
                    errorList.add("Please enter Last CCC")
                }

            }

            if (rootView.linearMaternalHAART.visibility == View.VISIBLE){

                if (spinnerCotrimoxazoleValue != "") {
                    addData("Cotrimoxazole Given", spinnerCotrimoxazoleValue, DbObservationValues.COTRIMOXAZOLE.name)
                }else{
                    errorList.add("Please select Cotrimoxazole Given")
                }

                if (spinnerBeforeFirstVisitValue != "") {
                    addData("On ARV before 1st ANC visit", spinnerBeforeFirstVisitValue, DbObservationValues.ARV_ANC.name)
                }else{
                    errorList.add("Please select On ARV before 1st ANC visit")
                }
                if (spinnerStartedHaartValue != "") {
                    addData("Started HAART in ANC", spinnerStartedHaartValue , DbObservationValues.HAART_ANC.name)
                }else{
                    errorList.add("Please select Started HAART in ANC")
                }

            }

        }else{
            errorList.add("Please select HIV status before 1st ANC")
        }


        if(spinnerPartnerHivValue != "") {
            addData("Partner HIV Status", spinnerPartnerHivValue, DbObservationValues.PARTNER_HIV.name)
        }else{
            errorList.add("Please select Partner HIV Status")
        }
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.E_HIV_STATUS.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.F_MATERNAL_HAART.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()

        if (errorList.size == 0){
            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)



            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentAntenatal3())
            ft.addToBackStack(null)
            ft.commit()
        }else{

            formatter.showErrorDialog(errorList, requireContext())
        }



    }

    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateIptDateGvnListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateIptNextVisitListener, year, month, day)
                //Convert weeks to milliseconds

                val nextContact = formatter.retrieveSharedPreference(requireContext(), DbAncSchedule.CONTACT_WEEK.name)
                if (nextContact != null){
                    val weeks = nextContact.toInt() * 7 * 24 * 60 * 60 * 1000L
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis() + weeks
                }else{
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                }
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound1Listener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            996 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound2Listener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            995 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateCCC, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            else -> null
        }


    }

    private val myDateIptDateGvnListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvIPTDateGiven.text = date

        }

    private val myDateIptNextVisitListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvIPTNextVisit.text = date

        }


    private val myDateSound1Listener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvUltraSound1.text = date

        }


    private val myDateSound2Listener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvUltraSound2.text = date

        }
    private val myDateCCC =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvLastCCC.text = date

        }

    private fun showDate(year: Int, month: Int, day: Int) :String{

        var dayDate = day.toString()
        if (day.toString().length == 1){
            dayDate = "0$day"
        }
        var monthDate = month.toString()
        if (month.toString().length == 1){
            monthDate = "0$monthDate"
        }

        val date = StringBuilder().append(year).append("-")
            .append(monthDate).append("-").append(dayDate)

        return date.toString()

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
                    DbResourceViews.ANTENATAL_PROFILE.name)

                if (encounterId != null){

                    val tbScreening = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TB_SCREENING.name), encounterId)

                    val tbScreeningResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TB_SCREENING_RESULTS.name), encounterId)

                    val tbScreeningPositive = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.POSITIVE_TB_DIAGNOSIS.name), encounterId)

                    val negativeTbDiagnosis = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NEGATIVE_TB_DIAGNOSIS.name), encounterId)

                    val iptDate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IPT_DATE.name), encounterId)

                    val iptVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IPT_VISIT.name), encounterId)

                    val multiplePregnancy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.MULTIPLE_BABIES.name), encounterId)

                    val noMultiplePregnancy = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NO_MULTIPLE_BABIES.name), encounterId)

                    val obstetricUltrasound1 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OBSTERIC_ULTRASOUND_1.name), encounterId)

                    val obstetricUltrasoundDate1 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OBSTERIC_ULTRASOUND_1_DATE.name), encounterId)

                    val obstetricUltrasound2 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OBSTERIC_ULTRASOUND_2.name), encounterId)

                    val obstetricUltrasoundDate2 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OBSTERIC_ULTRASOUND_2_DATE.name), encounterId)

                    val hivStatusAnc = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HIV_STATUS_BEFORE_1_ANC.name), encounterId)

                    val artEligibilty = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ART_ELIGIBILITY.name), encounterId)

                    val hivStatus = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PARTNER_HIV.name), encounterId)

                    val arvAnc = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ARV_ANC.name), encounterId)

                    val haartAnc = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HAART_ANC.name), encounterId)

                    val cotrimoxazole = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COTRIMOXAZOLE.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (tbScreening.isNotEmpty()){
                            val value = tbScreening[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpTb.check(R.id.radioYesTb)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpTb.check(R.id.radioNoTb)
                        }
                        if (tbScreeningResult.isNotEmpty()){
                            val value = tbScreeningResult[0].value
                            if(value.contains("Positive", ignoreCase = true)) rootView.radioGrpTbResults.check(R.id.radioPositiveTbResults)
                            if(value.contains("Negative", ignoreCase = true)) rootView.radioGrpTbResults.check(R.id.radioNegativeTbResults)
                        }
                        if (tbScreeningPositive.isNotEmpty()){
                            val value = tbScreeningPositive[0].value
                            rootView.etTb.setText(value)
                        }
                        if (negativeTbDiagnosis.isNotEmpty()){
                            val value = negativeTbDiagnosis[0].value
                            rootView.etIpt.setText(value)
                        }
                        if (iptDate.isNotEmpty()){
                            val value = iptDate[0].value
                            rootView.tvIPTDateGiven.setText(value)
                        }
                        if (iptVisit.isNotEmpty()){
                            val value = iptVisit[0].value
                            rootView.tvIPTNextVisit.setText(value)
                        }
                        if (multiplePregnancy.isNotEmpty()){
                            val value = multiplePregnancy[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpMultipleBaby.check(R.id.radioYesMultipleBaby)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpMultipleBaby.check(R.id.radioNoMultipleBaby)
                        }
                        if (noMultiplePregnancy.isNotEmpty()){
                            val value = noMultiplePregnancy[0].value
                            rootView.etMultipleBaby.setText(value)
                        }
                        if (obstetricUltrasound1.isNotEmpty()){
                            val value = obstetricUltrasound1[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpUltrasound1.check(R.id.radioYesUltrasound1)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpUltrasound1.check(R.id.radioNoUltrasound1)
                        }
                        if (obstetricUltrasoundDate1.isNotEmpty()){
                            val value = obstetricUltrasoundDate1[0].value
                            rootView.tvUltraSound1.setText(value)
                        }
                        if (obstetricUltrasound2.isNotEmpty()){
                            val value = obstetricUltrasound2[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpUltrasound2.check(R.id.radioYesUltrasound2)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpUltrasound2.check(R.id.radioNoUltrasound2)
                        }
                        if (obstetricUltrasoundDate2.isNotEmpty()){
                            val value = obstetricUltrasoundDate2[0].value
                            rootView.tvUltraSound2.setText(value)
                        }
                        if (hivStatusAnc.isNotEmpty()){
                            val value = hivStatusAnc[0].value
                            if(value.contains("P", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioBtnP)
                            if(value.contains("N", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioBtnN)
                            if(value.contains("U", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioBtnU)
                            if(value.contains("Kp", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioBtnKP)
                        }
                        if (artEligibilty.isNotEmpty()){
                            val value = artEligibilty[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerEligibility.setSelection(artEligibilityList.indexOf(noValue))
                        }
                        if (hivStatus.isNotEmpty()){
                            val value = hivStatus[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerPartnerHIVStatus.setSelection(partnerHivList.indexOf(noValue))
                        }
                        if (arvAnc.isNotEmpty()){
                            val value = arvAnc[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerOnARVBeforeANCVisit.setSelection(arvBeforeFirstVisitList.indexOf(noValue))
                        }
                        if (haartAnc.isNotEmpty()){
                            val value = haartAnc[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerStartedHaartInANC.setSelection(startedHaartList.indexOf(noValue))
                        }
                        if (cotrimoxazole.isNotEmpty()){
                            val value = cotrimoxazole[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerCotrimoxazole.setSelection(cotrimoxazoleList.indexOf(noValue))
                        }


                    }





                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}