package com.intellisoft.kabarakmhis.new_designs.clinical_notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intellisoft.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*

class ClinicalNotesList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinical_notes_list)

        fab.setOnClickListener {

            startActivity(Intent(this, ClinicalNotesAdd::class.java))

        }

    }
}