package com.kabarak.kabarakmhis.pnc

import android.content.Intent
import com.kabarak.kabarakmhis.pnc.ChildListViewModel.ChildItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.bcgvacination.BcgVaccinationViewActivity

class ChildListAdapter(private val onChildItemClicked: (ChildItem) -> Unit) :
    ListAdapter<ChildItem, ChildListAdapter.ChildViewHolder>(ChildDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val childItem = getItem(position)
        holder.bind(childItem)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChildProfileActivity::class.java)
            intent.putExtra("CHILD_ID", childItem.id) // Pass the ID
            context.startActivity(intent)
        }
    }


    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        private val genderTextView: TextView = itemView.findViewById(R.id.text_gender)
        private val dobTextView: TextView = itemView.findViewById(R.id.text_dob)

        fun bind(item: ChildItem) {
            nameTextView.text = item.name
            genderTextView.text = item.gender
            dobTextView.text = item.dob
        }
    }

    class ChildDiffCallback : DiffUtil.ItemCallback<ChildItem>() {
        override fun areItemsTheSame(oldItem: ChildItem, newItem: ChildItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChildItem, newItem: ChildItem): Boolean {
            return oldItem == newItem
        }
    }
}