package com.intellisoft.kabarakmhis.new_designs.new_patient

import android.app.Application
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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.dave.validations.PhoneNumberValidation
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.viewmodels.AddPatientViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.County
import com.intellisoft.kabarakmhis.new_designs.screens.FragmentConfirmDetails
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.fragment_info.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import org.hl7.fhir.r4.model.QuestionnaireResponse


class FragmentPatientInfo : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var educationList = arrayOf("","Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView = inflater.inflate(R.layout.fragment_info, container, false)

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }

//        allCountyList = CountyData().getCountyData(requireContext())

        countyDataList = kabarakViewModel.getCounties()

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
//        val wardName = rootView.etWard.text.toString()
        val townName = rootView.etTown.text.toString()
        val addressName = rootView.etAddress.text.toString()
        val estateName = rootView.etEstate.text.toString()

        val telephoneName = rootView.etTelePhone.text.toString()

        val kinName = rootView.etKinName.text.toString()
        val kinPhone = rootView.etTelePhonKin.text.toString()

        if (!TextUtils.isEmpty(townName) &&
            !TextUtils.isEmpty(addressName) && !TextUtils.isEmpty(estateName) &&
            !TextUtils.isEmpty(telephoneName) && !TextUtils.isEmpty(kinName) &&
            !TextUtils.isEmpty(kinPhone)){

            val patientNo = PhoneNumberValidation().getStandardPhoneNumber(telephoneName)
            val kinNo = PhoneNumberValidation().getStandardPhoneNumber(kinPhone)

            if (patientNo != null && kinNo != null){

                val clientName = formatter.retrieveSharedPreference(requireContext(), "clientName").toString()
                val dob = formatter.retrieveSharedPreference(requireContext(), "dob").toString()
                val maritalStatus = formatter.retrieveSharedPreference(requireContext(), "maritalStatus").toString()

                val dbDataList = ArrayList<DbDataList>()

                val countyData = DbDataList("County Name", spinnerCountyValue, "Residential Information", DbResourceType.Patient.name)
                val subCountyData = DbDataList("Sub county Name", spinnerSubCountyValue, "Residential Information", DbResourceType.Patient.name)
                val wardData = DbDataList("Ward Name", spinnerWardValue, "Residential Information", DbResourceType.Patient.name)
                val townData = DbDataList("Town Name", townName, "Residential Information", DbResourceType.Patient.name)
                val addressData = DbDataList("Address Name", addressName, "Residential Information", DbResourceType.Patient.name)
                val estateData = DbDataList("Estate Name", estateName, "Residential Information", DbResourceType.Patient.name)

                val telephoneData = DbDataList("Telephone", telephoneName, "Contact Details", DbResourceType.Patient.name)
                val kinNameData = DbDataList("Next of Kin Name", kinName, "Next of Kin Details", DbResourceType.Patient.name)
                val kinPhoneData = DbDataList("Next of Kin Phone", kinPhone, "Next of Kin Details", DbResourceType.Patient.name)
                val rshpValueData = DbDataList("Next Of Kin Relationship", spinnerRshpValue, "Next of Kin Details", DbResourceType.Patient.name)

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
                    "Kenya")
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

                val dbPatientFhirInformation = DbPatientFhirInformation(
                    clientName, telecomList,"female", dob, addressList,
                    kinContactList, maritalStatus
                )

                val questionnaireFragment = childFragmentManager.findFragmentByTag(
                    QUESTIONNAIRE_FRAGMENT_TAG
                ) as QuestionnaireFragment
                savePatient(dbPatientFhirInformation, questionnaireFragment.getQuestionnaireResponse())


//                CoroutineScope(Dispatchers.IO).launch {
//
//                    val clientName = formatter.retrieveSharedPreference(requireContext(), "clientName").toString()
//                    val dob = formatter.retrieveSharedPreference(requireContext(), "dob").toString()
//
//                    //Save to FHIR
//                    val nameList = java.util.ArrayList<DbName>()
//                    val givenNameList = java.util.ArrayList<String>()
//                    givenNameList.add(clientName)
//                    val dbName = DbName(clientName, givenNameList)
//                    nameList.add(dbName)
//
//                    val telecomList = java.util.ArrayList<DbTelecom>()
//                    val dbTelecom = DbTelecom("phone", telephoneName)
//                    telecomList.add(dbTelecom)
//
//                    val addressList = java.util.ArrayList<DbAddress>()
//                    val addressData1 = DbAddress(spinnerCountyValue, java.util.ArrayList(), spinnerSubCountyValue, spinnerWardValue, SYNC_VALUE, "Ke")
//                    addressList.add(addressData1)
//
//                    val contactList = java.util.ArrayList<DbContact>()
//                    val relationship = java.util.ArrayList<DbRshp>()
//                    val dbRshp = DbRshp(spinnerRshpValue)
//                    relationship.add(dbRshp)
//
//                    val givenKinNameList = java.util.ArrayList<String>()
//                    givenKinNameList.add(kinName)
//                    val dbKinName = DbName(kinName, givenKinNameList)
//
//                    val kinTelecomList = java.util.ArrayList<DbTelecom>()
//                    val kinDbTelecom = DbTelecom("phone", kinPhone)
//                    kinTelecomList.add(kinDbTelecom)
//
//                    val dbContact = DbContact(relationship, dbKinName, kinTelecomList)
//                    contactList.add(dbContact)
//
//                    val dbPatient = DbPatient(
//                        DbResourceType.Patient.name, FormatterClass().generateUuid(), true,
//                        nameList, telecomList, "female", dob, addressList, contactList)
//
////                    retrofitCallsFhir.createPatient(requireContext(), dbPatient)
//
//                }
//
//
//
//
//                startActivity(Intent(requireContext(), PatientDetailsView::class.java))

            }else{

                Toast.makeText(requireContext(), "Please provide Kenyan valid phone numbers", Toast.LENGTH_SHORT).show()

            }



        }else{
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

    }

    private fun savePatient(
        dbPatientFhirInformation: DbPatientFhirInformation,
        questionnaireResponse: QuestionnaireResponse
    ) {

        Log.e("----1 ", dbPatientFhirInformation.toString())

        viewModel.savePatient(dbPatientFhirInformation, questionnaireResponse)

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

//        for (county in allCountyList){
//            val name = county.name
//            countyList.add(name)
//        }

        countyList.add("")
        for (county in countyDataList){
            val name = county.countyName
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

        val wardDataList = ArrayList<String>()

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

    private fun initSubCounty() {

        val subCountyDataList = HashSet<String>()

        val countyData = kabarakViewModel.getCountyNameData(spinnerCountyValue)
        if (countyData != null){
            val countyId = countyData.id
            if (countyId != null){
                val subCountyList = kabarakViewModel.getSubCounty(countyId)
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

    private fun updateArguments(){
        requireArguments()
            .putString(FragmentConfirmDetails.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
    }

    private fun addQuestionnaireFragment(){
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment,
                FragmentConfirmDetails.QUESTIONNAIRE_FRAGMENT_TAG
            )
        }
    }


    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}