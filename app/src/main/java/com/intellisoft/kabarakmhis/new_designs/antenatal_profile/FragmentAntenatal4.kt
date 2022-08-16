package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

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
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*

import kotlinx.android.synthetic.main.fragment_antenatal4.view.*
import kotlinx.android.synthetic.main.fragment_antenatal4.view.navigation
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHIVStatus
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHiv
import kotlinx.android.synthetic.main.navigation.view.*

import java.util.*
import kotlin.collections.ArrayList


class FragmentAntenatal4 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private val antenatal4 = DbResourceViews.ANTENATAL_4.name

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal4, container, false)

        formatter.saveCurrentPage("4", requireContext())
        getPageDetails()


        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        rootView.radioGrpHiv.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {

                } else {

                }
            }
        }
        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Reactive") {
                    changeVisibility(rootView.linearReactive, true)
                } else {
                    changeVisibility(rootView.linearReactive, false)
                }
            }
        }
        rootView.radioGrpReactive.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReferral, true)
                } else {
                    changeVisibility(rootView.linearReferral, false)
                }
            }
        }
        rootView.tvHivTestDate.setOnClickListener { onCreateDialog(999) }


        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
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

        val errorList = ArrayList<Any>()

        val coupleCounselling = formatter.getRadioText(rootView.radioGrpHiv)
        if (coupleCounselling != ""){
            addData("Couple Counselling", coupleCounselling, DbObservationValues.COUPLE_HIV_TESTING.name)
        }else{
            errorList.add(rootView.radioGrpHiv)
        }
        val partnerHivStatus = formatter.getRadioText(rootView.radioGrpHIVStatus)
        if (partnerHivStatus != ""){
            addData("Partner HIV status", partnerHivStatus, DbObservationValues.PARTNER_HIV_STATUS.name)
        }else{
            errorList.add(rootView.radioGrpHIVStatus)
        }
        if (rootView.linearReactive.visibility == View.VISIBLE){
            val reactive = formatter.getRadioText(rootView.radioGrpReactive)
            if (reactive != ""){
                addData("Was the partner referred for HIV care", reactive, DbObservationValues.PARTNER_HIV_RESULTS.name)
            }else{
                errorList.add(rootView.radioGrpReactive)
            }
        }
        if (rootView.linearReferral.visibility == View.VISIBLE){
            val referral = rootView.tvHivTestDate.text.toString()
            if (!TextUtils.isEmpty(referral)){
                addData("Referral Date", referral , DbObservationValues.NEXT_VISIT_DATE.name)
            }else{
                errorList.add(rootView.tvHivTestDate)
            }
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.J_COUPLE_COUNSELLING_TESTING.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }

        if(errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.ANTENATAL_PROFILE.name)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.ANTENATAL_PROFILE.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{
            formatter.validate(errorList, requireContext())
        }



//        val intent = Intent(requireContext(), PatientProfile::class.java)
//        startActivity(intent)

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


}