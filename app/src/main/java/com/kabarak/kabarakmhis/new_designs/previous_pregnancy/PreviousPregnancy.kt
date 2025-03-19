package com.kabarak.kabarakmhis.new_designs.previous_pregnancy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.data_class.DbObservationData
import com.kabarak.kabarakmhis.new_designs.data_class.DbObservationValue
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_previous_pregnancy.*
import kotlinx.android.synthetic.main.activity_previous_pregnancy.btnSave
import kotlinx.android.synthetic.main.activity_register_new_patient.*

class PreviousPregnancy : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val retrofitCallsFhir = RetrofitCallsFhir()

    var pregnancyList = arrayOf("","1st", "2nd", "3rd", "4th", "5th", "6th", "7th","Other")
    var babySexList = arrayOf("","Male", "Female")
    var outcomeList = arrayOf("","Alive", "Dead")
    var deliveryModeList = arrayOf("","Vaginal delivery", "Assisted vaginal delivery", "Caesarean Section","Birth weight(grams)")

    private var spinnerPregnancyOrderData  = pregnancyList[0]
    private var spinnerBabySexData = babySexList[0]
    private var spinnerOutComeData  = outcomeList[0]
    private var spinnerDeliveryModeData  = deliveryModeList[0]

    private val formatter = FormatterClass()
    private val previousPregnancy = DbResourceViews.PREVIOUS_PREGNANCY.name



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previous_pregnancy)

        title = "Previous Pregnancy"

        formatter.saveSharedPreference(this, "totalPages", "1")

        if (savedInstanceState == null){

            val ft = supportFragmentManager.beginTransaction()

            when (formatter.retrieveSharedPreference(this, "FRAGMENT")) {
                previousPregnancy -> {
                    ft.replace(R.id.fragmentHolder, FragmentPreviousPregnancy())
                    formatter.saveCurrentPage("1", this)
                }
                else -> {
                    ft.replace(R.id.fragmentHolder, FragmentPreviousPregnancy())
                    formatter.saveCurrentPage("1", this)
                }
            }

            ft.commit()


        }


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

        val deliveryMethod = ArrayAdapter(this, android.R.layout.simple_spinner_item, deliveryModeList)
        deliveryMethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeliveryMode!!.adapter = deliveryMethod

        spinnerPregnancyOrder.onItemSelectedListener = this
        spinnerBabySex.onItemSelectedListener = this
        spinnerOutCome.onItemSelectedListener = this
        spinnerDeliveryMode.onItemSelectedListener = this

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
            val deliveryMode = getList(spinnerDeliveryModeData,"Delivery Mode")

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
            hashSet.add(deliveryMode)

            DbObservationValue(hashSet)

//            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
//                DbResourceViews.PREVIOUS_PREGNANCY.name)


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
            R.id.spinnerDeliveryMode -> { spinnerDeliveryModeData = spinnerDeliveryMode.selectedItem.toString() }
            else -> {}
        }

    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

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