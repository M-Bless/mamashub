package com.intellisoft.kabarakmhis.new_designs.clinical_notes

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_add.*
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList

class ClinicalNotesAdd : AppCompatActivity() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_add)

        title = "Add Clinical Notes"

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        handleNavigation()

        tvNextVisit.setOnClickListener { createDialog(999) }
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Save"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun saveData() {

        val clinicalNotes = etClinicalNotes.text.toString()
        val appointmentDate = tvNextVisit.text.toString()

        if (!TextUtils.isEmpty(clinicalNotes)){

            val todayDate = formatter.getTodayDateNoTime()
            val dbObserveValueList = ArrayList<DbObserveValue>()
            val dbClinicalValue = DbObserveValue("Clinical Note", clinicalNotes)
            val dbNextValue = DbObserveValue("Next Appointment", appointmentDate)
            val dbTodayValue = DbObserveValue("Date Collected", todayDate)

            dbObserveValueList.addAll(listOf(dbClinicalValue, dbNextValue, dbTodayValue))

            val dbCode = formatter.createObservation(dbObserveValueList, DbResourceViews.CLINICAL_NOTES.name)

            val encounterId = formatter.retrieveSharedPreference(this, DbResourceViews.CLINICAL_NOTES.name)
            if (encounterId != null){
                retrofitCallsFhir.createObservation(encounterId,this, dbCode)

            }else{
                retrofitCallsFhir.createFhirEncounter(this, dbCode, DbResourceViews.CLINICAL_NOTES.name)
            }



        }else
            etClinicalNotes.error = "Field cannot be empty"


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

            else -> null
        }


    }

    private val myDateDobListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvNextVisit.text = date

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