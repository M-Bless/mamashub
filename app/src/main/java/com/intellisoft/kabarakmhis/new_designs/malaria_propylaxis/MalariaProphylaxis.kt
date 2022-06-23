package com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.tvDate

import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList

class MalariaProphylaxis : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private val formatter = FormatterClass()
    private var observationList = mutableMapOf<String, String>()

    var contactNumberList = arrayOf("","ANC Contact 1", "ANC Contact 2", "ANC Contact 3", "ANC Contact 4", "ANC Contact 5", "ANC Contact 6", "ANC Contact 7")
    private var spinnerContactNumberValue  = contactNumberList[0]


    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malaria_prophylaxis)

        title = "Malaria Prophylaxis"

        initSpinner()

        radioGrpIPTp.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearIPTpYes, true)
                } else {
                    changeVisibility(linearIPTpYes, false)
                }

            }
        }
        radioGrpLLTIN.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearInciside, true)
                } else {
                    changeVisibility(linearInciside, false)
                }

            }
        }

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        tvDose.setOnClickListener { createDialog(999) }
        tvTDInjection.setOnClickListener { createDialog(998) }
        tvDate.setOnClickListener { createDialog(997) }
        tvNetDate.setOnClickListener { createDialog(996) }

        handleNavigation()
        
        
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Save"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDoseListener, year, month, day)
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDateGvnListener, year, month, day)
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateNextVisitListener, year, month, day)
                datePickerDialog.show()

            }
            996 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateLLITNDateListener, year, month, day)
                datePickerDialog.show()

            }

            else -> null
        }


    }

    private val myDateDoseListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        tvDose.text = date

        }
    private val myDateDateGvnListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        tvTDInjection.text = date

        }
    private val myDateNextVisitListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day

        val date = showDate(arg1, arg2 + 1, arg3)
        tvDate.text = date

        }
    private val myDateLLITNDateListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        tvNetDate.text = date

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

    private fun saveData() {

        val dose = tvDose.text.toString()
        val visitNext = tvDate.text.toString()

        val repeatSerology = formatter.getRadioText(radioGrpLLTIN)
        if (repeatSerology != ""){
            val netInsecticide = tvNetDate.text. toString()
            addData("LLITN Given Date", netInsecticide)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Malaria Prophylaxis", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        if (spinnerContactNumberValue != "" && !TextUtils.isEmpty(dose)){
            addData("ANC Contact", spinnerContactNumberValue)
            addData("Dose Date", dose)
            addData("Next Appointment", visitNext)

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.MALARIA_PROPHYLAXIS.name, dbDataDetailsList)
            formatter.saveToFhir(dbPatientData, this, DbResourceViews.MALARIA_PROPHYLAXIS.name)

            startActivity(Intent(this, PatientProfile::class.java))

        }else{
            Toast.makeText(this, "Please select an ANC Contact", Toast.LENGTH_SHORT).show()
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

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(this, android.R.layout.simple_spinner_item, contactNumberList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerContact!!.adapter = kinRshp

        spinnerContact.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerContact -> { spinnerContactNumberValue = spinnerContact.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}