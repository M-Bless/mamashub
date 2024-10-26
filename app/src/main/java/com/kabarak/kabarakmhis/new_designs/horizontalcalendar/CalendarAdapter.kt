package com.kabarak.kabarakmhis.new_designs.horizontalcalendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.databinding.DateItemBinding
import com.kabarak.kabarakmhis.new_designs.screens.BpMonitoring
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarAdapter(
    private val calendarInterface: BpMonitoring,
    private val list: ArrayList<CalendarData>
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    inner class ViewHolder(private val binding: DateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDataModel: CalendarData, position: Int) {
            val calendarDay = binding.tvCalendarDay
            val calendarDate = binding.tvCalendarDate
            val cardView = binding.root
           val dayOfMonth = SimpleDateFormat("d", Locale.ENGLISH).format(calendarDataModel.data)

            // Set the calendar daya and date
            calendarDay.text = calendarDataModel.calendarDay
            calendarDate.text = calendarDataModel.calendarDate

            // Highlight selected date
            if (selectedPosition == position) {
                calendarDataModel.isSelected = true
            } else {
                calendarDataModel.isSelected = false
            }

            if (calendarDataModel.isSelected) {
                // Apply selected date styling
                calendarDay.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
                calendarDate.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.blue)
                )
            } else {
                // Apply unselected date styling
                calendarDay.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.blue)
                )
                calendarDate.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.blue)
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
            }

            // Set the onClick listener for date selection
            cardView.setOnClickListener {
                selectedPosition = adapterPosition
                calendarInterface.onDateSelected(calendarDataModel, adapterPosition, calendarDate)
                notifyDataSetChanged()  // Refresh the RecyclerView to reflect selection changes
            }
        }
    }

    interface CalendarInterface {
        fun onDateSelected(calendarData: CalendarData, position: Int, tvDate: TextView)
    }

    // Helper function to update the selected position
    fun setPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()  // Refresh RecyclerView when position changes
    }

    // Helper function to update the calendar list and refresh the adapter
    fun updateList(calendarList: ArrayList<CalendarData>) {
        list.clear()
        list.addAll(calendarList)
        notifyDataSetChanged()  // Refresh RecyclerView after data change
    }
}
