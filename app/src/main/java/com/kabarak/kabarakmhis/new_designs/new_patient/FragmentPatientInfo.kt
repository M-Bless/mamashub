package com.kabarak.kabarakmhis.new_designs.new_patient

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dave.validations.PhoneNumberValidation
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.AddPatientViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.roomdb.tables.County
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.fragment_info.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class FragmentPatientInfo : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives", "Guardian")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private lateinit var kabarakViewModel: KabarakViewModel

    private val retrofitCallsFhir = RetrofitCallsFhir()

//    var allCountyList = listOf<DbCounty>()

    var countyList = ArrayList<String>()
    private var spinnerCountyValue  = "Please Select county"
    private var spinnerSubCountyValue  = "Please Select Sub county"
    private var spinnerWardValue  = "Please Select Ward"

    var countyDataList = ArrayList<County>()
    private val viewModel: AddPatientViewModel by viewModels()
    val subCountyDataList = HashSet<String>()
    val wardDataList = ArrayList<String>()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private var patientId: String? = null
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView = inflater.inflate(R.layout.fragment_info, container, false)


//        allCountyList = CountyData().getCountyData(requireContext())

        countyDataList = kabarakViewModel.getCounties()

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()
        initSpinner()

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        handleNavigation()

        return rootView
    }

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    override fun onResume() {
        super.onResume()

        getSavedData()
    }

    private fun getSavedData() {

        val county = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.COUNTY_NAME.name)
        val subCounty = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.SUB_COUNTY_NAME.name)
        val ward = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.WARD_NAME.name)
        val town = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.TOWN_NAME.name)
        val address = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.ADDRESS_NAME.name)
        val estate = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.ESTATE_NAME.name)
        val phone = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.PHONE_NUMBER.name)
        val companionPhone = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.COMPANION_NUMBER.name)
        val companionRelationship = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.COMPANION_RELATIONSHIP.name)
        val companionName = formatter.retrieveSharedPreference(requireContext(), DbObservationValues.COMPANION_NAME.name)

        if(!TextUtils.isEmpty(county)) {
            rootView.spinnerCounty.setSelection(countyList.indexOf(county))
            spinnerCountyValue = county.toString()

            initSubCounty(spinnerCountyValue)

            if(!TextUtils.isEmpty(subCounty)) {
                rootView.spinnerSubCounty.setSelection(subCountyDataList.indexOf(subCounty))
                spinnerSubCountyValue = subCounty.toString()

                initWard()

                if(!TextUtils.isEmpty(ward)) rootView.spinnerWard.setSelection(wardDataList.indexOf(ward))

            }

        }


        if(!TextUtils.isEmpty(town)) rootView.etTown.setText(town)
        if(!TextUtils.isEmpty(address)) rootView.etAddress.setText(address)
        if(!TextUtils.isEmpty(estate)) rootView.etEstate.setText(estate)
        if(!TextUtils.isEmpty(phone)) rootView.etTelePhone.setText(phone)
        if(!TextUtils.isEmpty(companionPhone)) rootView.etTelePhonKin.setText(companionPhone)
        if(!TextUtils.isEmpty(companionRelationship)) rootView.spinnerRshp.setSelection(relationshipList.indexOf(companionRelationship))
        if(!TextUtils.isEmpty(companionName)) rootView.etKinName.setText(companionName)

        getSavedFhirData()

    }

    private fun getSavedFhirData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                patientId = formatter.retrieveSharedPreference(requireContext(), "patientId")

                if (patientId != null){

                    fhirEngine = FhirApplication.fhirEngine(requireContext())

                    patientDetailsViewModel = ViewModelProvider(this@FragmentPatientInfo,
                        PatientDetailsViewModel.PatientDetailsViewModelFactory(
                            requireContext().applicationContext as Application, fhirEngine, patientId.toString())
                    )[PatientDetailsViewModel::class.java]

                    val observationList = patientDetailsViewModel.getObservationFromEncounter(
                        DbResourceViews.PATIENT_INFO.name)

                    if(observationList.isNotEmpty()){

                        CoroutineScope(Dispatchers.Main).launch {

                            val clientDetails = patientDetailsViewModel.getPatientData()
                            val phone = clientDetails.phone
                            val kinData = clientDetails.kinData
                            val address = clientDetails.address

                            val kinName = kinData.name
                            val kinPhone = kinData.phone
                            val kinRshp = kinData.relationship

                            if (kinName != "") rootView.etKinName.setText(kinName)
                            if (kinPhone != "") rootView.etTelePhonKin.setText(kinPhone)
                            if (kinRshp != "") rootView.spinnerRshp.setSelection(relationshipList.indexOf(kinRshp))

                            if(phone != "") rootView.etTelePhone.setText(phone)


                            if (address.isNotEmpty()){

                                val dbAddress = address[0]
                                val text = dbAddress.text
                                val city = dbAddress.city
                                val district = dbAddress.district
                                val state = dbAddress.state

                                if (state != ""){
                                    rootView.spinnerCounty.setSelection(countyList.indexOf(state))
                                    spinnerCountyValue = state.toString()
                                    initSubCounty(spinnerCountyValue)

                                    if (district != ""){

                                        spinnerSubCountyValue = district.toString()
                                        rootView.spinnerSubCounty.setSelection(subCountyDataList.indexOf(district))
                                        initWard()

                                        if (city != ""){
                                            rootView.spinnerWard.setSelection(wardDataList.indexOf(city))
                                        }
                                    }

                                }

                                if (text != ""){
                                    rootView.etAddress.setText(text)
                                }

                            }

                        }


                    }

                }



            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()

        val townName = rootView.etTown.text.toString()
        val addressName = rootView.etAddress.text.toString()
        val estateName = rootView.etEstate.text.toString()

        val telephoneName = rootView.etTelePhone.text.toString()

        val kinName = rootView.etKinName.text.toString()
        val kinPhone = rootView.etTelePhonKin.text.toString()

        if (!TextUtils.isEmpty(townName) &&
            !TextUtils.isEmpty(addressName) && !TextUtils.isEmpty(estateName) &&
            !TextUtils.isEmpty(telephoneName) && !TextUtils.isEmpty(kinName) &&
            !TextUtils.isEmpty(kinPhone) && spinnerCountyValue != "Please Select county" &&
            spinnerSubCountyValue != "Please Select Sub county" && spinnerWardValue != "Please Select Ward"){

            val patientNo = PhoneNumberValidation().getStandardPhoneNumber(telephoneName)
            val kinNo = PhoneNumberValidation().getStandardPhoneNumber(kinPhone)

            if (patientNo != null && kinNo != null){

                val dbDataList = ArrayList<DbDataList>()

                val countyData = DbDataList("County Name", spinnerCountyValue, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.COUNTY_NAME.name)
                val subCountyData = DbDataList("Sub county Name", spinnerSubCountyValue, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.SUB_COUNTY_NAME.name)
                val wardData = DbDataList("Ward Name", spinnerWardValue, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.WARD_NAME.name)
                val townData = DbDataList("Town Name", townName, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.TOWN_NAME.name)
                val addressData = DbDataList("Address Name", addressName, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.ADDRESS_NAME.name)
                val estateData = DbDataList("Estate Name", estateName, DbSummaryTitle.D_RESIDENTIAL_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.ESTATE_NAME.name)

                val telephoneData = DbDataList("Telephone", telephoneName, DbSummaryTitle.E_CONTACT_INFORMATION.name, DbResourceType.Patient.name, DbObservationValues.PHONE_NUMBER.name)
                val kinNameData = DbDataList("Next of Kin Name", kinName, DbSummaryTitle.F_NEXT_OF_KIN.name, DbResourceType.Patient.name, DbObservationValues.COMPANION_NAME.name)
                val kinPhoneData = DbDataList("Next of Kin Phone", kinPhone, DbSummaryTitle.F_NEXT_OF_KIN.name, DbResourceType.Patient.name, DbObservationValues.COMPANION_NUMBER.name)
                val rshpValueData = DbDataList("Next Of Kin Relationship", spinnerRshpValue, DbSummaryTitle.F_NEXT_OF_KIN.name, DbResourceType.Patient.name, DbObservationValues.RELATIONSHIP.name)

                formatter.saveSharedPreference(requireContext(), DbObservationValues.COUNTY_NAME.name, spinnerCountyValue)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.SUB_COUNTY_NAME.name, spinnerSubCountyValue)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.WARD_NAME.name, spinnerWardValue)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.TOWN_NAME.name, townName)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.ADDRESS_NAME.name, addressName)

                formatter.saveSharedPreference(requireContext(), DbObservationValues.ESTATE_NAME.name, estateName)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.PHONE_NUMBER.name, telephoneName)

                formatter.saveSharedPreference(requireContext(), DbObservationValues.COMPANION_NUMBER.name, kinPhone)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.COMPANION_RELATIONSHIP.name, spinnerRshpValue)
                formatter.saveSharedPreference(requireContext(), DbObservationValues.COMPANION_NAME.name, kinName)

                dbDataList.addAll(listOf(countyData, subCountyData, wardData, townData, addressData, estateData, telephoneData, kinNameData, kinPhoneData, rshpValueData))
                val dbDataDetailsList = ArrayList<DbDataDetails>()
                val dbDataDetails = DbDataDetails(dbDataList)
                dbDataDetailsList.add(dbDataDetails)
                val dbPatientData = DbPatientData(DbResourceViews.PATIENT_INFO.name, dbDataDetailsList)
                kabarakViewModel.insertInfo(requireContext(), dbPatientData)

                //Capture Patient Data and save to database

                val addressList = ArrayList<DbAddress>()
                val address = DbAddress(
                    addressName,
                    ArrayList(),
                    spinnerWardValue,
                    spinnerSubCountyValue,
                    spinnerCountyValue,
                    "KENYA-KABARAK-MHIS6")
                addressList.add(address)

                val kinContactList = ArrayList<DbKinDetails>()
                val kinPhoneList = ArrayList<DbTelecom>()
                val dbTelecom = DbTelecom("phone", kinPhone)
                kinPhoneList.add(dbTelecom)
                val kinContact = DbKinDetails(
                    spinnerRshpValue,
                    kinName, kinPhoneList)
                kinContactList.add(kinContact)

                val telecomList = ArrayList<DbTelecom>()
                val dbTelecom1 = DbTelecom("phone", telephoneName)
                telecomList.add(dbTelecom1)

                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.replace(R.id.fragmentHolder, formatter.startFragmentPatient(requireContext(),
                    DbResourceViews.PATIENT_INFO.name))
                ft.addToBackStack(null)
                ft.commit()

            }else{

                if (patientNo == null) errorList.add("Client phone number is required")
                if (kinNo == null) errorList.add("Next of kin Number is required")

                formatter.showErrorDialog(errorList, requireContext())
            }



        }else{

            if (TextUtils.isEmpty(townName)) errorList.add("Please provide a town name")
            if (TextUtils.isEmpty(addressName)) errorList.add("Please provide an address")
            if (TextUtils.isEmpty(estateName)) errorList.add("Please provide an estate")
            if (TextUtils.isEmpty(telephoneName)) errorList.add("Please provide a phone number")
            if (TextUtils.isEmpty(kinName)) errorList.add("Please provide a next of kin name")
            if (TextUtils.isEmpty(kinPhone)) errorList.add("Please provide a next of kin phone number")

            if (spinnerCountyValue == "Please Select county" || spinnerCountyValue == "") errorList.add("Please select a county")
            if (spinnerSubCountyValue == "Please Select Sub-county" || spinnerSubCountyValue == "") errorList.add("Please select a sub-county")
            if (spinnerWardValue == "Please Select Ward" || spinnerWardValue == "") errorList.add("Please select a ward")

            formatter.showErrorDialog(errorList, requireContext())
        }

    }

    private fun savePatient(
        dbPatientFhirInformation: DbPatientFhirInformation,
        questionnaireResponse: QuestionnaireResponse
    ) {


//        viewModel.savePatient(dbPatientFhirInformation, questionnaireResponse)


        //Insert Patient first then use id to insert other data
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
            DbResourceViews.PATIENT_INFO.name))
        ft.addToBackStack(null)
        ft.commit()

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

        countyList.add("")
        for (county in countyDataList){
            val name = county.countyName
            countyList.add(name)
        }

        val countyData = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countyList)
        countyData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerCounty!!.adapter = countyData
        rootView.spinnerCounty.onItemSelectedListener = this

        initSubCounty(spinnerCountyValue)

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {

            R.id.spinnerRshp -> { spinnerRshpValue = rootView.spinnerRshp.selectedItem.toString() }
            R.id.spinnerCounty -> {
                spinnerCountyValue = rootView.spinnerCounty.selectedItem.toString()
                initSubCounty(spinnerCountyValue)
            }
            R.id.spinnerSubCounty -> {
                spinnerSubCountyValue = rootView.spinnerSubCounty.selectedItem.toString()
                initWard()
            }
            R.id.spinnerWard -> {
                spinnerWardValue = rootView.spinnerWard.selectedItem.toString()
            }

            else -> {}
        }
    }

    private fun initWard() {

        wardDataList.clear()
        wardDataList.add("")


        val wardList = kabarakViewModel.getWards(spinnerSubCountyValue)
        for (ward in wardList){

            val wardName = ward.ward
            wardDataList.add(wardName)
        }


        val subWardData = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wardDataList)
        subWardData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerWard!!.adapter = subWardData
        rootView.spinnerWard.onItemSelectedListener = this


    }

    private fun initSubCounty(countyName: String) {

        val countyData = kabarakViewModel.getCountyNameData(countyName)

        if (countyData != null){
            val countyId = countyData.id
            if (countyId != null){

                val subCountyList = kabarakViewModel.getSubCounty(countyId)

                subCountyDataList.clear()
                subCountyDataList.add("")


                for(subCounty in subCountyList){
                    val subCountyName = subCounty.constituencyName
                    subCountyDataList.add(subCountyName)
                }
            }


            val subCountyData = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subCountyDataList.toList())
            subCountyData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rootView.spinnerSubCounty!!.adapter = subCountyData
            rootView.spinnerSubCounty.onItemSelectedListener = this
        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }



}