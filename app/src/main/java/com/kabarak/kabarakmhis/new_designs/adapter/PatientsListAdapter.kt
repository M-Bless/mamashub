package com.kabarak.kabarakmhis.new_designs.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbPatientDetails
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile

class PatientsListAdapter(private var entryList: List<DbPatientDetails>,
                          private val context: Context
) : RecyclerView.Adapter<PatientsListAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvId: TextView = itemView.findViewById(R.id.id)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].id

            FormatterClass().saveSharedPreference(context, "patientId", id)
            FormatterClass().saveSharedPreference(context, "FHIRID", id)

            context.startActivity(Intent(context, PatientProfile::class.java))

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val id = entryList[position].id
        val name = entryList[position].name
        val date = entryList[position].lastUpdated

        val pos = "${position + 1}"

        holder.tvName.paint?.isUnderlineText = true

        holder.tvId.text = pos
        holder.tvName.text = name
        holder.tvDate.text = date

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}