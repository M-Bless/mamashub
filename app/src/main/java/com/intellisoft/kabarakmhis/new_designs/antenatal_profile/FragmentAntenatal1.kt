package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_antenatal1.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentAntenatal1 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal1, container, false)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        //Restricted User
        rootView.radioGrpHb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearHb, true)
                } else {
                    changeVisibility(rootView.linearHb, false)
//                    clearEdiText(rootView.etBloodRBSReading)
                }

            }
        }
        rootView.radioGrpBloodGrpTest.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearBG, true)
                } else {
                    changeVisibility(rootView.linearBG, false)
                }

            }
        }
        rootView.radioGrpType.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
//                addData(checkedBtn, "Blood Group Test")

            }
        }
        rootView.radioGrpRhesus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearRhesus, true)
                } else {
                    changeVisibility(rootView.linearRhesus, false)
                }
            }
        }
        rootView.radioGrpRhesusTest.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
//                addData(checkedBtn, "Rhesus Test")
            }
        }
        rootView.radioGrpBloodRbs.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearRBS, true)
                } else {
                    changeVisibility(rootView.linearRBS, false)
//                    clearEdiText(rootView.etBloodRBSReading)
                }
            }
        }

        rootView.radioGrpExternalExam.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearUrine, true)
                    changeVisibility(rootView.linearUrineDate, false)
                } else {
                    changeVisibility(rootView.linearUrine, false)
                    changeVisibility(rootView.linearAbnormal, false)
                    changeVisibility(rootView.linearUrineDate, true)
                }

            }
        }
        rootView.radioGrpUrineResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearAbnormal, true)
                } else {
                    changeVisibility(rootView.linearAbnormal, false)
                }

            }
        }

        rootView.etHb.addTextChangedListener(object :  TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etHb.text.toString()

                if (!TextUtils.isEmpty(value)){
                    try {
                        validateHB(rootView.etHb, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })
        rootView.etBloodRBSReading.addTextChangedListener(object :  TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etBloodRBSReading.text.toString()

                if (!TextUtils.isEmpty(value)){
                    try {
                        validateRBS(rootView.etBloodRBSReading, value.toInt())

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })

        rootView.tvUrineTestAppointment.setOnClickListener { onCreateDialog(999) }

        handleNavigation()

        return rootView
    }

    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateUrineTest, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }

            else -> null
        }


    }

    private val myDateUrineTest =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvUrineTestAppointment.text = date

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

    private fun validateRBS(editText: EditText, value: Int){

        if (value > 4 && value < 8.3){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else{
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }


    }
    private fun validateHB(editText: EditText, value: Int){

        if (value < 11){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value in 11..13){
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

    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val hbTest = formatter.getRadioText(rootView.radioGrpHb)
        if (hbTest != ""){
            addData(hbTest, "HB Test", DbObservationValues.HB_TEST.name)
            if (rootView.linearHb.visibility == View.VISIBLE){
                val hbReading = rootView.etHb.text.toString()
                if (!TextUtils.isEmpty(hbReading)) {
                    addData("HB Reading",hbReading, DbObservationValues.SPECIFIC_HB_TEST.name)
                }else{
                    errorList.add("Please enter HB Reading")
                }
            }
        }else{
            errorList.add("Please make a selection on HB Test")
        }


        val bloodGroupTest = formatter.getRadioText(rootView.radioGrpBloodGrpTest)
        if (bloodGroupTest != ""){
            addData("Blood Group Test", bloodGroupTest, DbObservationValues.BLOOD_GROUP_TEST.name)
            if (rootView.linearBG.visibility == View.VISIBLE){
                val groupTypeResult = formatter.getRadioText(rootView.radioGrpType)
                if (groupTypeResult != "") {
                    addData("Blood Group Type", groupTypeResult ,DbObservationValues.SPECIFIC_BLOOD_GROUP_TEST.name)
                }else{
                    errorList.add("Please make a selection on Blood Group Type")
                }

            }
        }else{
            errorList.add("Please make a selection on Blood Group Test")
        }


        val rhesusTest = formatter.getRadioText(rootView.radioGrpRhesus)
        if (rhesusTest != "") {
            addData("Rhesus Test", rhesusTest ,DbObservationValues.RHESUS_TEST.name)
            if (rootView.linearRhesus.visibility == View.VISIBLE){
                val rhesusResult = formatter.getRadioText(rootView.radioGrpRhesusTest)
                if (rhesusResult != "") {
                    addData("Rhesus Test Result", rhesusResult ,DbObservationValues.SPECIFIC_RHESUS_TEST.name)
                }else{
                    errorList.add("Please make a selection on Rhesus Test Result")
                }
            }
        }else{
            errorList.add("Please make a selection on Rhesus Test")
        }


        val bloodRbs = formatter.getRadioText(rootView.radioGrpBloodRbs)
        if (bloodRbs != "") {
            addData("Blood RBS", bloodRbs ,DbObservationValues.BLOOD_RBS_TEST.name)
            if (rootView.linearRBS.visibility == View.VISIBLE){
                val rbsReading = rootView.etBloodRBSReading.text.toString()
                if (!TextUtils.isEmpty(rbsReading)) {
                    addData("Blood RBS Reading", rbsReading, DbObservationValues.SPECIFIC_BLOOD_RBS_TEST.name)
                }else{
                    errorList.add("Please enter Blood RBS Reading")
                }
            }
        }else{
            errorList.add("Please make a selection on Blood RBS")
        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_BLOOD_TESTS.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val urineTest = formatter.getRadioText(rootView.radioGrpExternalExam)
        if (urineTest != "") {
            addData("Urinalysis test", urineTest ,DbObservationValues.URINALYSIS_TEST.name)

            if (rootView.linearUrineDate.visibility == View.VISIBLE){
                val urineTestDate = rootView.tvUrineTestAppointment.text.toString()
                if (!TextUtils.isEmpty(urineTestDate)) {
                    addData("Urinalysis test date", urineTestDate, DbObservationValues.URINALYSIS_TEST_DATE.name)
                }else{
                    errorList.add("Please enter a Urinalysis test date")
                }
            }

            if (rootView.linearUrine.visibility == View.VISIBLE){
                val urineResult = formatter.getRadioText(rootView.radioGrpUrineResults)
                if (urineResult != "") {
                    addData("Urinalysis Result", urineResult ,DbObservationValues.URINALYSIS_RESULTS.name)
                }else{
                    errorList.add("Please make a selection on Urinalysis Result")
                }
            }
            if (rootView.linearAbnormal.visibility == View.VISIBLE){
                val abnormalResult = rootView.etAbnormalUrine.text.toString()
                if (!TextUtils.isEmpty(abnormalResult)) {
                    addData("Urinalysis Abnormal Result", abnormalResult ,DbObservationValues.ABNORMAL_URINALYSIS_TEST.name)
                }else{
                    errorList.add("Please enter Urinalysis Abnormal Result")
                }
            }


        }else{
            errorList.add("Please make a selection on Urinalysis test")
        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.B_URINE_TESTS.name, DbResourceType.Observation.name, label)
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
            ft.replace(R.id.fragmentHolder, FragmentAntenatal2())
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


}