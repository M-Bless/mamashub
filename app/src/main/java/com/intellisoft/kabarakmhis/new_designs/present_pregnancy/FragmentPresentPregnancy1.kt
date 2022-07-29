package com.intellisoft.kabarakmhis.new_designs.present_pregnancy

import android.app.Application
import android.app.DatePickerDialog
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
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal1
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.FragmentAntenatal2
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.linearUrine
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.navigation
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.radioGrpHb
import kotlinx.android.synthetic.main.fragment_present_preg_1.view.radioGrpUrineResults
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*


class FragmentPresentPregnancy1 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var contactNumberList = arrayOf("","1st", "2nd", "3rd", "4th", "5th", "6th", "7th")
    private var spinnerContactNumberValue  = contactNumberList[0]

    private var observationList = mutableMapOf<String, String>()
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

        rootView = inflater.inflate(R.layout.fragment_present_preg_1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

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


        handleNavigation()

        return rootView
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

        val systolic = rootView.etSystolicBp.text.toString()
        val diastolic = rootView.etDiastolicBp.text.toString()
        val gestation = rootView.etGestation.text.toString()
        val fundalHeight = rootView.etFundal.text.toString()
        val muac = rootView.etMuac.text.toString()


        if (rootView.linearHbReading.visibility == View.VISIBLE){
            val text = rootView.etHbReading.text.toString()
            addData("Hb Testing Done",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpHb)
            addData("Hb Testing Done",text)
        }
        val date = rootView.tvDate.text.toString()


        if (
            !TextUtils.isEmpty(systolic) && !TextUtils.isEmpty(diastolic)
            && !TextUtils.isEmpty(fundalHeight) && !TextUtils.isEmpty(gestation)
            && !TextUtils.isEmpty(date) && !TextUtils.isEmpty(muac)
        ){

            if (formatter.validateMuac(muac)){

                if (rootView.linearUrine.visibility == View.VISIBLE){
                    val text = rootView.etUrineResults.text.toString()
                    addData("Urine Results",text)
                }else{
                    val text = formatter.getRadioText(rootView.radioGrpUrineResults)
                    addData("Urine Results",text)
                }
                addData("MUAC",muac)
                addData("Pregnancy Contact",spinnerContactNumberValue)
                for (items in observationList){

                    val key = items.key
                    val value = observationList.getValue(key)

                    val data = DbDataList(key, value, "Current Pregnancy Details", DbResourceType.Observation.name)
                    dbDataList.add(data)

                }
                observationList.clear()


                addData("Systolic Blood Pressure",systolic)
                addData("Diastolic Blood Pressure",diastolic)
                for (items in observationList){

                    val key = items.key
                    val value = observationList.getValue(key)

                    val data = DbDataList(key, value, "Blood Pressure", DbResourceType.Observation.name)
                    dbDataList.add(data)

                }
                observationList.clear()

                addData("Gestation (Weeks)",gestation)
                addData("Fundal Height",fundalHeight)
                addData("Date",date)
                for (items in observationList){

                    val key = items.key
                    val value = observationList.getValue(key)

                    val data = DbDataList(key, value, "Hb Test", DbResourceType.Observation.name)
                    dbDataList.add(data)

                }
                observationList.clear()


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
                Toast.makeText(requireContext(), "Please enter valid MUAC", Toast.LENGTH_SHORT).show()
            }



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
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
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
}