package com.kabarak.kabarakmhis.new_designs.antenatal_profile

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
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*

import kotlinx.android.synthetic.main.fragment_antenatal4.view.*
import kotlinx.android.synthetic.main.fragment_antenatal4.view.navigation
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHIVStatus
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHiv
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.*
import kotlin.collections.ArrayList


class FragmentAntenatal4 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private val antenatal4 = DbResourceViews.ANTENATAL_4.name

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal4, container, false)

        formatter.saveCurrentPage("4", requireContext())
        getPageDetails()

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        rootView.radioGrpHiv.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {

                } else {

                }
            }
        }
        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Reactive") {
                    changeVisibility(rootView.linearReactive, true)
                } else {
                    changeVisibility(rootView.linearReactive, false)
                }
            }
        }
        rootView.radioGrpReactive.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReferral, true)
                } else {
                    changeVisibility(rootView.linearReferral, false)
                }
            }
        }
        rootView.tvHivTestDate.setOnClickListener { onCreateDialog(999) }


        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateIptDateGvnListener, year, month, day)
                //Convert weeks to milliseconds

                val nextContact = formatter.retrieveSharedPreference(requireContext(), DbAncSchedule.CONTACT_WEEK.name)
                if (nextContact != null){
                    val weeks = nextContact.toInt() * 7 * 24 * 60 * 60 * 1000L
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis() + weeks
                }else{
                    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                }
                datePickerDialog.show()

            }

            else -> null
        }


    }

    private val myDateIptDateGvnListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvHivTestDate.text = date

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

    private fun saveData() {

        val errorList = ArrayList<String>()

        val coupleCounselling = formatter.getRadioText(rootView.radioGrpHiv)
        if (coupleCounselling != ""){
            addData("Couple Counselling", coupleCounselling, DbObservationValues.COUPLE_HIV_TESTING.name)
        }
        val partnerHivStatus = formatter.getRadioText(rootView.radioGrpHIVStatus)
        if (partnerHivStatus != ""){
            addData("Partner HIV status", partnerHivStatus, DbObservationValues.PARTNER_HIV_STATUS.name)
        }
        if (rootView.linearReactive.visibility == View.VISIBLE){
            val reactive = formatter.getRadioText(rootView.radioGrpReactive)
            if (reactive != ""){
                addData("Was the partner referred for HIV care", reactive, DbObservationValues.REACTIVE_PARTNER_HIV_RESULTS.name)
            }else{
                errorList.add("Please select partner HIV results")
            }
        }
        if (rootView.linearReferral.visibility == View.VISIBLE){
            val referral = rootView.tvHivTestDate.text.toString()
            if (!TextUtils.isEmpty(referral)){
                addData("Referral Date", referral , DbObservationValues.REFERRAL_PARTNER_HIV_DATE.name)
            }else{
                errorList.add("Please select referral date")
            }
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.J_COUPLE_COUNSELLING_TESTING.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }

        if(errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.ANTENATAL_PROFILE.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{
            formatter.showErrorDialog(errorList, requireContext())
        }



    }

    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
    }


    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
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
                    DbResourceViews.ANTENATAL_PROFILE.name)

                if (encounterId != null){

                    val coupleTesting = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.COUPLE_HIV_TESTING.name), encounterId)

                    val partnerStatus = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PARTNER_HIV_STATUS.name), encounterId)

                    val partnerStatusResults = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REACTIVE_PARTNER_HIV_RESULTS.name), encounterId)

                    val referralHivDate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REFERRAL_PARTNER_HIV_DATE.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if(coupleTesting.isNotEmpty()){
                            val value = coupleTesting.get(0).value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHiv.check(R.id.radioYesHiv)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHiv.check(R.id.radioNoHiv)
                        }
                        if (partnerStatus.isNotEmpty()){
                            val value = partnerStatus.get(0).value
                            if (value.contains("Reactive", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioRHIVStatus)
                            if (value.contains("Non Reactive", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioNRHIVStatus)
                        }
                        if (partnerStatusResults.isNotEmpty()){
                            val value = partnerStatusResults.get(0).value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpReactive.check(R.id.radioYesReactive)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpReactive.check(R.id.radioNoReactive)
                        }
                        if (referralHivDate.isNotEmpty()){
                            val value = referralHivDate.get(0).value
                            rootView.tvHivTestDate.text = value
                        }

                    }



                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }


}