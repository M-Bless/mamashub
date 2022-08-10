package com.intellisoft.kabarakmhis.new_designs.pmtct

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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_pmtct1.view.*
import kotlinx.android.synthetic.main.fragment_pmtct1.view.navigation
import kotlinx.android.synthetic.main.fragment_pmtct1.view.tvDate
import kotlinx.android.synthetic.main.fragment_pmtct3.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentPmtct1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private var lifeART = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_pmtct1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        rootView.radioGrpIntervention.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "ART for life") {
                    lifeART = true
                    changeVisibility(rootView.linearART, true)
                } else {
                    lifeART = false
                    changeVisibility(rootView.linearART, false)
                }

            }
        }
        rootView.tvDate.setOnClickListener {

            onCreateDialog(999)

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

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun checkedText(checkBox: CheckBox){

        if (checkBox.isChecked){
            val value = checkBox.text.toString()
            addData("Regimen Given",value, DbObservationValues.REGIMEN.name)
        }

    }


    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()


        addData("ART for life", if (lifeART) "Yes" else "No", DbObservationValues.INTERVENTION_GIVEN.name)
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Intervention Given", DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()

        if(rootView.linearART.visibility == View.VISIBLE){
            checkedText(rootView.checkboxDolutegravir)
            checkedText(rootView.checkboxEmtricitabine)
            checkedText(rootView.checkboxTenofovir)
            checkedText(rootView.checkboxOvarian)
            checkedText(rootView.checkboxZidovudine)
            checkedText(rootView.checkboxLamivudine)
            checkedText(rootView.checkboxNevirapine)
            checkedText(rootView.checkboxEfavirenz)
            val otherRegimen = rootView.etOther.text.toString()

            if (!TextUtils.isEmpty(otherRegimen)){
                addData("Other Regimen Applied",otherRegimen, DbObservationValues.REGIMEN.name)
            }

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, "ART for life", DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

        }



        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.PMTCT.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        if (lifeART){

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPmtct2())
            ft.addToBackStack(null)
            ft.commit()

        }else{

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPmtct3())
            ft.addToBackStack(null)
            ft.commit()

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