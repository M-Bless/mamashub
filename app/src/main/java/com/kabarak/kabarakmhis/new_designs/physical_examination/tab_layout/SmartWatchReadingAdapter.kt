package com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbWatchData


class SmartWatchReadingAdapter(private var entryList: ArrayList<DbWatchData>,
                               private val context: Context) : RecyclerView.Adapter<SmartWatchReadingAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvReadingDate: TextView = itemView.findViewById(R.id.tvReadingDate)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

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
                R.layout.watch_reading,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val appointmentDate = entryList[position].date
        val readingsList = entryList[position].readings

        holder.tvReadingDate.text = appointmentDate

//        val confirmParentAdapter = SmartWatchReadingDataAdapter(readingsList,context)
//        holder.recyclerView.adapter = confirmParentAdapter



    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}