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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_view.*
import kotlinx.android.synthetic.main.activity_clinical_notes_view.tvValue
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*


class ClinicalNotesView : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_view)

        title = "Clinical Notes Details"


    }

    override fun onStart() {
        super.onStart()

        getObservationDetails()
    }

    private fun getObservationDetails() {

        val observationId = formatter.retrieveSharedPreference(this,"observationId")
        if (observationId != null) {
            val observationList = retrofitCallsFhir.getObservationDetails(this, observationId)

            var sourceString = ""

            for(item in observationList){

                val code = item.code
                val display = item.display

//                sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"
                sourceString = "$sourceString<br><b>${code.toUpperCase()}</b>: $display"
            }

//            tvValue.text = sourceString
            tvValue.text = Html.fromHtml(sourceString)

        }else{
            Toast.makeText(this, "This resource has an issue", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ClinicalNotesView::class.java))
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