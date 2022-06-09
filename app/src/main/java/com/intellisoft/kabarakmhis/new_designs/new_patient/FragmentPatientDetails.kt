package com.intellisoft.kabarakmhis.new_designs.new_patient

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.fragment_details.view.*

import java.util.*


class FragmentPatientDetails : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var maritalStatusList = arrayOf("Married", "Widowed", "Single", "Divorced", "Separated")
    private var spinnerMaritalValue  = maritalStatusList[0]
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private lateinit var rootView: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_details, container, false)

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        rootView.etDoB.setOnClickListener {
            onCreateDialog(999)

        }
        rootView.etLmp.setOnClickListener {
            onCreateDialog(998)
        }
        rootView.btnSave.setOnClickListener {

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPatientInfo())
            ft.addToBackStack(null)
            ft.commit()

        }

        initSpinner()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateLMPListener, year, month, day)
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
            rootView.etDoB.text = date
            val age = "${formatter.getAge(arg1, arg2 + 1, arg3)} years"
            rootView.etAge.setText(age)
        }

    private val myDateLMPListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            rootView.etLmp.text = date
            val edd = formatter.getCalculations(date)
            rootView.etEdd.setText(edd)

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

    private fun initSpinner() {

        val maritalStatus = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, maritalStatusList)
        maritalStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerMarital!!.adapter = maritalStatus

        rootView.spinnerMarital.onItemSelectedListener = this

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerMarital -> {spinnerMaritalValue = rootView.spinnerMarital.selectedItem.toString()}

            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


}