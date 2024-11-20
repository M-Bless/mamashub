package com.kabarak.kabarakmhis.pnc.cancer_screening

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.CancerScreening

class CancerScreeningAdapter(
    private val screenings: List<CancerScreening>,
    private val onChildClick: (String) -> Unit
) : RecyclerView.Adapter<CancerScreeningAdapter.ScreeningViewHolder>() {

    // Inflate item layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreeningViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancerscreening, parent, false) // Replace with your actual item layout
        return ScreeningViewHolder(itemView)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: ScreeningViewHolder, position: Int) {
        val screening = screenings[position]
        holder.bind(screening)
        holder.itemView.setOnClickListener {
            onChildClick(screening.responseId)
        }
    }

    // Return the size of the list
    override fun getItemCount(): Int = screenings.size

    // ViewHolder class for holding the view references
    class ScreeningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeTextView: TextView = itemView.findViewById(R.id.tvType)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(screening: CancerScreening) {
            typeTextView.text = screening.type
            dateTextView.text = screening.date
        }
    }
}
