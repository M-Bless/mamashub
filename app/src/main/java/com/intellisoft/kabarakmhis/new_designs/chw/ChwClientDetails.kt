package com.intellisoft.kabarakmhis.new_designs.chw

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.chw.viewmodel.ChwPatientListViewModel
import com.intellisoft.kabarakmhis.new_designs.data_class.DbConfirmDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_patient_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.stream.Stream

class ChwClientDetails : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chw_client_details)

        title = "Client Details"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
    }


    override fun onStart() {
        super.onStart()

        getPatientDetails()
    }

    private fun getPatientDetails() {

        try {

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@ChwClientDetails)
                progressDialog.setTitle("Please wait..")
                progressDialog.setMessage("Loading patient details")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {


                    //Get Client Details
                    val clientDetails = patientDetailsViewModel.getPatientData()
                    val id = clientDetails.id
                    val name = clientDetails.name
                    val dob = clientDetails.dob

                    CoroutineScope(Dispatchers.Main).launch {

                        progressDialog.dismiss()

                        val patientList = ArrayList<DbConfirmDetails>()

                        //Address
                        val observationList = patientDetailsViewModel.getObservationFromEncounter(
                            DbResourceViews.PATIENT_INFO.name
                        )


                    if (observationList.isNotEmpty()){
                        val encounterId = observationList[0].id
//
                        val text1 = DbObservationFhirData(
                            DbSummaryTitle.A_PATIENT_DATA.name, listOf(
                                formatter.getCodes(DbObservationValues.DATE_STARTED.name),
                                formatter.getCodes(DbObservationValues.BABY_SEX.name),
                                formatter.getCodes(DbObservationValues.TIMING_CONTACT.name)))

                        val text2 = DbObservationFhirData(
                            DbSummaryTitle.B_COMMUNITY_HEALTH_FACILITY_DETAILS.name,
                            listOf(
                                formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_UNIT.name),formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_LINK.name),
                                formatter.getCodes(DbObservationValues.REFERRAL_REASON.name),formatter.getCodes(DbObservationValues.MAIN_PROBLEM.name),
                                formatter.getCodes(DbObservationValues.CHW_INTERVENTION_GIVEN.name),formatter.getCodes(DbObservationValues.CHW_COMMENTS.name),
                            ))

                        val text3 = DbObservationFhirData(
                            DbSummaryTitle.C_CHV_REFERRING_THE_PATIENT.name,
                            listOf(
                                formatter.getCodes(DbObservationValues.OFFICER_NAME.name),formatter.getCodes(DbObservationValues.OFFICER_NUMBER.name),
                                formatter.getCodes(DbObservationValues.TOWN_NAME.name),formatter.getCodes(DbObservationValues.SUB_COUNTY_NAME.name),
                                formatter.getCodes(DbObservationValues.COUNTY_NAME.name),formatter.getCodes(DbObservationValues.COMMUNITY_HEALTH_UNIT.name),
                            )
                        )
                        val text4 = DbObservationFhirData(
                            DbSummaryTitle.C_CHV_REFERRING_THE_PATIENT.name,
                            listOf(
                                formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name),formatter.getCodes(DbObservationValues.TIMING_CONTACT_CHW.name),
                                formatter.getCodes(DbObservationValues.OFFICER_NAME.name),formatter.getCodes(DbObservationValues.PROFESSION.name),
                                formatter.getCodes(DbObservationValues.FACILITY_NAME.name),formatter.getCodes(DbObservationValues.ACTION_TAKEN.name),
                            )
                        )
                        val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
                        val text2List = formatter.getObservationList(patientDetailsViewModel, text2, encounterId)
                        val text3List = formatter.getObservationList(patientDetailsViewModel, text3, encounterId)
                        val text4List = formatter.getObservationList(patientDetailsViewModel, text4, encounterId)

                        val dbPatientList = ArrayList<DbObserveValue>()

                        if (text1List.isNotEmpty()){

                            val detailsList = text1List[0].detailsList
                            detailsList.forEach {
                                val dbObserveValue = DbObserveValue(it.title, it.value)
                                dbPatientList.add(dbObserveValue)
                            }

                        }

                        val dbObserveName = DbObserveValue("Client Name", name)
                        val dbObserveDob = DbObserveValue("Date of birth", dob)
                        dbPatientList.addAll(listOf(dbObserveName, dbObserveDob))

                        val dbPatient = DbConfirmDetails(DbSummaryTitle.A_PATIENT_DATA.name, dbPatientList)
                        patientList.add(dbPatient)

                        val observationDataList = merge(patientList, text2List, text3List, text4List)
                        CoroutineScope(Dispatchers.Main).launch {

                            progressDialog.dismiss()

                            if (observationDataList.isNotEmpty()) {
                                no_record.visibility = View.GONE
                                recycler_view.visibility = View.VISIBLE
                            } else {
                                no_record.visibility = View.VISIBLE
                                recycler_view.visibility = View.GONE
                            }

                            val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@ChwClientDetails)
                            recyclerView.adapter = confirmParentAdapter


                        }

                    }else{
                        CoroutineScope(Dispatchers.Main).launch {
                            progressDialog.dismiss()
                            Toast.makeText(this@ChwClientDetails, "No record found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    }.join()
                }
            }



        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, fourth:List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, fourth).forEach { item: List<T>? -> list.addAll(item!!) }
        return list
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {


                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}