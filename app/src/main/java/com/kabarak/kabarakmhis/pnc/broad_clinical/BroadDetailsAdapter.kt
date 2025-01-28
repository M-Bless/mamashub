package com.kabarak.kabarakmhis.pnc.broad_clinical

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.Detail

class BroadDetailsAdapter(
    private val detailsList: List<Detail>
) : RecyclerView.Adapter<BroadDetailsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val detailQuestion: TextView = itemView.findViewById(R.id.detailQuestionTextView)
        val detailAnswer: TextView = itemView.findViewById(R.id.detailAnswerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_other_problems_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detail = detailsList[position]
        holder.detailQuestion.text = detail.detailQuestion
        holder.detailAnswer.text = detail.detailAnswer
    }

    override fun getItemCount() = detailsList.size
}