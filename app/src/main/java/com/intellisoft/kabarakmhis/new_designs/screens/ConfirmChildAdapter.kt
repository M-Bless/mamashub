package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.new_designs.data_class.DbConfirmDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue

class ConfirmChildAdapter(private var entryList: List<DbObserveValue>?,
                          private val context: Context
) : RecyclerView.Adapter<ConfirmChildAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.confirm_children_details,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val title = entryList?.get(position)?.title
        val value = entryList?.get(position)?.value

        val titleData = "$title :"

        holder.tvTitle.text = titleData
        holder.tvDesc.text = value




    }

    override fun getItemCount(): Int {
        return entryList!!.size
    }

}