package com.intellisoft.kabarakmhis.new_designs.ifas

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_ifas2.*
import kotlinx.android.synthetic.main.fragment_ifas2.view.*
import kotlinx.android.synthetic.main.fragment_ifas2.view.etDosageAmount
import kotlinx.android.synthetic.main.fragment_ifas2.view.etFrequency
import kotlinx.android.synthetic.main.fragment_ifas2.view.navigation
import kotlinx.android.synthetic.main.fragment_ifas2.view.radioGrpBenefits
import kotlinx.android.synthetic.main.fragment_ifas2.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentIfas2 : Fragment() , AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    var contactNumberList = arrayOf("","ANC Contact 1", "ANC Contact 2", "ANC Contact 3", "ANC Contact 4", "ANC Contact 5", "ANC Contact 6", "ANC Contact 7","ANC Contact 8")
    private var spinnerContactNumberValue  = contactNumberList[0]

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_ifas2, container, false)
        formatter.saveCurrentPage("2", requireContext())

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        initSpinner()
        getPageDetails()
        rootView.tvDate.setOnClickListener { onCreateDialog(999) }


        handleNavigation()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateDialog(id: Int) {

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(), myDateListener, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            else -> null
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
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

        rootView.navigation.btnNext.text = "Confirm"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {


        val timeContact = rootView.tvContactTiming.text.toString()
        val tabletNo = rootView.tvTabletNo.text.toString()

        if (!TextUtils.isEmpty(timeContact) && !TextUtils.isEmpty(tabletNo)){
            addData("Time of contact",timeContact)
            addData("No of tablets",tabletNo)
        }

        val dosageAmnt = rootView.etDosageAmount.text.toString()
        if (!TextUtils.isEmpty(dosageAmnt)){
            addData("Dosage Amount",dosageAmnt)
        }

        val frequency = rootView.etFrequency.text.toString()
        if (!TextUtils.isEmpty(frequency)){
            addData("Dosage Frequency",frequency)
        }

        val dateGvn = rootView.tvDate.text.toString()
        if (!TextUtils.isEmpty(dateGvn)){
            addData("Date Dosage Given",dateGvn)
        }


        val text = formatter.getRadioText(rootView.radioGrpBenefits)
        addData("Was IFAS Counselling Done",text)


        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Ifas", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.IFAS.name, dbDataDetailsList)

        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.ANTENATAL_PROFILE.name)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.IFAS.name))
        ft.addToBackStack(null)
        ft.commit()

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.IFAS.name)
//
//        startActivity(Intent(requireContext(), PatientProfile::class.java))

    }

    private fun addData(key: String, value: String) {
        if (key != ""){
            observationList[key] = value
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

}