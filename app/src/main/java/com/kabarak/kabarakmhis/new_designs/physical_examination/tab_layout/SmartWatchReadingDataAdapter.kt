package com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbWatchTimeData


class SmartWatchReadingDataAdapter(private var entryList: ArrayList<DbWatchTimeData>,
                                   private val context: Context) : RecyclerView.Adapter<SmartWatchReadingDataAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvSys: TextView = itemView.findViewById(R.id.tvSys)
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)
        val tvHr: TextView = itemView.findViewById(R.id.tvHr)

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
    ): PagerViewHolder {
        return PagerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.watch_reading_child,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

        val time = entryList[position].time
        holder.tvTime.text = time

        val readingsData = entryList[position].readings

        holder.tvSys.text = readingsData.systolic
        holder.tvDia.text = readingsData.diastolic
        holder.tvHr.text = readingsData.pulse

    }



    override fun getItemCount(): Int {
        return entryList.size
    }

}