package com.intellisoft.kabarakmhis.new_designs.birth_plan

import android.app.Application
import android.app.DatePickerDialog
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
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.fragment_birthplan1.*
import kotlinx.android.synthetic.main.fragment_birthplan1.view.*
import kotlinx.android.synthetic.main.fragment_birthplan1.view.navigation
import kotlinx.android.synthetic.main.fragment_info.view.*

import kotlinx.android.synthetic.main.navigation.view.*
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_birthplan1, container, false)

        initSpinner()

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

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

        val edd = rootView.etEdd.text.toString()
        val facilityName = rootView.etFacilityName.text.toString()
        val facilityContact = rootView.etFacilityContact.text.toString()
        addData("Expected date of childbirth",edd, DbObservationValues.EDD.name)
        addData("Health facility name",facilityName ,DbObservationValues.FACILITY_NAME.name)
        addData("Health facility contact",facilityContact, DbObservationValues.FACILITY_NUMBER.name)

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label
            val data = DbDataList(key, value, "Birth Plan", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val attendantName = rootView.etAttendantName.text.toString()
        val attendantPhone = rootView.etAttendantPhone.text.toString()
        addData("Name",attendantName, DbObservationValues.ATTENDANT_NAME.name)
        addData("Telephone Number",attendantPhone ,DbObservationValues.ATTENDANT_NUMBER.name)
        addData("Designation",spinnerDesignationValue1, DbObservationValues.ATTENDANT_DESIGNATION.name)

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Birth Attendant", DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()

        val alternativeAttendantName = rootView.etAlternativeAttendantName.text.toString()
        val alternativeAttendantPhone = rootView.etAlternativeAttendantPhone.text.toString()
        addData("Name",alternativeAttendantName, DbObservationValues.ATTENDANT_NAME.name)
        addData("Telephone Number",alternativeAttendantPhone, DbObservationValues.ATTENDANT_NUMBER.name)
        addData("Designation",spinnerDesignationValue2 ,DbObservationValues.ATTENDANT_DESIGNATION.name)

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Alternative Birth Attendant", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val companionName = rootView.etCompanionName.text.toString()
        val companionPhone = rootView.etCompanionPhone.text.toString()
        val companionMeans = rootView.etTransportMeans.text.toString()
        addData("Name",companionName, DbObservationValues.COMPANION_NAME.name)
        addData("Telephone Number",companionPhone, DbObservationValues.COMPANION_NUMBER.name)
        addData("Transport",companionMeans ,DbObservationValues.COMPANION_TRANSPORT.name)
        addData("Relationship",spinnerRshpValue ,DbObservationValues.COMPANION_RELATIONSHIP.name)

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Birth Companion", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()




        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.BIRTH_PLAN.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentBirthPlan2())
        ft.addToBackStack(null)
        ft.commit()

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

}