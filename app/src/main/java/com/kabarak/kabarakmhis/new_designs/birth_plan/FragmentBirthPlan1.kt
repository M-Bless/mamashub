package com.kabarak.kabarakmhis.new_designs.birth_plan

import android.app.Application
import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.fragment_birthplan1.*
import kotlinx.android.synthetic.main.fragment_birthplan1.view.*
import kotlinx.android.synthetic.main.fragment_birthplan1.view.navigation
import kotlinx.android.synthetic.main.fragment_info.view.*

import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class FragmentBirthPlan1 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar

    var designationList = arrayOf("","Midwife", "Obstetrician")
    private var spinnerDesignationValue1  = designationList[0]
    private var spinnerDesignationValue2  = designationList[0]

    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerRshpValue  = relationshipList[0]

    var transportList = arrayOf("","Taxi", "Matatu", "Personal vehicle", "Donkey Cart", "Ambulance", "Motorcycle")
    private var spinnerTransportValue  = transportList[0]

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_birthplan1, container, false)

        initSpinner()

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]




        rootView.etEdd.setOnClickListener { createDialog(999) }

        formatter.saveCurrentPage("1", requireContext())

        getPageDetails()

        handleNavigation()

        return rootView
    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationshipList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerCompanionDesignation!!.adapter = kinRshp
        rootView.spinnerCompanionDesignation.onItemSelectedListener = this

        val designation = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, designationList)
        designation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val transport = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, transportList)
        transport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rootView.spinnerTransportMeans!!.adapter = transport
        rootView.spinnerTransportMeans.onItemSelectedListener = this

        rootView.spinnerDesignation!!.adapter = designation
        rootView.spinnerDesignation.onItemSelectedListener = this

        rootView.spinnerAlternativeDesignation!!.adapter = designation
        rootView.spinnerAlternativeDesignation.onItemSelectedListener = this




    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerCompanionDesignation -> { spinnerRshpValue = rootView.spinnerCompanionDesignation.selectedItem.toString() }
            R.id.spinnerDesignation -> { spinnerDesignationValue1 = rootView.spinnerDesignation.selectedItem.toString() }
            R.id.spinnerAlternativeDesignation -> { spinnerDesignationValue2 = rootView.spinnerAlternativeDesignation.selectedItem.toString() }
            R.id.spinnerTransportMeans -> { spinnerTransportValue = rootView.spinnerTransportMeans.selectedItem.toString() }


            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    private fun createDialog(id: Int) {

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDoseListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }

            else -> null
        }


    }
    private val myDateDoseListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
        // arg1 = year
        // arg2 = month
        // arg3 = day
        val date = showDate(arg1, arg2 + 1, arg3)
        etEdd.text = date

    }
    private fun showDate(year: Int, month: Int, day: Int) :String{

        var dayDate = day.toString()
        if (day.toString().length == 1){
            dayDate = "0$day"
        }
        var monthDate = month.toString()
        if (month.toString().length == 1){
            monthDate = "0$monthDate"
        }

        val date = StringBuilder().append(year).append("-")
            .append(monthDate).append("-").append(dayDate)

        return date.toString()

    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val edd = rootView.etEdd.text.toString()
        val facilityName = rootView.etFacilityName.text.toString()
        val facilityContact = rootView.etFacilityContact.text.toString()

        if (!TextUtils.isEmpty(edd) && !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(facilityContact)){

            val facilityContactNo = PhoneNumberValidation().getStandardPhoneNumber(facilityContact)
            if (facilityContactNo != null){

                addData("Expected date of childbirth",edd, DbObservationValues.EDD.name)
                addData("Health facility name",facilityName ,DbObservationValues.FACILITY_NAME.name)
                addData("Health facility contact",facilityContact, DbObservationValues.FACILITY_NUMBER.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label
                    val data = DbDataList(key, value, DbSummaryTitle.A_BIRTH_PLAN.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()

            }else{
                errorList.add("Invalid facility phone number")
            }

        }else{

            if (TextUtils.isEmpty(edd)) errorList.add("Expected date of childbirth is required")
            if (TextUtils.isEmpty(facilityName)) errorList.add("Health facility name is required")
            if (TextUtils.isEmpty(facilityContact)) errorList.add("Health facility contact is required")

        }

        val attendantName = rootView.etAttendantName.text.toString()
        val attendantPhone = rootView.etAttendantPhone.text.toString()

        if (!TextUtils.isEmpty(attendantName) && !TextUtils.isEmpty(attendantPhone) && spinnerDesignationValue1 != "") {

            val attendantPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(attendantPhone)
            if (attendantPhoneNo != null){

                addData("Name",attendantName, DbObservationValues.ATTENDANT_NAME.name)
                addData("Telephone Number",attendantPhoneNo ,DbObservationValues.ATTENDANT_NUMBER.name)
                addData("Designation",spinnerDesignationValue1, DbObservationValues.ATTENDANT_DESIGNATION.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.B_BIRTH_ATTENDANT.name, DbResourceType.Observation.name ,label)
                    dbDataList.add(data)

                }
                observationList.clear()

            }else{
                errorList.add("Attendant phone number is not valid")
            }

        }


        val alternativeAttendantName = rootView.etAlternativeAttendantName.text.toString()
        val alternativeAttendantPhone = rootView.etAlternativeAttendantPhone.text.toString()

        if (!TextUtils.isEmpty(alternativeAttendantName)) {

            if (!TextUtils.isEmpty(alternativeAttendantPhone) && spinnerDesignationValue2 != "") {

                val alternativeAttendantPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(alternativeAttendantPhone)
                if (alternativeAttendantPhoneNo != null){

                    addData("Name",alternativeAttendantName, DbObservationValues.ATTENDANT_NAME1.name)
                    addData("Telephone Number",alternativeAttendantPhoneNo, DbObservationValues.ATTENDANT_NUMBER1.name)
                    addData("Designation",spinnerDesignationValue2 ,DbObservationValues.ATTENDANT_DESIGNATION1.name)

                    for (items in observationList){

                        val key = items.key
                        val dbObservationLabel = observationList.getValue(key)

                        val value = dbObservationLabel.value
                        val label = dbObservationLabel.label

                        val data = DbDataList(key, value, DbSummaryTitle.C_ALTERNATIVE_BIRTH_ATTENDANT.name, DbResourceType.Observation.name, label)
                        dbDataList.add(data)

                    }
                    observationList.clear()

                }else{
                    errorList.add("Alternative attendant phone number is not valid")
                }

            }else{
                errorList.add("Alternative attendant phone number is required")
            }



        }

        val companionName = rootView.etCompanionName.text.toString()
        val companionPhone = rootView.etCompanionPhone.text.toString()

        if (!TextUtils.isEmpty(companionName) &&
            !TextUtils.isEmpty(companionPhone) && spinnerRshpValue != "" && spinnerTransportValue != "") {

            val companionPhoneNo = PhoneNumberValidation().getStandardPhoneNumber(companionPhone)
            if (companionPhoneNo != null){

                addData("Name",companionName, DbObservationValues.COMPANION_NAME.name)
                addData("Telephone Number",companionPhoneNo, DbObservationValues.COMPANION_NUMBER.name)
                addData("Transport",spinnerTransportValue ,DbObservationValues.COMPANION_TRANSPORT.name)
                addData("Relationship",spinnerRshpValue ,DbObservationValues.COMPANION_RELATIONSHIP.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.D_BIRTH_COMPANION.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()

            }else{
                errorList.add("Invalid companion phone number")
            }


        }else{

            if (TextUtils.isEmpty(companionName)) errorList.add("Companion name is required")
            if (TextUtils.isEmpty(companionPhone)) errorList.add("Companion phone number is required")
            if (spinnerRshpValue == "") errorList.add("Companion relationship is required")
            if (spinnerTransportValue == "") errorList.add("Companion transport is required")

        }

        if (errorList.size == 0) {
            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.BIRTH_PLAN.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentBirthPlan2())
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
        val edd = formatter.retrieveSharedPreference(requireContext(), "edd")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }

        if (edd != null){
            rootView.etEdd.text = edd
            rootView.etEdd.isEnabled = false
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

                    val facilityName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FACILITY_NAME.name), encounterId)
                    val facilityNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FACILITY_NUMBER.name), encounterId)
                    val birthAttName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_NAME.name), encounterId)
                    val birthAttNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_NUMBER.name), encounterId)
                    val birthAttDesignation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_DESIGNATION.name), encounterId)
                    val birthAttName1 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_NAME1.name), encounterId)
                    val birthAttNo1 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_NUMBER1.name), encounterId)
                    val birthAttDesignation1 = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ATTENDANT_DESIGNATION1.name), encounterId)
                    val companionName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_NAME.name), encounterId)
                    val companionNumber = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_NUMBER.name), encounterId)
                    val companionRshp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_RELATIONSHIP.name), encounterId)
                    val companionTransport = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COMPANION_TRANSPORT.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (facilityName.isNotEmpty()){
                            rootView.etFacilityName.setText(facilityName[0].value)
                        }
                        if (facilityNo.isNotEmpty()){
                            val value = facilityNo[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etFacilityContact.setText(valueNo)
                        }
                        if (birthAttName.isNotEmpty()){
                            rootView.etAttendantName.setText(birthAttName[0].value)
                        }
                        if (birthAttNo.isNotEmpty()){
                            val value = birthAttNo[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etAttendantPhone.setText(valueNo)
                        }
                        if (birthAttDesignation.isNotEmpty()){
                            val noValue = formatter.getValues(birthAttDesignation[0].value, 0)
                            rootView.spinnerDesignation.setSelection(designationList.indexOf(noValue))
                        }
                        if (birthAttName1.isNotEmpty()){
                            rootView.etAlternativeAttendantName.setText(birthAttName1[0].value)
                        }
                        if (birthAttNo1.isNotEmpty()){
                            val value = birthAttNo1[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etAlternativeAttendantPhone.setText(valueNo)
                        }
                        if (birthAttDesignation1.isNotEmpty()){
                            val noValue = formatter.getValues(birthAttDesignation1[0].value, 0)
                            rootView.spinnerAlternativeDesignation.setSelection(designationList.indexOf(noValue))
                        }
                        if (companionName.isNotEmpty()){
                            rootView.etCompanionName.setText(companionName[0].value)
                        }
                        if (companionNumber.isNotEmpty()){
                            val value = companionNumber[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.etCompanionPhone.setText(valueNo)
                        }
                        if (companionRshp.isNotEmpty()){
                            val noValue = formatter.getValues(companionRshp[0].value, 0)
                            rootView.spinnerCompanionDesignation.setSelection(relationshipList.indexOf(noValue))
                        }
                        if (companionTransport.isNotEmpty()){
                            val noValue = formatter.getValues(companionRshp[0].value, 0)
                            rootView.spinnerTransportMeans.setSelection(transportList.indexOf(noValue))
                        }



                    }



                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}