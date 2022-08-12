package com.intellisoft.kabarakmhis.new_designs.present_pregnancy

import android.app.Application
import android.app.DatePickerDialog
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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.navigation
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.tvDate
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.*

import java.util.*


class FragmentPresentPregnancy2 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var presentationList = arrayOf("","Unknown fetal presentation", "Cephalic fetal presentation",
        "Pelvic fetal presentation", "Transverse fetal presentation", "Other fetal presentation")
    private var spinnerPresentationValue  = presentationList[0]

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private val retrofitCallsFhir = RetrofitCallsFhir()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_present_preg_2, container, false)


        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        initSpinner()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        rootView.tvDate.setOnClickListener { createDialog(999) }


        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Preview"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val lieText = formatter.getRadioText(rootView.radioGrpLie)
        val foetalHeartRate = formatter.getRadioText(rootView.radGrpFoetalHeartRate)
        val foetalMovement = formatter.getRadioText(rootView.radGrpFoetalMovement)
        val date = tvDate.text.toString()

        if (
            lieText != "" && foetalHeartRate != "" && foetalMovement != "" && !TextUtils.isEmpty(date)
        ){

            addData("Presentation",spinnerPresentationValue, DbObservationValues.PRESENTATION.name)

            addData("Lie",lieText, DbObservationValues.LIE.name)
            addData("Foetal Heart Rate",foetalHeartRate, DbObservationValues.FOETAL_HEART_RATE.name)
            addData("Foetal Movement",foetalMovement, DbObservationValues.FOETAL_MOVEMENT.name)
            addData("Next Visit",date, DbObservationValues.NEXT_VISIT_DATE.name)


            val dbDataList = ArrayList<DbDataList>()

            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, "Presentation", DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PRESENT_PREGNANCY.name, dbDataDetailsList)

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)


            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(),
                DbResourceViews.PRESENT_PREGNANCY.name))
            ft.addToBackStack(null)
            ft.commit()

        }else{
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
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

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presentationList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerPresentation!!.adapter = kinRshp

        rootView.spinnerPresentation.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerPresentation -> { spinnerPresentationValue = rootView.spinnerPresentation.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
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
}