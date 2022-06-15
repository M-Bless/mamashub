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
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_present_preg_2.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.*
import kotlinx.android.synthetic.main.fragment_present_preg_2.view.tvDate
import kotlinx.coroutines.*

import java.util.*


class FragmentPresentPregnancy2 : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var presentationList = arrayOf("Unknown fetal presentation", "Cephalic fetal presentation",
        "Pelvic fetal presentation", "Transverse fetal presentation", "Other fetal presentation")
    private var spinnerPresentationValue  = presentationList[0]

    private var observationList = mutableMapOf<String, String>()
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

        rootView.btnSave.setOnClickListener {
            saveData()
        }
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        initSpinner()

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        rootView.tvDate.setOnClickListener { createDialog(999) }


        return rootView
    }


   


    private fun saveData() {

        val lieText = formatter.getRadioText(rootView.radioGrpLie)
        val foetalHeartRate = formatter.getRadioText(rootView.radGrpFoetalHeartRate)
        val foetalMovement = formatter.getRadioText(rootView.radGrpFoetalMovement)
        val date = tvDate.text.toString()

        if (
            lieText != "" && foetalHeartRate != "" && foetalMovement != "" && !TextUtils.isEmpty(date)
        ){

            addData("Presentation",spinnerPresentationValue)

            addData("Lie",lieText)
            addData("Foetal Heart Rate",foetalHeartRate)
            addData("Foetal Movement",foetalMovement)
            addData("Next Visit",date)


            val dbDataList = ArrayList<DbDataList>()

            for (items in observationList){

                val key = items.key
                val value = observationList.getValue(key)

                val data = DbDataList(key, value, "Present Pregnancy", DbResourceType.Observation.name)
                dbDataList.add(data)

            }

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PRESENT_PREGNANCY.name, dbDataDetailsList)

            formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.PRESENT_PREGNANCY.name)

            startActivity(Intent(requireContext(), PatientProfile::class.java))


        }else{
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }

    }


    private fun addData(key: String, value: String) {
        observationList[key] = value
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