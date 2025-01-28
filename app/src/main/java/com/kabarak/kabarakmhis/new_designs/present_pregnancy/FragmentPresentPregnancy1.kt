package com.kabarak.kabarakmhis.new_designs.present_pregnancy

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.fragment_present_preg_1.*
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.etDiastolicBp
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.etGestation
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.etSystolicBp
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.linearUrine
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.navigation
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.radioGrpHb
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.radioGrpUrineResults
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class FragmentPresentPregnancy1 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var contactNumberList = arrayOf("","1st", "2nd", "3rd", "4th", "5th", "6th", "7th","8th","9th","10th","11th","12th", "13th")
    private var spinnerContactNumberValue  = contactNumberList[0]

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_present_preg_1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        getPageDetails()

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        initSpinner()

        rootView.tvDate.setOnClickListener { createDialog(999) }

        rootView.radioGrpUrineResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearUrine, true)
                } else {
                    changeVisibility(rootView.linearUrine, false)
                }
            }
        }
        rootView.radioGrpHb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearHbReading, true)
                } else {
                    changeVisibility(rootView.linearHbReading, false)
                }
            }
        }

        rootView.etMuac.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etMuac.text.toString()

                if (!TextUtils.isEmpty(value)){
                    try {
                        validateMuac(rootView.etMuac, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })

        rootView.etSystolicBp.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etSystolicBp.text.toString()
                if (!TextUtils.isEmpty(value)){
                    try {
                        validateSystolicBloodPressure(rootView.etSystolicBp, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })
        rootView.etDiastolicBp.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etDiastolicBp.text.toString()
                if (!TextUtils.isEmpty(value)){
                    try {
                        validateDiastolicBloodPressure(rootView.etDiastolicBp, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })
        rootView.etHbReading.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etHbReading.text.toString()
                if (!TextUtils.isEmpty(value)){
                    try {
                        validateHbReading(rootView.etHbReading, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })


        handleNavigation()

        return rootView
    }
    private fun validateHbReading(editText: EditText, value: Int){

        if (value < 11){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value >= 11.5 && value <= 13){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        } else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }



    }
    private fun validateSystolicBloodPressure(editText: EditText, value: Int){

        if (value <= 70){
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }else if (value <= 80){
            editText.setBackgroundColor(resources.getColor(R.color.orange))
        }else if (value <= 110){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value <= 130)
            editText.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }



    }
    private fun validateDiastolicBloodPressure(editText: EditText, value: Int){

        if (value <= 60){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value <= 90){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }

    }
    private fun validateMuac(editText: EditText, value: Int){

        if (value < 23){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value in 24..30){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }

    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
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

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val systolic = rootView.etSystolicBp.text.toString()
        val diastolic = rootView.etDiastolicBp.text.toString()
        val gestation = rootView.etGestation.text.toString()
        val fundalHeight = rootView.etFundal.text.toString()
        val muac = rootView.etMuac.text.toString()

        val date = rootView.tvDate.text.toString()

        if (!TextUtils.isEmpty(systolic) && !TextUtils.isEmpty(diastolic)
            && !TextUtils.isEmpty(fundalHeight) && !TextUtils.isEmpty(gestation)
            && !TextUtils.isEmpty(date) && spinnerContactNumberValue != ""){


            val urineTest = formatter.getRadioText(rootView.radioGrpUrineResults)
            if (urineTest != ""){

                addData("Urine Test Done",urineTest, DbObservationValues.URINALYSIS_TEST.name)

                if (rootView.linearUrine.visibility == View.VISIBLE){

                    val text = rootView.etUrineResults.text.toString()
                    if (!TextUtils.isEmpty(text)) {
                        addData(
                            "Urine Results",
                            text,
                            DbObservationValues.URINALYSIS_RESULTS.name
                        )
                    }else{
                        errorList.add("You selected urine test but did not enter results")
                    }

                }

            }else{
                errorList.add("Urine Results is required")
            }

            if (!TextUtils.isEmpty(muac)){
                addData("MUAC (cm)",muac, DbObservationValues.MUAC.name)
            }

            addData("Pregnancy Contact",spinnerContactNumberValue, DbObservationValues.CONTACT_NUMBER.name)
            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_CURRENT_PREGNANCY.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()


            addData("Systolic Blood Pressure (mmHG)",systolic, DbObservationValues.SYSTOLIC_BP.name)
            addData("Diastolic Blood Pressure (mmHG)",diastolic, DbObservationValues.DIASTOLIC_BP.name)
            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.B_PRESENT_BLOOD_PRESSURE.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            val hbTestValue = formatter.getRadioText(rootView.radioGrpHb)
            if (hbTestValue != ""){

                addData("Hb Testing Done",hbTestValue, DbObservationValues.HB_TEST.name)
                if (rootView.linearHbReading.visibility == View.VISIBLE){

                    val hbReading = rootView.etHbReading.text.toString()
                    if (hbReading != ""){
                        addData("Hb Testing Results",hbReading, DbObservationValues.SPECIFIC_HB_TEST.name)
                    }else{
                        errorList.add("Hb Reading is required")
                    }
                }

            }else{
                errorList.add("Please select HB test")
            }
            val pallor = formatter.getRadioText(rootView.radioGrpPallor)
            if (pallor != "") {
                addData("Pallor", pallor, DbObservationValues.PALLOR.name)
            }else{
                errorList.add("Pallor is required")
            }

            addData("Gestation (Weeks)",gestation, DbObservationValues.GESTATION.name)
            addData("Fundal Height (cm)",fundalHeight, DbObservationValues.FUNDAL_HEIGHT.name)
            addData("Date",date, DbObservationValues.NEXT_CURRENT_VISIT.name)
            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.C_HB_TEST.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()


        }else{

            if (TextUtils.isEmpty(systolic)) errorList.add("Systolic Blood Pressure is required")
            if (TextUtils.isEmpty(diastolic)) errorList.add("Diastolic Blood Pressure is required")
            if (TextUtils.isEmpty(gestation)) errorList.add("Gestation is required")
            if (TextUtils.isEmpty(fundalHeight)) errorList.add("Fundal Height is required")
            if (TextUtils.isEmpty(date)) errorList.add("Date is required")
            if (spinnerContactNumberValue == "") errorList.add("Pregnancy Contact is required")

        }



        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PRESENT_PREGNANCY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPresentPregnancy2())
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

        val todayDate = formatter.getTodayDateNoTime()
        rootView.tvDate.text = todayDate

    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, contactNumberList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerContact!!.adapter = kinRshp

        rootView.spinnerContact.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerContact -> { spinnerContactNumberValue = rootView.spinnerContact.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }

            else -> null
        }


    }

    private val myDateDobListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvDate.text = date

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

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.PRESENT_PREGNANCY.name)

                if (encounterId != null){

                    val contactNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.CONTACT_NUMBER.name), encounterId)
                    val date = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name), encounterId)
                    val urineTest = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.URINALYSIS_TEST.name), encounterId)
                    val urineTestResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.URINALYSIS_RESULTS.name), encounterId)
                    val muac = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.MUAC.name), encounterId)
                    val systolicBp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SYSTOLIC_BP.name), encounterId)
                    val diastolicBp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DIASTOLIC_BP.name), encounterId)
                    val hbTest = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HB_TEST.name), encounterId)
                    val hbTestResult = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SPECIFIC_HB_TEST.name), encounterId)
                    val pallor = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PALLOR.name), encounterId)
                    val gestation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.GESTATION.name), encounterId)
                    val fundalHeight = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FUNDAL_HEIGHT.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {


                        if (contactNo.isNotEmpty()){
                            val value = contactNo[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.spinnerContact.setSelection(contactNumberList.indexOf(valueNo))
                        }
                        if (date.isNotEmpty()){
                            val value = date[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.tvDate.text = valueNo
                        }
                        if (urineTest.isNotEmpty()){
                            val value = urineTest[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpUrineResults.check(R.id.radioYes)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpUrineResults.check(R.id.radioNo)
                        }
                        if (urineTestResult.isNotEmpty()){
                            val value = urineTestResult[0].value
                            rootView.etUrineResults.setText(value)
                        }
                        if (muac.isNotEmpty()){
                            val value = muac[0].value
                            val valueNo = formatter.getValues(value, 3)
                            rootView.etMuac.setText(valueNo)
                        }
                        if (systolicBp.isNotEmpty()){
                            val value = systolicBp[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etSystolicBp.setText(valueNo)
                        }
                        if (diastolicBp.isNotEmpty()){
                            val value = diastolicBp[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etDiastolicBp.setText(valueNo)
                        }
                        if (hbTest.isNotEmpty()){
                            val value = hbTest[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpHb.check(R.id.radioHbYes)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpHb.check(R.id.radioHbNo)
                        }
                        if (hbTestResult.isNotEmpty()){
                            val value = hbTestResult[0].value
                            rootView.etHbReading.setText(value)
                        }
                        if (pallor.isNotEmpty()){
                            val value = pallor[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpPallor.check(R.id.radioPallorYes)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpPallor.check(R.id.radioPallorNo)
                        }
                        if (gestation.isNotEmpty()){
                            val value = gestation[0].value
                            val valueNo = formatter.getValues(value, 6)
                            rootView.etGestation.setText(valueNo)
                        }
                        if (fundalHeight.isNotEmpty()){
                            val value = fundalHeight[0].value
                            val valueNo = formatter.getValues(value, 3)
                            rootView.etFundal.setText(valueNo)
                        }


                    }

                }



            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}