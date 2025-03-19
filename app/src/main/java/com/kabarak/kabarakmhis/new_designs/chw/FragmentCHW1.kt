package com.kabarak.kabarakmhis.new_designs.chw

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
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_chw1.*
import kotlinx.android.synthetic.main.fragment_chw1.view.*
import kotlinx.android.synthetic.main.fragment_chw1.view.etHealthFacility
import kotlinx.android.synthetic.main.fragment_chw1.view.etOfficerName
import kotlinx.android.synthetic.main.fragment_chw1.view.etPatientName
import kotlinx.android.synthetic.main.fragment_chw1.view.navigation
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class FragmentCHW1 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    var dangerSignList = arrayOf(
        "Please select one",
        "Vaginal bleeding",
        "Convulsions/fits",
        "Increased blood pressure",
        "Severe headaches with blurred vision",
        "Fever and too weak to get out of bed",
        "Severe abdominal pain",
        "Fast or difficult breathing",
        "Swelling of fingers, face and legs",
        "Reduced / absence of fetal movements",
        "Persistent nausea and vomiting",
        "Leakage of amniotic fluid without labour",
        "Foul smelling discharge/fluid from the vagina"
    )
    private var spinnerDangerSignValue  = dangerSignList[0]

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_chw1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        rootView.tvDob.setOnClickListener { onCreateDialog(999) }

        initSpinner()

        return rootView
    }

    private fun initSpinner() {

        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dangerSignList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerDangerSigns!!.adapter = kinRshp

        rootView.spinnerDangerSigns.onItemSelectedListener = this

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerDangerSigns -> { spinnerDangerSignValue = rootView.spinnerDangerSigns.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)

                val tenYearsAgo = TimeUnit.DAYS.toMillis(365 * 10)
                val fiftyYearsAgo = TimeUnit.DAYS.toMillis(365 * 50)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis().minus(tenYearsAgo)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis().minus(fiftyYearsAgo)

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

            //Check if age is right
            rootView.tvDob.text = date


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

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val clientName = rootView.etPatientName.text.toString()
        val age = rootView.tvDob.text.toString()

        val communityHealthUnit = rootView.etCommunityHealthUnit.text.toString()
        val healthUnit = rootView.etHealthFacility.text.toString()
        val reason = rootView.etReferralReason.text.toString()
        val mainProblem = rootView.etMainProblem.text.toString()
        val interventionGiven = rootView.etIntervention.text.toString()

        val comments = rootView.etComments.text.toString()

        val officerName = rootView.etOfficerName.text.toString()
        val actionTaken = rootView.etActionTaken.text.toString()


        val isFemaleChecked = rootView.checkboxNoPast.isChecked

        if (
            !TextUtils.isEmpty(clientName) && !TextUtils.isEmpty(age) &&
            !TextUtils.isEmpty(communityHealthUnit) && !TextUtils.isEmpty(healthUnit) &&
            !TextUtils.isEmpty(reason) && !TextUtils.isEmpty(mainProblem) &&
            !TextUtils.isEmpty(officerName) && !TextUtils.isEmpty(actionTaken) &&
            !TextUtils.isEmpty(interventionGiven) && !TextUtils.isEmpty(comments) && isFemaleChecked &&
                spinnerDangerSignValue != dangerSignList[0]) {

            val id = FormatterClass().generateUuid()
            formatter.saveSharedPreference(requireContext(), "FHIRID", id)

            val date = formatter.getTodayDateNoTime()
            val time = formatter.getTodayTimeNoDate()

            addData("Name of client",clientName, DbObservationValues.CLIENT_NAME.name)
            addData("Sex","Female", DbObservationValues.BABY_SEX.name)
            addData("Age",age, DbObservationValues.DATE_OF_BIRTH.name)
            addData("Date of referral",date, DbObservationValues.DATE_STARTED.name)
            addData("Time of referral",time, DbObservationValues.TIMING_CONTACT.name)
            addData("Danger signs and symptoms",spinnerDangerSignValue, DbObservationValues.DANGER_SIGNS.name)


            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.A_PATIENT_DATA.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()


            addData("Name of community health unit",communityHealthUnit, DbObservationValues.COMMUNITY_HEALTH_UNIT.name)
            addData("Name of link health facility",healthUnit, DbObservationValues.COMMUNITY_HEALTH_LINK.name)
            addData("Reason(s) for referral",reason, DbObservationValues.REFERRAL_REASON.name)
            addData("Main problem(s)",mainProblem, DbObservationValues.MAIN_PROBLEM.name)
            addData("Intervention given",interventionGiven, DbObservationValues.CHW_INTERVENTION_GIVEN.name)
            addData("Comments",comments, DbObservationValues.CHW_COMMENTS.name)

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.B_COMMUNITY_HEALTH_FACILITY_DETAILS.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

            addData("Name of officer:",officerName, DbObservationValues.OFFICER_NAME.name)
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

            if (TextUtils.isEmpty(clientName)) errorList.add("Client Name is required")
            if (TextUtils.isEmpty(age)) errorList.add("Age is required")
            if (TextUtils.isEmpty(communityHealthUnit)) errorList.add("Community Health Unit is required")
            if (TextUtils.isEmpty(healthUnit)) errorList.add("Health Facility is required")
            if (TextUtils.isEmpty(reason)) errorList.add("Referral Reason is required")
            if (TextUtils.isEmpty(mainProblem)) errorList.add("Main Problem is required")
            if (TextUtils.isEmpty(interventionGiven)) errorList.add("Intervention Given is required")
            if (TextUtils.isEmpty(comments)) errorList.add("Comments is required")

            if (TextUtils.isEmpty(officerName)) errorList.add("Officer Name is required")
            if (TextUtils.isEmpty(actionTaken)) errorList.add("Action Taken is required")

            if (!isFemaleChecked) errorList.add("Please make a selection")

            if (spinnerDangerSignValue == dangerSignList[0]) errorList.add("Please select a danger sign")

            formatter.showErrorDialog(errorList, requireContext())


        }



    }
    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
    }



}