package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.ObservationAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicalSurgicalHistoryView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_surgical_history_view)

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

            val observations = ArrayList<DbObserveValue>()
            val observationList = retrofitCallsFhir.getPatientEncounters(this@MedicalSurgicalHistoryView, DbResourceViews.MEDICAL_HISTORY.name)
            for (keys in observationList){

                val key = keys.key
                val value = observationList.getValue(key)

                val dbObserveValue = DbObserveValue(key, value.toString())
                observations.add(dbObserveValue)
            }

            CoroutineScope(Dispatchers.Main).launch {
                val configurationListingAdapter = ObservationAdapter(
                    observations,this@MedicalSurgicalHistoryView)
                recyclerView.adapter = configurationListingAdapter
            }

        }


    }
}