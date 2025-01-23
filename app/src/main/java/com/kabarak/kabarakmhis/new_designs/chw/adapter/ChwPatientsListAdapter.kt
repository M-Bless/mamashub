package com.kabarak.kabarakmhis.new_designs.chw.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbChwPatientData
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.chw.ChwClientDetails

class ChwPatientsListAdapter(private var entryList: List<DbChwPatientData>,
                             private val context: Context
) : RecyclerView.Adapter<ChwPatientsListAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvId: TextView = itemView.findViewById(R.id.id)
        val tvDob: TextView = itemView.findViewById(R.id.tvDob)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].id

            FormatterClass().saveSharedPreference(context, "patientId", id)
            FormatterClass().saveSharedPreference(context, "FHIRID", id)

            context.startActivity(Intent(context, ChwClientDetails::class.java))

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.chw_client_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        entryList[position].id
        val name = entryList[position].name
        val dob = entryList[position].dob
        val date = entryList[position].referralDate



        val pos = "${position + 1}"

        holder.tvId.text = pos
        holder.tvName.text = name
        holder.tvDob.text = dob
        holder.tvDate.text = date

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}