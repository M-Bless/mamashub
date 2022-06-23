package com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.new_designs.adapter.ViewDetailsAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancy
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_previous_pregnancy_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MalariaProphylaxisView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malaria_prophylaxis_view)

        title = "Malaria Prophylaxis Details"

        btnPreviousPregnancy.setOnClickListener {
            val intent = Intent(this, MalariaProphylaxis::class.java)
            startActivity(intent)
        }
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

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

            val patientList = kabarakViewModel.getTittlePatientData(DbResourceViews.MALARIA_PROPHYLAXIS.name, this@MalariaProphylaxisView)
            CoroutineScope(Dispatchers.Main).launch {

                val configurationListingAdapter = ViewDetailsAdapter(
                    patientList,this@MalariaProphylaxisView)
                recyclerView.adapter = configurationListingAdapter

            }

//            val observations = ArrayList<DbObserveValue>()
//            val observationList = retrofitCallsFhir.getPatientEncounters(this@PreviousPregnancyView,
//                DbResourceViews.PREVIOUS_PREGNANCY.name)
//            for (keys in observationList){
//
//                val key = keys.key
//                val value = observationList.getValue(key)
//
//                val dbObserveValue = DbObserveValue(key, value.toString())
//                observations.add(dbObserveValue)
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    val configurationListingAdapter = ObservationAdapter(
//                        observations,this@PreviousPregnancyView)
//                    recyclerView.adapter = configurationListingAdapter
//                }
//            }



        }


    }
}