package com.intellisoft.kabarakmhis.new_designs.previous_pregnancy

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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.view.*
import java.util.ArrayList


class FragmentPreviousPregnancy : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var pregnancyOrderList = arrayOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th")
    private var spinnerPregnancyValue  = pregnancyOrderList[0]

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_prev_pregnancy, container, false)

        rootView.btnSave.setOnClickListener {
            saveData()
        }
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


        return rootView
    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }

    private fun getRadioText(radioGroup: RadioGroup): String {

        val checkedId = radioGroup.checkedRadioButtonId
        val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
        return checkedRadioButton.text.toString()

    }




    private fun saveData() {

        val year = rootView.etYear.text.toString()
        val ancTime = rootView.etVisitTime.text.toString()
        val birthPlace = rootView.etPlaceOfChildBirth.text.toString()
        val gestation = rootView.etGestation.text.toString()
        val duration = rootView.etDuration.text.toString()

        val babyWeight = rootView.etBabyWeight.text.toString()

        if (rootView.linearPurperium.visibility == View.VISIBLE){
            val text = rootView.etAbnormal.text.toString()
            addData("Purperium",text)
        }else{
            val text = getRadioText(rootView.radioGrpPurperium)
            addData("Purperium",text)
        }

        if (
            !TextUtils.isEmpty(year) && !TextUtils.isEmpty(ancTime) && !TextUtils.isEmpty(birthPlace) &&
            !TextUtils.isEmpty(gestation) && !TextUtils.isEmpty(duration) && !TextUtils.isEmpty(babyWeight)
        ){
            addData("Year",year)
            addData("ANC Time",ancTime)
            addData("Birth Place",birthPlace)
            addData("Gestation",gestation)
            addData("Duration",duration)
            addData("Baby Weigt",babyWeight)

            val deliveryMode = getRadioText(rootView.deliveryMode)
            addData("Delivery Mode",deliveryMode)

            val radioGrpBabySex = getRadioText(rootView.radioGrpBabySex)
            addData("Baby's Sex",radioGrpBabySex)

            val radioGrpOutcome = getRadioText(rootView.radioGrpOutcome)
            addData("Outcome",radioGrpOutcome)

            val radioGrpPurperium = getRadioText(rootView.radioGrpPurperium)
            addData("Purperium",radioGrpPurperium)

            addData("Pregnancy Order",spinnerPregnancyValue)

            val dbDataList = ArrayList<DbDataList>()

            for (items in observationList){

                val key = items.key
                val value = observationList.getValue(key)

                val data = DbDataList(key, value, "Previous Pregnancy", DbResourceType.Observation.name)
                dbDataList.add(data)

            }

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PREVIOUS_PREGNANCY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            startActivity(Intent(requireContext(), PreviousPregnancyView::class.java))


        }else{
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addData(key: String, value: String) {
        observationList[key] = value
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
}