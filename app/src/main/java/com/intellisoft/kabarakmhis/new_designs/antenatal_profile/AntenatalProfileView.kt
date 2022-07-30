package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
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

import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.*
import kotlinx.android.synthetic.main.activity_antenatal_profile_view.no_record
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntenatalProfileView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()
    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenatal_profile_view)

        title = "Antenatal Profile Details"

        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


        recyclerView = findViewById(R.id.patient_list);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        btnAddAntenatal.setOnClickListener {

            startActivity(Intent(this, AntenatalProfile::class.java))

        }
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            getObservationDetails()
        }

    }

    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@AntenatalProfileView,
            DbResourceViews.ANTENATAL_PROFILE.name)
        if (encounterId != null) {

            val observationList =
                patientDetailsViewModel.getObservationsFromEncounter(encounterId)

            CoroutineScope(Dispatchers.Main).launch {
                if (!observationList.isNullOrEmpty()){
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


                    Log.e("----code ", code.toString())
                    Log.e("----display ", display.toString())



//                    sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"


                    sourceString = "$sourceString<br><b>${code.toUpperCase()}</b>: $display"

                }

                CoroutineScope(Dispatchers.Main).launch {
//                    tvValue.text = sourceString


                    tvValue.text = Html.fromHtml(sourceString)
                    btnAddAntenatal.text = "Edit Antenatal Profile"


                }


            }


        }


    }

}