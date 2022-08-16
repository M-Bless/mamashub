package com.intellisoft.kabarakmhis.new_designs.ifas

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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_ifas1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("1", requireContext())

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        initSpinner()

        rootView.radioGrpIronSuppliment.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearSupplement, true)
                    changeVisibility(rootView.linearNoIron, false)
                } else {
                    changeVisibility(rootView.linearSupplement, false)
                    changeVisibility(rootView.linearNoIron, true)
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
                    addData("Reason for not providing iron suppliments", provideReason, DbObservationValues.REASON_FOR_NOT_PROVIDING_IRON_SUPPLIMENTS.name)
                }else{
                    errorList.add("Please provide reason for not providing iron suppliments")
                }

            }

            if (ironSupplimentValue == "Yes"){

                val otherDrug = rootView.etOtherDrug.text.toString()
                if (!TextUtils.isEmpty(otherDrug)){
                    addData("Other Provided Supplementary Drug",otherDrug, DbObservationValues.DRUG_GIVEN.name)
                }

                val drugGivenValue = formatter.getRadioText(rootView.radioGrpDrugGvn)
                if (drugGivenValue != ""){
                    addData("If yes, specify the drug given drug: ",drugGivenValue, DbObservationValues.DRUG_GIVEN.name)
                }else{
                    errorList.add("Please select drug given")
                }

            }


        }else{
            errorList.add("Please select iron supplement selection")
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
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }
}