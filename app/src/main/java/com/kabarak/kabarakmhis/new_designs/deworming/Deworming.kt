package com.kabarak.kabarakmhis.new_designs.deworming

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
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmPage
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_add.*
import kotlinx.android.synthetic.main.activity_deworming.*
import kotlinx.android.synthetic.main.activity_deworming.navigation
import kotlinx.android.synthetic.main.activity_deworming.tvAncId
import kotlinx.android.synthetic.main.activity_deworming.tvPatient
import kotlinx.android.synthetic.main.fragment_antenatal2.view.*
import kotlinx.android.synthetic.main.fragment_pmtct1.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Deworming : AppCompatActivity() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deworming)

        title = "Deworming"

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        kabarakViewModel = KabarakViewModel(application)


        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

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


        getSavedData()

    }



    private fun saveData() {

        val deworming = formatter.getRadioText(radioGrpDeworming)
        val dewormingList = ArrayList<DbDataList>()

        val errorList = ArrayList<String>()

        val dateGvn = tvDate.text.toString()

        if(deworming != ""){

            val dewormingValue = DbDataList("Was deworming given in the 2nd trimester", deworming,
                DbSummaryTitle.A_DEWORMING.name, DbResourceType.Observation.name,
                DbObservationValues.DEWORMING.name)
            dewormingList.add(dewormingValue)

            if(linearDewormingReason.visibility == View.VISIBLE){

                if (!TextUtils.isEmpty(dateGvn)){
                    val value1 = DbDataList("Date deworming was given", dateGvn,
                        DbSummaryTitle.A_DEWORMING.name, DbResourceType.Observation.name, DbObservationValues.DEWORMING_DATE.name)
                    dewormingList.add(value1)
                }else{
                    errorList.add("Date deworming is required")
                }

            }

        }else{
            errorList.add("Deworming is required")
        }


        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dewormingList)
            dbDataDetailsList.add(dbDataDetails)

            val dbPatientData = DbPatientData(DbResourceViews.DEWORMING.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(this, dbPatientData)

            formatter.saveSharedPreference(this, "pageConfirmDetails", DbResourceViews.DEWORMING.name)

            val intent = Intent(this, ConfirmPage::class.java)
            startActivity(intent)

        }else{
            formatter.showErrorDialog(errorList, this)
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

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(this@Deworming,
                    DbResourceViews.DEWORMING.name)
                if (encounterId != null) {

                    val deworming = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DEWORMING.name), encounterId)

                    val dewormingDate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DEWORMING_DATE.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (deworming.isNotEmpty()){
                            val dewormingValue = deworming[0].value
                            if(dewormingValue.contains("Yes", ignoreCase = true)) radioGrpDeworming.check(R.id.radioYesBenefit)
                            if(dewormingValue.contains("No", ignoreCase = true)) radioGrpDeworming.check(R.id.radioNoBenefit)
                        }
                        if (dewormingDate.isNotEmpty()){
                            val dewormingDateValue = dewormingDate[0].value
                            val valueNo = formatter.getValues(dewormingDateValue, 0)
                            tvDate.text = valueNo
                        }

                    }


                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }


    }
}