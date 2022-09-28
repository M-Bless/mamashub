package com.intellisoft.kabarakmhis.new_designs.present_pregnancy

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_present_preg_2.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.navigation
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.*

import java.util.*
import kotlin.collections.ArrayList


class FragmentPresentPregnancy2 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var presentationList = arrayOf("","Unknown fetal presentation", "Cephalic fetal presentation",
        "Pelvic fetal presentation", "Transverse fetal presentation", "Other fetal presentation")
    private var spinnerPresentationValue  = presentationList[0]

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_present_preg_2, container, false)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        initSpinner()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        rootView.tvDate.setOnClickListener { createDialog(999) }

        rootView.etFoetalMovement.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etFoetalMovement.text.toString()
                if (!TextUtils.isEmpty(value)){
                    try {
                        validateFoetal(rootView.etFoetalMovement, value.toInt())
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }

        })

        handleNavigation()

        return rootView
    }

    private fun validateFoetal(editText: EditText, value: Int) {
        if (value in 121..159){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
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
        val dbDataList = ArrayList<DbDataList>()

        val lieText = formatter.getRadioText(rootView.radioGrpLie)
        val foetalMovement = formatter.getRadioText(rootView.radGrpFoetalHeartRate)

        val foetalHeartRate = rootView.etFoetalMovement.text.toString()

        val date = tvDate.text.toString()

        if (lieText != "" && foetalHeartRate != "" && !TextUtils.isEmpty(foetalMovement)
            && !TextUtils.isEmpty(date) && spinnerPresentationValue != "") {

            addData("Presentation",spinnerPresentationValue, DbObservationValues.PRESENTATION.name)

            addData("Lie",lieText, DbObservationValues.LIE.name)
            addData("Foetal Heart Rate",foetalHeartRate, DbObservationValues.FOETAL_HEART_RATE.name)
            addData("Foetal Movement",foetalMovement, DbObservationValues.FOETAL_MOVEMENT.name)
            addData("Next Visit",date, DbObservationValues.NEXT_VISIT_DATE.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.D_PRESENTATION.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

        }else{

            if (TextUtils.isEmpty(date)) errorList.add("Next Visit Date is required")
            if (lieText == "") errorList.add("Lie is required")
            if (foetalHeartRate == "") errorList.add("Foetal Heart Rate is required")
            if (foetalMovement == "") errorList.add("Foetal Movement is required")
            if (spinnerPresentationValue == "") errorList.add("Presentation is required")

        }

        if (errorList.size == 0){
            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PRESENT_PREGNANCY.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
                DbResourceViews.PRESENT_PREGNANCY.name))
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


    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presentationList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerPresentation!!.adapter = kinRshp

        rootView.spinnerPresentation.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerPresentation -> { spinnerPresentationValue = rootView.spinnerPresentation.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)

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

    private val myDateDobListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvDate.text = date

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


    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.PRESENT_PREGNANCY.name)

                if (encounterId != null){

                    val presentation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PRESENTATION.name), encounterId)
                    val lie = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.LIE.name), encounterId)
                    val foetalHeartRate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FOETAL_HEART_RATE.name), encounterId)
                    val foetalMovement = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.FOETAL_MOVEMENT.name), encounterId)
                    val nextVisit = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NEXT_VISIT_DATE.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (presentation.isNotEmpty()){
                            val value = presentation[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.spinnerPresentation.setSelection(presentationList.indexOf(valueNo))
                        }
                        if (lie.isNotEmpty()){
                            val value = lie[0].value
                            if (value.contains("Longitudinal", ignoreCase = true)) rootView.radioGrpLie.check(R.id.radioLongitudinal)
                            if (value.contains("Obligue", ignoreCase = true)) rootView.radioGrpLie.check(R.id.radioObligue)
                            if (value.contains("Transverse", ignoreCase = true)) rootView.radioGrpLie.check(R.id.radioTransverse)
                        }
                        if (foetalHeartRate.isNotEmpty()){
                            val value = foetalHeartRate[0].value
                            if (value.contains("Normal", ignoreCase = true)) rootView.radGrpFoetalHeartRate.check(R.id.radioMaleBabySex)
                            if (value.contains("Abnormal", ignoreCase = true)) rootView.radGrpFoetalHeartRate.check(R.id.radioFemaleBabySex)
                        }
                        if (foetalMovement.isNotEmpty()){
                            val value = foetalMovement[0].value
                            val valueNo = formatter.getValues(value, 0)
                            if(formatter.isNumeric(valueNo)){
                                rootView.etFoetalMovement.setText(valueNo)
                            }

                        }
                        if (nextVisit.isNotEmpty()){
                            val value = nextVisit[0].value
                            val valueNo = formatter.getValues(value, 0)
                            rootView.tvDate.text = valueNo
                        }


                    }



                }


            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}