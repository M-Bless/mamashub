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
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.physical_examination.FragmentPhysicalExam2
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_password_reset.*
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.fragment_details.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import java.text.SimpleDateFormat
import java.util.*


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

    private var isAnc = false



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

                isAnc = true
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
                isAnc = false
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
        val nationalID = rootView.etNationalId.text.toString()

        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) &&
            !TextUtils.isEmpty(clientName) && !TextUtils.isEmpty(gravida) &&
            !TextUtils.isEmpty(parity) && !TextUtils.isEmpty(height) &&
            !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(dob) &&
            !TextUtils.isEmpty(lmp) && !TextUtils.isEmpty(edd) && !TextUtils.isEmpty(nationalID) &&
            spinnerMaritalValue != "" && educationLevelValue != "") {

            val isWeight = formatter.validateWeight(weight)
            val isHeight = formatter.validateHeight(height)

            if (isWeight && isHeight && parity.toInt() < gravida.toInt()){

                if (TextUtils.isEmpty(anc) && TextUtils.isEmpty(pnc)) {

                    Toast.makeText(requireContext(), "Please enter anc or pnc", Toast.LENGTH_SHORT)
                        .show()
                }else{

                    if (anc.length == 4){

                        var ancCodeValue = ""
                        if (isAnc){

                            
                            /**
                             * GET YEAR AND MONTH FROM System.currentTimeMillis()
                             */
                            ancCodeValue = "2022-08-${anc}"
                        }
                        val ancCode = DbDataList("ANC Code", ancCodeValue, DbSummaryTitle.B_PATIENT_DETAILS.name,
                            DbResourceType.Observation.name, DbObservationValues.ANC_PNC_CODE.name)

                        var pncCodeValue = ""
                        if (!isAnc){
                            pncCodeValue = pnc
                        }
                        val pncNo =  DbDataList("PNC Code", pncCodeValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.ANC_PNC_CODE.name)

                        val fhirId = formatter.generateUuid()
                        formatter.saveSharedPreference(requireContext(), "FHIRID",fhirId)

                        val dbDataList = ArrayList<DbDataList>()

                        val dbDataFacName = DbDataList("Facility Name", facilityName, DbSummaryTitle.A_FACILITY_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.FACILITY_NAME.name)
                        val dbDataKmhfl = DbDataList("KMHFL Code", kmhflCode, DbSummaryTitle.A_FACILITY_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.KMHFL_CODE.name)


                        val educationLevel = DbDataList("Level of Education", educationLevelValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.EDUCATION_LEVEL.name)

                        val nameClient = DbDataList("Client Name", clientName, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name, DbObservationValues.CLIENT_NAME.name)
                        val dateOfBirth = DbDataList("Date Of Birth", dob, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name, DbObservationValues.DATE_OF_BIRTH.name)
                        val statusMarriage = DbDataList("Marital Status", spinnerMaritalValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name,DbObservationValues.MARITAL_STATUS.name )
                        val nationalIDValue = DbDataList("National Identification", nationalID, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name,DbObservationValues.NATIONAL_ID.name )

                        val gravidaData = DbDataList("Gravida", gravida, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.GRAVIDA.name)
                        val parityData = DbDataList("Parity", parity, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.PARITY.name)
                        val heightData = DbDataList("Height (cm)", height, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.HEIGHT.name)
                        val weightData = DbDataList("Weight (kg)", weight, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.WEIGHT.name)
                        val eddData = DbDataList("Expected Date of Delivery", edd, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.EDD.name)
                        val lmpData = DbDataList("Last Menstrual Date", lmp, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.LMP.name)

                        dbDataList.addAll(listOf(dbDataFacName, dbDataKmhfl, ancCode, pncNo, educationLevel,
                            gravidaData, parityData, heightData, weightData, eddData, lmpData, nameClient, dateOfBirth, statusMarriage, nationalIDValue))

                        val dbDataDetailsList = ArrayList<DbDataDetails>()
                        val dbDataDetails = DbDataDetails(dbDataList)
                        dbDataDetailsList.add(dbDataDetails)
                        val dbPatientData = DbPatientData(DbResourceViews.PATIENT_INFO.name, dbDataDetailsList)

                        formatter.saveSharedPreference(requireContext(), "dob", dob)
                        formatter.saveSharedPreference(requireContext(), "clientName", clientName)
                        formatter.saveSharedPreference(requireContext(), "FHIRID", formatter.generateUuid())
                        formatter.saveSharedPreference(requireContext(), "maritalStatus", spinnerMaritalValue)

                        formatter.saveSharedPreference(requireContext(), "dob", dob)
                        formatter.saveSharedPreference(requireContext(), "LMP", lmp)

                        formatter.saveSharedPreference(requireContext(), "patientName", clientName)
                        formatter.saveSharedPreference(requireContext(), "identifier", ancCodeValue)

//                        val ft = requireActivity().supportFragmentManager.beginTransaction()
//                        ft.replace(R.id.fragmentHolder, formatter.startFragmentPatient(requireContext(),
//                            DbResourceViews.PATIENT_INFO.name))
//                        ft.addToBackStack(null)
//                        ft.commit()

                        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

                        val ft = requireActivity().supportFragmentManager.beginTransaction()
                        ft.replace(R.id.fragmentHolder, FragmentPatientInfo())
                        ft.addToBackStack(null)
                        ft.commit()

                    }else{
                        Toast.makeText(requireContext(), "Please enter a valid anc", Toast.LENGTH_SHORT)
                            .show()
                    }


                }



            }else{

                if (parity.toInt() >= gravida.toInt())Toast.makeText(requireContext(), "Parity cannot be higher than gravida.", Toast.LENGTH_SHORT).show()
                if (weight.toInt() < 31 || weight.toInt() > 159)Toast.makeText(requireContext(), "Weight should be between 31 and 159 kg.", Toast.LENGTH_SHORT).show()
                if (height.toInt() < 101 || height.toInt() > 199)Toast.makeText(requireContext(), "Height should be between 101 and 199 cm.", Toast.LENGTH_SHORT).show()


            }


        }else{

            if (TextUtils.isEmpty(rootView.etFacilityName.text.toString())) rootView.etFacilityName.error = "Please enter a valid facility name"
            if (TextUtils.isEmpty(rootView.etKmhflCode.text.toString())) rootView.etKmhflCode.error = "Please enter a valid KMHFL code"
            if (TextUtils.isEmpty(rootView.etClientName.text.toString())) rootView.etClientName.error = "Please enter a valid client name"
            if (TextUtils.isEmpty(rootView.etHeight.text.toString())) rootView.etHeight.error = "Please enter a valid height"
            if (TextUtils.isEmpty(rootView.etWeight.text.toString())) rootView.etWeight.error = "Please enter a valid weight"
            if (TextUtils.isEmpty(rootView.etLmp.text.toString())) rootView.etLmp.error = "Please enter a valid lmp"
            if (TextUtils.isEmpty(rootView.etEdd.text.toString())) rootView.etEdd.error = "Please enter a valid edd"
            if (TextUtils.isEmpty(rootView.etGravida.text.toString())) rootView.etGravida.error = "Please enter a valid gravida"
            if (TextUtils.isEmpty(rootView.etParity.text.toString())) rootView.etParity.error = "Please enter a valid parity"
            if (TextUtils.isEmpty(rootView.etDoB.text.toString())) rootView.etDoB.error = "Please enter a valid date of birth"
            if (TextUtils.isEmpty(nationalID)) rootView.etNationalId.error = "Please enter an identification number"

            val ancCode = rootView.etAnc.text.toString()
            val pncNo = rootView.etPnc.text.toString()

            if (ancCode =="" && pncNo ==""){
                rootView.etAnc.error = "Please enter a valid anc code"
                rootView.etPnc.error = "Please enter a valid pnc code"
            }

            if (spinnerMaritalValue == "") Toast.makeText(requireContext(), "Please select marital status", Toast.LENGTH_SHORT).show()
            if (educationLevelValue == "") Toast.makeText(requireContext(), "Please select education level", Toast.LENGTH_SHORT).show()

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

    override fun onStart() {
        super.onStart()

        getPastData()
    }

    private fun getPastData() {

        val dob = formatter.retrieveSharedPreference(requireContext(), "dob")
        val lmp = formatter.retrieveSharedPreference(requireContext(), "LMP")

        if (dob != null) rootView.etDoB.text = dob
        if (lmp != null) rootView.etLmp.text = lmp


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

            //Check if its past one month

            val diff = formatter.getDateDifference(date)

            val seconds: Long = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            if (days >= 28){

                rootView.etLmp.text = date
                val edd = formatter.getCalculations(date)
                rootView.etEdd.setText(edd)
            }else{
                Toast.makeText(requireContext(), "Please select a date more than a month", Toast.LENGTH_SHORT).show()
            }




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