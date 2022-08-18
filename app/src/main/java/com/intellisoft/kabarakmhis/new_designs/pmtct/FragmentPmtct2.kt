package com.intellisoft.kabarakmhis.new_designs.pmtct

import android.app.Application
import android.app.DatePickerDialog
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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_pmtct2.view.*
import kotlinx.android.synthetic.main.fragment_pmtct2.view.navigation
import kotlinx.android.synthetic.main.fragment_pmtct2.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentPmtct2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_pmtct2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        rootView.tvDate.setOnClickListener {

            onCreateDialog(999)

        }

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.radioGrpRegimen.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReason, true)
                } else {
                    changeVisibility(rootView.linearReason, false)
                }

            }
        }

        handleNavigation()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateListener, year, month, day)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

                datePickerDialog.show()

            }
            else -> null
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener =
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

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun checkedText(checkBox: CheckBox){

        if (checkBox.isChecked){
            val value = checkBox.text.toString()
            addData("Reason for regiment change",value, DbObservationValues.REGIMENT_CHANGE.name)
        }

    }
    private fun saveData() {

        val errorList = ArrayList<String>()

        if(rootView.linearReason.visibility == View.VISIBLE){
            checkedText(rootView.checkboxViralLoad)
            checkedText(rootView.checkboxAdverseReactions)
            checkedText(rootView.checkboxInteraction)
            checkedText(rootView.checkboxTrimester)
            val otherRegimen = rootView.etOther.text.toString()

            if (!TextUtils.isEmpty(otherRegimen)){
                addData("Other Regimen Applied",otherRegimen, DbObservationValues.REGIMEN.name)
            }
        }

        val artAmount = rootView.etDosageAmount.text.toString()

        if (!TextUtils.isEmpty(artAmount)){
            addData("ART Amount",artAmount, DbObservationValues.ART_DOSAGE.name)
        }else{
            errorList.add("ART Amount is required")
        }

        val frequency = rootView.etFrequency.text.toString()
        if (!TextUtils.isEmpty(frequency)){
            addData("ART Frequency",frequency, DbObservationValues.ART_FREQUENCY.name)
        }else{
            errorList.add("ART Frequency is required")
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.C_PMTCT_DOSAGE.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val date = rootView.tvDate.text.toString()
        val vrResults = rootView.etVLResults.text.toString()

        if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(vrResults)) {

            addData("Date VL was taken", date, DbObservationValues.VIRAL_LOAD_CHANGE.name)
            addData("Results", vrResults, DbObservationValues.VIRAL_LOAD_RESULTS.name)

            for (items in observationList) {

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(
                    key,
                    value,
                    DbSummaryTitle.D_VL_SAMPLE.name,
                    DbResourceType.Observation.name,
                    label
                )
                dbDataList.add(data)

            }

        }else{
            if (TextUtils.isEmpty(date)) errorList.add("Date VL was taken is required")
            if (TextUtils.isEmpty(vrResults)) errorList.add("Results is required")
        }

        if(errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PMTCT.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.PMTCT.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{
            formatter.showErrorDialog(errorList, requireContext())
        }


    }



    private fun addData(key: String, value: String, codeLabel: String) {
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

}