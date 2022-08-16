package com.intellisoft.kabarakmhis.new_designs.clinical_notes

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
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
    private lateinit var kabarakViewModel: KabarakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_add)

        title = "Add Clinical Notes"

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        kabarakViewModel = KabarakViewModel(application)

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        handleNavigation()

        tvNextVisit.setOnClickListener { createDialog(999) }
    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Preview"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    override fun onStart() {
        super.onStart()

        getUserData()
    }

    private fun getUserData() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        tvPatient.text = patientName
        tvAncId.text = identifier

    }

    private fun saveData() {

        val clinicalNotes = etClinicalNotes.text.toString()
        val appointmentDate = tvNextVisit.text.toString()

        if (!TextUtils.isEmpty(clinicalNotes)){

            val todayDate = formatter.getTodayDateNoTime()
            val dbObserveValueList = ArrayList<DbDataList>()

            val dbClinicalValue = DbDataList("Clinical Note", clinicalNotes, DbSummaryTitle.CLINICAL_NOTES.name, DbResourceType.Observation.name, DbObservationValues.CLINICAL_NOTES.name)
            val dbNextValue = DbDataList("Next Appointment", appointmentDate, DbSummaryTitle.CLINICAL_NOTES.name, DbResourceType.Observation.name, DbObservationValues.CLINICAL_NOTES_DATE.name)
            val dbTodayValue = DbDataList("Date Collected", todayDate, DbSummaryTitle.CLINICAL_NOTES.name, DbResourceType.Observation.name , DbObservationValues.CLINICAL_NOTES_DATE.name)

            dbObserveValueList.addAll(listOf(dbClinicalValue, dbNextValue, dbTodayValue))

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbObserveValueList)
            dbDataDetailsList.add(dbDataDetails)

            formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.CLINICAL_NOTES.name)

            val dbPatientData = DbPatientData(DbResourceViews.CLINICAL_NOTES.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(this, dbPatientData)

            val intent = Intent(this, ConfirmPage::class.java)
            startActivity(intent)




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