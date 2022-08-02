package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal2.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.navigation
import kotlinx.android.synthetic.main.fragment_birthplan2.view.*
import kotlinx.android.synthetic.main.navigation.view.*

import java.util.*


class FragmentAntenatal2 : Fragment() , AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    var artEligibilityList = arrayOf("","I", "II", "III", "IV")
    private var spinnerArtElligibilityValue  = artEligibilityList[0]

    var partnerHivList = arrayOf("","Reactive", "Non-Reactive")
    private var spinnerPartnerHivValue  = partnerHivList[0]

    var arvBeforeFirstVisitList = arrayOf("","Y", "N", "NA", "Revisit")
    private var spinnerBeforeFirstVisitValue  = arvBeforeFirstVisitList[0]

    var startedHaartList = arrayOf("","Y", "N", "NA", "Revisit")
    private var spinnerStartedHaartValue  = startedHaartList[0]

    var cotrimoxazoleList = arrayOf("","Y", "B")
    private var spinnerCotrimoxazoleValue  = cotrimoxazoleList[0]



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal2, container, false)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        rootView.radioGrpTb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTB, true)
                } else {
                    changeVisibility(rootView.linearTB, false)

                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpTbResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Positive") {
                    changeVisibility(rootView.linearPositive, true)
                    changeVisibility(rootView.linearNegative, false)
                } else if (checkedBtn == "Negative")  {
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, true)
                }else{
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpUltrasound1.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearDate, true)
                } else {
                    changeVisibility(rootView.linearDate, false)
                }
            }
        }
        rootView.radioGrpUltrasound2.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linear2ndUltra, true)
                } else {
                    changeVisibility(rootView.linear2ndUltra, false)
                }
            }
        }

        rootView.radioGrpMultipleBaby.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearMultipleBaby, true)
                } else {
                    changeVisibility(rootView.linearMultipleBaby, false)
                }
            }
        }

        rootView.tvIPTDateGiven.setOnClickListener { onCreateDialog(999) }
        rootView.tvIPTNextVisit.setOnClickListener { onCreateDialog(998) }
        rootView.tvUltraSound1.setOnClickListener { onCreateDialog(997) }
        rootView.tvUltraSound2.setOnClickListener { onCreateDialog(996) }

        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun initSpinner() {

        val artEligibility = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, artEligibilityList)
        val partnerHivStatus = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partnerHivList)
        val onArvBeforeFirstAnc = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arvBeforeFirstVisitList)
        val startedHaartAnc = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, startedHaartList)
        val cotrimoxazole = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cotrimoxazoleList)

        artEligibility.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        partnerHivStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        onArvBeforeFirstAnc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        startedHaartAnc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cotrimoxazole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rootView.spinnerEligibility!!.adapter = artEligibility
        rootView.spinnerPartnerHIVStatus!!.adapter = partnerHivStatus
        rootView.spinnerOnARVBeforeANCVisit!!.adapter = onArvBeforeFirstAnc
        rootView.spinnerStartedHaartInANC!!.adapter = startedHaartAnc
        rootView.spinnerCotrimoxazole!!.adapter = cotrimoxazole

        rootView.spinnerEligibility.onItemSelectedListener = this
        rootView.spinnerPartnerHIVStatus.onItemSelectedListener = this
        rootView.spinnerOnARVBeforeANCVisit.onItemSelectedListener = this
        rootView.spinnerStartedHaartInANC.onItemSelectedListener = this
        rootView.spinnerCotrimoxazole.onItemSelectedListener = this



    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerEligibility -> { spinnerArtElligibilityValue = rootView.spinnerEligibility.selectedItem.toString() }
            R.id.spinnerPartnerHIVStatus -> { spinnerPartnerHivValue = rootView.spinnerPartnerHIVStatus.selectedItem.toString() }
            R.id.spinnerOnARVBeforeANCVisit -> { spinnerBeforeFirstVisitValue = rootView.spinnerOnARVBeforeANCVisit.selectedItem.toString() }
            R.id.spinnerStartedHaartInANC -> { spinnerStartedHaartValue = rootView.spinnerStartedHaartInANC.selectedItem.toString() }
            R.id.spinnerCotrimoxazole -> { spinnerCotrimoxazoleValue = rootView.spinnerCotrimoxazole.selectedItem.toString() }


            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        if (rootView.linearTB.visibility == View.VISIBLE){
            val text = getRadioText(rootView.radioGrpTbResults)
            addData("TB test",text)
        }
        if (rootView.linearPositive.visibility == View.VISIBLE){
            val data = rootView.etTb.text.toString()
            addData("TB diagnosis",data)
        }
        if (rootView.linearNegative.visibility == View.VISIBLE){
            val data = rootView.etIpt.text.toString()
            addData("Negative IPT",data)
        }
        if (rootView.linearDate.visibility == View.VISIBLE){
            val data = rootView.tvUltraSound1.text.toString()
            addData("1st Obstetric Ultrasound",data)
        }
        if (rootView.linear2ndUltra.visibility == View.VISIBLE){
            val data = rootView.tvUltraSound2.text.toString()
            addData("2nd Obstetric Ultrasound",data)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "TB Screening & Obstetric Ultrasound", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)



        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentAntenatal3())
        ft.addToBackStack(null)
        ft.commit()

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
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
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
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
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
            rootView.tvIPTDateGiven.text = date

        }

    private val myDateIptNextVisitListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvIPTNextVisit.text = date

        }


    private val myDateSound1Listener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvUltraSound1.text = date

        }


    private val myDateSound2Listener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.tvUltraSound2.text = date

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


    private fun getRadioText(radioGroup: RadioGroup): String {

        val checkedId = radioGroup.checkedRadioButtonId
        val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
        return checkedRadioButton.text.toString()

    }


    private fun addData(key: String, value: String) {
        observationList[key] = value
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


}