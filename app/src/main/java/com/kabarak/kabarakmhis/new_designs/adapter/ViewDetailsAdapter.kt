package com.kabarak.kabarakmhis.new_designs.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.new_designs.data_class.DbTypeDataValue

class ViewDetailsAdapter(private var entryList: ArrayList<DbTypeDataValue>,
                         private val context: Context
) : RecyclerView.Adapter<ViewDetailsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

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
                R.layout.details_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val dbObserve = entryList[position].dbObserveValue
        val newValue = "${dbObserve.title}: ${dbObserve.value}"


        holder.tvTitle.text = entryList[position].type
        holder.tvValue.text = newValue



    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}