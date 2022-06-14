package com.intellisoft.kabarakmhis.new_designs.birth_plan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_birth_plan.*

class BirthPlan : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birth_plan)

        btnSave.setOnClickListener {

            val facilityName = etFacilityName.text.toString()
            val attendantName = etAttendantName.text.toString()
            val facilityContact = etFacilityContact.text.toString()
            val supportPerson = etSupportName.text.toString()
            val transport = etTransport.text.toString()
            val bloodDonorName = etBloodName.text.toString()
            val financialPlan = etFinancialChildBirth.text.toString()

            if (
                !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(attendantName) &&
                !TextUtils.isEmpty(facilityContact) && !TextUtils.isEmpty(supportPerson) &&
                !TextUtils.isEmpty(transport) && !TextUtils.isEmpty(bloodDonorName) &&
                !TextUtils.isEmpty(financialPlan)){

                val birthPlanList = ArrayList<DbObserveValue>()

                val valueFacName = DbObserveValue("Facility Name", facilityName)
                val valueAttendant = DbObserveValue("Attendant Name", attendantName)
                val valFacContact = DbObserveValue("Facility Contact", facilityContact)
                val valueSupportPerson = DbObserveValue("Support Person", supportPerson)
                val valueTransport = DbObserveValue("Transport", transport)
                val valueBloodDonor = DbObserveValue("Blood Donor Name", bloodDonorName)
                val valueFinancial = DbObserveValue("Financial Plan for Childbirth", financialPlan)

                birthPlanList.addAll(listOf(valueFacName, valueAttendant, valFacContact, valueSupportPerson,
                valueTransport, valueBloodDonor, valueFinancial))

                val dbObservationValue = createObservation(birthPlanList)

//                retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
//                    DbResourceViews.BIRTH_PLAN.name)


            }else{

                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun createObservation(birthPlanList: ArrayList<DbObserveValue>): DbObservationValue {

        val dbObservationDataList = HashSet<DbObservationData>()

        val birthPlanSet = HashSet<DbObserveValue>(birthPlanList)

        for (dbObserveValue in birthPlanSet){

            val hashSetList = HashSet<String>()

            val title = dbObserveValue.title
            val value = dbObserveValue.value

            hashSetList.add(value)

            val dbObservationData = DbObservationData(title, hashSetList)
            dbObservationDataList.add(dbObservationData)
        }

        return DbObservationValue(dbObservationDataList)

    }
}