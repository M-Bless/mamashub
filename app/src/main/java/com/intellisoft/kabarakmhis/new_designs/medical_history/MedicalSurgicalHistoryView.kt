package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.ObservationAdapter
import com.intellisoft.kabarakmhis.new_designs.adapter.ViewDetailsAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.no_record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicalSurgicalHistoryView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_surgical_history_view)
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        btnAddHistory.setOnClickListener {
            val intent = Intent(this, MedicalHistory::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycler_view);
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
            getObservationDetails()

        }

    }

    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@MedicalSurgicalHistoryView,
            DbResourceViews.MEDICAL_HISTORY.name)
        if (encounterId != null) {

            val observationList = retrofitCallsFhir.getEncounterDetails(this@MedicalSurgicalHistoryView,
                encounterId, DbResourceViews.MEDICAL_HISTORY.name)

            if (observationList.isNotEmpty()){



                var sourceString = ""

                for(item in observationList){

                    val code = item.title
                    val display = item.value

                    sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"

                }

                CoroutineScope(Dispatchers.Main).launch {

                    if (!observationList.isNullOrEmpty()){
                        no_record.visibility = View.GONE
                    }else{
                        no_record.visibility = View.VISIBLE
                    }

                    tvValue.text = sourceString
                    btnAddHistory.text = "Edit Medical History"
                }

            }


        }


    }
}