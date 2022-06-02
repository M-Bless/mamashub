package com.intellisoft.kabarakmhis.new_designs.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_previous_pregnancy.*
import kotlinx.android.synthetic.main.activity_previous_pregnancy.btnSave
import kotlinx.android.synthetic.main.activity_register_new_patient.*

class PreviousPregnancy : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val retrofitCallsFhir = RetrofitCallsFhir()

    var pregnancyList = arrayOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th","Other")
    var babySexList = arrayOf("Male", "Female")
    var outcomeList = arrayOf("Alive", "Dead")

    private var spinnerPregnancyOrderData  = pregnancyList[0]
    private var spinnerBabySexData = babySexList[0]
    private var spinnerOutComeData  = outcomeList[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previous_pregnancy)

        initSwitch()

        btnSave.setOnClickListener {

            saveData()

        }
    }

    private fun initSwitch() {

        val pregnancy = ArrayAdapter(this, android.R.layout.simple_spinner_item, pregnancyList)
        pregnancy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPregnancyOrder!!.adapter = pregnancy

        val babySex = ArrayAdapter(this, android.R.layout.simple_spinner_item, babySexList)
        babySex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBabySex!!.adapter = babySex

        val outCome = ArrayAdapter(this, android.R.layout.simple_spinner_item, outcomeList)
        outCome.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOutCome!!.adapter = outCome

        spinnerPregnancyOrder.onItemSelectedListener = this
        spinnerBabySex.onItemSelectedListener = this
        spinnerOutCome.onItemSelectedListener = this

    }

    private fun saveData() {

        val year = etYear.text.toString()
        val ancVisits = etAncVisits.text.toString()
        val placeOfBirth = etPlaceOfChildBirth.text.toString()
        val gestation = etGestation.text.toString()
        val labour = etLabour.text.toString()
        val babyWeight = etBabyWeight.text.toString()
        val purperium = etPurperium.text.toString()

        if (
            !TextUtils.isEmpty(year) && !TextUtils.isEmpty(ancVisits) && !TextUtils.isEmpty(placeOfBirth)&&
            !TextUtils.isEmpty(gestation) && !TextUtils.isEmpty(labour) && !TextUtils.isEmpty(babyWeight)&&
            !TextUtils.isEmpty(purperium)  ){

            val hashSet = HashSet<DbObservationData>()

            val yearData = getList(year,"Year")
            val ancVisitsData = getList(ancVisits,"ancVisits")
            val placeOfBirthData = getList(placeOfBirth,"placeOfBirth")
            val gestationData = getList(gestation,"gestation")
            val labourData = getList(labour,"labour")
            val babyWeightData = getList(babyWeight,"babyWeight")
            val purperiumData = getList(purperium,"purperium")

            val pregnancyData = getList(spinnerPregnancyOrderData,"Pregnancy Order")
            val babySex = getList(spinnerBabySexData,"Baby Sex")
            val outcome = getList(spinnerOutComeData,"Outcome")

            hashSet.add(yearData)
            hashSet.add(ancVisitsData)
            hashSet.add(placeOfBirthData)
            hashSet.add(gestationData)
            hashSet.add(labourData)
            hashSet.add(babyWeightData)
            hashSet.add(purperiumData)
            hashSet.add(pregnancyData)
            hashSet.add(babySex)
            hashSet.add(outcome)

            val dbObservationValue = DbObservationValue(hashSet)

            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue, DbResourceViews.PREGNANCY_DETAILS.name)


        }else{
            Toast.makeText(this, "Please fill all values", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getList(value: String, code: String): DbObservationData {

        val valueList = HashSet<String>()
        valueList.add(value)

        return DbObservationData(code, valueList)

    }
    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        // use position to know the selected item

        when (arg0.id) {
            R.id.spinnerPregnancyOrder -> {spinnerPregnancyOrderData = spinnerPregnancyOrder.selectedItem.toString()}
            R.id.spinnerBabySex -> {spinnerBabySexData = spinnerBabySex.selectedItem.toString()}
            R.id.spinnerOutCome -> { spinnerOutComeData = spinnerOutCome.selectedItem.toString() }
            else -> {}
        }

    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

}