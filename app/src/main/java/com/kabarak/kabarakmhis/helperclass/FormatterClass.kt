package com.kabarak.kabarakmhis.helperclass

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
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.new_designs.chw.FragmentConfirmChvPatient
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.new_patient.FragmentConfirmPatient
import com.kabarak.kabarakmhis.new_designs.screens.FragmentConfirmDetails
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ServiceRequest
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

    fun serviceReferralRequest(serviceRequest: ServiceRequest, position: Int):DbServiceReferralRequest{

        return serviceRequest.toServiceRequest()

    }

    private fun ServiceRequest.toServiceRequest(): DbServiceReferralRequest {

        val serviceId = if (hasIdElement()) idElement.idPart else ""
        var referralText = ""
        if (hasCode()){
            if (code.hasText()){
                referralText = code.text
            }else{
                if (code.hasCoding()){
                    if (code.coding[0].hasDisplay()){
                        referralText = code.coding[0].display
                    }
                }
            }
        }

        val patientReference = if (hasSubject()) subject.reference else ""
        val authoredOn = if (hasAuthoredOn()) authoredOn.toString() else ""
        val referralDetailsList = ArrayList<DbReasonCodeData>()
        if (hasReasonCode()){
            val reasonCodeList = reasonCode
            if (reasonCodeList.isNotEmpty()){

                reasonCodeList.forEach {

                    val text = it.text
                    if (it.hasCoding()){

                        val coding = it.coding
                        if (coding.isNotEmpty()){
                            coding.forEach { cd ->
                                val code = cd.code
                                val display = cd.display
                                val system = cd.system

                                val reasonCode = DbReasonCodeData(text, code, display)
                                referralDetailsList.add(reasonCode)
                            }
                        }
                    }

                }

            }
        }
        val referralDetails = referralDetailsList.ifEmpty { ArrayList() }

        val dbSupportingInfoList = ArrayList<DbSupportingInfo>()
        if (hasSupportingInfo()){
            val supportingInfo = supportingInfo
            if (supportingInfo.isNotEmpty()){
                supportingInfo.forEach {
                    val reference = it.reference
                    val display = it.display

                    val dbSupportingInfo = DbSupportingInfo(reference, display)
                    dbSupportingInfoList.add(dbSupportingInfo)
                }
            }
        }
        val supportingInfo = dbSupportingInfoList.ifEmpty { ArrayList() }

        val actionTaken = if (hasNote()){
            val note = note
            if (note.isNotEmpty()){
                note[0].text
            }else{
                ""
            }
        }else{
            ""
        }

        var loggedInChwUnit = ""
        var loggedInUserId = ""

        if (hasRequester()){
            val requester = requester
            if(requester.hasReference() && requester.hasDisplay()){
                loggedInChwUnit = requester.reference
                loggedInUserId = requester.display
            }
        }
        val dbChwDetails =  DbChwDetails(loggedInChwUnit, loggedInUserId)

        var clinicianRole = ""
        var clinicianId = ""
        if (hasPerformer()){
            val performer = performer
            if (performer.isNotEmpty()){
                performer.forEach {
                    if (it.hasReference() && it.hasDisplay()){
                        clinicianRole = it.reference
                        clinicianId = it.display
                    }
                }
            }
        }
        val dbClinicianDetails = DbClinicianDetails(clinicianRole, clinicianId)

        var facilityKmflCode = ""
        var facilityName = ""
        if (hasLocationReference()){
            val recipient = locationReference
            if (recipient.isNotEmpty()){
                recipient.forEach {
                    if (it.hasReference() && it.hasDisplay()){
                        facilityKmflCode = it.reference
                        facilityName = it.display
                    }
                }
            }
        }
        val dbFacilityDetails = DbLocation(facilityName, facilityKmflCode)

        //Get character after the last slash
        val patientId = patientReference.substring(patientReference.lastIndexOf('/') + 1)

        val dateAuthored = convertFhirDate(authoredOn) ?: ""

        return DbServiceReferralRequest(serviceId, referralText, patientId, dateAuthored,
            referralDetails, supportingInfo, actionTaken, dbChwDetails,
            dbClinicianDetails, dbFacilityDetails)

    }

    fun convertStringToDate(date: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(date)
    }
    fun convertDdMMyyyy(date: String): Date {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return formatter.parse(date)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun patientData(patient: Patient, position: Int):DbPatientDetails{
        return patient.toPatientItem(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Patient.toPatientItem(position: Int): DbPatientDetails {
        // Show nothing if no values available for gender and date of birth.
        val patientId = if (hasIdElement()) idElement.idPart else ""
        val name = if (hasName()) name[0].family else ""
        val dateUpdated = if (hasMeta()) meta.lastUpdated else ""
        val lastUpdated = if (dateUpdated != "") {
            convertFhirDate(dateUpdated.toString()) ?: ""
        } else {
            ""
        }
        val dob = if (hasBirthDate()) birthDate else ""
        val dobDate = convertFhirDate(dob.toString()) ?: ""

        var kmflCode = ""
        if (hasIdentifier()){

            val identifierList = identifier
            identifierList.forEach {

                val id = it.id
                val value = it.value

                if (id == "KMHFL_CODE"){
                    kmflCode = value
                }else{
                    if (id == "ANC_NUMBER"){
                        //Get the digits before '-'
                        kmflCode = value.substringBefore('-')
                    }
                }

            }

        }


        return DbPatientDetails(
            id = patientId,
            name = name,
            lastUpdated = lastUpdated,
            dob = dobDate,
            kmflCode = kmflCode,
        )
    }


    fun convertStringToLocalDate(date: String): String {

        val cal = Calendar.getInstance()

        val formatter = SimpleDateFormat("dd-MMM-yyyy")
        val dateValue = formatter.parse(date)
        cal.time = dateValue
        cal.add(Calendar.DATE, -280)
        val sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val newDate = cal.time

        return sdf1.format(newDate)

    }
    fun convertYYYYMMDD(date: String): String {

        val cal = Calendar.getInstance()

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val dateValue = formatter.parse(date)
        cal.time = dateValue
        val sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val newDate = cal.time

        return sdf1.format(newDate)

    }


    //Calculate number of weeks between two dates
    fun getWeeksBetweenDates(startDate: String, endDate: String): Int {

        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val startDateString = formatter.parse(startDate)
        val endDateString = formatter.parse(endDate)

        val diff = endDateString.time - startDateString.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt() / 7
    }

    fun getGestationWeeks(lmpDate: String, todayDate: String): String {

        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val startDateString = formatter.parse(lmpDate)
        val endDateString = formatter.parse(todayDate)

        val diff = endDateString.time - startDateString.time

        //Milliseconds to days
        val dayNo = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()

        val weeks = dayNo / 7
        val days = dayNo % 7

        return "$weeks weeks, $days days"
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

    //Convert date to new format
    fun convertDate1(dateValue: String): String {

        val ddMMyyyy = isDateFormat2(dateValue)
        return if (ddMMyyyy){
            //Convert to yyyy-MM-dd from dd-MMM-yyyy
            val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateValue)

            sdf2.format(date)
        }else{
            dateValue
        }

    }

    //Check if string is date in this format dd-MMM-yyyy
    fun isDateFormat2(date: String): Boolean {
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return try {
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }


    fun convertDate(convertDate: String): String {

        val originalFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = originalFormat.parse(convertDate)
        return targetFormat.format(date)
    }
    fun convertFhirDate(convertDate: String): String? {

        val originalFormat: DateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = originalFormat.parse(convertDate)

        return date?.let { targetFormat.format(it) }
    }

    fun convertFhirTime(convertDate: String): String? {

        val originalFormat: DateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
        val date = originalFormat.parse(convertDate)

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
    fun getTodayTimeNoDate(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
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
    fun getDateDetails(dateStr: String): Triple<Int?, Int?, Int?> {

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

    fun getProgress(value: String):Pair<Int, Int>{

        //Get value before of
        val valueBeforeOf = value.substringBefore(" of ").toInt()
        //Get value after of
        val valueAfterOf = value.substringAfter(" of ").toInt()

        return Pair(valueBeforeOf, valueAfterOf)


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
            DbResourceViews.COUNSELLING.name,
            DbResourceViews.TETENUS_DIPTHERIA.name,
            DbResourceViews.MATERNAL_SEROLOGY.name,
            DbResourceViews.MALARIA_PROPHYLAXIS.name,
            DbResourceViews.PMTCT.name,
            DbResourceViews.COMMUNITY_REFERRAL.name,
            DbResourceViews.PATIENT_INFO.name,
            DbResourceViews.DEWORMING.name,
            DbResourceViews.IFAS.name,

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
            DbObservationValues.GESTATION.name,

            "dob", "LMP","kinName","edd","patientId",
            "FHIRID","kinPhone","saveEncounterId","pageConfirmDetails",
            "hivStatus","savedEncounter","GRAVIDA","HEIGHT","PARITY","WEIGHT","clientName",

            "${DbResourceViews.PHYSICAL_EXAMINATION.name}_SUMMARY",
            "${DbResourceViews.PRESENT_PREGNANCY.name}_SUMMARY",
            "${DbResourceViews.TETENUS_DIPTHERIA.name}_SUMMARY",
            "${DbResourceViews.MALARIA_PROPHYLAXIS.name}_SUMMARY",
            "${DbResourceViews.IFAS.name}_SUMMARY"

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

    fun setUserDetails(context: Context, rootView: View){

        val userView = rootView.findViewById<View>(R.id.userView)

        val tvPatient :TextView = userView.findViewById(R.id.tvPatient)
        val tvAncId :TextView = userView.findViewById(R.id.tvAncId)

        val tvEdd :TextView = userView.findViewById(R.id.tvEdd)
        val tvGravida :TextView = userView.findViewById(R.id.tvGravida)
        val tvHeight :TextView = userView.findViewById(R.id.tvHeight)
        val tvParity :TextView = userView.findViewById(R.id.tvParity)
        val tvGestation :TextView = userView.findViewById(R.id.tvGestation)

        val identifier = retrieveSharedPreference(context, "identifier")
        val patientName = retrieveSharedPreference(context, "patientName")

        val parity = retrieveSharedPreference(context, DbObservationValues.PARITY.name)
        val gravida = retrieveSharedPreference(context, DbObservationValues.GRAVIDA.name)
        val height = retrieveSharedPreference(context, DbObservationValues.HEIGHT.name)
        val weight = retrieveSharedPreference(context, DbObservationValues.WEIGHT.name)
        val gestation = retrieveSharedPreference(context, DbObservationValues.GESTATION.name)

        val edd = retrieveSharedPreference(context, "edd")

        tvPatient.text = patientName
        tvAncId.text = identifier

        if (parity != null) {
            tvParity.text = parity
        }
        if (height != null) {
            tvHeight.text = height
        }
        if (gravida != null) {
            tvGravida.text = gravida
        }
        if (parity != null) {
            tvEdd.text = edd
        }
        if (gestation != null) {
            val gestationValue = "$gestation"
            tvGestation.text = gestationValue
        }

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
    fun validateParityGravida(parityValue: String, gravida: String):Pair<Boolean, String>{

        try {

            /**
             * Remove spaces in parity
             * Check if parity has atleast 3 digits
             * Check if the first and last digits for parity are integers, and the middle digit is a '+'
             * Check if the first digit is more than the last digit
             * Check if summation of parity (first and last digit) is less than gravida
             */
            val parity = parityValue.replace("\\s".toRegex(), "")

            val isParityValid = parity.length == 3 &&
                    parity[0].isDigit() &&
                    parity[2].isDigit() &&
                    parity[1] == '+'

            return if (isParityValid) {

                //Get the first and last digits
                val parityFirstDigit = parity[0].toString().toInt()
                val parityLastDigit = parity[2].toString().toInt()

                if (parityFirstDigit == 0 && parityLastDigit == 0) {
                    Pair(true, "")
                }else{

                    //Check if the first digit is more than the last digit
                    if (parityFirstDigit > parityLastDigit) {

                        //Check if summation of parity (first and last digit) is less than gravida
                        if (parityFirstDigit + parityLastDigit < gravida.toInt()) {
                            Pair(true,"")
                        }else{
                            Pair(false, "Summation of parity (first and last digit) is more than gravida")
                        }

                    }else{
                        Pair(false, "The first digit should be more than the last digit")
                    }

                }


            } else {

                var error = ""
                if (parity.length != 3) {
                    error = "Parity should have 3 digits e.g. 2+1"
                } else if (!parity[0].isDigit()) {
                    error = "The first digit should be an integer"
                } else if (!parity[2].isDigit()) {
                    error = "The last digit should be an integer"
                } else if (parity[1] != '+') {
                    error = "The middle digit should be a '+'"
                }

                Pair(false, error)
            }

        }catch (e: Exception){
            return Pair(false, "Check on the parity and gravida value")
        }




    }


    fun getDayOfWeek(dateValue: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateValue)
        val day = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        return day.substring(0, 3)
    }

    //Get Number of days from today date
    fun getDaysFromToday(dateValue: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateValue)
        val today = Date()
        val diff = date.time - today.time
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        if (days > 1){

            return if (days == 2){
                "Tomorrow"
            }else if (days == 3) {
                "2 \ndays"
            }else if (days in 8..29){
                "${days / 7} \nweeks"
            }else if (days > 30) {
                "${days / 30} \nmonths"
            }else{
                "$days \ndays"
            }

        }else{
            return "Today"
        }

    }


    fun startFragmentConfirm(context: Context, encounterName: String): FragmentConfirmDetails {

        saveSharedPreference(context, "encounterTitle", encounterName)

        if (encounterName == DbResourceViews.COMMUNITY_REFERRAL_WORKER.name){

            val frag = FragmentConfirmDetails()
            val bundle = Bundle()
            bundle.putString(FragmentConfirmChvPatient.QUESTIONNAIRE_FILE_PATH_KEY, "client.json")
            frag.arguments = bundle
            return frag

        }else{

            val frag = FragmentConfirmDetails()
            val bundle = Bundle()
            bundle.putString(FragmentConfirmDetails.QUESTIONNAIRE_FILE_PATH_KEY, "client.json")
            frag.arguments = bundle
            return frag

        }



    }

    fun getObservationList(patientDetailsViewModel : PatientDetailsViewModel,
                           dbObservationFhirData:DbObservationFhirData, encounterId:String):ArrayList<DbConfirmDetails>{

        val observationDataList = ArrayList<DbConfirmDetails>()
        val detailsList = ArrayList<DbObserveValue>()

        dbObservationFhirData.codeList.forEach {

            val list = patientDetailsViewModel.getObservationsPerCodeFromEncounter(it, encounterId)
            list.forEach { obs ->
                val text = obs.text
                val value = obs.value
                val dbObserveValue = DbObserveValue(text, value)
                detailsList.add(dbObserveValue)
            }

        }
        val dbConfirmDetails = DbConfirmDetails(dbObservationFhirData.title, detailsList)
        observationDataList.add(dbConfirmDetails)

        return observationDataList
    }

    fun startFragmentPatient(context: Context, encounterName: String): FragmentConfirmPatient {

        saveSharedPreference(context, "encounterTitle", encounterName)

        val frag = FragmentConfirmPatient()
        val bundle = Bundle()
        bundle.putString(FragmentConfirmPatient.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
        frag.arguments = bundle
        return frag
    }
    fun startChvFragmentPatient(context: Context, encounterName: String): FragmentConfirmChvPatient {

        saveSharedPreference(context, "encounterTitle", encounterName)

        val frag = FragmentConfirmChvPatient()
        val bundle = Bundle()
        bundle.putString(FragmentConfirmChvPatient.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
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
            DbObservationValues.LMP.name -> { "21840007" }
            DbObservationValues.KMHFL_CODE.name -> { "76967697" }
            DbObservationValues.ANC_NO.name -> { "9889789" }
            DbObservationValues.EDUCATION_LEVEL.name -> { "276031006" }
            DbObservationValues.EDD.name -> { "161714006" }
            DbObservationValues.NATIONALITY.name -> { "186034007" }

            DbObservationValues.RELATIONSHIP.name -> { "263498003"
            }
            DbObservationValues.GESTATION.name -> { "77386006" }

            DbObservationValues.STUDY_WORK.name -> { "773860062314" }
            DbObservationValues.HOME_SITUATION.name -> { "77386006424" }
            DbObservationValues.RELATIONSHIP_SURROUNDS.name -> { "773860064244" }
            DbObservationValues.RECENT_CHANGE.name -> { "773860065367" }
            DbObservationValues.RECENT_CHANGE_CLIENT.name -> { "7738600645642" }
            DbObservationValues.SAFE_ENVIRONMENT.name -> { "773860062556" }


            DbObservationValues.SURGICAL_HISTORY.name -> {
                "161615003"
            }
            DbObservationValues.OTHER_GYNAECOLOGICAL_HISTORY.name -> {
                "267011001"
            }
            DbObservationValues.OTHER_SURGICAL_HISTORY.name -> {
                "12658000"
            }


            DbObservationValues.DIABETES.name -> {
                "405751000"
            }
            DbObservationValues.HYPERTENSION.name -> {
                "38341003"
            }
            DbObservationValues.OTHER_CONDITIONS.name -> {
                "7867677"
            }
            DbObservationValues.OTHER_CONDITIONS_SPECIFY.name -> {
                "7867677-S"
            }

            DbObservationValues.BLOOD_TRANSFUSION.name -> {
                "116859006"
            }
            DbObservationValues.BLOOD_TRANSFUSION_REACTION.name -> {
                "82545002"
            }
            DbObservationValues.SPECIFY_BLOOD_TRANSFUSION_REACTION.name -> {
                "252314007"
            }
            DbObservationValues.TUBERCULOSIS.name -> {
                "371569005"
            }

            DbObservationValues.DRUG_ALLERGY.name -> {
                "416098002"
            }
            DbObservationValues.SPECIFIC_DRUG_ALLERGY.name -> {
                "416098002-S"
            }
            DbObservationValues.NON_DRUG_ALLERGY.name -> {
                "609328004"
            }
            DbObservationValues.SPECIFIC_NON_DRUG_ALLERGY.name -> {
                "609328004-S"
            }
            DbObservationValues.TWINS.name -> {
                "169828005"
            }
            DbObservationValues.TWINS_SPECIFY.name -> {
                "169828005-S"
            }
            DbObservationValues.TB_FAMILIY_HISTORY.name -> {
                "161414005"
            }
            DbObservationValues.TB_FAMILIY_NAME.name -> {
                "161414005-N"
            }
            DbObservationValues.TB_FAMILIY_RELATIONSHIP.name -> {
                "161414005-R"
            }
            DbObservationValues.FAMILY_LIVING_HOUSEHOLD.name -> {
                "161414005-H"
            }
            DbObservationValues.FAMILIY_TB_SCREENING.name -> {
                "171126009"
            }

            DbObservationValues.GENERAL_EXAMINATION.name -> {
                "25656009"
            }
            DbObservationValues.ABNORMAL_GENERAL_EXAMINATION.name -> {
                "25656009-A"
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
            DbObservationValues.TEMPARATURE.name -> {
                "703421000"
            }
            DbObservationValues.CVS.name -> {
                "267037003"
            }
            DbObservationValues.ABNORMAL_CVS.name -> {
                "267037003-A"
            }

            DbObservationValues.RESPIRATORY_MONITORING.name -> {
                "53617003"
            }
            DbObservationValues.ABNORMAL_RESPIRATORY_MONITORING.name -> {
                "53617003-A"
            }
            DbObservationValues.BREAST_EXAM.name -> {
                "185712006"
            }
            DbObservationValues.ABNORMAL_BREAST_EXAM.name -> {
                "185712006-A"
            }
            DbObservationValues.NORMAL_BREAST_EXAM.name -> {
                "185712006-N"
            }
            DbObservationValues.ABDOMINAL_INSPECTION.name -> {
                "163133003"
            }
            DbObservationValues.SPECIFY_ABDOMINAL_INSPECTION.name -> {
                "163133003-A"
            }

            DbObservationValues.ABDOMINAL_PALPATION.name -> {
                "113011001"
            }
            DbObservationValues.SPECIFY_ABDOMINAL_PALPATION.name -> {
                "113011001-A"
            }
            DbObservationValues.ABDOMINAL_AUSCALATION.name -> {
                "37931006"
            }
            DbObservationValues.SPECIFY_ABDOMINAL_AUSCALATION.name -> {
                "37931006-A"
            }

            DbObservationValues.EXTERNAL_INSPECTION.name -> {
                "77142006"
            }
            DbObservationValues.SPECIFY_EXTERNAL_INSPECTION.name -> {
                "77142006-I"
            }


            DbObservationValues.EXTERNAL_PALPATION.name -> {
                "731273008"
            }
            DbObservationValues.SPECIFY_EXTERNAL_PALPATION.name -> {
                "731273008-P"
            }

            DbObservationValues.EXTERNAL_DISCHARGE.name -> {
                "271939006"
            }
            DbObservationValues.SPECIFY_EXTERNAL_DISCHARGE.name -> {
                "271939006-D"
            }
            DbObservationValues.EXTERNAL_GENITAL_ULCER.name -> {
                "427788009"
            }
            DbObservationValues.SPECIFY_EXTERNAL_GENITAL_ULCER.name -> {
                "427788009-G"
            }

            DbObservationValues.EXTERNAL_FGM.name -> {
                "95041000119101"
            }
            DbObservationValues.COMPLICATIONS_EXTERNAL_FGM.name -> {
                "95041000119101-C"
            }




            DbObservationValues.PREGNANCY_ORDER.name -> { "818602026" }
            DbObservationValues.PREGNANCY_OUTCOME.name -> { "818606686" }
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
            DbObservationValues.ABNORMAL_BABY_PURPERIUM.name -> {
                "289910000-A"
            }

            /**
             * Antenatal Profile
             */
            DbObservationValues.HB_TEST.name -> {
                "302763003"
            }
            DbObservationValues.SPECIFIC_HB_TEST.name -> {
                "302763003-S"
            }
            DbObservationValues.BLOOD_GROUP_TEST.name -> {
                "365636006"
            }
            DbObservationValues.SPECIFIC_BLOOD_GROUP_TEST.name -> {
                "365636006-S"
            }
            DbObservationValues.RHESUS_TEST.name -> {
                "169676009"
            }
            DbObservationValues.SPECIFIC_RHESUS_TEST.name -> {
                "169676009-S"
            }
            DbObservationValues.BLOOD_RBS_TEST.name -> {
                "33747003"
            }
            DbObservationValues.SPECIFIC_BLOOD_RBS_TEST.name -> {
                "33747003-S"
            }
            DbObservationValues.URINALYSIS_TEST.name -> {
                "27171005"
            }
            DbObservationValues.URINALYSIS_RESULTS.name -> {
                "45295008"
            }
            DbObservationValues.ABNORMAL_URINALYSIS_TEST.name -> {
                "45295008-A"
            }
            DbObservationValues.URINALYSIS_TEST_DATE.name -> {
                "390840006"
            }


            DbObservationValues.TB_SCREENING.name -> {
                "171126009"
            }
            DbObservationValues.TB_SCREENING_RESULTS.name -> {
                "371569005"
            }
            DbObservationValues.POSITIVE_TB_DIAGNOSIS.name -> {
                "148264888-P"
            }
            DbObservationValues.NEGATIVE_TB_DIAGNOSIS.name -> {
                "148264888-N"
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
            DbObservationValues.NO_MULTIPLE_BABIES.name -> {
                "45384004-N"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_1.name -> {
                "268445003-1"
            }
            DbObservationValues.GESTATION_BY_ULTRASOUND_1.name -> {
                "268445113-1"
            }
            DbObservationValues.GESTATION_BY_ULTRASOUND_2.name -> {
                "268445113-2"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_1_DATE.name -> {
                "410672004-1"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_2.name -> {
                "268445003-2"
            }
            DbObservationValues.OBSTERIC_ULTRASOUND_2_DATE.name -> {
                "410672004-2"
            }
            DbObservationValues.HIV_STATUS_BEFORE_1_ANC.name -> {
                "19030005-ANC"
            }
            DbObservationValues.ART_ELIGIBILITY.name -> {
                "860046068"
            }
            DbObservationValues.PARTNER_HIV.name -> {
                "278977008-P"
            }
            DbObservationValues.LAST_CCC.name -> {
                "84251009"
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
            DbObservationValues.YES_HIV_RESULTS.name -> {
                "31676001-Y"
            }
            DbObservationValues.NO_HIV_RESULTS.name -> {
                "31676001-NO"
            }
            DbObservationValues.HIV_MOTHER_STATUS.name -> {
                "278977008"
            }
            DbObservationValues.HIV_NR_DATE.name -> {
                "31676001-NR"
            }

            DbObservationValues.SYPHILIS_TESTING.name -> {
                "76272004"
            }

            DbObservationValues.YES_SYPHILIS_RESULTS.name -> {
                "76272004-Y"
            }
            DbObservationValues.NO_SYPHILIS_RESULTS.name -> {
                "76272004-N"
            }
            DbObservationValues.SYPHILIS_MOTHER_STATUS.name -> {
                "10759921000119107"
            }


            DbObservationValues.HEPATITIS_TESTING.name -> {
                "128241005"
            }
            DbObservationValues.YES_HEPATITIS_RESULTS.name -> {
                "128241005-R"
            }
            DbObservationValues.NO_HEPATITIS_RESULTS.name -> {
                "128241005-N"
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
            DbObservationValues.REACTIVE_PARTNER_HIV_RESULTS.name -> {
                "31676001-R"
            }
            DbObservationValues.REFERRAL_PARTNER_HIV_DATE.name -> {
                "31676001-RRD"
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

            DbObservationValues.ATTENDANT_NAME1.name -> {
                "308210000-A"
            }
            DbObservationValues.ATTENDANT_NUMBER1.name -> {
                "308210000-AN"
            }
            DbObservationValues.ATTENDANT_DESIGNATION1.name -> {
                "308210000-AD"
            }
            DbObservationValues.COMPANION_NAME1.name -> {
                "62071000-AC"
            }
            DbObservationValues.COMPANION_NUMBER1.name -> {
                "359993007-ACN"
            }
            DbObservationValues.COMPANION_RELATIONSHIP1.name -> {
                "263498003-ACR"
            }
            DbObservationValues.COMPANION_TRANSPORT1.name -> {
                "360300001-ACT"
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
            DbObservationValues.CLINICAL_NOTES_NEXT_VISIT.name -> {
                "390840007"
            }

            DbObservationValues.CONTACT_NUMBER.name -> {
                "424525001"
            }

            DbObservationValues.MUAC.name -> {
                "284473002"
            }
            DbObservationValues.PALLOR.name -> {
                "274643008"
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
            DbObservationValues.NEXT_CURRENT_VISIT.name -> {
                "390840006-C"
            }
            DbObservationValues.NEXT_VISIT_DATE.name -> {
                "390840006"
            }
            DbObservationValues.IPTP_DATE.name -> {
                "520474952"
            }
            DbObservationValues.IPTP_RESULT_YES.name -> {
                "388435640-Y"
            }
            DbObservationValues.IPTP_RESULT_NO.name -> {
                "388435640-N"
            }
            DbObservationValues.TT_PROVIDED.name -> {
                "73152006"
            }
            DbObservationValues.TT_RESULTS.name -> {
                "73152006-R"
            }
            DbObservationValues.LLITN_GIVEN.name -> {
                "412894909"
            }
            DbObservationValues.LLITN_GIVEN_NEXT_DATE.name -> {
                "784030374-N"
            }
            DbObservationValues.LLITN_RESULTS.name -> {
                "784030374-Y"
            }

            DbObservationValues.TIMING_CONTACT_CHW.name -> {
                "657575557"
            }
            DbObservationValues.TIMING_CONTACT.name -> {
                "6877878876"
            }





            DbObservationValues.REPEAT_SEROLOGY.name -> {
                "412690006"
            }
            DbObservationValues.REPEAT_SEROLOGY_RESULTS_YES.name -> {
                "412690006-Y"
            }
            DbObservationValues.REPEAT_SEROLOGY_RESULTS_NO.name -> {
                "412690006-N"
            }
            DbObservationValues.REPEAT_SEROLOGY_DETAILS.name -> {
                "412690006-R"
            }
            DbObservationValues.REACTIVE_MATERNAL_SEROLOGY_PMTCT.name -> {
                "412690006-RRPMCT"
            }
            DbObservationValues.PARTNER_REACTIVE_SEROLOGY.name -> {
                "412690006-PR"
            }
            DbObservationValues.NON_REACTIVE_SEROLOGY_BOOK.name -> {
                "412690006-B"
            }
            DbObservationValues.NON_REACTIVE_SEROLOGY_CONTINUE_TEST.name -> {
                "412690006-CT"
            }
            DbObservationValues.NON_REACTIVE_SEROLOGY_APPOINTMENT.name -> {
                "412690006-A"
            }


            DbObservationValues.DEWORMING.name -> {
                "14369007"
            }
            DbObservationValues.DEWORMING_DATE.name -> {
                "410671006"
            }


            DbObservationValues.IRON_SUPPLIMENTS.name -> {
                "74935093"
            }
            DbObservationValues.DRUG_GIVEN.name -> {
                "6709950"
            }
            DbObservationValues.REASON_FOR_NOT_PROVIDING_IRON_SUPPLIMENTS.name -> {
                "410666004"
            }
            DbObservationValues.OTHER_SUPPLIMENTS.name -> {
                "26462991"
            }
            DbObservationValues.CONTACT_TIMING.name -> {
                "39667636"
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
            DbObservationValues.DATE_STARTED.name -> {
                "16714043"
            }
            DbObservationValues.REGIMEN.name -> {
                "54840574"
            }
            DbObservationValues.ART_DOSAGE.name -> {
                "48668776"
            }
            DbObservationValues.OTHER_REGIMEN.name -> {
                "54840574-O"
            }
            DbObservationValues.ART_DOSAGE.name -> {
                "48668776"
            }
            DbObservationValues.ART_FREQUENCY.name -> {
                "73670926"
            }
            DbObservationValues.REGIMEN_CHANGE.name -> {
                "69335547"
            }

            DbObservationValues.REASON_FOR_REGIMENT_CHANGE.name -> {
                "9697869"
            }
            DbObservationValues.OTHER_REASON_FOR_REGIMENT_CHANGE.name -> {
                "7676996"
            }

            DbObservationValues.VIRAL_LOAD_CHANGE.name -> {
                "98046364"
            }
            DbObservationValues.VIRAL_LOAD_RESULTS.name -> {
                "93778367"
            }
            DbObservationValues.RH_NEGATIVE.name -> {
                "87392289"
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

            DbObservationValues.OFFICER_NAME.name -> { "106292003" }
            DbObservationValues.OFFICER_NUMBER.name -> { "408402003-1" }
            DbObservationValues.CHV_NAME.name -> { "303119007" }
            DbObservationValues.CHV_NUMBER.name -> { "408402003-2" }
            DbObservationValues.COMMUNITY_HEALTH_UNIT.name -> { "6827000" }

            DbObservationValues.REFERRING_OFFICER.name -> { "420942008" }
            DbObservationValues.CLIENT_SERVICE.name -> { "224930009" }
            DbObservationValues.SIGNATURE.name -> { "700856009" }
            DbObservationValues.PROFESSION.name -> { "14679004" }

            DbObservationValues.COMMUNITY_HEALTH_LINK.name -> { "89696967" }
            DbObservationValues.REFERRAL_REASON.name -> { "762883008" }
            DbObservationValues.MAIN_PROBLEM.name -> { "55607006" }
            DbObservationValues.CHW_INTERVENTION_GIVEN.name -> { "225334002" }
            DbObservationValues.CHW_COMMENTS.name -> { "281296001" }

            DbObservationValues.TOWN_NAME.name -> { "288521009" }
            DbObservationValues.SUB_COUNTY_NAME.name -> { "223922000-S" }
            DbObservationValues.COUNTY_NAME.name -> { "223922000" }

            DbObservationValues.ACTION_TAKEN.name -> { "273248003" }


            ReferralTypes.REFERRAL_TO_FACILITY.name -> { "675765751" }
            ReferralTypes.REFERRAL_TO_CHW.name -> { "675765751-C" }

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

    //Save Data to Shared Preferences
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

    fun getNumber(pos: Int): String{
        return when (pos) {
            1 -> { "First" }
            2 -> { "Second" }
            3 -> { "Third" }
            4 -> { "Fourth" }
            5 -> { "Fifth" }
            6 -> { "Sixth" }
            7 -> { "Seventh" }
            8 -> { "Eighth" }
            9 -> { "Ninth" }
            else -> { "" }
        }
    }

    fun stringToWords(s : String) = s.trim().splitToSequence(',')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    fun getValues(value: String, intNo: Int): String{
        val valueReversed = value.reversed()

        val valueLength = valueReversed.length
        return if (valueLength > intNo && intNo != 0){
            val newValue = valueReversed.substring(intNo, valueReversed.length)
            newValue.reversed().replace(" ", "")
        }else{
            value.replace(" ", "")
        }

    }
    fun isNumeric(toCheck: String): Boolean {
        return toCheck.all { char -> char.isDigit() }
    }

    //Check if string is a date in the future
    fun isDateInFuture(date: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault())
        val currentDate = sdf.format(Date())
        return currentDate < date
    }




}