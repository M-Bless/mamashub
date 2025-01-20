package com.kabarak.kabarakmhis.pnc.familyplannig

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.FamilyPlanning

class FamilyPlanningAdapter(
    private val children: MutableList<FamilyPlanning>,
    private val onChildClick: (String) -> Unit // Lambda function to handle child click
) : RecyclerView.Adapter<FamilyPlanningAdapter.FamilyPlanningViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyPlanningViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_family_planning, parent, false) // view is a LinearLayout
        return FamilyPlanningViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyPlanningViewHolder, position: Int) {
        val child = children[position]
        holder.bind(child)

        // Set click listener to pass the child's responseId (id)
        holder.itemView.setOnClickListener {
            onChildClick(child.id) // Pass the child's ID (responseId) to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return children.size
    }

    class FamilyPlanningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.tvFamilyPlanningDate)
         private val methodTextView: TextView = itemView.findViewById(R.id.tvFamilyPlanningMethod)
        private val weightTextView: TextView = itemView.findViewById(R.id.tvFamilyPlanningWeight)
        private  val bloodpressureTextView: TextView = itemView.findViewById(R.id.tvFamilyPlanningBloodPressure)
        private val remarksTextView: TextView = itemView.findViewById(R.id.tvFamilyPlanningRemarks)

        fun bind(child: FamilyPlanning) {
            dateTextView.text = child.date
             methodTextView.text = child.method
            weightTextView.text = child.weight
            bloodpressureTextView.text=child.bloodPressure
            remarksTextView.text=child.remarks
        }
    }
}
