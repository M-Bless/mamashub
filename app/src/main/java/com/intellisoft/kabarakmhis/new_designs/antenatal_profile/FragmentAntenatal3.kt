package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal3.view.*
import kotlinx.android.synthetic.main.fragment_antenatal3.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.*
import kotlin.collections.ArrayList


class FragmentAntenatal3 : Fragment() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal3, container, false)

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH)



        rootView.radioGrpHiv.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTestDate, true)
                    changeVisibility(rootView.linearNo, false)
                } else {
                    changeVisibility(rootView.linearTestDate, false)
                    changeVisibility(rootView.linearNo, true)
                }
            }
        }
        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "NR") {
                    changeVisibility(rootView.linearNR, true)
                } else {
                    changeVisibility(rootView.linearNR, false)
                }
            }
        }
        rootView.radioGrpSyphilis.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearSyphTestDate, true)
                    changeVisibility(rootView.linearSyphNo, false)
                } else {
                    changeVisibility(rootView.linearSyphTestDate, false)
                    changeVisibility(rootView.linearSyphNo, true)
                }
            }
        }
        rootView.radioGrpHepatitis.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearHepatitis, true)
                    changeVisibility(rootView.linearHepaNo, false)
                } else {
                    changeVisibility(rootView.linearHepatitis, false)
                    changeVisibility(rootView.linearHepaNo, true)
                }
            }
        }

        rootView.tvHivDate.setOnClickListener { onCreateDialog(999) }
        rootView.tvSyphilisDate.setOnClickListener { onCreateDialog(998) }
        rootView.tvHepatitisDate.setOnClickListener { onCreateDialog(997) }
        rootView.tvHivTestDate.setOnClickListener { onCreateDialog(996) }



        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
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
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateIptNextVisitListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound1Listener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            996 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound2Listener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
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
            rootView.tvHivDate.text = date

        }

    private val myDateIptNextVisitListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvSyphilisDate.text = date

        }


    private val myDateSound1Listener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvHepatitisDate.text = date

        }


    private val myDateSound2Listener =
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

        val dbDataList = ArrayList<DbDataList>()

        val errorList = ArrayList<String>()

        if (rootView.linearHiv.visibility == View.VISIBLE){

            val hivTest = formatter.getRadioText(rootView.radioGrpHiv)
            if (hivTest != "") {
                addData("HIV Testing", hivTest, DbObservationValues.HIV_TESTING.name)
                if (rootView.linearTestDate.visibility == View.VISIBLE) {

                    val value = rootView.tvHivDate.text.toString()
                    if (!TextUtils.isEmpty(value)) {
                        addData("HIV Test Date", value , DbObservationValues.YES_HIV_RESULTS.name)
                    } else {
                        errorList.add("Please select HIV Test Date")
                    }
                }
            }else{
                errorList.add("Please select HIV Testing")
            }

            if (rootView.linearNo.visibility == View.VISIBLE) {
                val value = rootView.etTb.text.toString()
                if (!TextUtils.isEmpty(value)) {
                    addData("HIV Further counselling", value , DbObservationValues.NO_HIV_RESULTS.name)
                } else {
                    errorList.add("Please enter HIV Further counselling")
                }
            }


            val hivStatus = formatter.getRadioText(rootView.radioGrpHIVStatus)
            if (hivStatus != "") {
                addData("HIV Status", hivStatus , DbObservationValues.HIV_MOTHER_STATUS.name)
            }else{
                errorList.add("Please select HIV Status")
            }
            if (rootView.linearNR.visibility == View.VISIBLE) {
                val value = rootView.tvHivTestDate.text.toString()
                if (!TextUtils.isEmpty(value)) {
                    addData("HIV Test Date", value , DbObservationValues.HIV_NR_DATE.name)
                } else {
                    errorList.add("Please select HIV Test Date")
                }
            }

            Log.e("errorList", observationList.toString())

        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.G_HIV_TESTING.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()

        val syphilisTesting = formatter.getRadioText(rootView.radioGrpSyphilis)
        if (syphilisTesting != "") {
            addData("Syphilis Testing", syphilisTesting , DbObservationValues.SYPHILIS_TESTING.name)
        }else{
            errorList.add("Please select Syphilis Testing")
        }
        if (rootView.linearSyphTestDate.visibility == View.VISIBLE) {
            val value = rootView.tvSyphilisDate.text.toString()
            if (!TextUtils.isEmpty(value)) {
                addData("Syphilis Test Date", value , DbObservationValues.YES_SYPHILIS_RESULTS.name)
            } else {
                errorList.add("Please select Syphilis Test Date")
            }
        }
        if (rootView.linearSyphNo.visibility == View.VISIBLE) {
            val value = rootView.etSyphilisCounselling.text.toString()
            if (!TextUtils.isEmpty(value)) {
                addData("Syphilis Further counselling", value , DbObservationValues.NO_SYPHILIS_RESULTS.name)
            } else {
                errorList.add("Please enter Syphilis Further counselling")
            }
        }
        val syphilisStatus = formatter.getRadioText(rootView.radioGrpSyphilisStatus)
        if (syphilisStatus != "") {
            addData("Syphilis Status", syphilisStatus , DbObservationValues.SYPHILIS_MOTHER_STATUS.name)
        }else{
            errorList.add("Please select Syphilis Status")
        }
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.H_SYPHILIS_TESTING.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val hepatitisB = formatter.getRadioText(rootView.radioGrpHepatitis)
        if (hepatitisB != "") {
            addData("Hepatitis Status", hepatitisB, DbObservationValues.HEPATITIS_TESTING.name)
        }else{
            errorList.add("Please select Hepatitis Status")
        }
        if (rootView.linearHepatitis.visibility == View.VISIBLE) {
            val value = rootView.tvHepatitisDate.text.toString()
            if (!TextUtils.isEmpty(value)) {
                addData("Hepatitis Test Date", value , DbObservationValues.YES_HEPATITIS_RESULTS.name)
            } else {
                errorList.add("Please select Hepatitis Test Date")
            }
        }
        if (rootView.linearHepaNo.visibility == View.VISIBLE) {
            val value = rootView.etHepatitisCounselling.text.toString()
            if (!TextUtils.isEmpty(value)) {
                addData("Hepatitis Further counselling", value , DbObservationValues.NO_HEPATITIS_RESULTS.name)
            } else {
                errorList.add("Please enter Hepatitis Further counselling")
            }
        }
        val hepatitisStatus = formatter.getRadioText(rootView.radioGrpHepatitisStatus)
        if (hepatitisStatus != "") {
            addData("Hepatitis Status", hepatitisStatus , DbObservationValues.HEPATITIS_MOTHER_STATUS.name)
        }else{
            errorList.add("Please select Hepatitis Status")
        }
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.I_HEPATITIS_TESTING.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()

        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentAntenatal4())
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

                val hivTesting = formatter.retrieveSharedPreference(requireContext(), "hivStatus")
                if (hivTesting != null){
                    CoroutineScope(Dispatchers.Main).launch {
                        if (hivTesting == "Yes") rootView.linearHiv.visibility = View.VISIBLE else rootView.linearHiv.visibility = View.GONE
                    }
                }


                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.ANTENATAL_PROFILE.name)

                if (encounterId != null){

                    val hivTest = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HIV_TESTING.name), encounterId)
                    val hivTestYes = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.YES_HIV_RESULTS.name), encounterId)
                    val hivTestNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NO_HIV_RESULTS.name), encounterId)
                    val hivTestStatus = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HIV_MOTHER_STATUS.name), encounterId)
                    val hivTestDate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HIV_NR_DATE.name), encounterId)

                    val syphilisTest = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SYPHILIS_TESTING.name), encounterId)
                    val syphilisTestYes = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.YES_SYPHILIS_RESULTS.name), encounterId)
                    val syphilisTestNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NO_SYPHILIS_RESULTS.name), encounterId)
                    val syphilisStatus = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SYPHILIS_MOTHER_STATUS.name), encounterId)

                    val hepatitisTest = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HEPATITIS_TESTING.name), encounterId)
                    val hepatitisResultsYes = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.YES_HEPATITIS_RESULTS.name), encounterId)
                    val hepatitisResultsNo = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NO_HEPATITIS_RESULTS.name), encounterId)
                    val hepatitisStatus = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.HEPATITIS_MOTHER_STATUS.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (hivTest.isNotEmpty()){
                            val value = hivTest[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHiv.check(R.id.radioYesHiv)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHiv.check(R.id.radioNoHiv)
                        }
                        if (hivTestYes.isNotEmpty()){
                            val value = hivTestYes[0].value
                            rootView.tvHivDate.setText(value)
                        }
                        if (hivTestNo.isNotEmpty()){
                            val value = hivTestNo[0].value
                            rootView.etTb.setText(value)
                        }
                        if (hivTestStatus.isNotEmpty()){
                            val value = hivTestStatus[0].value
                            if (value.contains("R", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioRHIVStatus)
                            if (value.contains("NR", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioNRHIVStatus)
                            if (value.contains("Inconclusive", ignoreCase = true)) rootView.radioGrpHIVStatus.check(R.id.radioInconclusiveHIVStatus)
                        }
                        if (hivTestDate.isNotEmpty()){
                            val value = hivTestDate[0].value
                            rootView.tvHivTestDate.setText(value)
                        }

                        if (syphilisTest.isNotEmpty()){
                            val value = syphilisTest[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpSyphilis.check(R.id.radioYesSyphilis)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpSyphilis.check(R.id.radioNoSyphilis)
                        }
                        if (syphilisTestYes.isNotEmpty()){
                            val value = syphilisTestYes[0].value
                            rootView.tvSyphilisDate.setText(value)
                        }
                        if (syphilisTestNo.isNotEmpty()){
                            val value = syphilisTestNo[0].value
                            rootView.etSyphilisCounselling.setText(value)
                        }
                        if (syphilisStatus.isNotEmpty()){
                            val value = syphilisStatus[0].value
                            if (value.contains("R", ignoreCase = true)) rootView.radioGrpSyphilisStatus.check(R.id.radioRSyphilisStatus)
                            if (value.contains("NR", ignoreCase = true)) rootView.radioGrpSyphilisStatus.check(R.id.radioNRSyphilisStatus)
                            if (value.contains("Inconclusive", ignoreCase = true)) rootView.radioGrpSyphilisStatus.check(R.id.radioInconclusiveSyphilisStatus)
                        }


                        if (hepatitisTest.isNotEmpty()){
                            val value = hepatitisTest[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHepatitis.check(R.id.radioYesHepatitis)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHepatitis.check(R.id.radioNoHepatitis)
                        }
                        if (hepatitisResultsYes.isNotEmpty()){
                            val value = hepatitisResultsYes[0].value
                            rootView.tvHepatitisDate.setText(value)
                        }
                        if (hepatitisResultsNo.isNotEmpty()){
                            val value = hepatitisResultsNo[0].value
                            rootView.etHepatitisCounselling.setText(value)
                        }
                        if (hepatitisStatus.isNotEmpty()){
                            val value = hepatitisStatus[0].value
                            if (value.contains("R", ignoreCase = true)) rootView.radioGrpHepatitisStatus.check(R.id.radioRHepatitisStatus)
                            if (value.contains("NR", ignoreCase = true)) rootView.radioGrpHepatitisStatus.check(R.id.radioNRHepatitisStatus)
                            if (value.contains("Inconclusive", ignoreCase = true)) rootView.radioGrpHepatitisStatus.check(R.id.radioInconclusiveHepatitisStatus)
                        }

                    }





                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }


}