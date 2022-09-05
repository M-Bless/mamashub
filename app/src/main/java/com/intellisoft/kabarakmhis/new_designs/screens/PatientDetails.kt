package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbConfirmDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.new_patient.RegisterNewPatient
import kotlinx.android.synthetic.main.activity_maternal_serology_view.*
import kotlinx.android.synthetic.main.activity_patient_details.*
import kotlinx.android.synthetic.main.activity_patient_details.btnAdd
import kotlinx.android.synthetic.main.activity_patient_details.no_record
import kotlinx.android.synthetic.main.activity_patient_details.recycler_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.stream.Stream

class PatientDetails : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

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

        btnAdd.setOnClickListener {

            startActivity(Intent(this, RegisterNewPatient::class.java))

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

    override fun onStart() {
        super.onStart()

        getPatientDetails()
    }

    private fun getPatientDetails() {

        try {

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@PatientDetails)
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
                    val phone = clientDetails.phone
                    val kinData = clientDetails.kinData
                    val identifierList = clientDetails.identifier
                    val maritalStatus = clientDetails.maritalStatus
                    val address = clientDetails.address

                    CoroutineScope(Dispatchers.Main).launch {

                        progressDialog.dismiss()

                        val patientList = ArrayList<DbConfirmDetails>()

                        var identifier = ""
                        var nationalId = ""

                        identifierList.forEach {

                            if (it.id == "ANC_NUMBER"){
                                identifier = it.value
                            }
                            if (it.id == "NATIONAL_ID"){
                                nationalId = it.value
                            }

                        }

                        val dbPatientList = ArrayList<DbObserveValue>()
                        val dbAncCode = DbObserveValue("ANC code", identifier.toString())
                        val dbNational = DbObserveValue("National Id", nationalId.toString())
                        val dbName = DbObserveValue("Name of client", name.toString())

                        val age = "${formatter.calculateAge(dob)} years"
                        val dbDob = DbObserveValue("Age", age.toString())
                        val dbMaritalStatus = DbObserveValue("Marital Status", maritalStatus.toString())


                        val dbContactList = ArrayList<DbConfirmDetails>()

                        val dbObserveValueList = ArrayList<DbObserveValue>()
                        val dbObserveValue = DbObserveValue("Telephone", phone.toString())
                        dbObserveValueList.add(dbObserveValue)
                        val dbConfirmDetails = DbConfirmDetails(DbSummaryTitle.E_CONTACT_INFORMATION.name, dbObserveValueList)
                        dbContactList.add(dbConfirmDetails)

                        //Next of Kin
                        val dbKinList = ArrayList<DbConfirmDetails>()

                        val dbKinValueList = ArrayList<DbObserveValue>()
                        val kinName = kinData.name
                        val kinPhone = kinData.phone
                        val kinRshp = kinData.relationship
                        val dbKinName = DbObserveValue("Name", kinName.toString())
                        val dbKinPhone = DbObserveValue("Telephone", kinPhone.toString())
                        val dbKinRshp = DbObserveValue("Relationship", kinRshp.toString())
                        dbKinValueList.addAll(listOf(dbKinName, dbKinPhone,dbKinRshp))

                        val dbKinDetails = DbConfirmDetails(DbSummaryTitle.F_NEXT_OF_KIN.name, dbKinValueList)
                        dbKinList.add(dbKinDetails)

                        //Address

                        val dbResedentialDataList = ArrayList<DbConfirmDetails>()
                        val dbResidentialList = ArrayList<DbObserveValue>()
                        if (address.isNotEmpty()){
                            val dbAddress = address[0]
                            val text = dbAddress.text
                            val city = dbAddress.city
                            val district = dbAddress.district
                            val state = dbAddress.state

                            if (text != "") {
                                val dbAddressText = DbObserveValue("text", text)
                                dbResidentialList.add(dbAddressText)
                            }
                            if (city != "") {
                                val dbAddressCity = DbObserveValue("city", city)
                                dbResidentialList.add(dbAddressCity)
                            }
                            if (district != "") {
                                val dbAddressDistrict = DbObserveValue("district", district)
                                dbResidentialList.add(dbAddressDistrict)
                            }
                            if (state != "") {
                                val dbAddressState = DbObserveValue("state", state)
                                dbResidentialList.add(dbAddressState)
                            }
                        }

                        val dbResidentialInfo = DbConfirmDetails(DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, dbResidentialList)
                        dbResedentialDataList.add(dbResidentialInfo)

                        dbPatientList.addAll(listOf(dbAncCode, dbName, dbDob, dbNational,dbMaritalStatus))

                        val dbPatient = DbConfirmDetails(DbSummaryTitle.B_PATIENT_DETAILS.name, dbPatientList)
                        patientList.add(dbPatient)
                        val observationDataList = merge(patientList, dbContactList, dbKinList,dbResedentialDataList)

                        if (observationDataList.isNotEmpty()) {
                            btnAdd.visibility = View.VISIBLE
                            no_record.visibility = View.GONE
                            recycler_view.visibility = View.VISIBLE
                        } else {
                            btnAdd.visibility = View.GONE
                            no_record.visibility = View.VISIBLE
                            recycler_view.visibility = View.GONE
                        }

                        val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@PatientDetails)
                        recyclerView.adapter = confirmParentAdapter

                        progressDialog.dismiss()
                    }



                    val observationList = patientDetailsViewModel.getObservationFromEncounter(
                        DbResourceViews.PATIENT_INFO.name)

                    Log.e("observationList",observationList.toString())
//
//                    if (observationList.isNotEmpty()){
//                        val encounterId = observationList[0].id
//
//                        val text1 = DbObservationFhirData(
//                            DbSummaryTitle.A_FACILITY_DETAILS.name, listOf(
//                                formatter.getCodes(DbObservationValues.FACILITY_NAME.name),
//                                formatter.getCodes(DbObservationValues.KMHFL_CODE.name)))
//
//                        val text2 = DbObservationFhirData(
//                            DbSummaryTitle.C_CLINICAL_INFORMATION.name,
//                            listOf(
//                                formatter.getCodes(DbObservationValues.GRAVIDA.name),formatter.getCodes(DbObservationValues.PARITY.name),
//                                formatter.getCodes(DbObservationValues.HEIGHT.name),formatter.getCodes(DbObservationValues.WEIGHT.name),
//                                formatter.getCodes(DbObservationValues.EDD.name),formatter.getCodes(DbObservationValues.LMP.name),
//                            ))
//
//                        val text3 = DbObservationFhirData(
//                            DbSummaryTitle.B_PATIENT_DETAILS.name,
//                            listOf(
//                                formatter.getCodes(DbObservationValues.EDUCATION_LEVEL.name)
//                            )
//                        )
//
//
//                        val patientList = ArrayList<DbConfirmDetails>()
//
//                        var identifier = ""
//                        var nationalId = ""
//
//                        identifierList.forEach {
//
//                            if (it.id == "ANC_NUMBER"){
//                                identifier = it.value
//                            }
//                            if (it.id == "NATIONAL_ID"){
//                                nationalId = it.value
//                            }
//
//                        }
//
//                        val dbPatientList = ArrayList<DbObserveValue>()
//                        val dbAncCode = DbObserveValue("ANC code", identifier.toString())
//                        val dbNational = DbObserveValue("National Id", nationalId.toString())
//                        val dbName = DbObserveValue("Name of client", name.toString())
//
//                        val age = "${formatter.calculateAge(dob)} years"
//                        val dbDob = DbObserveValue("Age", age.toString())
//                        val dbMaritalStatus = DbObserveValue("Marital Status", maritalStatus.toString())
//
//
//                        val dbContactList = ArrayList<DbConfirmDetails>()
//
//                        val dbObserveValueList = ArrayList<DbObserveValue>()
//                        val dbObserveValue = DbObserveValue("Telephone", phone.toString())
//                        dbObserveValueList.add(dbObserveValue)
//                        val dbConfirmDetails = DbConfirmDetails(DbSummaryTitle.E_CONTACT_INFORMATION.name, dbObserveValueList)
//                        dbContactList.add(dbConfirmDetails)
//
//                        //Next of Kin
//                        val dbKinList = ArrayList<DbConfirmDetails>()
//
//                        val dbKinValueList = ArrayList<DbObserveValue>()
//                        val kinName = kinData.name
//                        val kinPhone = kinData.phone
//                        val kinRshp = kinData.relationship
//                        val dbKinName = DbObserveValue("Name", kinName.toString())
//                        val dbKinPhone = DbObserveValue("Telephone", kinPhone.toString())
//                        val dbKinRshp = DbObserveValue("Relationship", kinRshp.toString())
//                        dbKinValueList.addAll(listOf(dbKinName, dbKinPhone,dbKinRshp))
//
//                        val dbKinDetails = DbConfirmDetails(DbSummaryTitle.F_NEXT_OF_KIN.name, dbKinValueList)
//                        dbKinList.add(dbKinDetails)
//
//                        //Address
//
//                        val dbResedentialDataList = ArrayList<DbConfirmDetails>()
//                        val dbResidentialList = ArrayList<DbObserveValue>()
//                        if (address.isNotEmpty()){
//                            val dbAddress = address[0]
//                            val text = dbAddress.text
//                            val city = dbAddress.city
//                            val district = dbAddress.district
//                            val state = dbAddress.state
//
//                            if (text != "") {
//                                val dbAddressText = DbObserveValue("text", text)
//                                dbResidentialList.add(dbAddressText)
//                            }
//                            if (city != "") {
//                                val dbAddressCity = DbObserveValue("city", city)
//                                dbResidentialList.add(dbAddressCity)
//                            }
//                            if (district != "") {
//                                val dbAddressDistrict = DbObserveValue("district", district)
//                                dbResidentialList.add(dbAddressDistrict)
//                            }
//                            if (state != "") {
//                                val dbAddressState = DbObserveValue("state", state)
//                                dbResidentialList.add(dbAddressState)
//                            }
//                        }
//
//                        val dbResidentialInfo = DbConfirmDetails(DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, dbResidentialList)
//                        dbResedentialDataList.add(dbResidentialInfo)
//
//                        val text1List = formatter.getObservationList(patientDetailsViewModel, text1, encounterId)
//                        val text2List = formatter.getObservationList(patientDetailsViewModel, text2, encounterId)
//                        val text3List = formatter.getObservationList(patientDetailsViewModel, text3, encounterId)
//
//                        //Get Patient Details
//                        if (text3List.isNotEmpty()){
//
//                            val detailsList = text3List[0].detailsList
//                            detailsList.forEach { it ->
//
//                                val dbEducationValue = DbObserveValue(it.title, it.value)
//                                dbPatientList.add(dbEducationValue)}
//
//                        }
//
//                        dbPatientList.addAll(listOf(dbAncCode, dbName, dbDob, dbNational,dbMaritalStatus))
//
//                        val dbPatient = DbConfirmDetails(DbSummaryTitle.B_PATIENT_DETAILS.name, dbPatientList)
//                        patientList.add(dbPatient)
//
//                        val observationDataList = merge(text1List,patientList, text2List, dbContactList, dbKinList,dbResedentialDataList)
//                        CoroutineScope(Dispatchers.Main).launch {
//
//                            progressDialog.dismiss()
//
//                            if (observationDataList.isNotEmpty()) {
//                                btnAdd.visibility = View.VISIBLE
//                                no_record.visibility = View.GONE
//                                recycler_view.visibility = View.VISIBLE
//                            } else {
//                                btnAdd.visibility = View.GONE
//                                no_record.visibility = View.VISIBLE
//                                recycler_view.visibility = View.GONE
//                            }
//
//                            val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@PatientDetails)
//                            recyclerView.adapter = confirmParentAdapter
//
//
//                        }
//
//
//
//
//                    }else{
//                        CoroutineScope(Dispatchers.Main).launch {
//                            progressDialog.dismiss()
//                            Toast.makeText(this@PatientDetails, "No record found", Toast.LENGTH_SHORT).show()
//                        }
//                    }

                }.join()
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



}