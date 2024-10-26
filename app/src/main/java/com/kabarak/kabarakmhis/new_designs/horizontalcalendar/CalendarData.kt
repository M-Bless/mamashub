package com.kabarak.kabarakmhis.new_designs.horizontalcalendar

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalendarData(var data: Date, var isSelected: Boolean = false) {

    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())

    // Getter for the calendar day (day of the month, 2 digits)
    val calendarDay: String
        get() = dayFormat.format(data)

    // Getter for the calendar date (day of the month)
    val calendarDate: String
        get() = dateFormat.format(data)
}
