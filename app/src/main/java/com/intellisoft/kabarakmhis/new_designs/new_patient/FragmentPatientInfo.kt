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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_info.view.*


class FragmentPatientInfo : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var educationList = arrayOf("Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private lateinit var kabarakViewModel: KabarakViewModel


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_info, container, false)

        rootView.btnSave.setOnClickListener {

            saveData()
        }

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()
        initSpinner()

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        return rootView
    }

    private fun saveData() {

        val countyName = rootView.etCounty.text.toString()
        val subCountyName = rootView.etSubCounty.text.toString()
        val wardName = rootView.etWard.text.toString()
        val townName = rootView.etTown.text.toString()
        val addressName = rootView.etAddress.text.toString()
        val estateName = rootView.etEstate.text.toString()

        val telephoneName = rootView.etTelePhone.text.toString()

        val kinName = rootView.etKinName.text.toString()
        val kinPhone = rootView.etTelePhonKin.text.toString()

        if (
            !TextUtils.isEmpty(countyName) && !TextUtils.isEmpty(subCountyName) &&
            !TextUtils.isEmpty(wardName) && !TextUtils.isEmpty(townName) &&
            !TextUtils.isEmpty(addressName) && !TextUtils.isEmpty(estateName) &&
            !TextUtils.isEmpty(telephoneName) && !TextUtils.isEmpty(kinName) &&
            !TextUtils.isEmpty(kinPhone)){

            val dbDataList = ArrayList<DbDataList>()

            val countyData = DbDataList("County Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val subCountyData = DbDataList("Sub county Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val wardData = DbDataList("Ward Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val townData = DbDataList("Town Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val addressData = DbDataList("Address Name", countyName, "Residential Information", DbResourceType.Patient.name)
            val estateData = DbDataList("Estate Name", countyName, "Residential Information", DbResourceType.Patient.name)

            val telephoneData = DbDataList("Telephone", countyName, "Contact Details", DbResourceType.Patient.name)
            val kinNameData = DbDataList("Next of Kin Name", countyName, "Next of Kin Details", DbResourceType.Patient.name)
            val kinPhoneData = DbDataList("Next of Kin Phone", countyName, "Next of Kin Details", DbResourceType.Patient.name)
            val rshpValueData = DbDataList("Next Of Kin Relationship", spinnerRshpValue, "Next of Kin Details", DbResourceType.Patient.name)

            dbDataList.addAll(listOf(countyData, subCountyData, wardData, townData, addressData, estateData, telephoneData, kinNameData, kinPhoneData, rshpValueData))
            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PATIENT_INFO.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

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


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRshp -> { spinnerRshpValue = rootView.spinnerRshp.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}