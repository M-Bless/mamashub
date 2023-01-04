package com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.*
import com.vivekkaushik.datepicker.DatePickerTimeline
import com.vivekkaushik.datepicker.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_bp_monitoring.*
import kotlinx.android.synthetic.main.fragment_smart_reading.*
import kotlinx.android.synthetic.main.fragment_smart_reading.no_record
import kotlinx.android.synthetic.main.fragment_smart_reading.tvDate
import kotlinx.android.synthetic.main.fragment_smart_reading.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class FragmentWatchReadings : Fragment() {

    private lateinit var rootView: View
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val mainViewModel: MainActivityViewModel by viewModels()
    private val formatterClass = FormatterClass()

    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var recyclerView: RecyclerView

    private lateinit var datePickerTimeline: DatePickerTimeline

    //Get current year
    private val currentYear = LocalDate.now().year
    //Get current month
    private val currentMonth = LocalDate.now().monthValue - 2
    //Get current day
    private val currentDay = LocalDate.now().dayOfMonth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_smart_reading, container, false)

        datePickerTimeline = rootView.findViewById(R.id.datePickerTimeline)


        mainViewModel.poll()

        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.
            PatientDetailsViewModelFactory(requireActivity().application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = rootView.findViewById(R.id.recyclerView);
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        initDateSelector(currentYear, currentMonth, currentDay)


        return rootView
    }

    private fun initDateSelector(year: Int, month: Int, day: Int) {

        // Set a Start date as today's date

        rootView.datePickerTimeline.setInitialDate(year, month, day)
        // Set a date Selected Listener
        rootView.datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {

                //Pass the date to the function
                getWatchReadings(year, month, day)

            }

            override fun onDisabledDateSelected(
                year: Int,
                month: Int,
                day: Int,
                dayOfWeek: Int,
                isDisabled: Boolean
            ) {
                // Do Something
            }
        })

    }

    override fun onStart() {
        super.onStart()

        getWatchReadings(currentYear, currentMonth, currentDay)

    }

    private fun getWatchReadings(year: Int, month: Int, day: Int) {

        val newDate = "$year-${month + 1}-$day"

        CoroutineScope(Dispatchers.IO).launch {

            val smartWatchReadingList = patientDetailsViewModel.getObservationFromEncounter(
                DbObservationValues.CLIENT_WEARABLE_RECORDING.name)
            if (smartWatchReadingList.isNotEmpty()){

                val datesAvailable = ArrayList<String>()

                //Get Observations
                val id = smartWatchReadingList[0].id
                val observationList = patientDetailsViewModel.getObservationsFromEncounter(id)
                val dbWatchDataList = ArrayList<DbWatchData>()
                observationList.groupBy { it.issued }.forEach { (issued, observationItems) ->
                    //Get the group date and convert to human readable date
                    val issuedDate = issued.toString()
                    datesAvailable.add(issuedDate)
                    val dbWatchDataValuesList = ArrayList<DbWatchDataValues>()
                    //Get the observations for the group date
                    observationItems.forEach {
                        val dbSmartWatchReading = DbWatchDataValues(
                            time = it.issuedTime.toString(),
                            text = it.text,
                            value = it.value)
                        dbWatchDataValuesList.add(dbSmartWatchReading)
                    }
                    dbWatchDataValuesList.sortBy { it.time }
                    val dbWatchData = DbWatchData(date = issuedDate, readings = dbWatchDataValuesList)
                    dbWatchDataList.add(dbWatchData)
                }
                //Sort the list by date
                dbWatchDataList.sortBy { it.date }
                val dbWatchTimeDataList = ArrayList<DbWatchTimeData>()
                //Check if the date is in the list and filter the list
                val filteredList = dbWatchDataList.filter { it.date == newDate }
                filteredList.forEach {
                    it.readings.groupBy { it.time }.forEach { (time, readings) ->

                        var systolic = ""
                        var diastolic = ""
                        var pulse = ""

                        readings.forEach { watch ->

                            val text = watch.text
                            val value = watch.value

                            if (text.contains("Systolic")){
                                systolic = value
                            }
                            if (text.contains("Diastolic")){
                                diastolic = value
                            }
                            if (text.contains("Heart")){
                                pulse = value
                            }

                        }

                        val readingsData = DbWatchRecord(systolic = systolic, diastolic = diastolic, pulse = pulse)

                        val dbWatchTimeData = DbWatchTimeData(time = time, readings = readingsData)
                        dbWatchTimeDataList.add(dbWatchTimeData)

                    }
                }

                datesAvailable.sortBy { it }

                CoroutineScope(Dispatchers.Main).launch {

                    //Convert the list to string
                    val datesAvailableString = datesAvailable.joinToString(separator = " , ")
                    tvDate.text = "Available Recordings: $datesAvailableString"

                    if (filteredList.isNotEmpty()){
                        no_record.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }else{
                        no_record.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                    val configurationListingAdapter = SmartWatchReadingDataAdapter(
                        dbWatchTimeDataList,requireContext())
                    recyclerView.adapter = configurationListingAdapter
                }


            }

        }


    }

}
