package com.intellisoft.kabarakmhis.new_designs.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.clinical_notes.ClinicalNotesView
import com.intellisoft.kabarakmhis.new_designs.data_class.DBEntry
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.data_class.DbSimpleEncounter
import com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis.MalariaProphylaxisView
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExaminationView
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyView
import com.intellisoft.kabarakmhis.new_designs.preventive_service.PreventiveServiceView
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancyView
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile

class EncounterAdapter(private var entryList: ArrayList<DbObserveValue>,
                       private val context: Context,
                       private val encounterType: String
) : RecyclerView.Adapter<EncounterAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvValue: TextView = itemView.findViewById(R.id.tvValue)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].title

            FormatterClass().saveSharedPreference(context, "observationId", id)

            if (encounterType == DbResourceViews.PRESENT_PREGNANCY.name){
                context.startActivity(Intent(context, PresentPregnancyView::class.java))
            }
            if (encounterType == DbResourceViews.CLINICAL_NOTES.name){
                context.startActivity(Intent(context, ClinicalNotesView::class.java))
            }
            if (encounterType == DbResourceViews.PHYSICAL_EXAMINATION.name){
                context.startActivity(Intent(context, PhysicalExaminationView::class.java))
            }
            if (encounterType == DbResourceViews.PREVIOUS_PREGNANCY.name){
                context.startActivity(Intent(context, PreviousPregnancyView::class.java))
            }
            if (encounterType == DbResourceViews.MALARIA_PROPHYLAXIS.name){
                context.startActivity(Intent(context, MalariaProphylaxisView::class.java))
            }
            if (encounterType == DbResourceViews.PREVENTIVE_SERVICE.name){
                context.startActivity(Intent(context, PreventiveServiceView::class.java))
            }



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.encounter_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val id = entryList[position].title
        val appointmentDate = entryList[position].value

        holder.tvValue.text = appointmentDate


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}