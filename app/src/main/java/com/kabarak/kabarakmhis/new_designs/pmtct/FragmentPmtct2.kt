package com.kabarak.kabarakmhis.new_designs.pmtct

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
import kotlinx.android.synthetic.main.fragment_pmtct2.view.*
import kotlinx.android.synthetic.main.fragment_pmtct2.view.etOther
import kotlinx.android.synthetic.main.fragment_pmtct2.view.navigation
import kotlinx.android.synthetic.main.fragment_pmtct2.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class FragmentPmtct2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
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

        rootView = inflater.inflate(R.layout.fragment_pmtct2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        rootView.tvDate.setOnClickListener {

            onCreateDialog(999)

        }

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.radioGrpRegimen.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReason, true)
                } else {
                    changeVisibility(rootView.linearReason, false)
                }

            }
        }

        handleNavigation()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateListener, year, month, day)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

                datePickerDialog.show()

            }
            else -> null
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener =
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

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()


        val regimenChange = formatter.getRadioText(rootView.radioGrpRegimen)
        if (regimenChange != ""){
            addData("Was regimen changed? ",regimenChange, DbObservationValues.REGIMEN_CHANGE.name)

            if(rootView.linearReason.visibility == View.VISIBLE){

                val regimenList = ArrayList<String>()
                if (rootView.checkboxViralLoad.isChecked) regimenList.add("Change in viral load")
                if (rootView.checkboxAdverseReactions.isChecked) regimenList.add("Adverse reaction")
                if (rootView.checkboxInteraction.isChecked) regimenList.add("Interaction with another drug concomitantly used")
                if (rootView.checkboxTrimester.isChecked) regimenList.add("Pregnancy trimester")

                addData("Reason for regimen change",regimenList.joinToString(","), DbObservationValues.REASON_FOR_REGIMENT_CHANGE.name)

                val otherRegimen = rootView.etOther.text.toString()

                if (!TextUtils.isEmpty(otherRegimen)){
                    addData("Other reason for Regimen change",otherRegimen, DbObservationValues.OTHER_REASON_FOR_REGIMENT_CHANGE.name)
                }
            }

        }else{
            errorList.add("Regimen change is required.")
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.C_PMTCT_DOSAGE.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val date = rootView.tvDate.text.toString()
        val vrResults = rootView.etVLResults.text.toString()

        if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(vrResults)) {

            addData("Date VL was taken", date, DbObservationValues.VIRAL_LOAD_CHANGE.name)
            addData("Results", vrResults, DbObservationValues.VIRAL_LOAD_RESULTS.name)

            for (items in observationList) {

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(
                    key,
                    value,
                    DbSummaryTitle.D_VL_SAMPLE.name,
                    DbResourceType.Observation.name,
                    label
                )
                dbDataList.add(data)

            }

        }else{
            if (TextUtils.isEmpty(date)) errorList.add("Date VL was taken is required")
            if (TextUtils.isEmpty(vrResults)) errorList.add("Results is required")
        }




        if(errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PMTCT.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.PMTCT.name))
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

                val encounterId = formatter.retrieveSharedPreference(requireContext(), DbResourceViews.PMTCT.name)
                if (encounterId != null){

                    val regimenChange = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REGIMEN_CHANGE.name), encounterId)
                    val reason = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REASON_FOR_REGIMENT_CHANGE.name), encounterId)
                    val otherReason = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OTHER_REASON_FOR_REGIMENT_CHANGE.name), encounterId)
                    val viralLoadChange = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.VIRAL_LOAD_CHANGE.name), encounterId)
                    val viralLoadResults = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.VIRAL_LOAD_RESULTS.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (regimenChange.isNotEmpty()){
                            val value = regimenChange[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpRegimen.check(R.id.radioYesRegimen)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpRegimen.check(R.id.radioNoRegimen)
                        }
                        if (reason.isNotEmpty()){
                            val value = reason[0].value
                            val valueList = formatter.stringToWords(value)
                            val checkBoxList = ArrayList<CheckBox>()
                            checkBoxList.addAll(listOf(
                                rootView.checkboxViralLoad,
                                rootView.checkboxAdverseReactions,
                                rootView.checkboxInteraction,
                                rootView.checkboxTrimester
                            ))
                            valueList.forEach {

                                val valueData = it.replace(" ", "").toLowerCase()
                                checkBoxList.forEach { checkBox ->
                                    if (checkBox.text.toString().replace(" ", "").lowercase() == valueData){
                                        checkBox.isChecked = true
                                    }
                                }

                            }
                        }
                        if (otherReason.isNotEmpty()){
                            val value = otherReason[0].value
                            rootView.etOther.setText(value)
                        }
                        if (viralLoadChange.isNotEmpty()){
                            val value = viralLoadChange[0].value
                            val valueData = formatter.getValues(value, 0)
                            rootView.tvDate.setText(valueData)
                        }
                        if (viralLoadResults.isNotEmpty()){
                            val value = viralLoadResults[0].value
                            rootView.etVLResults.setText(value)
                        }

                    }

                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}