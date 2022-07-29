package com.intellisoft.kabarakmhis.new_designs.deworming

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_deworming.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.*
import kotlinx.android.synthetic.main.fragment_pmtct1.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*

class Deworming : AppCompatActivity() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deworming)

        title = "Deworming"

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        kabarakViewModel = KabarakViewModel(application)

        radioGrpDeworming.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearDewormingReason, true)
                } else {
                    changeVisibility(linearDewormingReason, false)
                }
            }
        }

        tvDate.setOnClickListener {
            createDialog(999)
        }
        handleNavigation()
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Save"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun saveData() {

        val deworming = formatter.getRadioText(radioGrpDeworming)
        val dewormingList = ArrayList<DbDataList>()

        val dateGvn = tvDate.text.toString()

        if (deworming != ""){

            if (deworming == "No"){
                val value = DbDataList("Deworming given in the 2nd trimester", deworming, "Deworming", DbResourceType.Observation.name)
                dewormingList.add(value)
            }
            if (deworming == "Yes"){

                if (!TextUtils.isEmpty(dateGvn)){
                    val value1 = DbDataList("Date deworming was given", dateGvn, "Deworming", DbResourceType.Observation.name)
                    dewormingList.add(value1)
                }else{
                    Toast.makeText(this, "Please enter date", Toast.LENGTH_SHORT).show()
                }

            }

            if(dewormingList.isNotEmpty()){

                val dbDataDetailsList = ArrayList<DbDataDetails>()
                val dbDataDetails = DbDataDetails(dewormingList)
                dbDataDetailsList.add(dbDataDetails)

                val dbPatientData = DbPatientData(DbResourceViews.DEWORMING.name, dbDataDetailsList)
                kabarakViewModel.insertInfo(this, dbPatientData)

                formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.DEWORMING.name)

                val intent = Intent(this, ConfirmPage::class.java)
                startActivity(intent)

            }

//            val dbObservationValue = formatter.createObservation(dewormingList,
//                DbResourceViews.DEWORMING.name)
//
//            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
//                DbResourceViews.DEWORMING.name)

//            startActivity(Intent(this, PatientProfile::class.java))

        }else{
            Toast.makeText(this, "Please make a selection", Toast.LENGTH_SHORT).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( this,
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

            tvDate.text = date


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