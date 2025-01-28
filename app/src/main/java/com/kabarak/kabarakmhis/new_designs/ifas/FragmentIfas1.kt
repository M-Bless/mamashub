package com.kabarak.kabarakmhis.new_designs.ifas

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
import kotlinx.android.synthetic.main.fragment_ifas1.*
import kotlinx.android.synthetic.main.fragment_ifas1.view.*
import kotlinx.android.synthetic.main.fragment_ifas1.view.etDosageAmount
import kotlinx.android.synthetic.main.fragment_ifas1.view.etFrequency
import kotlinx.android.synthetic.main.fragment_ifas1.view.navigation
import kotlinx.android.synthetic.main.fragment_ifas1.view.radioGrpBenefits
import kotlinx.android.synthetic.main.fragment_ifas1.view.spinnerAncContact
import kotlinx.android.synthetic.main.fragment_ifas1.view.tvContactTiming
import kotlinx.android.synthetic.main.fragment_ifas1.view.tvDate
import kotlinx.android.synthetic.main.fragment_ifas1.view.tvTabletNo
import kotlinx.android.synthetic.main.fragment_ifas2.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class FragmentIfas1 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    var contactNumberList = arrayOf("","ANC Contact 1", "ANC Contact 2", "ANC Contact 3",
        "ANC Contact 4", "ANC Contact 5", "ANC Contact 6", "ANC Contact 7","ANC Contact 8")
    private var spinnerContactNumberValue  = contactNumberList[0]

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_ifas1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        formatter.saveCurrentPage("1", requireContext())

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        initSpinner()

        rootView.radioGrpIronSuppliment.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearSupplement, true)
                    changeVisibility(rootView.linearNoIron, false)
                    changeVisibility(rootView.linearContactDosage, true)
                } else {
                    changeVisibility(rootView.linearSupplement, false)
                    changeVisibility(rootView.linearNoIron, true)
                    changeVisibility(rootView.linearContactDosage, false)
                }

            }
        }

        rootView.tvDate.setOnClickListener { onCreateDialog(999) }

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
        rootView.navigation.btnPrevious.text = "Back"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val ironSupplimentValue = formatter.getRadioText(rootView.radioGrpIronSuppliment)
        if(ironSupplimentValue != ""){

            addData("Was iron Supplements issued",ironSupplimentValue, DbObservationValues.IRON_SUPPLIMENTS.name)

            if (ironSupplimentValue == "No"){

                val provideReason = etProvideReason.text.toString()
                if (provideReason != "") {
                    addData("Reason for not providing iron supplements", provideReason, DbObservationValues.REASON_FOR_NOT_PROVIDING_IRON_SUPPLIMENTS.name)
                }else{
                    errorList.add("Please provide reason for not providing iron supplements")
                }

            }

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_SUPPLIMENTS_ISSUING_TO_CLIENT.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            if (ironSupplimentValue == "Yes"){

                val otherDrug = rootView.etOtherDrug.text.toString()
                if (!TextUtils.isEmpty(otherDrug)){
                    addData("Other Provided Supplementary Drug",otherDrug, DbObservationValues.OTHER_SUPPLIMENTS.name)
                }

                val drugGivenValue = formatter.getRadioText(rootView.radioGrpDrugGvn)
                if (drugGivenValue != ""){
                    addData("If yes, specify the drug given drug: ",drugGivenValue, DbObservationValues.DRUG_GIVEN.name)
                }else{
                    errorList.add("Please select drug given")
                }

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.A_SUPPLIMENTS_ISSUING_TO_CLIENT.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()



                if (rootView.linearContactDosage.visibility == View.VISIBLE){

                    val timeContact = rootView.tvContactTiming.text.toString()
                    val tabletNo = rootView.tvTabletNo.text.toString()

                    if (!TextUtils.isEmpty(timeContact) && !TextUtils.isEmpty(tabletNo) && spinnerContactNumberValue != ""){
                        addData("Time of contact",timeContact, DbObservationValues.ANC_CONTACT.name)
                        addData("No of tablets",tabletNo, DbObservationValues.TABLET_NUMBER.name)
                        addData("ANC Contact: ",spinnerContactNumberValue, DbObservationValues.CONTACT_TIMING.name)
                    }else{

                        if (TextUtils.isEmpty(timeContact)) errorList.add("Please select time of contact")
                        if (TextUtils.isEmpty(tabletNo)) errorList.add("Please select no of tablets")
                        if (spinnerContactNumberValue == "") errorList.add("Please select contact timing")

                    }

                    for (items in observationList){

                        val key = items.key
                        val dbObservationLabel = observationList.getValue(key)

                        val value = dbObservationLabel.value
                        val label = dbObservationLabel.label

                        val data = DbDataList(key, value, DbSummaryTitle.B_ANC_CONTACT.name, DbResourceType.Observation.name, label)
                        dbDataList.add(data)

                    }
                    observationList.clear()

                    val dosageAmnt = rootView.etDosageAmount.text.toString()
                    if (!TextUtils.isEmpty(dosageAmnt)){
                        addData("Dosage Amount",dosageAmnt, DbObservationValues.DOSAGE_AMOUNT.name)
                    }else{
                        errorList.add("Please enter dosage amount")
                    }

                    val frequency = rootView.etFrequency.text.toString()
                    if (!TextUtils.isEmpty(frequency)){
                        addData("Dosage Frequency",frequency, DbObservationValues.DOSAGE_FREQUENCY.name)
                    }else{
                        errorList.add("Please enter dosage frequency")
                    }

                    val dateGvn = rootView.tvDate.text.toString()
                    if (!TextUtils.isEmpty(dateGvn)){
                        addData("Date Dosage Given",dateGvn, DbObservationValues.DOSAGE_DATE_GIVEN.name)
                    }else{
                        errorList.add("Please enter dosage date given")
                    }

                    val counsellingIfas = formatter.getRadioText(rootView.radioGrpBenefits)
                    if (counsellingIfas != ""){
                        addData("Was IFAS Counselling Done",counsellingIfas, DbObservationValues.IRON_AND_FOLIC_COUNSELLING.name)
                    }else{
                        errorList.add("Please select IFAS counselling")
                    }

                    for (items in observationList){

                        val key = items.key
                        val dbObservationLabel = observationList.getValue(key)

                        val value = dbObservationLabel.value
                        val label = dbObservationLabel.label

                        val data = DbDataList(key, value, DbSummaryTitle.C_DOSAGE.name, DbResourceType.Observation.name, label)
                        dbDataList.add(data)

                    }
                    observationList.clear()


                }


            }


        }else{
            errorList.add("Please select iron supplement selection")
        }




        if (errorList.size == 0) {

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.IFAS.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.IFAS.name))
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

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerAncContact -> {
                spinnerContactNumberValue = spinnerAncContact.selectedItem.toString()
                when (spinnerContactNumberValue) {
                    "ANC Contact 1" -> {
                        val contactTiming = "12"
                        val tabletNo = "56"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 2" -> {
                        val contactTiming = "20"
                        val tabletNo = "42"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 3" -> {
                        val contactTiming = "26"
                        val tabletNo = "28"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 4" -> {
                        val contactTiming = "30"
                        val tabletNo = "28"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 5" -> {
                        val contactTiming = "34"
                        val tabletNo = "14"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 6" -> {
                        val contactTiming = "36"
                        val tabletNo = "14"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 7" -> {
                        val contactTiming = "38"
                        val tabletNo = "14"
                        setText(contactTiming, tabletNo)
                    }
                    "ANC Contact 8" -> {
                        val contactTiming = "40"
                        val tabletNo = "14"
                        setText(contactTiming, tabletNo)
                    }
                    else -> {
//                        val contactTiming = "Upto 12"
//                        val tabletNo = "60"
//                        setText(contactTiming, tabletNo)
                    }
                }
            }
            else -> {}
        }
    }

    private fun setText(contactTiming: String, tabletNo: String) {
        rootView.tvContactTiming.text = "$contactTiming weeks"
        rootView.tvTabletNo.text = "$tabletNo"
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
    private fun initSpinner() {


        val ancContact = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, contactNumberList)
        ancContact.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerAncContact!!.adapter = ancContact

        rootView.spinnerAncContact.onItemSelectedListener = this


    }

    override fun onStart() {
        super.onStart()

        getPageDetails()

        getSavedData()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(), DbResourceViews.IFAS.name)

                if (encounterId != null) {

                    val ironSuppliment = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IRON_SUPPLIMENTS.name), encounterId)
                    val drugGiven = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DRUG_GIVEN.name), encounterId)
                    val reason = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.REASON_FOR_NOT_PROVIDING_IRON_SUPPLIMENTS.name), encounterId)
                    val otherSupplement = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.OTHER_SUPPLIMENTS.name), encounterId)
                    val contactTiming = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.CONTACT_TIMING.name), encounterId)
                    patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ANC_CONTACT.name), encounterId)
                    patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TABLET_NUMBER.name), encounterId)
                    val dosageAmnt = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DOSAGE_AMOUNT.name), encounterId)
                    val dosageFreq = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DOSAGE_FREQUENCY.name), encounterId)
                    val dosageDate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DOSAGE_DATE_GIVEN.name), encounterId)
                    val ifas = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.IRON_AND_FOLIC_COUNSELLING.name), encounterId)


                    CoroutineScope(Dispatchers.Main).launch {

                        if (ironSuppliment.isNotEmpty()){
                            val value = ironSuppliment[0].value
                            if(value.contains("Yes", ignoreCase = true)) rootView.radioGrpIronSuppliment.check(R.id.radioYesSupplement)
                            if(value.contains("No", ignoreCase = true)) rootView.radioGrpIronSuppliment.check(R.id.radioNoSupplement)
                        }
                        if (drugGiven.isNotEmpty()){
                            val value = drugGiven[0].value
                            if (value.contains("Element", ignoreCase = true)) rootView.radioGrpDrugGvn.check(R.id.radioYesDrugGvn)
                            if (value.contains("Combined", ignoreCase = true)) rootView.radioGrpDrugGvn.check(R.id.radioNoDrugGvn)
                        }
                        if (reason.isNotEmpty()){
                            val value = reason[0].value
                            rootView.etProvideReason.setText(value)
                        }
                        if (otherSupplement.isNotEmpty()){
                            val value = otherSupplement[0].value
                            rootView.etOtherDrug.setText(value)
                        }
                        if (contactTiming.isNotEmpty()){
                            val value = contactTiming[0].value
                            val valueNo = value.substring(0, value.length-1)
                            rootView.spinnerAncContact.setSelection(contactNumberList.indexOf(valueNo))
                        }
                        if (dosageAmnt.isNotEmpty()){
                            val value = dosageAmnt[0].value
                            val valueNo = formatter.getValues(value, 3)
                            rootView.etDosageAmount.setText(valueNo)
                        }
                        if (dosageFreq.isNotEmpty()){
                            val value = dosageFreq[0].value
                            rootView.etFrequency.setText(value)
                        }
                        if (dosageDate.isNotEmpty()){
                            val value = dosageDate[0].value
                            val valueNo = formatter.getValues(value, 0)
                            tvDate.setText(valueNo)
                        }
                        if (ifas.isNotEmpty()){
                            val value = ifas[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpBenefits.check(R.id.radioYesBenefit)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpBenefits.check(R.id.radioNoBenefit)
                        }

                    }


                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}