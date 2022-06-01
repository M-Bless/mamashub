package com.intellisoft.kabarakmhis.new_designs.screens

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import ca.uhn.fhir.context.FhirContext
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.data.SYNC_VALUE
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import kotlinx.android.synthetic.main.activity_register_new_patient.*
import org.hl7.fhir.r4.model.*
import java.util.*
import kotlin.collections.ArrayList


class RegisterNewPatient : AppCompatActivity() {

    private lateinit var mySpinner: Spinner
    private val retrofitCallsFhir = RetrofitCallsFhir()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_patient)

        initiateSpinners()

        btnSave.setOnClickListener {

            try {

                createPatient()

            }catch (e: Exception){
                Log.e("-----" , "-----")
                println(e)
            }



        }
    }

    private fun createPatient() {

        val clientName = etClientName.text.toString()
        val dob = etDoB.text.toString()
        val telephone = etTelePhone.text.toString()
        val spinnerMarital = spinnerMarital.selectedItem.toString()

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



        val spinnerEducation = spinnerEducation.selectedItem.toString()
        val spinnerRshp = spinnerRshp.selectedItem.toString()

        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) && !TextUtils.isEmpty(clientName) &&
            !TextUtils.isEmpty(dob) && !TextUtils.isEmpty(gravida) && !TextUtils.isEmpty(parity) &&
            !TextUtils.isEmpty(height) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(lmp) &&
            !TextUtils.isEmpty(town) && !TextUtils.isEmpty(estate) && !TextUtils.isEmpty(address) &&
            !TextUtils.isEmpty(telephone) && !TextUtils.isEmpty(kinName) && !TextUtils.isEmpty(telephoneKin)
        ){

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

            val dbPatient = DbPatient(
                DbResourceType.Patient.name, FormatterClass().generateUuid(), true,
                nameList, telecomList, "female", dob, addressList)

            retrofitCallsFhir.createPatient(this, dbPatient)

        }

    }


    private fun initiateSpinners() {



    }


}