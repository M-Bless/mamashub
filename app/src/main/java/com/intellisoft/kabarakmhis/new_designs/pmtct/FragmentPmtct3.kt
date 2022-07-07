package com.intellisoft.kabarakmhis.new_designs.pmtct

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
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_ifas1.view.*
import kotlinx.android.synthetic.main.fragment_pmtct3.view.*
import kotlinx.android.synthetic.main.fragment_pmtct3.view.navigation
import kotlinx.android.synthetic.main.fragment_pmtct3.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*


class FragmentPmtct3 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_pmtct3, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        getPageDetails()

        rootView.tvDate.setOnClickListener {

            onCreateDialog(999)

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

        rootView.navigation.btnNext.text = "Previous"
        rootView.navigation.btnPrevious.text = "Save"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {


        val date = rootView.tvDate.text.toString()
        val vrResults = rootView.etVLResults.text.toString()

        if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(vrResults)){

            addData("Date VL was taken",date)
            addData("Results",vrResults)

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

            formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.PMTCT.name)

            startActivity(Intent(requireContext(), PatientProfile::class.java))

        }else{
            Toast.makeText(requireContext(), "Please provide all fields.", Toast.LENGTH_SHORT).show()
        }





    }



    private fun addData(key: String, value: String) {
        if (key != ""){
            observationList[key] = value
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