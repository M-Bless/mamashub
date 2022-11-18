package com.kabarak.kabarakmhis.new_designs.pmtct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbPMTCTRegimen

class RegimenPmtctAdapter(private var entryList: List<DbPMTCTRegimen>?,
                          private val context: Context
) : RecyclerView.Adapter<RegimenPmtctAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvRegimen: TextView = itemView.findViewById(R.id.tvRegimen)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val vtDosage: TextView = itemView.findViewById(R.id.vtDosage)
        val tvFrequency: TextView = itemView.findViewById(R.id.tvFrequency)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.regimen_pmtct_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val regimen = entryList?.get(position)?.regimen
        val amount = entryList?.get(position)?.amount
        val dosage = entryList?.get(position)?.dosage
        val frequency = entryList?.get(position)?.frequency

        holder.tvRegimen.text = regimen
        holder.tvAmount.text = amount.toString()
        holder.vtDosage.text = dosage.toString()
        holder.tvFrequency.text = frequency.toString()




    }

    override fun getItemCount(): Int {
        return entryList!!.size
    }

}