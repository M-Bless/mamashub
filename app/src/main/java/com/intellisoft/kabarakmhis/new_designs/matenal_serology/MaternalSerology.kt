package com.intellisoft.kabarakmhis.new_designs.matenal_serology

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_maternal_serology.*
import kotlinx.android.synthetic.main.fragment_medical.view.*

class MaternalSerology : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maternal_serology)

        title = "Maternal Serology"

        btnSave.setOnClickListener {

            saveData()

        }

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

    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }

    private fun saveData() {

        val repeatSerology = formatter.getRadioText(radioGrpRepeatSerology)
        if (repeatSerology != ""){

            val birthPlanList = ArrayList<DbObserveValue>()

            if (linearRepeatNo.visibility == View.VISIBLE){
               val nextAppointment = tvNoNextAppointment.text.toString()
                val valueName = DbObserveValue("Date of Next Appointment", nextAppointment)
                birthPlanList.add(valueName)
            }


            if (linearRepeatYes.visibility == View.VISIBLE){
                val testDoneDate = tvDate.text.toString()
                val valueName = DbObserveValue("Date Test was done", testDoneDate)
                birthPlanList.add(valueName)

                val radioGrpTestResults = formatter.getRadioText(radioGrpTestResults)
                if (radioGrpTestResults != ""){

                    if (linearReactive.visibility == View.VISIBLE){
                        val pmtctClinic = etPMTCTClinic.text.toString()
                        val partnerTested = etTestPartner.text.toString()

                        if (!TextUtils.isEmpty(pmtctClinic) && !TextUtils.isEmpty(partnerTested)){

                            val valueName1 = DbObserveValue("Refer PMTCT Clinic", pmtctClinic)
                            val valueName2 = DbObserveValue("Partner Test", partnerTested)

                            birthPlanList.addAll(listOf(valueName1, valueName2))

                        }else{
                            Toast.makeText(this, "Please fill all records", Toast.LENGTH_SHORT).show()
                        }

                    }
                    if (linearNoReactive.visibility == View.VISIBLE){
                        val bookSerology = etRepeatSerology.text.toString()
                        val breastFeeding = etContinueTest.text.toString()
                        val nextVisit = tvNextVisit.text.toString()

                        if (!TextUtils.isEmpty(bookSerology) && !TextUtils.isEmpty(breastFeeding) && !TextUtils.isEmpty(nextVisit)){

                            val valueName1 = DbObserveValue("Book Serology Test", bookSerology)
                            val valueName2 = DbObserveValue("Complete Breastfeeding Cessation", breastFeeding)
                            val valueName3 = DbObserveValue("Next appointment", nextVisit)

                            birthPlanList.addAll(listOf(valueName1, valueName2, valueName3))

                        }else{
                            Toast.makeText(this, "Please fill all records", Toast.LENGTH_SHORT).show()
                        }

                    }

                }

            }



            val dbObservationValue = formatter.createObservation(birthPlanList,
                DbResourceViews.MATERNAL_SEROLOGY.name)

            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
                DbResourceViews.MATERNAL_SEROLOGY.name)

        }

    }
}