package com.kabarak.kabarakmhis.pnc.poliovacination


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.PolioVaccination

class PolioVaccinationAdapter(
    private val vaccinations: List<PolioVaccination>,
    private val onVaccinationClick: (String) -> Unit
) : RecyclerView.Adapter<PolioVaccinationAdapter.VaccinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_polio_vaccination, parent, false)
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
        private val doseTextView: TextView = itemView.findViewById(R.id.tvDose)
        private val dateGivenTextView: TextView = itemView.findViewById(R.id.tvDateGiven)
        private val dateNextVisitTextView: TextView = itemView.findViewById(R.id.tvDateNextVisit)

        fun bind(vaccination: PolioVaccination) {
            statusTextView.text = vaccination.status
            doseTextView.text = vaccination.dose ?: "No dose info"
            dateGivenTextView.text = vaccination.dateGiven ?: "No date given"
            dateNextVisitTextView.text = vaccination.dateOfNextVisit ?: "No next visit date"
        }
    }
}
