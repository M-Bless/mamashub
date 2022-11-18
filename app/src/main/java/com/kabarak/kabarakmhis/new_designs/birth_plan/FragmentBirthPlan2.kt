package com.kabarak.kabarakmhis.new_designs.birth_plan

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dave.validations.PhoneNumberValidation
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_birthplan2.view.*
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etCompanionName
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etCompanionPhone
import kotlinx.android.synthetic.main.fragment_birthplan2.view.etTransportMeans
import kotlinx.android.synthetic.main.fragment_birthplan2.view.navigation
import kotlinx.android.synthetic.main.fragment_birthplan2.view.spinnerCompanionDesignation

import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentBirthPlan2 : Fragment() , AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    var designationList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerDesignationValue1  = designationList[0]

    var bloodGroupList = arrayOf("","A", "AB", "B", "O")
    private var spinnerBloodGroupValue  = bloodGroupList[0]

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_birthplan2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        formatter.saveCurrentPage("2", requireContext())

        getPageDetails()
        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun initSpinner() {

        val designation = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, designationList)
        designation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rootView.spinnerCompanionDesignation!!.adapter = designation
        rootView.spinnerCompanionDesignation.onItemSelectedListener = this

        val bloodGroup = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroupList)
        bloodGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerDonorGroup!!.adapter = bloodGroup
        rootView.spinnerDonorGroup.onItemSelectedListener = this




    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerCompanionDesignation -> { spinnerDesignationValue1 = rootView.spinnerCompanionDesignation.selectedItem.toString() }
            R.id.spinnerDonorGroup -> { spinnerBloodGroupValue = rootView.spinnerDonorGroup.selectedItem.toString() }


            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val companionName = rootView.etCompanionName.text.toString()
        val companionPhone = rootView.etCompanionPhone.text.toString()
        val companionMeans = rootView.etTransportMeans.text.toString()

        if (!TextUtils.isEmpty(companionName) && !TextUtils.isEmpty(companionPhone)
            && !TextUtils.isEmpty(companionMeans) && spinnerDesignationValue1 != "") {

            val companionPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(companionPhone)
            if (companionPhoneNo != null){

                addData("Name",companionName, DbObservationValues.COMPANION_NAME1.name)
                addData("Telephone Number",companionPhone ,DbObservationValues.COMPANION_NUMBER1.name)
                addData("Transport",companionMeans, DbObservationValues.COMPANION_TRANSPORT1.name)
                addData("Designation",spinnerDesignationValue1, DbObservationValues.ATTENDANT_DESIGNATION1.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.E_ALTERNATIVE_BIRTH_COMPANION.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()

            }else{
                errorList.add("Invalid Phone Number")
            }

        }

        val donorName = rootView.etDonorName.text.toString()
        val donorPhone = rootView.etDonorPhone.text.toString()

        if (!TextUtils.isEmpty(donorName) && !TextUtils.isEmpty(donorPhone) && spinnerBloodGroupValue != "") {

            val donorPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(donorPhone)
            if (donorPhoneNo != null){

                addData("Blood Donor Name",donorName, DbObservationValues.DONOR_NAME.name)
                addData("Blood Donor Phone Number",donorPhone, DbObservationValues.DONOR_NUMBER.name)
                addData("Blood Group",spinnerBloodGroupValue, DbObservationValues.DONOR_BLOOD_GROUP.name)

            }else{
                errorList.add("Invalid Phone Number")
            }

        }else{

            if (TextUtils.isEmpty(donorName)) errorList.add("Blood Donor Name is required")
            if (TextUtils.isEmpty(donorPhone)) errorList.add("Blood Donor Phone Number is required")
            if (spinnerBloodGroupValue == "") errorList.add("Blood Group is required")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.F_BLOOD_DONOR.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()


        val financialPlan = rootView.etFinancialPlan.text.toString()
        if (!TextUtils.isEmpty(financialPlan)){

            addData("Financial plan for childbirth",financialPlan, DbObservationValues.FINANCIAL_PLAN.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.E_FINANCIAL_PLAN.name, DbResourceType.Observation.name ,label)
                dbDataList.add(data)

            }
            observationList.clear()

        }else{
            errorList.add("Financial plan for childbirth is required")
        }

        if (errorList.size == 0) {

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.BIRTH_PLAN.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.BIRTH_PLAN.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{

            formatter.showErrorDialog(errorList, requireContext())

        }


    }



    private fun addData(key: String, value: String, codeLabel: String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
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

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.BIRTH_PLAN.name)

                if (encounterId != null){

                    val alternativeBirthPlanName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_NAME1.name), encounterId)
                    val alternativeBirthPlanNumber = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_NUMBER1.name), encounterId)
                    val alternativeBirthPlanRshp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_RELATIONSHIP1.name), encounterId)
                    val alternativeBirthPlanTransport = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_TRANSPORT1.name), encounterId)
                    val bloodDonorName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DONOR_NAME.name), encounterId)
                    val bloodDonorNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DONOR_NUMBER.name), encounterId)
                    val bloodDonorBlood = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DONOR_BLOOD_GROUP.name), encounterId)
                    val bloodDonorGrpFinancial = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FINANCIAL_PLAN.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (alternativeBirthPlanName.isNotEmpty()){
                            rootView.etCompanionName.setText(alternativeBirthPlanName[0].value)
                        }
                        if (alternativeBirthPlanNumber.isNotEmpty()){
                            val value = alternativeBirthPlanNumber[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etCompanionPhone.setText(valueNo)
                        }
                        if (alternativeBirthPlanRshp.isNotEmpty()){
                            val value = alternativeBirthPlanRshp[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerCompanionDesignation.setSelection(designationList.indexOf(noValue))
                        }
                        if (alternativeBirthPlanTransport.isNotEmpty()){
                            val value = alternativeBirthPlanTransport[0].value
                            rootView.etTransportMeans.setText(value)
                        }
                        if (bloodDonorName.isNotEmpty()){
                            rootView.etDonorName.setText(bloodDonorName[0].value)
                        }
                        if (bloodDonorNo.isNotEmpty()){
                            val value = bloodDonorNo[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etDonorPhone.setText(valueNo)
                        }
                        if (bloodDonorBlood.isNotEmpty()){
                            val value = bloodDonorBlood[0].value
                            val noValue = formatter.getValues(value, 0)
                            rootView.spinnerDonorGroup.setSelection(bloodGroupList.indexOf(noValue))
                        }
                        if (bloodDonorGrpFinancial.isNotEmpty()){
                            val value = bloodDonorGrpFinancial[0].value
                            rootView.etFinancialPlan.setText(value)
                        }

                    }




                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}