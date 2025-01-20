package com.kabarak.kabarakmhis.pnc.familyplannig

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.QuestionnaireDetails
import kotlinx.android.synthetic.main.item_civil_registration_details.view.answerTextView
import kotlinx.android.synthetic.main.item_civil_registration_details.view.questionTextView

class  FamilyPlanningDetailsAdapter(
    private val civilRegistrationDetails: List<QuestionnaireDetails>,
    param: (Any) -> Unit
) : RecyclerView.Adapter<FamilyPlanningDetailsAdapter.CivilRegistrationDetailsViewHolder>() {

    class CivilRegistrationDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val detailQuestion: TextView = itemView.questionTextView
        val detailAnswer: TextView = itemView.answerTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CivilRegistrationDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_civil_registration_details, parent, false)
        return CivilRegistrationDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CivilRegistrationDetailsViewHolder, position: Int) {
        val civilRegistrationDetails = civilRegistrationDetails[position]
        holder.detailQuestion.text = civilRegistrationDetails.detailQuestion
        holder.detailAnswer.text = civilRegistrationDetails.detailAnswer

    }

    override fun getItemCount(): Int {
        return civilRegistrationDetails.size
    }

}