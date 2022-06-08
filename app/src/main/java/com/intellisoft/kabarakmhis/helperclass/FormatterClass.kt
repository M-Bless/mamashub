package com.intellisoft.kabarakmhis.helperclass

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import com.intellisoft.kabarakmhis.R
import java.lang.Double
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.NumberFormatException
import kotlin.String
import kotlin.Triple
import kotlin.let
import kotlin.toString

class FormatterClass {

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

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

    fun checkPhoneNo(string: String): Boolean {
        var isNo = true

        try {
            val num = Double.parseDouble(string)
        } catch (e: NumberFormatException) {
            isNo = false
        }
        return isNo
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

    fun getHeaders(context: Context):HashMap<String, String>{

        val stringStringMap = HashMap<String, String>()

        val accessToken = retrieveSharedPreference(context, "token")

        stringStringMap["Authorization"] = " Bearer $accessToken"

        return stringStringMap
    }
    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

}