package com.intellisoft.kabarakmhis.new_designs.birth_plan

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_birth_plan.*

import kotlinx.android.synthetic.main.navigation.view.*
import java.text.Normalizer
import java.util.*
import kotlin.collections.ArrayList

class BirthPlan : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()

    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birth_plan)

        title = "Birth Plan"

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        etEdd.setOnClickListener { createDialog(999) }

        handleNavigation()

    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Save"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun saveData() {

        val facilityName = etFacilityName.text.toString()
        val attendantName = etAttendantName.text.toString()
        val facilityContact = etFacilityContact.text.toString()
        val supportPerson = etSupportName.text.toString()
        val transport = etTransport.text.toString()
        val bloodDonorName = etBloodName.text.toString()
        val financialPlan = etFinancialChildBirth.text.toString()
        val birthPlan = etEdd.text.toString()

        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(attendantName) &&
            !TextUtils.isEmpty(facilityContact) && !TextUtils.isEmpty(supportPerson) &&
            !TextUtils.isEmpty(transport) && !TextUtils.isEmpty(bloodDonorName) &&
            !TextUtils.isEmpty(financialPlan) && !TextUtils.isEmpty(birthPlan)){

            val birthPlanList = ArrayList<DbObserveValue>()

            val valueFacName = DbObserveValue("Facility Name", facilityName)
            val valueAttendant = DbObserveValue("Attendant Name", attendantName)
            val valFacContact = DbObserveValue("Facility Contact", facilityContact)
            val valueSupportPerson = DbObserveValue("Support Person", supportPerson)
            val valueTransport = DbObserveValue("Transport", transport)
            val valueBirthPlan = DbObserveValue("Birth Plan", birthPlan)
            val valueBloodDonor = DbObserveValue("Blood Donor Name", bloodDonorName)
            val valueFinancial = DbObserveValue("Financial Plan for Childbirth", financialPlan)

            birthPlanList.addAll(listOf(valueFacName, valueAttendant, valFacContact, valueSupportPerson,
                valueTransport, valueBloodDonor, valueFinancial, valueBirthPlan))

            val dbObservationValue = formatter.createObservation(birthPlanList,
                DbResourceViews.BIRTH_PLAN.name)

            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
                DbResourceViews.BIRTH_PLAN.name)


        }else{

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
        
    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDoseListener, year, month, day)
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
        etEdd.text = date

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