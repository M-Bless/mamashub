package com.intellisoft.kabarakmhis.new_designs.new_patient

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.fragment_details.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*

import java.util.*
import kotlin.collections.ArrayList


class FragmentPatientDetails : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var maritalStatusList = arrayOf("","Married", "Widowed", "Single", "Divorced", "Separated")
    var educationLevelList = arrayOf("","Don't know level of education", "No education",
        "Primary school", "Secondary school", "Higher education")

    private var spinnerMaritalValue  = maritalStatusList[0]
    private var educationLevelValue  = educationLevelList[0]

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    private lateinit var rootView: View

    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_details, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

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

        initSpinner()

        rootView.etAnc.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun afterTextChanged(p0: Editable?) {
                val ancValue = rootView.etAnc.text.toString()
                rootView.etPnc.isEnabled = TextUtils.isEmpty(ancValue)
            }

        })
        rootView.etPnc.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun afterTextChanged(p0: Editable?) {
                val ancValue = rootView.etPnc.text.toString()
                rootView.etAnc.isEnabled = TextUtils.isEmpty(ancValue)
            }

        })

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val facilityName = rootView.etFacilityName.text.toString()
        val kmhflCode = rootView.etKmhflCode.text.toString()
        val anc = rootView.etAnc.text.toString()
        val pnc = rootView.etPnc.text.toString()
        val clientName = rootView.etClientName.text.toString()

        val gravida = rootView.etGravida.text.toString()
        val parity = rootView.etParity.text.toString()
        val height = rootView.etHeight.text.toString()
        val weight = rootView.etWeight.text.toString()

        val dob = rootView.etDoB.text.toString()
        val lmp = rootView.etLmp.text.toString()
        val edd = rootView.etEdd.text.toString()

        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) &&
            !TextUtils.isEmpty(anc) && !TextUtils.isEmpty(pnc) &&
            !TextUtils.isEmpty(clientName) && !TextUtils.isEmpty(gravida) &&
            !TextUtils.isEmpty(parity) && !TextUtils.isEmpty(height) &&
            !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(dob) &&
            !TextUtils.isEmpty(lmp) && !TextUtils.isEmpty(edd)){

            val fhirId = formatter.generateUuid()
            formatter.saveSharedPreference(requireContext(), "FHIRID",fhirId)

            val dbDataList = ArrayList<DbDataList>()

            val dbDataFacName = DbDataList("Facility Name", facilityName, "Facility Details", DbResourceType.Observation.name)
            val dbDataKmhfl = DbDataList("KMHFL Code", kmhflCode, "Facility Details", DbResourceType.Observation.name)

            val ancCode = DbDataList("ANC Code", anc, "Patient Details", DbResourceType.Observation.name)
            val pncNo = DbDataList("PNC Code", pnc, "Patient Details", DbResourceType.Observation.name)
            val educationLevel = DbDataList("Level of Education", educationLevelValue, "Patient Details", DbResourceType.Observation.name)

            val nameClient = DbDataList("Client Name", clientName, "Patient Details", DbResourceType.Patient.name)
            val dateOfBirth = DbDataList("Date Of Birth", dob, "Patient Details", DbResourceType.Patient.name)
            val statusMarriage = DbDataList("Marital Status", spinnerMaritalValue, "Patient Details", DbResourceType.Patient.name)

            val gravidaData = DbDataList("Gravida", gravida, "Clinical Information", DbResourceType.Observation.name)
            val parityData = DbDataList("Parity", parity, "Clinical Information", DbResourceType.Observation.name)
            val heightData = DbDataList("Height", height, "Clinical Information", DbResourceType.Observation.name)
            val weightData = DbDataList("Weight", weight, "Clinical Information", DbResourceType.Observation.name)
            val eddData = DbDataList("Expected Date of Delivery", edd, "Clinical Information", DbResourceType.Observation.name)
            val lmpData = DbDataList("Last Menstrual Date", lmp, "Clinical Information", DbResourceType.Observation.name)

            dbDataList.addAll(listOf(dbDataFacName, dbDataKmhfl, ancCode, pncNo, educationLevel,
                gravidaData, parityData, heightData, weightData, eddData, lmpData, nameClient, dateOfBirth, statusMarriage))

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PATIENT_INFO.name, dbDataDetailsList)

            formatter.saveSharedPreference(requireContext(), "dob", dob)
            formatter.saveSharedPreference(requireContext(), "clientName", clientName)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPatientInfo())
            ft.addToBackStack(null)
            ft.commit()

            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        }else{
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
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

            //Check if age is right
            val ageNumber = formatter.getAge(arg1, arg2 + 1, arg3)
            if (ageNumber != null){

                if (ageNumber.toInt() > 9){

                    rootView.etDoB.text = date
                    val age = "$ageNumber years"
                    rootView.etAge.setText(age)

                }else{
                    Toast.makeText(requireContext(), "Please select a higher age.", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(requireContext(), "The age is invalid!", Toast.LENGTH_SHORT).show()

            }


        }

    @RequiresApi(Build.VERSION_CODES.O)
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

        val educationLevel = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, educationLevelList)
        educationLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerEducation!!.adapter = educationLevel
        rootView.spinnerEducation.onItemSelectedListener = this

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerMarital -> {spinnerMaritalValue = rootView.spinnerMarital.selectedItem.toString()}
            R.id.spinnerEducation -> {educationLevelValue = rootView.spinnerEducation.selectedItem.toString()}

            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


}