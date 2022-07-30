package com.intellisoft.kabarakmhis.new_designs.clinical_notes

import android.content.Intent
import android.graphics.Typeface
import android.icu.lang.UProperty.INT_START
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*
import kotlinx.android.synthetic.main.activity_clinical_notes_view.*
import kotlinx.android.synthetic.main.activity_clinical_notes_view.tvValue
import kotlinx.android.synthetic.main.activity_maternal_serology_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.no_record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ClinicalNotesView : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()


    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_view)

        title = "Clinical Notes Details"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


    }

    override fun onStart() {
        super.onStart()

        getObservationDetails()
    }

    private fun getObservationDetails() {

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(this@ClinicalNotesView,
                DbResourceViews.CLINICAL_NOTES.name)

            if (encounterId != null) {

                val observationList =
                    patientDetailsViewModel.getObservationsFromEncounter(encounterId)

                CoroutineScope(Dispatchers.Main).launch {
                    if (observationList.isNotEmpty()){
                        no_record.visibility = View.GONE
                    }else{
                        no_record.visibility = View.VISIBLE
                    }
                }

                if (observationList.isNotEmpty()){
                    var sourceString = ""

                    for(item in observationList){

                        val code = item.text
                        val display = item.value

//                    sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"
                        sourceString = "$sourceString<br><b>${code.toUpperCase()}</b>: $display"

                    }

                    CoroutineScope(Dispatchers.Main).launch {
//                    tvValue.text = sourceString
                        tvValue.text = Html.fromHtml(sourceString)
                    }


                }


            }

//            val observationId = formatter.retrieveSharedPreference(this@PresentPregnancyView,"observationId")
//            if (observationId != null) {
//                val observationList = retrofitCallsFhir.getObservationDetails(this@PresentPregnancyView, observationId)
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    val configurationListingAdapter = ObservationAdapter(
//                        observationList,this@PresentPregnancyView)
//                    recyclerView.adapter = configurationListingAdapter
//                }
//
//            }



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