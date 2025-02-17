package com.kabarak.kabarakmhis.pnc.other_problems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.OtherProblems

class OtherProblemsAdapter(
    private val problems: MutableList<OtherProblems>,
    private val onProblemClick: (String) -> Unit // Lambda function to handle child click
) : RecyclerView.Adapter<OtherProblemsAdapter.ChildViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_other_problems, parent, false) // view is a LinearLayout
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val problem = problems[position]
        holder.bind(problem)

        // Set click listener to pass the child's responseId (id)
        holder.itemView.setOnClickListener {
            onProblemClick(problem.id) // Pass the child's ID (responseId) to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return problems.size
    }

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sleepingProblemsTextView: TextView = itemView.findViewById(R.id.tvSleepingProblems)
        private val irritabilityTextView: TextView = itemView.findViewById(R.id.tvIrritability)
        private val othersSpecifyTextView: TextView = itemView.findViewById(R.id.tvOthersSpecify)

        fun bind(problem: OtherProblems) {
            sleepingProblemsTextView.text = problem.sleepingProblems
            irritabilityTextView.text = problem.irritability
            othersSpecifyTextView.text = problem.othersSpecify
        }
    }
}
