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
import kotlinx.android.synthetic.main.fragment_antenatal3.view.*
import kotlinx.android.synthetic.main.navigation.view.*

import java.util.*


class FragmentAntenatal3 : Fragment() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal3, container, false)

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

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
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateIptNextVisitListener, year, month, day)
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound1Listener, year, month, day)
                datePickerDialog.show()

            }
            996 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateSound2Listener, year, month, day)
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

        if (rootView.linearTestDate.visibility == View.VISIBLE){
            val data = rootView.tvHivDate.text.toString()
            addData("HIV Date Test",data)
        }

        if (rootView.linearSyphTestDate.visibility == View.VISIBLE){
            val data = rootView.tvSyphilisDate.text.toString()
            addData("Syphilis Date Test",data)
        }
        if (rootView.linearHepatitis.visibility == View.VISIBLE){
            val data = rootView.tvHepatitisDate.text.toString()
            addData("Hepatitis B Date Test",data)
        }


        val text = getRadioText(rootView.radioGrpHIVStatus)
        addData("Mother HIV Status",text)

        val text1 = getRadioText(rootView.radioGrpSyphilisStatus)
        addData("Mother Syphilis Status",text1)

        val text2 = getRadioText(rootView.radioGrpHepatitisStatus)
        addData("Mother Hepatitis B Status",text2)

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "HIV, Syphilis & Hepatitis B Test", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.ANTENATAL_PROFILE.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentAntenatal4())
        ft.addToBackStack(null)
        ft.commit()

    }

    private fun getRadioText(radioGroup: RadioGroup?): String {

        return if (radioGroup != null){
            val checkedId = radioGroup.checkedRadioButtonId
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            checkedRadioButton.text.toString()
        }else{
            ""
        }



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