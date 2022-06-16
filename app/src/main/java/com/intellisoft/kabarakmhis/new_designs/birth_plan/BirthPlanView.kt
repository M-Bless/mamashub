package com.intellisoft.kabarakmhis.new_designs.birth_plan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.clinical_notes.ClinicalNotesView
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_birth_plan_view.*
import kotlinx.android.synthetic.main.activity_birth_plan_view.tvValue
import kotlinx.android.synthetic.main.activity_clinical_notes_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BirthPlanView : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birth_plan_view)

        btnAddBirthPlan.setOnClickListener {

            startActivity(Intent(this, BirthPlan::class.java))

        }
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            getObservationDetails()

        }

    }

    private fun getObservationDetails() {

        val encounterId = formatter.retrieveSharedPreference(this@BirthPlanView,
            DbResourceViews.BIRTH_PLAN.name)
        if (encounterId != null) {

            val observationList = retrofitCallsFhir.getEncounterDetails(this@BirthPlanView,
                encounterId, DbResourceViews.BIRTH_PLAN.name)

            if (observationList.isNotEmpty()){
                var sourceString = ""

                for(item in observationList){

                    val code = item.title
                    val display = item.value

//                    sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"
                    sourceString = "$sourceString<br><b>${code.toUpperCase()}</b>: $display"

                }

                CoroutineScope(Dispatchers.Main).launch {
//                    tvValue.text = sourceString
                    tvValue.text = Html.fromHtml(sourceString)
                    btnAddBirthPlan.text = "Edit Birth Plan"}


            }


        }


    }
}