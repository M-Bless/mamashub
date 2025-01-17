package com.kabarak.kabarakmhis.pnc.measlesimmunization


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.MeaslesImmunization

class MeaslesImmunizationAdapter(
    private val immunizations: List<MeaslesImmunization>,
    private val onImmunizationClick: (String) -> Unit
) : RecyclerView.Adapter<MeaslesImmunizationAdapter.ImmunizationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunizationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_measles_immunization, parent, false)
        return ImmunizationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImmunizationViewHolder, position: Int) {
        val immunization = immunizations[position]
        holder.bind(immunization)
        holder.itemView.setOnClickListener {
            onImmunizationClick(immunization.id)
        }
    }

    override fun getItemCount(): Int = immunizations.size

    class ImmunizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val visitTextView: TextView = itemView.findViewById(R.id.tvVisit)
        private val doseTextView: TextView = itemView.findViewById(R.id.tvDose)
        private val batchNumberTextView: TextView = itemView.findViewById(R.id.tvBatchNumber)
        private val lotNumberTextView: TextView = itemView.findViewById(R.id.tvLotNumber)
        private val manufacturerTextView: TextView = itemView.findViewById(R.id.tvManufacturer)
        private val dateOfExpiryTextView: TextView = itemView.findViewById(R.id.tvDateOfExpiry)

        fun bind(immunization: MeaslesImmunization) {
            visitTextView.text = immunization.visit
            doseTextView.text = immunization.dose ?: "No dose info"
            batchNumberTextView.text = immunization.batchNumber ?: "No batch number"
            lotNumberTextView.text = immunization.lotNumber ?: "No lot number"
            manufacturerTextView.text = immunization.manufacturer ?: "No manufacturer info"
            dateOfExpiryTextView.text = immunization.dateOfExpiry ?: "No expiry date"
        }
    }
}
