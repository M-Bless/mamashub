package com.kabarak.kabarakmhis.pnc.milestone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.Milestone

class MilestoneAdapter(
    private val milestones: List<Milestone>,
    private val onMilestoneClick: (String) -> Unit // Lambda function to handle milestone click
) : RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_milestone, parent, false) // view is a LinearLayout
        return MilestoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        val milestone = milestones[position]
        holder.bind(milestone)

        // Set click listener to pass the milestone's responseId (id)
        holder.itemView.setOnClickListener {
            onMilestoneClick(milestone.id) // Pass the milestone's ID (responseId) to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return milestones.size
    }

    class MilestoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val visitTextView: TextView = itemView.findViewById(R.id.tvMilestonevisit)
        private val ageTextView: TextView = itemView.findViewById(R.id.tvMilestoneage)
        private val timeTextView: TextView = itemView.findViewById(R.id.tvMilestonetime)

        fun bind(milestone: Milestone) {
            visitTextView.text = milestone.visit
            ageTextView.text = milestone.age
            timeTextView.text = milestone.time
        }
    }
}