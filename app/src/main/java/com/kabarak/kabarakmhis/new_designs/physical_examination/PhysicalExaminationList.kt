package com.kabarak.kabarakmhis.new_designs.physical_examination

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.adapter.FhirEncounterAdapter
import com.kabarak.kabarakmhis.new_designs.data_class.DbFhirEncounter
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_present_pregnancy_list.*
import kotlinx.android.synthetic.main.activity_present_pregnancy_list.no_record
import kotlinx.android.synthetic.main.activity_previous_pregnancy_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhysicalExaminationList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_examination_list)

        title = "Physical Examination List"


        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        fab.setOnClickListener {
            startActivity(Intent(this, PhysicalExamination::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {

            formatter.deleteSharedPreference(this@PhysicalExaminationList, DbResourceViews.PHYSICAL_EXAMINATION.name)

            val observationList = patientDetailsViewModel.getObservationFromEncounter(DbResourceViews.PHYSICAL_EXAMINATION.name)

            CoroutineScope(Dispatchers.Main).launch {

                if (observationList.isNotEmpty()){
                    no_record.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }else{
                    no_record.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                val encounterList = ArrayList<DbFhirEncounter>()

                observationList.sortedByDescending { it.effective }

                observationList.forEachIndexed { index, encounterItem ->

                    val pos = index + 1
                    val visitNo = formatter.getNumber(pos)

                    val id = encounterItem.id
                    val encounterName = "$visitNo Visit"
                    val encounterType = encounterItem.code

                    val dbFhirEncounter = DbFhirEncounter(
                        id = id,
                        encounterName = encounterName,
                        encounterType = encounterType
                    )
                    encounterList.add(dbFhirEncounter)

                }

                val configurationListingAdapter = FhirEncounterAdapter(
                    encounterList,this@PhysicalExaminationList, DbResourceViews.PHYSICAL_EXAMINATION.name)
                recyclerView.adapter = configurationListingAdapter
            }






        }




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}