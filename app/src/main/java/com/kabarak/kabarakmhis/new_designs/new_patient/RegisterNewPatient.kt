package com.kabarak.kabarakmhis.new_designs.new_patient

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.NewMainActivity
import com.kabarak.kabarakmhis.new_designs.data_class.*
import kotlinx.android.synthetic.main.activity_register_new_patient.*
import kotlinx.android.synthetic.main.patient_list_item_view.*
import org.hl7.fhir.r4.model.*
import java.util.*


class RegisterNewPatient : AppCompatActivity() {

    private lateinit var mySpinner: Spinner
    private val retrofitCallsFhir = RetrofitCallsFhir()

    private var formatter = FormatterClass()
    private val newPatient1 = DbResourceViews.NEW_PATIENT_1.name
    private val newPatient2 = DbResourceViews.NEW_PATIENT_2.name

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_patient)

        title = "Register New Patient"

        formatter.saveSharedPreference(this, "totalPages", "2")

        if (savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                newPatient1 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPatientDetails())
                    formatter.saveCurrentPage("1", this)
                }
                newPatient2 -> {
                    ft.replace(R.id.fragmentHolder, FragmentPatientInfo())
                    formatter.saveCurrentPage("2", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentPatientDetails())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, NewMainActivity::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun createPatient() {

//        val clientName = etClientName.text.toString()
//        val dob = etDoB.text.toString()
//        val telephone = etTelePhone.text.toString()
//
//        val town = etTown.text.toString()
//        val estate = etEstate.text.toString()
//        val address = etAddress.text.toString()
//
//        val kinName = etKinName.text.toString()
//        val telephoneKin = etTelePhonKin.text.toString()
//
//        val facilityName = etFacilityName.text.toString()
//        val kmhflCode = etKmhflCode.text.toString()
//        val anc = etAnc.text.toString()
//        val pnc = etPnc.text.toString()
//
//        val age = etAge.text.toString()
//        val gravida = etGravida.text.toString()
//        val parity = etParity.text.toString()
//
//        val height = etHeight.text.toString()
//        val weight = etWeight.text.toString()
//        val lmp = etLmp.text.toString()
//
//        if (
//            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) && !TextUtils.isEmpty(clientName) &&
//            !TextUtils.isEmpty(dob) && !TextUtils.isEmpty(gravida) && !TextUtils.isEmpty(parity) &&
//            !TextUtils.isEmpty(height) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(lmp) &&
//            !TextUtils.isEmpty(town) && !TextUtils.isEmpty(estate) && !TextUtils.isEmpty(address) &&
//            !TextUtils.isEmpty(telephone) && !TextUtils.isEmpty(kinName) && !TextUtils.isEmpty(telephoneKin)){
//
//            val nameList = ArrayList<DbName>()
//            val givenNameList = ArrayList<String>()
//            givenNameList.add(clientName)
//            val dbName = DbName(clientName, givenNameList)
//            nameList.add(dbName)
//
//            val telecomList = ArrayList<DbTelecom>()
//            val dbTelecom = DbTelecom("phone", telephone)
//            telecomList.add(dbTelecom)
//
//            val addressList = ArrayList<DbAddress>()
//            val addressData = DbAddress(town, ArrayList(), address, estate, SYNC_VALUE, "Ke")
//            addressList.add(addressData)
//
//            val contactList = ArrayList<DbContact>()
//            val relationship = ArrayList<DbRshp>()
//            val dbRshp = DbRshp(spinnerRshpValue)
//            relationship.add(dbRshp)
//
//            val givenKinNameList = ArrayList<String>()
//            givenKinNameList.add(clientName)
//            val dbKinName = DbName(clientName, givenKinNameList)
//
//            val kinTelecomList = ArrayList<DbTelecom>()
//            val kinDbTelecom = DbTelecom("phone", telephoneKin)
//            kinTelecomList.add(kinDbTelecom)
//
//            val dbContact = DbContact(relationship, dbKinName, kinTelecomList)
//            contactList.add(dbContact)
//
//            val dbPatient = DbPatient(
//                DbResourceType.Patient.name, FormatterClass().generateUuid(), true,
//                nameList, telecomList, "female", dob, addressList, contactList)
//
//            retrofitCallsFhir.createPatient(this, dbPatient)
//
//
//
//        }

    }






}