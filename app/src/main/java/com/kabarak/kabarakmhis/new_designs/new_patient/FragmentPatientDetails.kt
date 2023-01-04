package com.kabarak.kabarakmhis.new_designs.new_patient

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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.fragment_details.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        rootView.checkboxApproximateAge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rootView.etAge.isEnabled = true
                rootView.etDoB.isEnabled = false
                rootView.etDoB.text = ""

            }else{
                rootView.etAge.isEnabled = false
                rootView.etDoB.isEnabled = true
                rootView.etAge.setText("")
            }
        }

        rootView.etAge.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()){
                    val age = s.toString().toInt()
                    if (age > 10){

                        //Check if checkboxApproximateAge is checked
                        if (rootView.checkboxApproximateAge.isChecked){
                            val dob = LocalDate.now().minusYears(age.toLong())
                            //Get the year from the date
                            val year = dob.year
                            val approximateDob = "$year-01-01"
                            rootView.etDoB.text = approximateDob.toString()
                        }



                    }else{
                        rootView.etAge.error = "Age must be greater than 10"
                    }


                }else{
                    rootView.etDoB.setText("")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


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

        rootView.rgClientType.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "No Id") {
                    changeVisibility(rootView.etNationalId, false)
                } else {
                    changeVisibility(rootView.etNationalId, true)
                }

            }
        }

        handleNavigation()

        return rootView
    }

    private fun changeVisibility(editText: EditText, showLinear: Boolean){

        /**
         * TODO: REMOVE VALIDATION ON THE NATIONAL ID
         */

        if (showLinear){
            editText.visibility = View.VISIBLE
        }else{
            editText.visibility = View.GONE
        }

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

        val firstName = rootView.etFirstName.text.toString().trim()
        val secondName = rootView.etMiddleName.text.toString().trim()
        val surname = rootView.etSurname.text.toString().trim()

        val clientName = "$firstName $secondName $surname"

        val gravida = rootView.etGravida.text.toString()
        val parity = rootView.etParity.text.toString()

        val dob = rootView.etDoB.text.toString()
        val lmp = rootView.etLmp.text.toString()
        val edd = rootView.etEdd.text.toString()

        val nationality = formatter.getRadioText(rootView.radioGroupNationality)

        var weightValue = 0
        val weight = rootView.etWeight.text.toString()


        if (
            !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(kmhflCode) &&
            !TextUtils.isEmpty(clientName) && !TextUtils.isEmpty(gravida) &&
            !TextUtils.isEmpty(parity) && !TextUtils.isEmpty(dob) &&
            spinnerMaritalValue != "" && educationLevelValue != "" && !TextUtils.isEmpty(weight)) {

            val isWeight = formatter.validateWeight(weight)
            if (isWeight){
                weightValue = weight.toInt()
            }else{
                rootView.etWeight.error = "Weight should be between 31 and 159 kg."
            }
            val weightData = DbDataList("Weight (kg)", weightValue.toString(), DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.WEIGHT.name)


            val parityGravidaPair = formatter.validateParityGravida(parity, gravida)
            val isParityGravida = parityGravidaPair.first
            var ancNationalID = ""
            if (rootView.etNationalId.visibility == View.VISIBLE){
                val nationalID = rootView.etNationalId.text.toString()
                if (!TextUtils.isEmpty(nationalID)){
                    ancNationalID = nationalID
                }else{
                    rootView.etNationalId.error = "Field cannot be empty."
                }
            }

                if (isParityGravida){

                    if (TextUtils.isEmpty(anc) && TextUtils.isEmpty(pnc)) {

                        Toast.makeText(requireContext(), "Please enter anc or pnc", Toast.LENGTH_SHORT)
                            .show()
                    }else{

                        if (anc.length == 4){

                            val kmflCode = formatter.retrieveSharedPreference(requireContext(), "kmhflCode")
                            var ancCodeValue = ""
                            if (isAnc){

                                //Get current year
                                val currentYear = LocalDate.now().year
                                //Get current month
                                val currentMonth = LocalDate.now().monthValue

                                /**
                                 * GET YEAR AND MONTH FROM System.currentTimeMillis()
                                 */
                                ancCodeValue = "$kmflCode-$currentYear-$currentMonth-${anc}"
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

                            if (ancNationalID == "") {
                                ancNationalID = ancCodeValue
                            }


                            val errorList = ArrayList<String>()

                            val dbDataList = ArrayList<DbDataList>()

                            val dbDataFacName = DbDataList("Facility Name", facilityName, DbSummaryTitle.A_FACILITY_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.FACILITY_NAME.name)
                            val dbDataKmhfl = DbDataList("KMHFL Code", kmhflCode, DbSummaryTitle.A_FACILITY_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.KMHFL_CODE.name)


                            val educationLevel = DbDataList("Level of Education", educationLevelValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Observation.name, DbObservationValues.EDUCATION_LEVEL.name)
                            val nationalIDValue = DbDataList("National Identification", ancNationalID, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name,DbObservationValues.NATIONAL_ID.name )
                            val nameClient = DbDataList("Client Name", clientName, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name, DbObservationValues.CLIENT_NAME.name)
                            val dateOfBirth = DbDataList("Date Of Birth", dob, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name, DbObservationValues.DATE_OF_BIRTH.name)
                            val statusMarriage = DbDataList("Marital Status", spinnerMaritalValue, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Patient.name,DbObservationValues.MARITAL_STATUS.name )
                            val nationalityData = DbDataList("Nationality", nationality, DbSummaryTitle.B_PATIENT_DETAILS.name, DbResourceType.Observation.name,DbObservationValues.NATIONALITY.name)

                            val gravidaData = DbDataList("Gravida", gravida, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.GRAVIDA.name)
                            val parityData = DbDataList("Parity", parity, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.PARITY.name)

                            var healthValue = 0
                            val height = rootView.etHeight.text.toString()
                            if (!TextUtils.isEmpty(height)){
                                val isHeight = formatter.validateHeight(height)
                                if (isHeight){
                                    healthValue = height.toInt()
                                }else{
                                    errorList.add("Height should be between 101 and 199 cm.")
                                    rootView.etHeight.error = "Invalid height"
                                }

                            }
                            val heightData = DbDataList("Height (cm)", healthValue.toString(), DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.HEIGHT.name)

                            if (!TextUtils.isEmpty(lmp)){
                                val lmpData = DbDataList("Last Menstrual Date", lmp, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name, DbObservationValues.LMP.name)
                                dbDataList.add(lmpData)
                            }
                            if (!TextUtils.isEmpty(edd)){
                                val eddData = DbDataList("Expected Date of Delivery", edd, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.EDD.name)
                                dbDataList.add(eddData)
                            }

                            if (rootView.linearLessAge.visibility == View.VISIBLE){


                                val studyWork = formatter.getRadioText(rootView.rgStudyWork)
                                val homeSituation = rootView.etHomeSituation.text.toString()
                                val relationship = rootView.etRelationship.text.toString()
                                val clientChange = rootView.etClientChange.text.toString()
                                val clientSafe = rootView.etClientSafe.text.toString()

                                if (studyWork != "" &&
                                    !TextUtils.isEmpty(homeSituation) &&
                                    !TextUtils.isEmpty(relationship) &&
                                    !TextUtils.isEmpty(clientChange) &&
                                    !TextUtils.isEmpty(clientSafe)){

                                    val studyWorkData = DbDataList("Does client study or work",
                                        studyWork, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.STUDY_WORK.name)
                                    dbDataList.add(studyWorkData)

                                    val homeSituationData = DbDataList("Client's perceive of their home situation", homeSituation, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.HOME_SITUATION.name)
                                    val relationshipData = DbDataList("Relationship with family members", relationship, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.RELATIONSHIP_SURROUNDS.name)
                                    val clientChangeData = DbDataList("Client's perception of changes in their situation", clientChange, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.RECENT_CHANGE_CLIENT.name)
                                    val clientSafeData = DbDataList("Client's perception of their safety", clientSafe, DbSummaryTitle.C_CLINICAL_INFORMATION.name, DbResourceType.Observation.name,DbObservationValues.SAFE_ENVIRONMENT.name)
                                    dbDataList.addAll(listOf(
                                        homeSituationData, relationshipData, clientChangeData, clientSafeData
                                    ))

                                }else{

                                    if(TextUtils.isEmpty(homeSituation)) errorList.add("Home Situation is required.")
                                    if(TextUtils.isEmpty(relationship)) errorList.add("Relationship is required.")
                                    if(TextUtils.isEmpty(clientChange)) errorList.add("Client Change is required.")
                                    if(TextUtils.isEmpty(clientSafe)) errorList.add("Client Safe is required.")
                                    if(studyWork == "") errorList.add("Study/Work is required.")
                                }


                            }

                            dbDataList.addAll(listOf(dbDataFacName, dbDataKmhfl, ancCode, pncNo, educationLevel,
                                gravidaData, parityData, heightData, weightData, nameClient, dateOfBirth, statusMarriage, nationalIDValue, nationalityData))

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

                            if (errorList.isEmpty()){

                                kabarakViewModel.insertInfo(requireContext(), dbPatientData)

                                val ft = requireActivity().supportFragmentManager.beginTransaction()
                                ft.replace(R.id.fragmentHolder, FragmentPatientInfo())
                                ft.addToBackStack(null)
                                ft.commit()

                            }else{

                                formatter.showErrorDialog(errorList, requireContext())
                            }



                        }else{
                            Toast.makeText(requireContext(), "Please enter a valid anc", Toast.LENGTH_SHORT)
                                .show()
                        }


                    }



            }else{

                val parityGravidaError = parityGravidaPair.second
                if (!isParityGravida) Toast.makeText(requireContext(), parityGravidaError, Toast.LENGTH_SHORT).show()


            }


        }else{

            if (TextUtils.isEmpty(rootView.etFirstName.text.toString())) rootView.etFirstName.error = "Please enter a valid client name"
            if (TextUtils.isEmpty(rootView.etMiddleName.text.toString())) rootView.etMiddleName.error = "Please enter a valid client name"
            if (TextUtils.isEmpty(rootView.etSurname.text.toString())) rootView.etSurname.error = "Please enter a valid client name"


            if (TextUtils.isEmpty(rootView.etFacilityName.text.toString())) rootView.etFacilityName.error = "Please enter a valid facility name"
            if (TextUtils.isEmpty(rootView.etKmhflCode.text.toString())) rootView.etKmhflCode.error = "Please enter a valid KMHFL code"
//            if (TextUtils.isEmpty(rootView.etHeight.text.toString())) rootView.etHeight.error = "Please enter a valid height"
            if (TextUtils.isEmpty(rootView.etWeight.text.toString())) rootView.etWeight.error = "Please enter a valid weight"
//            if (TextUtils.isEmpty(rootView.etLmp.text.toString())) rootView.etLmp.error = "Please enter a valid lmp"
//            if (TextUtils.isEmpty(rootView.etEdd.text.toString())) rootView.etEdd.error = "Please enter a valid edd"
            if (TextUtils.isEmpty(rootView.etGravida.text.toString())) rootView.etGravida.error = "Please enter a valid gravida"
            if (TextUtils.isEmpty(rootView.etParity.text.toString())) rootView.etParity.error = "Please enter a valid parity"
            if (TextUtils.isEmpty(rootView.etDoB.text.toString())) rootView.etDoB.error = "Please enter a valid date of birth"

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

                val facilityName = formatter.retrieveSharedPreference(requireContext(), "facilityName")
                val kmhflCode = formatter.retrieveSharedPreference(requireContext(), "kmhflCode")
                val clientName = formatter.retrieveSharedPreference(requireContext(), "clientName")
                val dob1 = formatter.retrieveSharedPreference(requireContext(), "dob")

                CoroutineScope(Dispatchers.Main).launch {

                    //Get first name, middle name and surname from client name
                    val nameList = clientName?.split(" ")
                    if (nameList?.isNotEmpty() == true){

                        when (nameList.size) {
                            3 -> {
                                rootView.etFirstName.setText(nameList[0])
                                rootView.etMiddleName.setText(nameList[1])
                                rootView.etSurname.setText(nameList[2])
                            }
                            2 -> {
                                rootView.etFirstName.setText(nameList[0])
                                rootView.etSurname.setText(nameList[1])
                            }
                            else -> {
                                rootView.etFirstName.setText(nameList[0])
                            }
                        }


                    }

                    if (facilityName != null) rootView.etFacilityName.setText(facilityName)
                    if (kmhflCode != null) rootView.etKmhflCode.setText(kmhflCode)

                    if (dob1 != null){
                        rootView.etDoB.text = dob1
                        val age = formatter.calculateAge(dob1).toString()
                        rootView.etAge.setText(age)

                        if (age.toInt() < 18){
                            rootView.linearLessAge.visibility = View.VISIBLE
                        }else{
                            rootView.linearLessAge.visibility = View.GONE
                        }
                    }

                }

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

                                rootView.spinnerEducation.setSelection(educationLevelList.indexOf(valueNo))
                            }
                            if (maritalStatus != ""){
                                rootView.spinnerMarital.setSelection(maritalStatusList.indexOf(maritalStatus))
                            }
                            if (dob != ""){
                                rootView.etDoB.setText(dob)
                                val age = formatter.calculateAge(dob).toString()
                                rootView.etAge.setText(age)

                                if (age.toInt() < 18){
                                    rootView.linearLessAge.visibility = View.VISIBLE
                                }else{
                                    rootView.linearLessAge.visibility = View.GONE
                                }
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

                val tenYearsAgo = TimeUnit.DAYS.toMillis(365 * 10)
                val fiftyYearsAgo = TimeUnit.DAYS.toMillis(365 * 50)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis().minus(tenYearsAgo)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis().minus(fiftyYearsAgo)

                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog( requireContext(),
                    myDateLMPListener, year, month, day)

                val fourtyWeeksAgo = TimeUnit.DAYS.toMillis(280)
                val twoWeeksAgo = TimeUnit.DAYS.toMillis(14)

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis().minus(twoWeeksAgo)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis().minus(fourtyWeeksAgo)
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

                if (ageNumber.toInt() < 18){
                    rootView.linearLessAge.visibility = View.VISIBLE
                }else{
                    rootView.linearLessAge.visibility = View.GONE
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