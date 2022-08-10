package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
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
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_antenatal1.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentAntenatal1 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal1, container, false)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

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
                } else {
                    changeVisibility(rootView.linearUrine, false)
                    changeVisibility(rootView.linearAbnormal, false)
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
                    validateHB(rootView.etHb, value.toInt())
                }

            }

        })

        handleNavigation()

        return rootView
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

        val errorList = ArrayList<Any>()

        val hbTest = formatter.getRadioText(rootView.radioGrpHb)
        if (hbTest != ""){
            addData(hbTest, "HB Test")
        }else{
            errorList.add(rootView.radioGrpHb)
        }
        if (rootView.linearHb.visibility == View.VISIBLE){
            val hbReading = rootView.etHb.text.toString()
            if (!TextUtils.isEmpty(hbReading)) {
                addData("HB Reading",hbReading)
            }else{
                errorList.add(rootView.etHb)
            }
        }

        val bloodGroupTest = formatter.getRadioText(rootView.radioGrpBloodGrpTest)
        if (bloodGroupTest != ""){
            addData("Blood Group Test", bloodGroupTest)
        }else{
            errorList.add(rootView.radioGrpHb)
        }
        if (rootView.linearBG.visibility == View.VISIBLE){
            val groupTypeResult = formatter.getRadioText(rootView.radioGrpType)
            if (groupTypeResult != "") {
                addData("Blood Group Type", groupTypeResult)
            }else{
                errorList.add(rootView.radioGrpType)
            }

        }

        val rhesusTest = formatter.getRadioText(rootView.radioGrpRhesus)
        if (rhesusTest != "") {
            addData("Rhesus Test", rhesusTest)
        }else{
            errorList.add(rootView.radioGrpRhesus)
        }
        if (rootView.linearRhesus.visibility == View.VISIBLE){
            val rhesusResult = formatter.getRadioText(rootView.radioGrpRhesusTest)
            if (rhesusResult != "") {
                addData("Rhesus Test Result", rhesusResult)
            }else{
                errorList.add(rootView.radioGrpRhesusTest)
            }
        }

        val bloodRbs = formatter.getRadioText(rootView.radioGrpBloodRbs)
        if (bloodRbs != "") {
            addData("Blood RBS", bloodRbs)
        }else{
            errorList.add(rootView.radioGrpBloodRbs)
        }
        if (rootView.linearRBS.visibility == View.VISIBLE){
            val rbsReading = rootView.etBloodRBSReading.text.toString()
            if (!TextUtils.isEmpty(rbsReading)) {
                addData("Blood RBS Reading", rbsReading)
            }else{
                errorList.add(rootView.etBloodRBSReading)
            }
        }

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Blood Test", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()

        val urineTest = formatter.getRadioText(rootView.radioGrpExternalExam)
        if (urineTest != "") {
            addData("Urinalysis test", urineTest)
        }else{
            errorList.add(rootView.radioGrpExternalExam)
        }
        if (rootView.linearUrine.visibility == View.VISIBLE){
            val urineResult = formatter.getRadioText(rootView.radioGrpUrineResults)
            if (urineResult != "") {
                addData("Urinalysis Result", urineResult)
            }else{
                errorList.add(rootView.radioGrpUrineResults)
            }
        }
        if (rootView.linearAbnormal.visibility == View.VISIBLE){
            val abnormalResult = rootView.etAbnormalUrine.text.toString()
            if (!TextUtils.isEmpty(abnormalResult)) {
                addData("Urinalysis Abnormal Result", abnormalResult)
            }else{
                errorList.add(rootView.etAbnormalUrine)
            }
        }
        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Urine Tests", DbResourceType.Observation.name)
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
            formatter.validate(errorList, requireContext())
        }








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