package com.intellisoft.kabarakmhis.helperclass

import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import com.intellisoft.kabarakmhis.R
import java.lang.Double
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*

class FormatterClass {

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    fun validateEmail(emailAddress: String):Boolean{
        return emailAddress.matches(emailPattern.toRegex())
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

}