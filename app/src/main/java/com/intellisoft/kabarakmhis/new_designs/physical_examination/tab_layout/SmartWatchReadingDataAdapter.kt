package com.intellisoft.kabarakmhis.new_designs.physical_examination.tab_layout

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbSmartWatchReadings
import com.intellisoft.kabarakmhis.helperclass.DbWatchDataValues
import com.intellisoft.kabarakmhis.helperclass.DbWatchReading


class SmartWatchReadingDataAdapter(private var entryList: ArrayList<DbWatchDataValues>,
                                   private val context: Context) : RecyclerView.Adapter<SmartWatchReadingDataAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvValue: TextView = itemView.findViewById(R.id.tvValue)

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

        val text = entryList[position].text
        val value = entryList[position].value
        val time = entryList[position].time

        holder.tvTime.text = time
        holder.tvValue.text = value
        holder.tvType.text = text

//        validateReadings(text, value, holder.tvValue)


    }

    //Validate diastolic, sytolic and pulse readings
    private fun validateReadings(text: String, valueData: String, textView: TextView) {


        val reversedText = valueData.reversed()
        if (text.contains("Systolic") || text.contains("Diastolic")) {

            val value = reversedText.substring(4, reversedText.length)
            val reversedValue = value.reversed().trim()

            val valueInt = reversedValue.toInt()

            if (text.contains("Systolic")) {
                if (valueInt <= 70) {
                    textView.setBackgroundColor(context.resources.getColor(R.color.moderate_risk))
                } else if (valueInt <= 80) {
                    textView.setBackgroundColor(context.resources.getColor(R.color.orange))
                } else if (valueInt <= 110) {
                    textView.setBackgroundColor(context.resources.getColor(R.color.yellow))
                } else if (valueInt <= 130)
                    textView.setBackgroundColor(context.resources.getColor(android.R.color.holo_green_light))
                else {
                    textView.setBackgroundColor(context.resources.getColor(R.color.moderate_risk))
                }

            }
            if (text.contains("Diastolic")) {
                if (valueInt <= 60) {
                    textView.setBackgroundColor(context.resources.getColor(R.color.yellow))
                } else if (valueInt <= 90) {
                    textView.setBackgroundColor(context.resources.getColor(R.color.low_risk))
                } else {
                    textView.setBackgroundColor(context.resources.getColor(R.color.moderate_risk))
                }
            }

        }

        if (text.contains("Heart")) {
            val value = reversedText.substring(3, reversedText.length)
            val reversedValue = value.reversed().trim()
            val valueInt = reversedValue.toInt()

            Log.e("value", value.toString())
            Log.e("valueInt", valueInt.toString())


            if (valueInt <= 60) {
                textView.setBackgroundColor(context.resources.getColor(R.color.moderate_risk))
            } else if (valueInt <= 100) {
                textView.setBackgroundColor(context.resources.getColor(R.color.low_risk))
            } else {
                textView.setBackgroundColor(context.resources.getColor(R.color.moderate_risk))
            }
        }

    }



    override fun getItemCount(): Int {
        return entryList.size
    }

}