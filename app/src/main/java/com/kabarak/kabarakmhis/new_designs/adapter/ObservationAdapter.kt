package com.kabarak.kabarakmhis.new_designs.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.new_designs.data_class.DbCodingData

class ObservationAdapter(private var entryList: ArrayList<DbCodingData>,
                         private val context: Context
) : RecyclerView.Adapter<ObservationAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            adapterPosition

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.observation_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val title = entryList[position].code
        val name = entryList[position].display

        holder.tvTitle.text = title
        holder.tvValue.text = name


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}