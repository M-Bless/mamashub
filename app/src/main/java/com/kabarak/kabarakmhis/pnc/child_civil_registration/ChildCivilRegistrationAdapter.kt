package com.kabarak.kabarakmhis.pnc.child_civil_registration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.CivilRegistration

class ChildCivilRegistrationAdapter(
    private val children: MutableList<CivilRegistration>,
    private val onChildClick: (String) -> Unit // Lambda function to handle child click
) : RecyclerView.Adapter<ChildCivilRegistrationAdapter.ChildCivilRegistrationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildCivilRegistrationViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_child_civil_registration, parent, false) // view is a LinearLayout
        return ChildCivilRegistrationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildCivilRegistrationViewHolder, position: Int) {
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

    class ChildCivilRegistrationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvChildName)
       // private val birthDateTextView: TextView = itemView.findViewById(R.id.tvbirthDate)
        private val sexOfChildTextView: TextView = itemView.findViewById(R.id.tvsexOfChild)

        fun bind(child: CivilRegistration) {
            nameTextView.text = child.name
           // birthDateTextView.text = child.birthDate
            sexOfChildTextView.text = child.sexOfChild
        }
    }
}
