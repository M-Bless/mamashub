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
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmPage
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_add.*
import kotlinx.android.synthetic.main.activity_preventive_service.*
import kotlinx.android.synthetic.main.activity_preventive_service.navigation
import kotlinx.android.synthetic.main.activity_preventive_service.tvAncId
import kotlinx.android.synthetic.main.activity_preventive_service.tvPatient
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class PreventiveService : AppCompatActivity() {

    private val formatter = FormatterClass()
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preventive_service)

        title = "Tetanus Diphtheria"
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        kabarakViewModel = KabarakViewModel(application)

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


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

        navigation.btnNext.text = "Preview"
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
                //Convert weeks to milliseconds

                val nextContact = formatter.retrieveSharedPreference(this, DbAncSchedule.CONTACT_WEEK.name)
                if (nextContact != null){
                    val weeks = nextContact.toInt() * 7 * 24 * 60 * 60 * 1000L
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis() + weeks
                }else{
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                }
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

        val errorList = ArrayList<String>()

        val date = tvDate.text.toString()
        if (!TextUtils.isEmpty(date)){
            addData("Next Visit",date, DbObservationValues.NEXT_VISIT_DATE.name)
        }else{
            errorList.add("Next Visit is required")
        }

        val tdProvided = formatter.getRadioText(radioGrpTD)
        if (tdProvided != ""){
            addData("Was TT Immunization provided",tdProvided, DbObservationValues.TT_PROVIDED.name)

            if (linearTTYes.visibility == View.VISIBLE){

                val ttImmunization = tvTTDate.text. toString()
                if (!TextUtils.isEmpty(ttImmunization)){
                    addData("Immunization Date",ttImmunization, DbObservationValues.TT_RESULTS.name)
                }else{
                    errorList.add("Immunization Date is required")
                }

            }

        }else{
            errorList.add("Immunization selection is required")
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_TETANUS_DIPHTHERIA.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()

        if(errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()

            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.TETENUS_DIPTHERIA.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(this, dbPatientData)

            formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.TETENUS_DIPTHERIA.name)

            val intent = Intent(this, ConfirmPage::class.java)
            startActivity(intent)

        }else{
            formatter.showErrorDialog(errorList, this)
        }


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

        getData()
    }

    private fun getData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {


                val encounterId = formatter.retrieveSharedPreference(this@PreventiveService,
                    DbResourceViews.TETENUS_DIPTHERIA.name)

                if (encounterId != null){

                    val ttImmunization = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TT_PROVIDED.name), encounterId)

                    val nextVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name), encounterId)

                    val ttResults = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TT_RESULTS.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (ttImmunization.isNotEmpty()){
                            val ttImmunizationValue = ttImmunization[0].value
                            if (ttImmunizationValue.contains("Yes", ignoreCase = true)) radioGrpTD.check(R.id.radioBtnYes)
                            if (ttImmunizationValue.contains("No", ignoreCase = true)) radioGrpTD.check(R.id.radioBtnNo)
                        }

                        if (nextVisit.isNotEmpty()){
                            val nextVisitValue = nextVisit[0].value
                            val nextVisitDate = formatter.getValues(nextVisitValue, 0)
                            tvDate.text = nextVisitDate

                        }

                        if (ttResults.isNotEmpty()){
                            val ttResultsValue = ttResults[0].value
                            val ttResultsDate = formatter.getValues(ttResultsValue, 0)
                            tvTTDate.text = ttResultsDate

                        }

                    }


                }


            }

        }catch (e: Exception){
            e.printStackTrace()
        }


    }

    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
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