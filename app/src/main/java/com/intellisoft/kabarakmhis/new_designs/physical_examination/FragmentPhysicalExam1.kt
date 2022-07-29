package com.intellisoft.kabarakmhis.new_designs.physical_examination

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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_physical_exam.view.*
import kotlinx.android.synthetic.main.fragment_physical_exam.view.linearAbnormal
import kotlinx.android.synthetic.main.fragment_physical_exam.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentPhysicalExam1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_physical_exam, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        rootView.radioGrpGeneralExam.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearGeneralExam, true)
                } else {
                    changeVisibility(rootView.linearGeneralExam, false)
                }

            }
        }
        rootView.radioGrpCVS.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearCvs, true)
                } else {
                    changeVisibility(rootView.linearCvs, false)
                }

            }
        }
        rootView.radioGrpRespiratory.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearResp, true)
                } else {
                    changeVisibility(rootView.linearResp, false)
                }

            }
        }
        rootView.radioGrpRespiratory.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearResp, true)
                } else {
                    changeVisibility(rootView.linearResp, false)
                }

            }
        }
        rootView.radioGrpBreasts.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                when (checkedRadioButton.text.toString()) {
                    "Abnormal" -> {
                        changeVisibility(rootView.linearAbnormal, true)
                        changeVisibility(rootView.linearNormal, false)
                    }
                    "Normal" -> {
                        changeVisibility(rootView.linearNormal, true)
                        changeVisibility(rootView.linearAbnormal, false)
                    }
                    else -> {
                        changeVisibility(rootView.linearNormal, false)
                        changeVisibility(rootView.linearAbnormal, false)

                    }
                }

            }
        }

        handleNavigation()

        rootView.etSystolicBp.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etSystolicBp.text.toString()
                if (!TextUtils.isEmpty(value)){
                    validateSystolicBloodPressure(rootView.etSystolicBp, value.toInt())
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
                    validateDiastolicBloodPressure(rootView.etDiastolicBp, value.toInt())
                }

            }

        })
        rootView.etPulseRate.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etPulseRate.text.toString()
                if (!TextUtils.isEmpty(value)){
                    validatePulseRateBloodPressure(rootView.etPulseRate, value.toInt())
                }

            }

        })

        return rootView
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
    private fun validatePulseRateBloodPressure(editText: EditText, value: Int){

        if (value < 60){
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }else if (value <= 100){
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
        val systolicBp = rootView.etSystolicBp.text.toString()
        val diastolicBp = rootView.etDiastolicBp.text.toString()
        val pulseRate = rootView.etPulseRate.text.toString()

        val motherWeight = rootView.etMotherWeight.text.toString()
        val gestation = rootView.etGestation.text.toString()

        if(rootView.linearGeneralExam.visibility == View.VISIBLE){
            val text = rootView.etAbnomality.text.toString()
            addData("General Examination",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpGeneralExam)
            addData("General Examination",text)
        }

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Physical Examination", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()

        if(rootView.linearCvs.visibility == View.VISIBLE){
            val text = rootView.etCvsAbnormal.text.toString()
            addData("CVS",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpCVS)
            addData("CVS",text)
        }
        if (!TextUtils.isEmpty(systolicBp)){
            addData("Systolic Bp",systolicBp)
        }
        if (!TextUtils.isEmpty(diastolicBp)){
            addData("Diastolic BP",diastolicBp)
        }
        if (!TextUtils.isEmpty(pulseRate)){
            addData("Pulse Rate",pulseRate)
        }

        if(rootView.linearResp.visibility == View.VISIBLE){
            val text = rootView.etCvsRespiratory.text.toString()
            addData("Respiratory",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpRespiratory)
            addData("Respiratory",text)
        }
        if(rootView.linearResp.visibility == View.VISIBLE){
            val text = rootView.etBreastFinding.text.toString()
            addData("Breasts Exam",text)
        }
        if(rootView.linearNormal.visibility == View.VISIBLE){
            val text = rootView.etBreastFinding.text.toString()
            addData("Normal Breasts Findings",text)
        }
        if(rootView.linearAbnormal.visibility == View.VISIBLE){
            val text = rootView.etBreastAbnormal.text.toString()
            addData("Abnormal Breasts Findings",text)
        }

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Blood pressure", DbResourceType.Observation.name)
            dbDataList.add(data)

        }
        observationList.clear()



        if (!TextUtils.isEmpty(motherWeight) && !TextUtils.isEmpty(gestation)){

            val isWeight = formatter.validateWeight(motherWeight)
            if (isWeight){
                addData("Mother Weight",motherWeight)
                addData("Gestation",gestation)

                for (items in observationList){

                    val key = items.key
                    val value = observationList.getValue(key)

                    val data = DbDataList(key, value, "Weight Monitoring", DbResourceType.Observation.name)
                    dbDataList.add(data)

                }
                observationList.clear()


                val dbDataDetailsList = ArrayList<DbDataDetails>()
                val dbDataDetails = DbDataDetails(dbDataList)
                dbDataDetailsList.add(dbDataDetails)
                val dbPatientData = DbPatientData(DbResourceViews.PHYSICAL_EXAMINATION.name, dbDataDetailsList)
                kabarakViewModel.insertInfo(requireContext(), dbPatientData)

                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.replace(R.id.fragmentHolder, FragmentPhysicalExam2())
                ft.addToBackStack(null)
                ft.commit()

            }else{
                Toast.makeText(requireContext(), "Invalid Weight", Toast.LENGTH_SHORT).show()
            }


        }else{
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }





    }

    private fun getListData(valueData: String): ArrayList<DbDataList> {
        val dbDataList = ArrayList<DbDataList>()
        for (items in observationList){
            val key = items.key
            val value = observationList.getValue(key)
            val data = DbDataList(key, value, valueData, DbResourceType.Observation.name)
            dbDataList.add(data)
        }

        observationList.clear()
        return dbDataList
    }
    


    private fun addData(key: String, value: String) {
        if (key != ""){
            observationList[key] = value
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

}