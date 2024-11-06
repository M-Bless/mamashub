package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.ChildPnc

class ChildPncAdapter(
    private val patients: List<ChildPnc>,
    private val onChildPncClick: (String) -> Unit
) : RecyclerView.Adapter<ChildPncAdapter.ChildPncViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildPncViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_child_pnc, parent, false)
        return ChildPncViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildPncViewHolder, position: Int) {
        val childPnc = patients[position]
        holder.bind(childPnc)

        // Handle click events for each item
        holder.itemView.setOnClickListener {
            onChildPncClick(childPnc.id) // Pass visitDate or other unique identifier if needed
        }
    }

    override fun getItemCount(): Int = patients.size

    class ChildPncViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val visitTextView: TextView = itemView.findViewById(R.id.tvVisitTime)
        private val generalConditionTextView: TextView = itemView.findViewById(R.id.tvGeneralCondition)


        fun bind(childPnc: ChildPnc) {
            visitTextView.text = "Visit: ${childPnc.visitTime}"
            generalConditionTextView.text = "General Condition: ${childPnc.generalCondition ?: "N/A"}"

        }
    }
}
