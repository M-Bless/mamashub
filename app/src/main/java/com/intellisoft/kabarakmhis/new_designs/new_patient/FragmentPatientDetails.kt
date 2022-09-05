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
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.physical_examination.FragmentPhysicalExam2
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.fragment_details.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
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

    private var isAnc = false

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private var patientId: String? = null
    private lateinit var fhirEngine: FhirEngine

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

                            //Get current year
                            val currentYear = LocalDate.now().year
                            //Get current month
                            val currentMonth = LocalDate.now().monthValue

                            Log.e("--------", "-----")
                            println(currentYear)
                            println(currentMonth)

                            /**
                             * GET YEAR AND MONTH FROM System.currentTimeMillis()
                             */
                            ancCodeValue = "$currentYear-$currentMonth-${anc}"
                        }
                        val ancCode = DbDataList("ANC Code", ancCodeValue, DbSummaryTitle.B_PATIENT_DETAILS.name,
                            DbResourceType.Observation.name, DbObservationValues.ANC_PNC_CODE.name)

                        var pncCodeValue = ""
                        if (!isAnc){
                            pncCodeValue = pnc
                        }
                        val pncNo =  DbDataList("PNC Code", pncCodeValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.ANC_PNC_CODE.name)

                        var patientId = ""

                        val patientSavedId = formatter.retrieveSharedPreference(requireContext(), "FHIRID")
                        patientId = if (patientSavedId != null){
                            patientSavedId
                        }else{
                            formatter.generateUuid()
                        }



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
                        formatter.saveSharedPreference(requireContext(), "FHIRID", patientId)
                        formatter.saveSharedPreference(requireContext(), "patientId", patientId)
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

        try {

            CoroutineScope(Dispatchers.IO).launch {

                patientId = formatter.retrieveSharedPreference(requireContext(), "patientId")
                if (patientId != null){

                    fhirEngine = FhirApplication.fhirEngine(requireContext())

                    patientDetailsViewModel = ViewModelProvider(this@FragmentPatientDetails,
                        PatientDetailsViewModel.PatientDetailsViewModelFactory(
                            requireContext().applicationContext as Application, fhirEngine, patientId.toString())
                    )[PatientDetailsViewModel::class.java]

                    val observationList = patientDetailsViewModel.getObservationFromEncounter(
                        DbResourceViews.PATIENT_INFO.name)

                    if (observationList.isNotEmpty()){

                        val encounterId = observationList[0].id

                        val facilityName = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.FACILITY_NAME.name), encounterId)
                        val kmhflCode = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.KMHFL_CODE.name), encounterId)
                        val gravida = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.GRAVIDA.name), encounterId)
                        val parity = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.PARITY.name), encounterId)
                        val lmp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.LMP.name), encounterId)
                        val edd = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.EDD.name), encounterId)
                        val height = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.HEIGHT.name), encounterId)
                        val weight = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.WEIGHT.name), encounterId)
                        val educationLevel = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                            formatter.getCodes(DbObservationValues.EDUCATION_LEVEL.name), encounterId)

                        val clientDetails = patientDetailsViewModel.getPatientData()
                        val dob = clientDetails.dob
                        val maritalStatus = clientDetails.maritalStatus
                        val clientName = clientDetails.name

                        val identifierList = clientDetails.identifier
                        var identifier = ""
                        var nationalId = ""

                        identifierList.forEach {

                            if (it.id == "ANC_NUMBER"){
                                identifier = it.value
                            }
                            if (it.id == "NATIONAL_ID"){
                                nationalId = it.value
                            }

                        }

                        CoroutineScope(Dispatchers.Main).launch {

                            if (clientName.isNotEmpty()){
                                rootView.etClientName.setText(clientName)
                            }

                            if (facilityName.isNotEmpty()){
                                rootView.etFacilityName.setText(facilityName[0].value)
                            }
                            if (kmhflCode.isNotEmpty()){
                                rootView.etKmhflCode.setText(kmhflCode[0].value)
                            }
                            if (gravida.isNotEmpty()){
                                val valueNo = formatter.getValues(gravida[0].value, 0)
                                rootView.etGravida.setText(valueNo)
                            }
                            if (parity.isNotEmpty()){
                                val valueNo = formatter.getValues(parity[0].value, 0)
                                rootView.etParity.setText(valueNo)
                            }
                            if (lmp.isNotEmpty()){
                                rootView.etLmp.setText(lmp[0].value)
                            }
                            if (edd.isNotEmpty()){
                                rootView.etEdd.setText(edd[0].value)
                            }
                            if (height.isNotEmpty()){
                                val valueNo = formatter.getValues(height[0].value, 0)
                                rootView.etHeight.setText(valueNo)
                            }
                            if (weight.isNotEmpty()){
                                val valueNo = formatter.getValues(weight[0].value, 0)
                                rootView.etWeight.setText(valueNo)
                            }
                            if (educationLevel.isNotEmpty()){
                                val value = educationLevel[0].value
                                val valueNo = value.substring(0, value.length-1)

                                Log.e("noValue", valueNo)

                                rootView.spinnerEducation.setSelection(educationLevelList.indexOf(valueNo))
                            }
                            if (maritalStatus != ""){
                                Log.e("maritalStatus", maritalStatus)
                                rootView.spinnerMarital.setSelection(maritalStatusList.indexOf(maritalStatus))
                            }
                            if (dob != ""){
                                rootView.etDoB.setText(dob)
                                val age = formatter.calculateAge(dob).toString()
                                rootView.etAge.setText(age)
                            }

                            if (identifier != ""){
                                val reversedId = identifier.reversed()
                                reversedId.substring(0, 4).reversed()
                                rootView.etAnc.setText(reversedId)
                            }
                            if (nationalId != ""){
                                rootView.etNationalId.setText(nationalId)
                            }

                        }



                    }



                }



            }


        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateDobListener, year, month, day)

                val tenYearsAgo = TimeUnit.DAYS.toMillis(365 * 11)
                val fiftyYearsAgo = TimeUnit.DAYS.toMillis(365 * 50)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis().minus(tenYearsAgo)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis().minus(fiftyYearsAgo)

                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateLMPListener, year, month, day)

                val fourtyWeeksAgo = TimeUnit.DAYS.toMillis(98)
                val twoWeeksAgo = TimeUnit.DAYS.toMillis(14)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis().minus(twoWeeksAgo)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis().minus(fourtyWeeksAgo)
                datePickerDialog.show()

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

                rootView.etDoB.text = date
                val age = "$ageNumber years"
                rootView.etAge.setText(age)

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

            if (days >= 14){

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