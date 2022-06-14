package com.intellisoft.kabarakmhis.new_designs.clinical_notes

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_clinical_notes_add.*
import java.util.*
import kotlin.collections.HashSet

class ClinicalNotesAdd : AppCompatActivity() {

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private val retrofitCallsFhir = RetrofitCallsFhir()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_add)

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        btnSave.setOnClickListener {

            val clinicalNotes = etClinicalNotes.text.toString()
            val appointmentDate = tvNextVisit.text.toString()

            if (!TextUtils.isEmpty(clinicalNotes)){

                val dbObservationValue = createObservation(clinicalNotes, appointmentDate)
                retrofitCallsFhir.createFhirEncounter(this, dbObservationValue, DbResourceViews.CLINICAL_NOTES.name)

            }else
                etClinicalNotes.error = "Field cannot be empty"


        }

        tvNextVisit.setOnClickListener { createDialog(999) }
    }

    private fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog( this,
                    myDateDobListener, year, month, day)
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
            tvNextVisit.text = date

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

    private fun createObservation(
        clinicalNotes: String,
        nextAppointment: String
    ): DbObservationValue {

        val dbObservationDataList = HashSet<DbObservationData>()

        val clinicalList = HashSet<String>()
        clinicalList.add(clinicalNotes)

        val appointmentList = HashSet<String>()
        appointmentList.add(nextAppointment)

        val dbClinicalData = DbObservationData("Clinical Notes", clinicalList)
        val dbAppointmentData = DbObservationData("Next Appointment", appointmentList)

        dbObservationDataList.addAll(listOf(dbClinicalData, dbAppointmentData))

        return DbObservationValue(dbObservationDataList)

    }

}