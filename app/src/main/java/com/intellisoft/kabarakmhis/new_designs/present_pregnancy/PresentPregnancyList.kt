package com.intellisoft.kabarakmhis.new_designs.present_pregnancy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.EncounterAdapter
import com.intellisoft.kabarakmhis.new_designs.clinical_notes.ClinicalNotesAdd
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_present_pregnancy_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PresentPregnancyList : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present_pregnancy_list)

        title = "Present Pregnancy List"

        fab.setOnClickListener {

            startActivity(Intent(this, PresentPregnancyAdd::class.java))

        }

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

            val encounterId = formatter.retrieveSharedPreference(this@PresentPregnancyList, DbResourceViews.PRESENT_PREGNANCY.name)
            if (encounterId != null) {
                val observationList = retrofitCallsFhir.getEncounterDetails(this@PresentPregnancyList, encounterId, DbResourceViews.PRESENT_PREGNANCY.name)
                CoroutineScope(Dispatchers.Main).launch {

                    if (!observationList.isNullOrEmpty()){
                        no_record.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }else{
                        no_record.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                    val configurationListingAdapter = EncounterAdapter(
                        observationList,this@PresentPregnancyList,DbResourceViews.PRESENT_PREGNANCY.name)
                    recyclerView.adapter = configurationListingAdapter
                }
            }




        }

    }
}