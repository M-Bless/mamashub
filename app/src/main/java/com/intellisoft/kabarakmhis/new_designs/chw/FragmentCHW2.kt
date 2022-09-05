package com.intellisoft.kabarakmhis.new_designs.chw

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
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
import com.dave.validations.PhoneNumberValidation
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_chw2.*
import kotlinx.android.synthetic.main.fragment_chw2.view.*
import kotlinx.android.synthetic.main.fragment_chw2.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentCHW2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private var year = 0
    private  var month = 0
    private  var day = 0
    private lateinit var calendar : Calendar

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_chw2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.tvDate.setOnClickListener { createDialog(999) }
        rootView.tvTime.setOnClickListener { createDialog(888) }

        return rootView
    }
    private fun createDialog(id: Int) {

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDoseListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            888 -> {
                val timePickerDialog = TimePickerDialog(requireContext(),
                    myTimeDoseListener, 0, 0, false)
                timePickerDialog.show()
            }

            else -> null
        }


    }

    private val myTimeDoseListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val hour = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay
        val min = if (minute < 10) "0$minute" else minute
        rootView.tvTime.text = "$hour:$min"
    }

    private val myDateDoseListener = DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
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

        handleNavigation()
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

        val chvName = rootView.etPatientName.text.toString()
        val mobileNumber = rootView.etNumber.text.toString()
        val village = rootView.etEstate.text.toString()
        val subLocation = rootView.etSubCounty.text.toString()
        val location = rootView.etLocation.text.toString()
        val communityUnit = rootView.etCommunityName.text.toString()

        val date = rootView.tvDate.text.toString()
        val time = rootView.tvTime.text.toString()
        val officerName = rootView.etOfficerName.text.toString()
        val professionName = rootView.etProfession.text.toString()
        val facilityName = rootView.etHealthFacility.text.toString()
        val actionTaken = rootView.etActionTaken.text.toString()

        if (
            !TextUtils.isEmpty(chvName) && !TextUtils.isEmpty(mobileNumber) &&
            !TextUtils.isEmpty(village) && !TextUtils.isEmpty(subLocation) &&
            !TextUtils.isEmpty(location) && !TextUtils.isEmpty(communityUnit) &&
            !TextUtils.isEmpty(date) && !TextUtils.isEmpty(time) &&
            !TextUtils.isEmpty(officerName) && !TextUtils.isEmpty(professionName) &&
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(actionTaken)
        ){

            val chvNumber = PhoneNumberValidation().getStandardPhoneNumber(mobileNumber)
            if (chvNumber != null){

                addData("Name:",chvName, DbObservationValues.OFFICER_NAME.name)
                addData("Mobile Number:",mobileNumber, DbObservationValues.OFFICER_NUMBER.name)
                addData("Village/Estate:",village, DbObservationValues.TOWN_NAME.name)
                addData("Sub-location:",subLocation, DbObservationValues.SUB_COUNTY_NAME.name)
                addData("Location",location, DbObservationValues.COUNTY_NAME.name)
                addData("Name of community health unit",communityUnit, DbObservationValues.COMMUNITY_HEALTH_UNIT.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.C_CHV_REFERRING_THE_PATIENT.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()

                addData("Date:",date, DbObservationValues.NEXT_VISIT_DATE.name)
                addData("Time:",time, DbObservationValues.TIMING_CONTACT_CHW.name)
                addData("Name of officer:",officerName, DbObservationValues.OFFICER_NAME.name)
                addData("Profession:",professionName, DbObservationValues.PROFESSION.name)
                addData("Name of health facility:",facilityName, DbObservationValues.FACILITY_NAME.name)
                addData("Action taken:",actionTaken, DbObservationValues.ACTION_TAKEN.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.D_RECEIVING_OFFICER.name, DbResourceType.Observation.name, label)
                    dbDataList.add(data)

                }
                observationList.clear()

                val dbDataDetailsList = ArrayList<DbDataDetails>()
                val dbDataDetails = DbDataDetails(dbDataList)
                dbDataDetailsList.add(dbDataDetails)
                val dbPatientData = DbPatientData(DbResourceViews.COMMUNITY_REFERRAL_WORKER.name, dbDataDetailsList)
                kabarakViewModel.insertInfo(requireContext(), dbPatientData)


                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.replace(R.id.fragmentHolder, formatter.startChvFragmentPatient(requireContext(),
                    DbResourceViews.COMMUNITY_REFERRAL_WORKER.name))
                ft.addToBackStack(null)
                ft.commit()

            }else{
                errorList.add("Invalid CHV  mobile number")
                formatter.showErrorDialog(errorList, requireContext())
            }


        }else{

            if (TextUtils.isEmpty(chvName)) errorList.add("Patient Name is required")
            if (TextUtils.isEmpty(mobileNumber)) errorList.add("Mobile Number is required)")
            if (TextUtils.isEmpty(village)) errorList.add("Village is required")
            if (TextUtils.isEmpty(subLocation)) errorList.add("Sub-County is required")
            if (TextUtils.isEmpty(location)) errorList.add("Location is required")
            if (TextUtils.isEmpty(communityUnit)) errorList.add("Community Unit is required")
            if (TextUtils.isEmpty(date)) errorList.add("Date is required")
            if (TextUtils.isEmpty(time)) errorList.add("Time is required")
            if (TextUtils.isEmpty(officerName)) errorList.add("Officer Name is required")
            if (TextUtils.isEmpty(professionName)) errorList.add("Profession is required")
            if (TextUtils.isEmpty(facilityName)) errorList.add("Health Facility is required")
            if (TextUtils.isEmpty(actionTaken)) errorList.add("Action Taken is required")

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

}