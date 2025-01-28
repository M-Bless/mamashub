package com.kabarak.kabarakmhis.new_designs.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.clinical_notes.ClinicalNotesView
import com.kabarak.kabarakmhis.new_designs.data_class.DbFhirEncounter
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.ifas.IfasView2
import com.kabarak.kabarakmhis.new_designs.malaria_propylaxis.MalariaProphylaxisView
import com.kabarak.kabarakmhis.new_designs.physical_examination.PhysicalExaminationView
import com.kabarak.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyView
import com.kabarak.kabarakmhis.new_designs.tetanus_diptheria.PreventiveServiceView
import com.kabarak.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancyView

class FhirEncounterAdapter(private var entryList: ArrayList<DbFhirEncounter>,
                           private val context: Context,
                           private val encounterType: String
) : RecyclerView.Adapter<FhirEncounterAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvValue: TextView = itemView.findViewById(R.id.tvValue)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].id

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
            if (encounterType == DbResourceViews.TETENUS_DIPTHERIA.name){
                context.startActivity(Intent(context, PreventiveServiceView::class.java))
            }
            if (encounterType == DbResourceViews.IFAS.name){
                context.startActivity(Intent(context, IfasView2::class.java))
            }
            FormatterClass().saveSharedPreference(context, encounterType, id)
            FormatterClass().saveSharedPreference(context, "savedEncounter", id)



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


        entryList[position].encounterName
        val appointmentDate = entryList[position].encounterName

        holder.tvValue.text = appointmentDate


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}