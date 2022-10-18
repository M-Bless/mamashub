package com.intellisoft.kabarakmhis.new_designs.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.DbAppointments
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.appointments.AppointmentAdapter
import kotlinx.android.synthetic.main.activity_upcoming_appointments.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpcomingAppointments : AppCompatActivity() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val mainViewModel: MainActivityViewModel by viewModels()
    private val formatterClass = FormatterClass()

    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var recyclerView: RecyclerView

    private lateinit var tvEdd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_appointments)

        mainViewModel.poll()

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.
            PatientDetailsViewModelFactory(this.application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerView)
        tvEdd = findViewById(R.id.tvEdd)

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

    }

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {


        CoroutineScope(Dispatchers.IO).launch {

            //Get edd

            val eddList = patientDetailsViewModel.getObservationFromCode(formatterClass.getCodes(DbObservationValues.EDD.name))
            if (eddList.isNotEmpty()) {
                val value = eddList[0].value
                val edd = "Expected Delivery Date: $value"
                tvEdd.text = edd
            }

            val appointmentList = ArrayList<DbAppointments>()

            val dateList = ArrayList<String>()
            dateList.addAll(listOf(
                formatterClass.getCodes(DbObservationValues.EDD.name),
                formatterClass.getCodes(DbObservationValues.IPT_VISIT.name),
                formatterClass.getCodes(DbObservationValues.HIV_NR_DATE.name),
                formatterClass.getCodes(DbObservationValues.REFERRAL_PARTNER_HIV_DATE.name),
                formatterClass.getCodes(DbObservationValues.CLINICAL_NOTES_NEXT_VISIT.name),
                formatterClass.getCodes(DbObservationValues.NEXT_VISIT_DATE.name),
                formatterClass.getCodes(DbObservationValues.LLITN_GIVEN_NEXT_DATE.name),
                formatterClass.getCodes(DbObservationValues.IPTP_RESULT_NO.name),
                formatterClass.getCodes(DbObservationValues.REPEAT_SEROLOGY_RESULTS_NO.name),
                formatterClass.getCodes(DbObservationValues.NON_REACTIVE_SEROLOGY_APPOINTMENT.name),
            ))
            dateList.forEach {

                val appointmentObservationList = patientDetailsViewModel.getObservationFromCode(it)
                appointmentObservationList.forEach { observation ->

                    val value = observation.value.trim()
                    if (formatterClass.isDateInFuture(value)){

                        val appointmentDate = FormatterClass().convertDate1(value)

                        var text = ""
                        val id = observation.id
                        val code = observation.code

                        text = when (code) {
                            formatterClass.getCodes(DbObservationValues.EDD.name) -> { "Expected Delivery Date" }
                            formatterClass.getCodes(DbObservationValues.IPT_VISIT.name) -> { "IPT Visit" }
                            formatterClass.getCodes(DbObservationValues.HIV_NR_DATE.name) -> { "HIV NR Date" }
                            formatterClass.getCodes(DbObservationValues.REFERRAL_PARTNER_HIV_DATE.name) -> { "Referral Partner HIV Date" }
                            formatterClass.getCodes(DbObservationValues.CLINICAL_NOTES_NEXT_VISIT.name) -> { "Clinical Next Visit" }
                            formatterClass.getCodes(DbObservationValues.NEXT_VISIT_DATE.name) -> { "Next Visit Date" }
                            formatterClass.getCodes(DbObservationValues.LLITN_GIVEN_NEXT_DATE.name) -> { "LLITN Given Next Date" }
                            formatterClass.getCodes(DbObservationValues.IPTP_RESULT_NO.name) -> { "IPTP Result No" }
                            formatterClass.getCodes(DbObservationValues.REPEAT_SEROLOGY_RESULTS_NO.name) -> { "Repeat Serology Results No" }
                            formatterClass.getCodes(DbObservationValues.NON_REACTIVE_SEROLOGY_APPOINTMENT.name) -> { "Non Reactive Serology Appointment" }
                            else -> { "No Appointment" }
                        }

                        val dbAppointments = DbAppointments(id, text, appointmentDate)
                        appointmentList.add(dbAppointments)

                    }

                }
            }

            val appointment = "${appointmentList.size} upcoming appointments"
            tvAppointment.text = appointment

            appointmentList.sortBy { it.date }

            CoroutineScope(Dispatchers.Main).launch {

                if (appointmentList.isNotEmpty()){
                    no_record.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }else{
                    no_record.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                val configurationListingAdapter = AppointmentAdapter(
                    appointmentList,this@UpcomingAppointments)
                recyclerView.adapter = configurationListingAdapter

            }


        }



    }
    
}