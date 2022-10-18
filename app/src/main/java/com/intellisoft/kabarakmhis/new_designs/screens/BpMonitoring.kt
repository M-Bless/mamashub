package com.intellisoft.kabarakmhis.new_designs.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.physical_examination.tab_layout.SmartWatchReadingAdapter
import kotlinx.android.synthetic.main.fragment_smart_reading.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BpMonitoring : AppCompatActivity() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val mainViewModel: MainActivityViewModel by viewModels()
    private val formatterClass = FormatterClass()

    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var recyclerView: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bp_monitoring)

        mainViewModel.poll()

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.
            PatientDetailsViewModelFactory(this.application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerView);
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

        CoroutineScope(Dispatchers.IO).launch {

            val smartWatchReadingList = patientDetailsViewModel.getObservationFromEncounter(
                DbObservationValues.CLIENT_WEARABLE_RECORDING.name)
            if (smartWatchReadingList.isNotEmpty()){

                //Get Observations

                val id = smartWatchReadingList[0].id

                val observationList = patientDetailsViewModel.getObservationsFromEncounter(id)

                val dbWatchDataList = ArrayList<DbWatchData>()

                observationList.groupBy { it.issued }.forEach { (issued, observationItems) ->

                    //Get the group date and convert to human readable date
                    val issuedDate = issued.toString()

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





                CoroutineScope(Dispatchers.Main).launch {

                    if (observationList.isNotEmpty()){
                        no_record.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }else{
                        no_record.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                    val configurationListingAdapter = SmartWatchReadingAdapter(
                        dbWatchDataList,this@BpMonitoring)
                    recyclerView.adapter = configurationListingAdapter

                }

                Log.e("dbSmartWatchReadingsList", dbWatchDataList.toString())


            }

        }




    }

}