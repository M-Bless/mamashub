package com.kabarak.kabarakmhis.pnc.eye_problems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.EyeProblems

class EyeProblemAdapter(
    private val problems: MutableList<EyeProblems>,
    private val onProblemClick: (String) -> Unit // Lambda function to handle problem click
) : RecyclerView.Adapter<EyeProblemAdapter.EyeProblemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EyeProblemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_eyeproblem, parent, false)
        return EyeProblemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EyeProblemViewHolder, position: Int) {
        val problem = problems[position]
        holder.bind(problem)

        // Set click listener to pass the problem's ID
        holder.itemView.setOnClickListener {
            onProblemClick(problem.id) // Pass the problem's ID to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return problems.size
    }

    class EyeProblemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val visitTextView: TextView = itemView.findViewById(R.id.tvVisitType)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvVisitDate)

        fun bind(problem: EyeProblems) { // Corrected to EyeProblems
            visitTextView.text = problem.VisitType
            dateTextView.text = problem.VisitDate
        }
    }
}
