package com.intellisoft.kabarakmhis.new_designs.new_patient

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.dave.county.CountyData
import com.dave.county.DbCounty
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.data.SYNC_VALUE
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.fragment_info.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentPatientInfo : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private lateinit var kabarakViewModel: KabarakViewModel

    private val retrofitCallsFhir = RetrofitCallsFhir()

    var allCountyList = listOf<DbCounty>()

    var countyList = ArrayList<String>()
    private var spinnerCountyValue  = "Please Select county"
    private var spinnerSubCountyValue  = "Please Select Sub county"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_info, container, false)

        allCountyList = CountyData().getCountyData(requireContext())

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()
        initSpinner()

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Save"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

//        val countyName = rootView.etCounty.text.toString()
//        val subCountyName = rootView.etSubCounty.text.toString()
        val wardName = rootView.etWard.text.toString()
        val townName = rootView.etTown.text.toString()
        val addressName = rootView.etAddress.text.toString()
        val estateName = rootView.etEstate.text.toString()

        val telephoneName = rootView.etTelePhone.text.toString()

        val kinName = rootView.etKinName.text.toString()
        val kinPhone = rootView.etTelePhonKin.text.toString()

        if (
            !TextUtils.isEmpty(wardName) && !TextUtils.isEmpty(townName) &&
            !TextUtils.isEmpty(addressName) && !TextUtils.isEmpty(estateName) &&
            !TextUtils.isEmpty(telephoneName) && !TextUtils.isEmpty(kinName) &&
            !TextUtils.isEmpty(kinPhone)){

            val dbDataList = ArrayList<DbDataList>()

//            val countyData = DbDataList("County Name", countyName, "Residential Information", DbResourceType.Patient.name)
//            val subCountyData = DbDataList("Sub county Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val wardData = DbDataList("Ward Name", wardName, "Residential Information", DbResourceType.Patient.name)
            val townData = DbDataList("Town Name", townName, "Residential Information", DbResourceType.Patient.name)
            val addressData = DbDataList("Address Name", addressName, "Residential Information", DbResourceType.Patient.name)
            val estateData = DbDataList("Estate Name", estateName, "Residential Information", DbResourceType.Patient.name)

            val telephoneData = DbDataList("Telephone", telephoneName, "Contact Details", DbResourceType.Patient.name)
            val kinNameData = DbDataList("Next of Kin Name", kinName, "Next of Kin Details", DbResourceType.Patient.name)
            val kinPhoneData = DbDataList("Next of Kin Phone", kinPhone, "Next of Kin Details", DbResourceType.Patient.name)
            val rshpValueData = DbDataList("Next Of Kin Relationship", spinnerRshpValue, "Next of Kin Details", DbResourceType.Patient.name)

            dbDataList.addAll(listOf( wardData, townData, addressData, estateData, telephoneData, kinNameData, kinPhoneData, rshpValueData))
            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PATIENT_INFO.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            CoroutineScope(Dispatchers.IO).launch {

                val clientName = formatter.retrieveSharedPreference(requireContext(), "clientName").toString()
                val dob = formatter.retrieveSharedPreference(requireContext(), "dob").toString()

                //Save to FHIR
                val nameList = java.util.ArrayList<DbName>()
                val givenNameList = java.util.ArrayList<String>()
                givenNameList.add(clientName)
                val dbName = DbName(clientName, givenNameList)
                nameList.add(dbName)

                val telecomList = java.util.ArrayList<DbTelecom>()
                val dbTelecom = DbTelecom("phone", telephoneName)
                telecomList.add(dbTelecom)

                val addressList = java.util.ArrayList<DbAddress>()
                val addressData1 = DbAddress("countyName", java.util.ArrayList(), "subCountyName", wardName, SYNC_VALUE, "Ke")
                addressList.add(addressData1)

                val contactList = java.util.ArrayList<DbContact>()
                val relationship = java.util.ArrayList<DbRshp>()
                val dbRshp = DbRshp(spinnerRshpValue)
                relationship.add(dbRshp)

                val givenKinNameList = java.util.ArrayList<String>()
                givenKinNameList.add(kinName)
                val dbKinName = DbName(kinName, givenKinNameList)

                val kinTelecomList = java.util.ArrayList<DbTelecom>()
                val kinDbTelecom = DbTelecom("phone", kinPhone)
                kinTelecomList.add(kinDbTelecom)

                val dbContact = DbContact(relationship, dbKinName, kinTelecomList)
                contactList.add(dbContact)

                val dbPatient = DbPatient(
                    DbResourceType.Patient.name, FormatterClass().generateUuid(), true,
                    nameList, telecomList, "female", dob, addressList, contactList)

                retrofitCallsFhir.createPatient(requireContext(), dbPatient)

            }




            startActivity(Intent(requireContext(), PatientDetailsView::class.java))

        }else{
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationshipList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerRshp!!.adapter = kinRshp
        rootView.spinnerRshp.onItemSelectedListener = this

        for (county in allCountyList){
            val name = county.name
            countyList.add(name)
        }

        val countyData = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countyList)
        countyData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerCounty!!.adapter = countyData
        rootView.spinnerCounty.onItemSelectedListener = this

        initSubCounty()

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRshp -> { spinnerRshpValue = rootView.spinnerRshp.selectedItem.toString() }
            R.id.spinnerCounty -> {
                spinnerCountyValue = rootView.spinnerCounty.selectedItem.toString()
                initSubCounty()
            }
            R.id.spinnerSubCounty -> { spinnerSubCountyValue = rootView.spinnerSubCounty.selectedItem.toString() }

            else -> {}
        }
    }

    private fun initSubCounty() {

        val subCountList = ArrayList<String>()
        for (county in allCountyList){

            val name = county.name
            if (spinnerCountyValue == name){

                county.sub_counties.forEach {
                    subCountList.add(it)
                }

            }

        }

        val subCountyData = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subCountList)
        subCountyData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerSubCounty!!.adapter = subCountyData
        rootView.spinnerSubCounty.onItemSelectedListener = this

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}