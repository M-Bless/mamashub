package com.kabarak.kabarakmhis.pnc.congenitalabnormalities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.CongenitalAbnormality
class CongenitalAbnormalitiesAdapter(
    private val abnormalities: List<CongenitalAbnormality>,
    private val onAbnormalityClick: (String) -> Unit
) : RecyclerView.Adapter<CongenitalAbnormalitiesAdapter.AbnormalityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbnormalityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_congenital_abnormality, parent, false)
        return AbnormalityViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbnormalityViewHolder, position: Int) {
        val abnormality = abnormalities[position]
        holder.bind(abnormality)
        holder.itemView.setOnClickListener {
            onAbnormalityClick(abnormality.id)
        }
    }

    override fun getItemCount(): Int = abnormalities.size

    class AbnormalityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        private val remarksTextView: TextView = itemView.findViewById(R.id.tvRemarks)

        fun bind(abnormality: CongenitalAbnormality) {
            descriptionTextView.text = abnormality.description
            remarksTextView.text = abnormality.remarks ?: "No remarks available"
        }
    }
}
