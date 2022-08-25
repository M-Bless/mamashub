package com.intellisoft.kabarakmhis.new_designs.matenal_serology

import android.app.Application
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_maternal_serology.*
import kotlinx.android.synthetic.main.activity_maternal_serology.navigation
import kotlinx.android.synthetic.main.activity_maternal_serology.tvDate
import kotlinx.android.synthetic.main.activity_maternal_serology.tvNextVisit
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MaternalSerology : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maternal_serology)

        title = "Maternal Serology Repeat Testing"
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        kabarakViewModel = KabarakViewModel(application)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        radioGrpRepeatSerology.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearRepeatYes, true)
                    changeVisibility(linearRepeatNo, false)
                } else {
                    changeVisibility(linearRepeatNo, true)
                    changeVisibility(linearRepeatYes, false)
                    changeVisibility(linearNoReactive, false)
                    changeVisibility(linearReactive, false)
                    radioGrpTestResults.clearCheck()
                }

            }
        }
        radioGrpTestResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup?.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton?.isChecked
            if (isChecked == true) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "R") {
                    changeVisibility(linearReactive, true)
                    changeVisibility(linearNoReactive, false)
                } else {
                    changeVisibility(linearNoReactive, true)
                    changeVisibility(linearReactive, false)
                }

            }
        }

        tvNextVisit.setOnClickListener { createDialog(999) }
        tvNoNextAppointment.setOnClickListener { createDialog(998) }
        tvDate.setOnClickListener { createDialog(997) }

        handleNavigation()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener1, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener2, year, month, day)
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
            tvNextVisit.text = date



        }
    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener1 =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvNoNextAppointment.text = date



        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener2 =
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

    override fun onStart() {
        super.onStart()

        getUserData()

        getSavedData()
    }



    private fun getUserData() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        tvPatient.text = patientName
        tvAncId.text = identifier

    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Preview"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

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
        val errorList = ArrayList<String>()

        val repeatSerology = formatter.getRadioText(radioGrpRepeatSerology)
        if (repeatSerology != ""){

            val repeatSerologyValue = DbDataList("Was repeat serology test done", repeatSerology,
                DbSummaryTitle.A_MATERNAL_SEROLOGY.name, DbResourceType.Observation.name,
                DbObservationValues.REPEAT_SEROLOGY.name)

            dbDataList.add(repeatSerologyValue)


            if (linearRepeatNo.visibility == View.VISIBLE){
                val nextAppointment = tvNoNextAppointment.text.toString()
                if (!TextUtils.isEmpty(nextAppointment)){

                    val valueName = DbDataList("Date of Next Appointment", nextAppointment,
                        DbSummaryTitle.A_MATERNAL_SEROLOGY.name, DbResourceType.Observation.name,
                        DbObservationValues.REPEAT_SEROLOGY_RESULTS_NO.name)

                    dbDataList.add(valueName)

                }else{
                    errorList.add("Date of Next Appointment is required")
                }

            }


            if (linearRepeatYes.visibility == View.VISIBLE){
                val testDoneDate = tvDate.text.toString()
                if (!TextUtils.isEmpty(testDoneDate)){
                    val valueName = DbDataList("Date Test was done", testDoneDate,
                        DbSummaryTitle.A_MATERNAL_SEROLOGY.name, DbResourceType.Observation.name,
                        DbObservationValues.REPEAT_SEROLOGY_RESULTS_YES.name)
                    dbDataList.add(valueName)
                }else{
                    errorList.add("Test Done Date is required")
                }


                val radioGrpTestResults = formatter.getRadioText(radioGrpTestResults)
                if (radioGrpTestResults != ""){

                    val valueName = DbDataList("Test Results", radioGrpTestResults,
                        DbSummaryTitle.A_MATERNAL_SEROLOGY.name, DbResourceType.Observation.name,
                        DbObservationValues.REPEAT_SEROLOGY_DETAILS.name)
                    dbDataList.add(valueName)

                    if (linearReactive.visibility == View.VISIBLE){
                        val pmtctClinic = etPMTCTClinic.text.toString()
                        val partnerTested = etTestPartner.text.toString()

                        if (!TextUtils.isEmpty(pmtctClinic) && !TextUtils.isEmpty(partnerTested)){

                            val valueName1 = DbDataList("Refer PMTCT Clinic", pmtctClinic,
                                DbSummaryTitle.B_REACTIVE.name, DbResourceType.Observation.name, DbObservationValues.REACTIVE_MATERNAL_SEROLOGY_PMTCT.name)
                            val valueName2 = DbDataList("Partner Test", partnerTested,
                                DbSummaryTitle.B_REACTIVE.name, DbResourceType.Observation.name, DbObservationValues.PARTNER_REACTIVE_SEROLOGY.name)

                            dbDataList.addAll(listOf(valueName1, valueName2))

                        }else{

                            if (TextUtils.isEmpty(pmtctClinic)) errorList.add("Refer PMTCT Clinic is required")
                            if (TextUtils.isEmpty(partnerTested)) errorList.add("Partner Test is required")
                        }

                    }
                    if (linearNoReactive.visibility == View.VISIBLE){
                        val bookSerology = etRepeatSerology.text.toString()
                        val breastFeeding = etContinueTest.text.toString()
                        val nextVisit = tvNextVisit.text.toString()

                        if (!TextUtils.isEmpty(bookSerology) && !TextUtils.isEmpty(breastFeeding) && !TextUtils.isEmpty(nextVisit)){

                            val valueName1 = DbDataList("Book Serology Test", bookSerology,
                                DbSummaryTitle.C_NON_REACTIVE.name, DbResourceType.Observation.name,DbObservationValues.NON_REACTIVE_SEROLOGY_BOOK.name)
                            val valueName2 = DbDataList("Complete Breastfeeding Cessation", breastFeeding,
                                DbSummaryTitle.C_NON_REACTIVE.name, DbResourceType.Observation.name,DbObservationValues.NON_REACTIVE_SEROLOGY_CONTINUE_TEST.name)
                            val valueName3 = DbDataList("Next appointment", nextVisit,
                                DbSummaryTitle.C_NON_REACTIVE.name, DbResourceType.Observation.name, DbObservationValues.NON_REACTIVE_SEROLOGY_APPOINTMENT.name)

                            dbDataList.addAll(listOf(valueName1, valueName2, valueName3))

                        }else{

                            if (TextUtils.isEmpty(bookSerology)) errorList.add("Book Serology Test is required")
                            if (TextUtils.isEmpty(breastFeeding)) errorList.add("Complete Breastfeeding Cessation is required")
                            if (TextUtils.isEmpty(nextVisit)) errorList.add("Next appointment is required")

                        }

                    }

                }else{
                    errorList.add("Test Results is required")
                }

            }


        }else{
            errorList.add("Repeat Serology is required")
        }

        if (errorList.size == 0) {

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)

            val dbPatientData = DbPatientData(DbResourceViews.MATERNAL_SEROLOGY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(this, dbPatientData)

            formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.MATERNAL_SEROLOGY.name)

            val intent = Intent(this, ConfirmPage::class.java)
            startActivity(intent)

        }else{
            formatter.showErrorDialog(errorList, this)
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

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(this@MaternalSerology,
                    DbResourceViews.MATERNAL_SEROLOGY.name)

                if (encounterId != null){

                    val repeatSerology = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REPEAT_SEROLOGY.name), encounterId)

                    val yesRepeatSerology = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REPEAT_SEROLOGY_RESULTS_YES.name), encounterId)
                    val noRepeatSerology = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REPEAT_SEROLOGY_RESULTS_NO.name), encounterId)

                    val repeatSerologyResults = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REPEAT_SEROLOGY_DETAILS.name), encounterId)
                    val repeatSerologyPmtct = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REACTIVE_MATERNAL_SEROLOGY_PMTCT.name), encounterId)
                    val repeatSerologyPartner = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PARTNER_REACTIVE_SEROLOGY.name), encounterId)
                    val nonReactiveBook = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NON_REACTIVE_SEROLOGY_BOOK.name), encounterId)
                    val nonReactiveContinue = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NON_REACTIVE_SEROLOGY_CONTINUE_TEST.name), encounterId)
                    val nonReactiveNextAppointment = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NON_REACTIVE_SEROLOGY_APPOINTMENT.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {
                        if (repeatSerology.isNotEmpty()){
                            val value = repeatSerology[0].value
                            if (value.contains("Yes", ignoreCase = true)) radioGrpRepeatSerology.check(R.id.radioBtnSeroYes)
                            if (value.contains("No", ignoreCase = true)) radioGrpRepeatSerology.check(R.id.radioBtnSeroNo)
                        }
                        if (yesRepeatSerology.isNotEmpty()){
                            val value = yesRepeatSerology[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvDate.setText(valueNo)
                        }
                        if (noRepeatSerology.isNotEmpty()){
                            val value = noRepeatSerology[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvNoNextAppointment.setText(valueNo)
                        }
                        if (repeatSerologyResults.isNotEmpty()){
                            val value = repeatSerologyResults[0].value
                            if (value.contains("R", ignoreCase = true)) radioGrpTestResults.check(R.id.radioBtnTestR)
                            if (value.contains("NR", ignoreCase = true)) radioGrpTestResults.check(R.id.radioBtnTestNR)
                        }
                        if (repeatSerologyPmtct.isNotEmpty()){
                            val value = repeatSerologyPmtct[0].value
                            etPMTCTClinic.setText(value)
                        }
                        if (repeatSerologyPartner.isNotEmpty()){
                            val value = repeatSerologyPartner[0].value
                            etTestPartner.setText(value)
                        }
                        if (nonReactiveBook.isNotEmpty()){
                            val value = nonReactiveBook[0].value
                            etRepeatSerology.setText(value)
                        }
                        if (nonReactiveContinue.isNotEmpty()){
                            val value = nonReactiveContinue[0].value
                            etContinueTest.setText(value)
                        }
                        if (nonReactiveNextAppointment.isNotEmpty()){
                            val value = nonReactiveNextAppointment[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvNextVisit.setText(valueNo)
                        }


                    }


                }


            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}