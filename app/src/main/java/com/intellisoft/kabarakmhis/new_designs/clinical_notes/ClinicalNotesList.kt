package com.intellisoft.kabarakmhis.new_designs.clinical_notes

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
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.FhirEncounterAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbFhirEncounter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*
import kotlinx.android.synthetic.main.activity_clinical_notes_list.no_record
import kotlinx.android.synthetic.main.activity_clinical_notes_list.recyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClinicalNotesList : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val formatter = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_list)

        title = "Clinical Notes"

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        fab.setOnClickListener {

            startActivity(Intent(this, ClinicalNotesAdd::class.java))

        }
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

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

            val observationList = patientDetailsViewModel.getObservationFromEncounter(DbResourceViews.CLINICAL_NOTES.name)


            CoroutineScope(Dispatchers.Main).launch {

                if (observationList.isNotEmpty()){
                    no_record.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }else{
                    no_record.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                val encounterList = ArrayList<DbFhirEncounter>()
                observationList.forEachIndexed { index, encounterItem ->

                    val pos = index + 1

                    if (encounterItem.effective != ""){
                        val lastUpdate = formatterClass.convertFhirDate(encounterItem.effective)
                        if (lastUpdate != null){
                            val id = encounterItem.id
                            val encounterType = encounterItem.code

                            val dbFhirEncounter = DbFhirEncounter(
                                id = id,
                                encounterName = lastUpdate,
                                encounterType = encounterType
                            )
                            encounterList.add(dbFhirEncounter)
                        }
                    }




                }

                val configurationListingAdapter = FhirEncounterAdapter(
                    encounterList,this@ClinicalNotesList, DbResourceViews.CLINICAL_NOTES.name)
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