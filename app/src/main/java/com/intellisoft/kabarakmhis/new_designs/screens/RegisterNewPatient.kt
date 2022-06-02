package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.data.SYNC_VALUE
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import kotlinx.android.synthetic.main.activity_register_new_patient.*
import kotlinx.android.synthetic.main.patient_list_item_view.*
import org.hl7.fhir.r4.model.*
import java.util.*


class RegisterNewPatient : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var mySpinner: Spinner
    private val retrofitCallsFhir = RetrofitCallsFhir()

    var maritalStatusList = arrayOf("Married", "Widowed", "Single", "Divorced", "Separated")
    var educationList = arrayOf("Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")

    private var spinnerMaritalValue  = maritalStatusList[0]
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var datePicker : DatePicker
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_patient)

        initiateSpinners()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        etDoB.setOnClickListener {
            showDialog(999)

        }
        etLmp.setOnClickListener {
            showDialog(998)
        }

        btnSave.setOnClickListener {

            try {

                createPatient()

            }catch (e: Exception){
                Log.e("-----" , "-----")
                println(e)
            }



        }
    }


    override fun onCreateDialog(id: Int): Dialog? {
        // TODO Auto-generated method stub

        return when (id) {
            999 -> {
                DatePickerDialog(
                    this,
                    myDateDobListener, year, month, day
                )
            }
            998 -> {
                DatePickerDialog(
                    this,
                    myDateLMPListener, year, month, day
                )
            }
            else -> null
        }


    }

    private val myDateDobListener =
        OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            etDoB.text = date
        }

    private val myDateLMPListener =
        OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            etLmp.text = date
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

    private fun createPatient() {

        val clientName = etClientName.text.toString()
        val dob = etDoB.text.toString()
        val telephone = etTelePhone.text.toString()

        val town = etTown.text.toString()
        val estate = etEstate.text.toString()
        val address = etAddress.text.toString()

        val kinName = etKinName.text.toString()
        val telephoneKin = etTelePhonKin.text.toString()

        val facilityName = etFacilityName.text.toString()
        val kmhflCode = etKmhflCode.text.toString()
        val anc = etAnc.text.toString()
        val pnc = etPnc.text.toString()

        val age = etAge.text.toString()
        val gravida = etGravida.text.toString()
        val parity = etParity.text.toString()

        val height = etHeight.text.toString()
        val weight = etWeight.text.toString()
        val lmp = etLmp.text.toString()

        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) && !TextUtils.isEmpty(clientName) &&
            !TextUtils.isEmpty(dob) && !TextUtils.isEmpty(gravida) && !TextUtils.isEmpty(parity) &&
            !TextUtils.isEmpty(height) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(lmp) &&
            !TextUtils.isEmpty(town) && !TextUtils.isEmpty(estate) && !TextUtils.isEmpty(address) &&
            !TextUtils.isEmpty(telephone) && !TextUtils.isEmpty(kinName) && !TextUtils.isEmpty(telephoneKin)){

            val nameList = ArrayList<DbName>()
            val givenNameList = ArrayList<String>()
            givenNameList.add(clientName)
            val dbName = DbName(clientName, givenNameList)
            nameList.add(dbName)

            val telecomList = ArrayList<DbTelecom>()
            val dbTelecom = DbTelecom("phone", telephone)
            telecomList.add(dbTelecom)

            val addressList = ArrayList<DbAddress>()
            val addressData = DbAddress(town, ArrayList(), address, estate, SYNC_VALUE, "Ke")
            addressList.add(addressData)

            val contactList = ArrayList<DbContact>()
            val relationship = ArrayList<DbRshp>()
            val dbRshp = DbRshp(spinnerRshpValue)
            relationship.add(dbRshp)

            val givenKinNameList = ArrayList<String>()
            givenKinNameList.add(clientName)
            val dbKinName = DbName(clientName, givenKinNameList)

            val kinTelecomList = ArrayList<DbTelecom>()
            val kinDbTelecom = DbTelecom("phone", telephoneKin)
            kinTelecomList.add(kinDbTelecom)

            val dbContact = DbContact(relationship, dbKinName, kinTelecomList)
            contactList.add(dbContact)

            val dbPatient = DbPatient(
                DbResourceType.Patient.name, FormatterClass().generateUuid(), true,
                nameList, telecomList, "female", dob, addressList, contactList)

            retrofitCallsFhir.createPatient(this, dbPatient)



        }

    }


    private fun initiateSpinners() {

        val maritalStatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, maritalStatusList)
        maritalStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMarital!!.adapter = maritalStatus

        val levelOfEducation = ArrayAdapter(this, android.R.layout.simple_spinner_item, educationList)
        levelOfEducation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEducation!!.adapter = levelOfEducation


        val kinRshp = ArrayAdapter(this, android.R.layout.simple_spinner_item, relationshipList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRshp!!.adapter = kinRshp

        spinnerMarital.onItemSelectedListener = this
        spinnerEducation.onItemSelectedListener = this
        spinnerRshp.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        // use position to know the selected item

        when (arg0.id) {
            R.id.spinnerMarital -> {spinnerMaritalValue = spinnerMarital.selectedItem.toString()}
            R.id.spinnerEducation -> {spinnerEducationValue = spinnerEducation.selectedItem.toString()}
            R.id.spinnerRshp -> {

                spinnerRshpValue = spinnerRshp.selectedItem.toString()
                Log.e("------ ", spinnerRshpValue!!)
            }
            else -> {}
        }

    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }


}