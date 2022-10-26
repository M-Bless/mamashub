package com.kabarak.kabarakmhis.new_designs.malaria_propylaxis

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmPage
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_add.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.navigation
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.tvAncId
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.tvDate
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.tvPatient

import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MalariaProphylaxis : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private val formatter = FormatterClass()
    private var observationList = mutableMapOf<String, DbObservationLabel>()

    var contactNumberList = arrayOf("","ANC Contact 1", "ANC Contact 2", "ANC Contact 3", "ANC Contact 4", "ANC Contact 5", "ANC Contact 6", "ANC Contact 7")
    private var spinnerContactNumberValue  = contactNumberList[0]

    private lateinit var kabarakViewModel: KabarakViewModel

    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malaria_prophylaxis)

        title = "Malaria Prophylaxis"

        initSpinner()
        kabarakViewModel = KabarakViewModel(application)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

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
                    changeVisibility(linearLLINNo, false)
                } else {
                    changeVisibility(linearInciside, false)
                    changeVisibility(linearLLINNo, true)
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
        tvNoNextAppointment.setOnClickListener { createDialog(995) }
        tvIptpNextVisit.setOnClickListener { createDialog(994) }

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
                    myDateDoseListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDateGvnListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateNextVisitListener, year, month, day)
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
            996 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateLLITNDateListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            995 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateLLITNNextDateListener, year, month, day)
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
            994 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateNoNextVisit, year, month, day)
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
    private val myDateLLITNNextDateListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        tvNoNextAppointment.text = date

        }
    private val myDateNoNextVisit = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        tvIptpNextVisit.text = date

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

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val dose = tvDose.text.toString()
        val visitNext = tvDate.text.toString()

        val repeatSerology = formatter.getRadioText(radioGrpLLTIN)
        if (repeatSerology != ""){

            addData("Was LLTN given to the mother ", repeatSerology, DbObservationValues.LLITN_GIVEN.name)

            if(repeatSerology == "No"){

                if (linearLLINNo.visibility == View.VISIBLE){

                    val llinNo = tvNoNextAppointment.text.toString()
                    if(!TextUtils.isEmpty(llinNo)){
                        addData("If LLITN is not given: ", llinNo, DbObservationValues.LLITN_GIVEN_NEXT_DATE.name)
                    }else{
                        errorList.add("Please enter LLIN No")
                    }
                }

            }

            if (repeatSerology == "Yes"){

                val netInsecticide = tvNetDate.text. toString()
                if (!TextUtils.isEmpty(netInsecticide)){
                    addData("LLITN Given Date", netInsecticide, DbObservationValues.LLITN_RESULTS.name)
                }else{
                    errorList.add("LLITN Given Date is required")
                }

            }



        }else{
            errorList.add("Repeat Serology is required")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.B_LLITN_GIVEN.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val iptpValue = formatter.getRadioText(radioGrpIPTp)

        if (spinnerContactNumberValue != "" && !TextUtils.isEmpty(dose) && iptpValue != "") {

            addData("ANC Contact", spinnerContactNumberValue, DbObservationValues.ANC_CONTACT.name)
            addData("Dose Date", dose, DbObservationValues.DOSAGE_DATE_GIVEN.name)
            addData("Next Appointment", visitNext, DbObservationValues.NEXT_VISIT_DATE.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_ANC_VISIT.name,
                    DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            addData("IPTp", iptpValue, DbObservationValues.IPTP_DATE.name)
            if (linearIPTpYes.visibility == View.VISIBLE){

                val iptpYes = tvTDInjection.text.toString()
                if(!TextUtils.isEmpty(iptpYes)){
                    addData("If IPTP is given: ", iptpYes, DbObservationValues.IPTP_RESULT_YES.name)
                }else{
                    errorList.add("Please enter date IPTP was given")
                }
            }

            if (linearIPTpNo.visibility == View.VISIBLE){

                val iptpNo = tvIptpNextVisit.text.toString()
                if(!TextUtils.isEmpty(iptpNo)){
                    addData("If IPTP is not given: ", iptpNo, DbObservationValues.IPTP_RESULT_NO.name)
                }else{
                    errorList.add("Please enter date IPTP was not given")
                }
            }


            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_ANC_VISIT.name,
                    DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()


        }else{

            if (spinnerContactNumberValue == "") errorList.add("ANC Contact is required")
            if (TextUtils.isEmpty(dose)) errorList.add("Dose Date is required")
            if (iptpValue == "") errorList.add("IPTp is required")

        }

        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.MALARIA_PROPHYLAXIS.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(this, dbPatientData)

            formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.MALARIA_PROPHYLAXIS.name)

            val intent = Intent(this, ConfirmPage::class.java)
            startActivity(intent)

        }else{
            formatter.showErrorDialog(errorList, this)
        }




    }

    override fun onStart() {
        super.onStart()

        getUserData()

        getData()
    }


    private fun getUserData() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")

        tvPatient.text = patientName
        tvAncId.text = identifier

    }

    private fun addData(key: String, value: String, codeLabel:String) {
        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
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
            R.id.spinnerContact -> {
                spinnerContactNumberValue = spinnerContact.selectedItem.toString()
                contactNumberList.forEachIndexed { index, item ->

                    if (item == spinnerContactNumberValue){
                        tvIptp.text = "IPTp - SP dose $index"
                    }

                }

            }
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

    private fun getData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(this@MalariaProphylaxis,
                    DbResourceViews.MALARIA_PROPHYLAXIS.name)

                if (encounterId != null){

                    val ancTimings = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ANC_CONTACT.name), encounterId)
                    val dose = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DOSAGE_DATE_GIVEN.name), encounterId)

                    val iptp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IPTP_DATE.name), encounterId)
                    val yesDateGiven = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IPTP_RESULT_YES.name), encounterId)
                    val noNextVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IPTP_RESULT_NO.name), encounterId)
                    val nextVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name), encounterId)
                    val lltn = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.LLITN_GIVEN.name), encounterId)
                    val lltnYesDateGiven = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.LLITN_RESULTS.name), encounterId)
                    val lltnNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.LLITN_GIVEN_NEXT_DATE.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        Log.e("DOSE", dose.toString())

                        if (ancTimings.isNotEmpty()){
                            val value = ancTimings[0].value
                            val valueNo = value.substring(0, value.length-1)
                            spinnerContact.setSelection(contactNumberList.indexOf(valueNo))
                        }
                        if (dose.isNotEmpty()){
                            val value = dose[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvDose.setText(valueNo)
                        }
                        if (iptp.isNotEmpty()){
                            val value = iptp[0].value
                            if (value.contains("Yes", ignoreCase = true)) radioGrpIPTp.check(R.id.radioBtnYes)
                            if (value.contains("No", ignoreCase = true)) radioGrpIPTp.check(R.id.radioBtnNo)
                        }
                        if (yesDateGiven.isNotEmpty()){
                            val value = yesDateGiven[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvTDInjection.setText(valueNo)
                        }
                        if (noNextVisit.isNotEmpty()){
                            val value = noNextVisit[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvIptpNextVisit.setText(valueNo)
                        }
                        if (nextVisit.isNotEmpty()){
                            val value = nextVisit[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvDate.setText(valueNo)
                        }
                        if (lltn.isNotEmpty()){
                            val value = lltn[0].value
                            if (value.contains("Yes", ignoreCase = true)) radioGrpLLTIN.check(R.id.radioBtnLLTINYes)
                            if (value.contains("No", ignoreCase = true)) radioGrpLLTIN.check(R.id.radioBtnLLTINNo)
                        }
                        if (lltnYesDateGiven.isNotEmpty()){
                            val value = lltnYesDateGiven[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvNetDate.setText(valueNo)
                        }
                        if (lltnNo.isNotEmpty()){
                            val value = lltnNo[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvNoNextAppointment.setText(valueNo)
                        }

                    }


                }


            }

        } catch (e: Exception){
            e.printStackTrace()
        }


    }
}