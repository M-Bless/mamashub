package com.intellisoft.kabarakmhis.new_designs.counselling

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_counselling_view.*
import kotlinx.android.synthetic.main.activity_counselling_view.no_record
import kotlinx.android.synthetic.main.activity_counselling_view.tvValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CounsellingView : AppCompatActivity() {
    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counselling_view)

        title = "Counselling"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]



        btnAddCounselling.setOnClickListener {
            startActivity(Intent(this, Counselling::class.java))
        }

        CoroutineScope(Dispatchers.IO).launch { getObservationDetails() }


    }

    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@CounsellingView,
            DbResourceViews.COUNSELLING.name)
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
                    btnAddCounselling.text = "Edit Counselling"}


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