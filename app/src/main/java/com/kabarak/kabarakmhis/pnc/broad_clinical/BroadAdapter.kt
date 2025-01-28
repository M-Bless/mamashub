package com.kabarak.kabarakmhis.pnc.broad_clinical

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.BroadClinical
import com.kabarak.kabarakmhis.pnc.data_class.Child

class BroadAdapter(
    private val broads: MutableList<BroadClinical>,
    private val onChildClick: (String) -> Unit // Lambda function to handle child click
) : RecyclerView.Adapter<BroadAdapter.BroadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BroadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_broad_clinical, parent, false) // view is a LinearLayout
        return BroadViewHolder(view)
    }

    override fun onBindViewHolder(holder: BroadViewHolder, position: Int) {
        val broad = broads[position]
        holder.bind(broad)

        // Set click listener to pass the child's responseId (id)
        holder.itemView.setOnClickListener {
            onChildClick(broad.id) // Pass the child's ID (responseId) to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return broads.size
    }

    class BroadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ageTextView: TextView = itemView.findViewById(R.id.tvAge)
        private val lengthTextview: TextView = itemView.findViewById(R.id.tvLength)
        private val weightTextView: TextView = itemView.findViewById(R.id.tvWeight)

        fun bind(broad: BroadClinical) {
            ageTextView.text = broad.age
            lengthTextview.text = broad.weight
            weightTextView.text = broad.length

        }

        fun bind(child: Child) {

        }
    }
}
