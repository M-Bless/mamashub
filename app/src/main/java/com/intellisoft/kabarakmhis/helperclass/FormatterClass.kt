package com.intellisoft.kabarakmhis.helperclass

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentConfirmPatient
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.FragmentConfirmDetails
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.Patient
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class FormatterClass {

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"


    @RequiresApi(Build.VERSION_CODES.O)
    fun patientData(patient: Patient, position: Int):DbPatientDetails{
        return patient.toPatientItem(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Patient.toPatientItem(position: Int): DbPatientDetails {
        // Show nothing if no values available for gender and date of birth.
        val patientId = if (hasIdElement()) idElement.idPart else ""
        val name = if (hasName()) name[0].family else ""


//        val gender = if (hasGenderElement()) genderElement.valueAsString else ""
//        val dob =
//            if (hasBirthDateElement())
//                LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
//            else null
//        val phone = if (hasTelecom()) telecom[0].value else ""
//        val city = if (hasAddress()) address[0].city else ""
//        val country = if (hasAddress()) address[0].country else ""
//        val isActive = active
//        val html: String = if (hasText()) text.div.valueAsString else ""
        return DbPatientDetails(
            id = patientId,
            name = name
        )
    }

    fun convertStringToDate(date: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(date)
    }

    fun validateEmail(emailAddress: String):Boolean{
        return emailAddress.matches(emailPattern.toRegex())
    }
    fun getCalculations(dateStr: String): String {

        val tripleData = getDateDetails(dateStr)
        val month = tripleData.second.toString().toInt()
        var year = tripleData.third.toString().toInt()

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = sdf.parse(dateStr)
        cal.time = date

        if (month > 3) {
            cal.add(Calendar.YEAR, 1)
        }

        cal.add(Calendar.MONTH, -3)
        cal.add(Calendar.DATE, 7)

        if (month < 4) {
            cal.add(Calendar.YEAR, 1)
        }

        val sdf1 = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)

        val newDate = cal.time

        return sdf1.format(newDate)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAge(input: String):Int{

        val dob = LocalDate.parse(input)
        val curDate = LocalDate.now()

        return if (dob != null && curDate != null){
            Period.between(dob, curDate).years
        } else {
            0
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateLmpAge(input: String):Int{

        var days = 0



        return days

    }

    fun getAge(year: Int, month: Int, day: Int): String? {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob[year, month] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            age--
        }
        val ageInt = age
        return ageInt.toString()
    }

    fun getDateDifference(dateStr: String):Long{

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val gvnDate = sdf.parse(dateStr)
        val today = convertDate(getTodayDate())
        val todayDate = sdf.parse(today)

        val diff: Long = todayDate.time - gvnDate.time
        return diff

    }

    fun convertDate(convertDate:String):String{

        val originalFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = originalFormat.parse(convertDate)
        val formattedDate = targetFormat.format(date)
        return formattedDate
    }
    fun convertFhirDate(convertDate: String): String? {

        Log.e("=======", "======")


        val originalFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = originalFormat.parse(convertDate)

        println(convertDate)
        println(date)

        return date?.let { targetFormat.format(it) }
    }

    fun calculateGestation(lmpDate: String): String {

        val days = try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val today = getTodayDateNoTime()
            val formatted = getRefinedDate(lmpDate)
            val date1 = sdf.parse(today)
            val date2 = sdf.parse(formatted)

            val diff: Long = date1.time - date2.time

            val totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            val daysOfWeek = 7
            val weeks = totalDays / daysOfWeek
            val days = totalDays % daysOfWeek

            "$weeks week(s) $days days"

        } catch (e: Exception) {
            e.printStackTrace()
            "0"
        }
        return days

    }
    private fun getRefinedDate(date: String): String {

        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }

    fun getTodayDateNoTime(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = Date()
        return formatter.format(date)
    }


    fun refineLMP(dateStr: String): String {

        val tripleData = getDateDetails(dateStr)

        val day = tripleData.first.toString().toInt()
        val month = tripleData.second.toString().toInt()
        val year = tripleData.third.toString().toInt()

        return "$day-$month-$year"
    }
    private fun getDateDetails(dateStr: String): Triple<Int?, Int?, Int?> {

        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        } else {
            return Triple(null, null, null)
        }
        val date = LocalDate.parse(dateStr, formatter)

        val day = date.dayOfMonth
        val month = date.monthValue
        val year = date.year

        return Triple(day, month, year)

    }




    fun getTodayDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val date = Date()
        return formatter.format(date)
    }
    fun checkDate(birthDate: String, d2: String): Boolean {

        val sdf1 = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val currentdate = sdf1.parse(birthDate)

        val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val newCurrentDate = sdf2.format(currentdate)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val date1 = sdf.parse(newCurrentDate)
        val date2 = sdf.parse(d2)

        // after() will return true if and only if date1 is after date 2
        if (date1.after(date2)) {
            return false
        }
        return true

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedAge(
        dob: String,
        resources: Resources
    ): String {
        if (dob.isEmpty()) return ""

        return Period.between(LocalDate.parse(dob), LocalDate.now()).let {
            when {
                it.years > 0 -> resources.getQuantityString(R.plurals.ageYear, it.years, it.years)
                it.months > 0 -> resources.getQuantityString(R.plurals.ageMonth, it.months, it.months)
                else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
            }
        }
    }

    fun saveSharedPreference(
        context: Context,
        sharedKey: String,
        sharedValue: String){

        val appName = context.getString(R.string.app_name)
        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(sharedKey, sharedValue)
        editor.apply()
    }

    fun retrieveSharedPreference(
        context: Context,
        sharedKey: String): String? {

        val appName = context.getString(R.string.app_name)

        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(sharedKey, null)

    }

    fun deleteSharedPreference(
        context: Context,
        sharedKey: String
    ){
        val appName = context.getString(R.string.app_name)
        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.remove(sharedKey)
        editor.apply()

    }

    fun nukeEncounters(context: Context) {

        val encounterList = ArrayList<String>()
        encounterList.addAll(listOf(
            DbResourceViews.MEDICAL_HISTORY.name,
            DbResourceViews.PREVIOUS_PREGNANCY.name,
            DbResourceViews.PHYSICAL_EXAMINATION.name,
            DbResourceViews.CLINICAL_NOTES.name,
            DbResourceViews.BIRTH_PLAN.name,
            DbResourceViews.SURGICAL_HISTORY.name,
            DbResourceViews.MEDICAL_DRUG_HISTORY.name,
            DbResourceViews.FAMILY_HISTORY.name,
            DbResourceViews.PRESENT_PREGNANCY.name,
            DbResourceViews.ANTENATAL_PROFILE.name,

            DbObservationValues.COUNTY_NAME.name,
            DbObservationValues.SUB_COUNTY_NAME.name,
            DbObservationValues.WARD_NAME.name,
            DbObservationValues.TOWN_NAME.name,
            DbObservationValues.ADDRESS_NAME.name,
            DbObservationValues.ESTATE_NAME.name,
            DbObservationValues.PHONE_NUMBER.name,
            DbObservationValues.COMPANION_NUMBER.name,
            DbObservationValues.COMPANION_RELATIONSHIP.name,
            DbObservationValues.COMPANION_NAME.name,
            "dob", "LMP"
            ))

        for (items in encounterList){
            deleteSharedPreference(context, items)
        }

    }

    fun getHeaders(context: Context):HashMap<String, String>{

        val stringStringMap = HashMap<String, String>()

        val accessToken = retrieveSharedPreference(context, "token")

        stringStringMap["Authorization"] = " Bearer $accessToken"

        return stringStringMap
    }
    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun progressBarFun(context: Context, currentPos: Int, finaPos: Int, rootView: View){

        val progress = rootView.findViewById<View>(R.id.progress_bar)

        val progressBar : ProgressBar = progress.findViewById(R.id.progressBar)
        val tvProgress : TextView = progress.findViewById(R.id.tvProgress)

        val progressStatusStr = "Page $currentPos / $finaPos"
        tvProgress.text = progressStatusStr

        val progressStatus = ( currentPos.toDouble() / finaPos.toDouble() ) * 100
        progressBar.setProgress(progressStatus.toInt(), true)

        setUserDetails(context, rootView)



    }

    private fun setUserDetails(context: Context, rootView: View){

        val userView = rootView.findViewById<View>(R.id.userView)

        val tvPatient :TextView = userView.findViewById(R.id.tvPatient)
        val tvAncId :TextView = userView.findViewById(R.id.tvAncId)

        val identifier = retrieveSharedPreference(context, "identifier")
        val patientName = retrieveSharedPreference(context, "patientName")

        tvPatient.text = patientName
        tvAncId.text = identifier
    }




    fun saveCurrentPage(currentPage: String,context: Context) {
        saveSharedPreference(context, "currentPage", currentPage)
    }

    fun navigateUser(type: String, context: Context, activity: Activity?){

        when (type) {
            Navigation.FRAGMENT.name -> {



            }
            Navigation.ACTIVITY.name -> {
                if (activity != null){
                    val intent = Intent(context, activity::class.java)
                    context.startActivity(intent)
                }

            }
            else -> {
                null
            }
        }

    }


    fun createObservation(dbObserveList: ArrayList<DbObserveValue>, text: String): DbCode {

        val codingList = ArrayList<DbCodingData>()

        for(items in dbObserveList){

            val code = items.title
            val value = items.value

            val dbData = DbCodingData("http://snomed.info/sct", code, value)
            codingList.add(dbData)

        }

        return DbCode(codingList, text)

    }

    private fun convertToFhir(dbTypeDataValueList: ArrayList<DbTypeDataValue>):ArrayList<DbObserveValue>{

        val dbObserveValueList =  ArrayList<DbObserveValue>()

        for (items in dbTypeDataValueList){

            val type = items.type
            val dbObserveValue = items.dbObserveValue

            dbObserveValueList.add(dbObserveValue)

        }
        return dbObserveValueList

    }

    fun getRadioText(radioGroup: RadioGroup?): String {

        return if (radioGroup != null){
            val checkedId = radioGroup.checkedRadioButtonId
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            checkedRadioButton?.text?.toString() ?: ""

        }else{
            ""
        }

    }

    fun saveToFhir(dbPatientData: DbPatientData, context: Context, encounterType:String) {

        CoroutineScope(Dispatchers.IO).launch {

            val kabarakViewModel = KabarakViewModel(context.applicationContext as Application)
            val retrofitCallsFhir = RetrofitCallsFhir()

            coroutineScope {
                launch(Dispatchers.IO) {

                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).launch {

                        kabarakViewModel.insertInfo(context, dbPatientData)

                    }.join()

                    val list = kabarakViewModel.getTittlePatientData(encounterType, context)
                    val fhirObserveList = convertToFhir(list)

                    val dbCode = createObservation(fhirObserveList, encounterType)

                    val encounterId = retrieveSharedPreference(context, encounterType)
                    if (encounterId != null){
                        retrofitCallsFhir.createObservation(encounterId,context, dbCode)
                    }else{
                        retrofitCallsFhir.createFhirEncounter(context, dbCode, encounterType)
                    }
                }
            }

        }

    }

    fun Context.validated(edittextlist: MutableList<EditText>): Boolean {
        edittextlist.forEach {
            val edittext = it
            if (TextUtils.isEmpty(edittext.text.toString())) {
                edittext.error = "You cannot leave this field blank"
                return false
            }
        }
        return true
    }



    fun Context.mytext(edittext: EditText): String {
        return edittext.text.toString().trim()
    }

    fun validateMuac(muac:String):Boolean{
        return muac.toInt() in 23..30
    }
    fun validateWeight(weight: String):Boolean{
        return weight.toInt() in 31..159
    }
    fun validateHeight(height: String):Boolean{
        return height.toInt() in 101..199
    }

    fun startFragmentConfirm(context: Context, encounterName: String): FragmentConfirmDetails {

        saveSharedPreference(context, "encounterTitle", encounterName)

        val frag = FragmentConfirmDetails()
        val bundle = Bundle()
        bundle.putString(FragmentConfirmDetails.QUESTIONNAIRE_FILE_PATH_KEY, "client.json")
        frag.arguments = bundle
        return frag
    }

    fun startFragmentPatient(context: Context, encounterName: String): FragmentConfirmPatient {

        saveSharedPreference(context, "encounterTitle", encounterName)
        val frag = FragmentConfirmPatient()
        val bundle = Bundle()
        bundle.putString(FragmentConfirmPatient.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
        frag.arguments = bundle
        return frag
    }

    fun checkObservations(code: String):String{

        if (code.contains("weight")){
            return "g"
        }else if (code.contains("height")){
            return "cm"
        }else if (code.contains("Gestation")){
            return "weeks"
        }else if (code.contains("BP")) {
            return "mmHg"
        }else if (code.contains("Pulse")) {
            return "bpm"
        }else if (code.contains("Temperature")) {
            return "°C"
        }else if (code.contains("BMI")) {
            return "kg/m2"
        }else if (code.contains("Head")) {
            return "cm"
        }else if (code.contains("Hemoglobin")) {
            return "g/dl"
        }else if (code.contains("Fundal")) {
            return "cm"
        }else if (code.contains("Dose")) {
            return "gms"
        }else if (code.contains("Amount")) {
            return "mg"
        }else if (code.contains("Duration")) {
            return "hrs"
        }else if (code.contains("MUAC")) {
            return "cm"
        }else{
            return ""
        }


    }

    fun getCodes(value: String): String{

        return when (value) {
            DbObservationValues.GRAVIDA.name -> { "161732006" }
            DbObservationValues.PARITY.name -> { "364325004" }
            DbObservationValues.HEIGHT.name -> { "1153637007" }
            DbObservationValues.WEIGHT.name -> { "726527001" }
            DbObservationValues.LMP.name -> {
                "21840007"
            }
            DbObservationValues.KMHFL_CODE.name -> {
                "76967697"
            }DbObservationValues.ANC_NO.name -> {
                "9889789"
            }
            DbObservationValues.EDUCATION_LEVEL.name -> {
                "276031006"
            }
            DbObservationValues.EDD.name -> {
                "161714006"
            }

            DbObservationValues.RELATIONSHIP.name -> {
                "263498003"
            }
            DbObservationValues.GESTATION.name -> {
                "77386006"
            }
            DbObservationValues.SURGICAL_HISTORY.name -> {
                "161615003"
            }


            DbObservationValues.DIABETES.name -> {
                "405751000"
            }
            DbObservationValues.HYPERTENSION.name -> {
                "38341003"
            }
            DbObservationValues.MEDICAL_HISTORY.name -> {
                "417662000"
            }

            DbObservationValues.BLOOD_TRANSFUSION.name -> {
                "116859006"
            }
            DbObservationValues.TUBERCULOSIS.name -> {
                "371569005"
            }
            DbObservationValues.DRUG_ALLERGY.name -> {
                "416098002"
            }


            DbObservationValues.TWINS.name -> {
                "169828005"
            }
            DbObservationValues.GENERAL_EXAMINATION.name -> {
                "25656009"
            }
            DbObservationValues.SYSTOLIC_BP.name -> {
                "271649006"
            }

            DbObservationValues.DIASTOLIC_BP.name -> {
                "271650006"
            }
            DbObservationValues.PULSE_RATE.name -> {
                "78564009"
            }
            DbObservationValues.CVS.name -> {
                "267037003"
            }

            DbObservationValues.RESPIRATORY_MONITORING.name -> {
                "267037003"
            }
            DbObservationValues.BREAST_EXAM.name -> {
                "185712006"
            }
            DbObservationValues.ABDOMINAL_INSPECTION.name -> {
                "163133003"
            }

            DbObservationValues.ABDOMINAL_PALPATION.name -> {
                "113011001"
            }
            DbObservationValues.ABDOMINAL_AUSCALATION.name -> {
                "37931006"
            }
            DbObservationValues.EXTERNAL_INSPECTION.name -> {
                "77142006"
            }


            DbObservationValues.EXTERNAL_PALPATION.name -> {
                "731273008"
            }
            DbObservationValues.EXTERNAL_DISCHARGE.name -> {
                "271939006"
            }
            DbObservationValues.EXTERNAL_GENITAL_ULCER.name -> {
                "427788009"
            }
            DbObservationValues.PREGNANCY_ORDER.name -> {
                "818602026"
            }
            DbObservationValues.YEAR.name -> {
                "258707000"
            }
            DbObservationValues.ANC_NO.name -> {
                "424525001"
            }

            DbObservationValues.CHILDBIRTH_PLACE.name -> {
                "257557008"
            }
            DbObservationValues.LABOUR_DURATION.name -> {
                "289248003"
            }
            DbObservationValues.DELIVERY_MODE.name -> {
                "386216000"
            }
            DbObservationValues.BABY_WEIGHT.name -> {
                "47340003"
            }
            DbObservationValues.BABY_SEX.name -> {
                "268476009"
            }
            DbObservationValues.BABY_OUTCOME.name -> {
                "364587008"
            }
            DbObservationValues.BABY_PURPERIUM.name -> {
                "289910000"
            }
            DbObservationValues.HB_TEST.name -> {
                "302763003"
            }
            DbObservationValues.BLOOD_GROUP_TEST.name -> {
                "365636006"
            }
            DbObservationValues.RHESUS_TEST.name -> {
                "169676009"
            }
            DbObservationValues.BLOOD_RBS_TEST.name -> {
                "33747003"
            }
            DbObservationValues.URINALYSIS_TEST.name -> {
                "27171005"
            }

            DbObservationValues.URINALYSIS_RESULTS.name -> {
                "45295008"
            }
            DbObservationValues.TB_SCREENING.name -> {
                "171126009"
            }
            DbObservationValues.TB_RESULTS.name -> {
                "371569005"
            }
            DbObservationValues.TB_DIAGNOSIS.name -> {
                "148264888"
            }
            DbObservationValues.IPT_ELIGIBILITY.name -> {
                "521195552"
            }
            DbObservationValues.IPT_DATE.name -> {
                "384813511"
            }

            DbObservationValues.IPT_VISIT.name -> {
                "423337059"
            }
            DbObservationValues.MULTIPLE_BABIES.name -> {
                "45384004"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_1.name -> {
                "268445003-1"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_2.name -> {
                "268445003-2"
            }
            DbObservationValues.ART_ELIGIBILITY.name -> {
                "860046068"
            }
            DbObservationValues.ARV_ANC.name -> {
                "120841000"
            }
            DbObservationValues.HAART_ANC.name -> {
                "416234007"
            }
            DbObservationValues.COTRIMOXAZOLE.name -> {
                "5111197"
            }
            DbObservationValues.HIV_TESTING.name -> {
                "31676001"
            }
            DbObservationValues.HIV_RESULTS.name -> {
                "31676001-R"
            }
            DbObservationValues.HIV_STATUS.name -> {
                "278977008"
            }
            DbObservationValues.SYPHILIS_TESTING.name -> {
                "76272004"
            }

            DbObservationValues.SYPHILIS_RESULTS.name -> {
                "406115008"
            }
            DbObservationValues.SYPHILIS_MOTHER_STATUS.name -> {
                "10759921000119107"
            }
            DbObservationValues.HEPATITIS_TESTING.name -> {
                "128241005"
            }
            DbObservationValues.HEPATITIS_RESULTS.name -> {
                "128241005-R"
            }
            DbObservationValues.HEPATITIS_MOTHER_STATUS.name -> {
                "10759151000119101"
            }
            DbObservationValues.COUPLE_HIV_TESTING.name -> {
                "31676001"
            }
            DbObservationValues.PARTNER_HIV_STATUS.name -> {
                "31676001-S"
            }
            DbObservationValues.PARTNER_HIV_RESULTS.name -> {
                "31676001-R"
            }
            DbObservationValues.FACILITY_NAME.name -> {
                "257622000"
            }
            DbObservationValues.FACILITY_NUMBER.name -> {
                "257622000-N"
            }
            DbObservationValues.ATTENDANT_NAME.name -> {
                "308210000"
            }
            DbObservationValues.ATTENDANT_NUMBER.name -> {
                "308210000-N"
            }
            DbObservationValues.ATTENDANT_DESIGNATION.name -> {
                "308210000-D"
            }
            DbObservationValues.COMPANION_NAME.name -> {
                "62071000"
            }
            DbObservationValues.COMPANION_NUMBER.name -> {
                "359993007"
            }
            DbObservationValues.COMPANION_RELATIONSHIP.name -> {
                "263498003"
            }
            DbObservationValues.COMPANION_TRANSPORT.name -> {
                "360300001"
            }
            DbObservationValues.DONOR_NAME.name -> {
                "308210000"
            }

            DbObservationValues.DONOR_NUMBER.name -> {
                "359993007"
            }
            DbObservationValues.DONOR_BLOOD_GROUP.name -> {
                "365636006"
            }
            DbObservationValues.FINANCIAL_PLAN.name -> {
                "224164009"
            }
            DbObservationValues.CLINICAL_NOTES_DATE.name -> {
                "410671006"
            }
            DbObservationValues.CLINICAL_NOTES.name -> {
                "371524004"
            }
            DbObservationValues.CONTACT_NUMBER.name -> {
                "390840006"
            }

            DbObservationValues.MUAC.name -> {
                "284473002"
            }
            DbObservationValues.FUNDAL_HEIGHT.name -> {
                "249016007"
            }
            DbObservationValues.PRESENTATION.name -> {
                "246105001"
            }
            DbObservationValues.LIE.name -> {
                "249062004"
            }
            DbObservationValues.FOETAL_HEART_RATE.name -> {
                "289438002"
            }
            DbObservationValues.FOETAL_MOVEMENT.name -> {
                "169731002"
            }
            DbObservationValues.NEXT_VISIT_DATE.name -> {
                "390840006"
            }
            DbObservationValues.TT_PROVIDED.name -> {
                "73152006"
            }
            DbObservationValues.LLITN_GIVEN.name -> {
                "412894909"
            }
            DbObservationValues.REPEAT_SEROLOGY_RESULTS.name -> {
                "412690006"
            }
            DbObservationValues.REPEAT_SEROLOGY_DETAILS.name -> {
                "412690006-D"
            }
            DbObservationValues.REACTIVE_MATERNAL_SEROLOGY.name -> {
                "412690006-RR"
            }

            DbObservationValues.NON_REACTIVE_SEROLOGY.name -> {
                "412690006-NR"
            }
            DbObservationValues.DEWORMING.name -> {
                "14369007"
            }
            DbObservationValues.IRON_SUPPLIMENTS.name -> {
                "74935093"
            }
            DbObservationValues.DRUG_GIVEN.name -> {
                "6709950"
            }
            DbObservationValues.ANC_CONTACT.name -> {
                "46645665"
            }
            DbObservationValues.TABLET_NUMBER.name -> {
                "76449731"
            }
            DbObservationValues.DOSAGE_AMOUNT.name -> {
                "14420047"
            }
            DbObservationValues.DOSAGE_FREQUENCY.name -> {
                "20726931"
            }
            DbObservationValues.DOSAGE_DATE_GIVEN.name -> {
                "39234792"
            }
            DbObservationValues.IRON_AND_FOLIC_COUNSELLING.name -> {
                "70346388"
            }


            DbObservationValues.INTERVENTION_GIVEN.name -> {
                "82261064"
            }
            DbObservationValues.REGIMEN.name -> {
                "54840574"
            }
            DbObservationValues.ART_DOSAGE.name -> {
                "48668776"
            }
            DbObservationValues.ART_FREQUENCY.name -> {
                "73670926"
            }
            DbObservationValues.REGIMENT_CHANGE.name -> {
                "69335547"
            }
            DbObservationValues.VIRAL_LOAD_CHANGE.name -> {
                "98046364"
            }
            DbObservationValues.VIRAL_LOAD_RESULTS.name -> {
                "93778367"
            }
            DbObservationValues.DANGER_SIGNS.name -> {
                "50206362"
            }
            DbObservationValues.DENTAL_HEALTH.name -> {
                "69475666"
            }
            DbObservationValues.BIRTH_PLAN.name -> {
                "97129423"
            }

            DbObservationValues.RH_NEGATIVE.name -> {
                "87392289"
            }
            DbObservationValues.EAT_ONE_MEAL.name -> {
                "22723167"
            }
            DbObservationValues.EAT_MORE_MEALS.name -> {
                "43183900"
            }
            DbObservationValues.DRINK_WATER.name -> {
                "96204638"
            }
            DbObservationValues.TAKE_IFAS.name -> {
                "30822033"
            }
            DbObservationValues.AVOID_HEAVY_WORK.name -> {
                "55268779"
            }
            DbObservationValues.SLEEP_UNDER_LLIN.name -> {
                "51049855"
            }

            DbObservationValues.GO_FOR_ANC.name -> {
                "96888625"
            }
            DbObservationValues.NON_STRENUOUS_ACTIVITY.name -> {
                "1528937"
            }
            DbObservationValues.INFANT_FEEDING.name -> {
                "91232116"
            }
            DbObservationValues.EXCLUSIVE_BREASTFEEDING.name -> {
                "80142304"
            }
            DbObservationValues.MOTHER_PALE.name -> {
                "16673572"
            }
            DbObservationValues.SEVERE_HEADACHE.name -> {
                "5745006"
            }
            DbObservationValues.VAGINAL_BLEEDING.name -> {
                "55623893"
            }

            DbObservationValues.ABDOMINAL_PAIN.name -> {
                "46209251"
            }
            DbObservationValues.REDUCED_MOVEMENT.name -> {
                "83359346"
            }
            DbObservationValues.MOTHER_FITS.name -> {
                "25865687"
            }
            DbObservationValues.WATER_BREAKING.name -> {
                "15859810"
            }
            DbObservationValues.SWOLLEN_FACE.name -> {
                "77178232"
            }
            DbObservationValues.FEVER.name -> {
                "35317232"
            }

            else -> {
                ""
            }
        }


    }

    fun validate(edittextList: List<Any>, context: Context) {

        edittextList.forEach {
            if (it is EditText) {
                if (TextUtils.isEmpty(it.text.toString())) {
                    it.error = "You cannot leave this field blank"
                }
            }
            if (it is TextView){
                it.requestFocus()
                it.error = "You cannot leave this field blank"
            }
            if (it is RadioGroup){
                Toast.makeText(context, "Please select an option from the radio buttons", Toast.LENGTH_SHORT).show()
            }



        }
    }

    fun changeStringCase(s: String): String {
        return s.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun showErrorDialog(errorList:ArrayList<String>, context: Context){

        var errors = "Please Resolve the following errors \n\n"
        errorList.forEach {
            errors += "-$it\n"
        }

        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setMessage(errors)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ -> dialog.cancel() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        val alert = dialogBuilder.create()
        alert.setTitle("Errors")
        alert.show()

    }

    fun saveDataLocal(context: Context, key: String, value: String){

        val localData = "KabarakMHIS_DATA"
        val sharedPreferences = context.getSharedPreferences(localData, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()

    }

    fun getDataLocal(context: Context, key: String): String? {

        val localData = "KabarakMHIS_DATA"
        val sharedPreferences = context.getSharedPreferences(localData, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)

    }

}