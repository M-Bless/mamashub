package com.intellisoft.kabarakmhis.new_designs.tetanus_diptheria

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_preventive_service.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*

class PreventiveService : AppCompatActivity() {

    private val formatter = FormatterClass()
    private var observationList = mutableMapOf<String, String>()
    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar
    private lateinit var kabarakViewModel: KabarakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preventive_service)

        title = "Preventive Service"
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        kabarakViewModel = KabarakViewModel(application)

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        radioGrpTD.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearTTYes, true)
                } else {
                    changeVisibility(linearTTYes, false)
                }

            }
        }

        tvDate.setOnClickListener { createDialog(999) }
        tvTTDate.setOnClickListener { createDialog(998) }


        
        handleNavigation()

    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Confirm"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDobListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDobListener1, year, month, day)
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
            tvDate.text = date

        }

    private val myDateDobListener1 =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvTTDate.text = date

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

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }

    private fun saveData() {

        val date = tvDate.text.toString()
        if (!TextUtils.isEmpty(date)){
            addData("Next Visit",date)
        }

        val repeatSerology = formatter.getRadioText(radioGrpTD)
        if (repeatSerology != ""){
            val ttImmunization = tvTTDate.text. toString()
            if (!TextUtils.isEmpty(ttImmunization)){
                addData("Immunization Date",ttImmunization)
            }

        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Tetanus Diphtheria", DbResourceType.Observation.name)
            dbDataList.add(data)

        }



        val dbDataDetailsList = ArrayList<DbDataDetails>()


        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.PREVENTIVE_SERVICE.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(this, dbPatientData)

        formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.PREVENTIVE_SERVICE.name)

        val intent = Intent(this, ConfirmPage::class.java)
        startActivity(intent)


//        formatter.saveToFhir(dbPatientData, this, DbResourceViews.PREVENTIVE_SERVICE.name)
//
//        startActivity(Intent(this, PatientProfile::class.java))


    }

    private fun addData(key: String, value: String) {
        observationList[key] = value
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