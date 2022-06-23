package com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.EncounterAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExamination
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MalariaProphylaxisList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private val formatter = FormatterClass()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malaria_prophylaxis_list)

        title = "Malaria Prophylaxis List"

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
            startActivity(Intent(this, MalariaProphylaxis::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(this@MalariaProphylaxisList, DbResourceViews.MALARIA_PROPHYLAXIS.name)
            if (encounterId != null) {
                val observationList = retrofitCallsFhir.getEncounterDetails(this@MalariaProphylaxisList, encounterId, DbResourceViews.MALARIA_PROPHYLAXIS.name)
                CoroutineScope(Dispatchers.Main).launch {

                    if (!observationList.isNullOrEmpty()){
                        no_record.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }else{
                        no_record.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                    Log.e("----- ", observationList.toString())

                    val configurationListingAdapter = EncounterAdapter(
                        observationList,this@MalariaProphylaxisList, DbResourceViews.MALARIA_PROPHYLAXIS.name)
                    recyclerView.adapter = configurationListingAdapter
                }
            }




        }




    }
}