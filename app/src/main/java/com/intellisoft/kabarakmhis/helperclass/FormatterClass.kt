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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.new_patient.FragmentPatientInfo
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
        val name = if (hasName()) name[0].nameAsSingleString else ""

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

        Log.e("++++++remove ", sharedKey)

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
            DbResourceViews.ANTENATAL_PROFILE.name))

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

        Log.e("-------current ", currentPos.toString())
        Log.e("-------final ", finaPos.toString())


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

    fun startFragmentPatient(context: Context, encounterName: String): FragmentPatientInfo {

        saveSharedPreference(context, "encounterTitle", encounterName)
        val frag = FragmentPatientInfo()
        val bundle = Bundle()
        bundle.putString(FragmentPatientInfo.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
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

}