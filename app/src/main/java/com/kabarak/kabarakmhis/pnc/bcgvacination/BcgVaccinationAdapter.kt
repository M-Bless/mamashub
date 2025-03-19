package com.kabarak.kabarakmhis.pnc.bcg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.BcgVaccination

class BcgVaccinationAdapter(
    private val vaccinations: List<BcgVaccination>,
    private val onVaccinationClick: (String) -> Unit
) : RecyclerView.Adapter<BcgVaccinationAdapter.VaccinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bcg_vaccination, parent, false)
        return VaccinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccinationViewHolder, position: Int) {
        val vaccination = vaccinations[position]
        holder.bind(vaccination)
        holder.itemView.setOnClickListener {
            onVaccinationClick(vaccination.id)
        }
    }

    override fun getItemCount(): Int = vaccinations.size

    class VaccinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusTextView: TextView = itemView.findViewById(R.id.tvStatus)
        private val dateGivenTextView: TextView = itemView.findViewById(R.id.tvDateGiven)
        private val dateNextVisitTextView: TextView = itemView.findViewById(R.id.tvDateNextVisit)

        fun bind(vaccination: BcgVaccination) {
            statusTextView.text = vaccination.status
            dateGivenTextView.text = vaccination.dateGiven ?: "No date given"
            dateNextVisitTextView.text = vaccination.dateOfNextVisit ?: "No next visit date"
        }
    }
}
