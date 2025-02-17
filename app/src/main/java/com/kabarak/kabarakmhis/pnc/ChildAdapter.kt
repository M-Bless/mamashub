package com.kabarak.kabarakmhis.pnc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.Child

class ChildAdapter(
    private val children: List<Child>,
    private val onChildClick: (String) -> Unit // Lambda function to handle child click
) : RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_child, parent, false) // view is a LinearLayout
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
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

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvChildName)

        fun bind(child: Child) {
            nameTextView.text = child.name
        }
    }
}