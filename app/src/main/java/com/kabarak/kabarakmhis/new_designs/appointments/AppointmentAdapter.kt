package com.kabarak.kabarakmhis.new_designs.appointments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbAppointments
import com.kabarak.kabarakmhis.helperclass.FormatterClass


class AppointmentAdapter(private var entryList: ArrayList<DbAppointments>,
                         private val context: Context
) : RecyclerView.Adapter<AppointmentAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvNoOfDay: TextView = itemView.findViewById(R.id.tvNoDay)
        val tvAppointment: TextView = itemView.findViewById(R.id.tvAppointment)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            entryList[pos].id


        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.appointment_schedule,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        entryList[position].id
        val appointmentDate = entryList[position].date
        val title = entryList[position].title

        val dayOfWeek = FormatterClass().getDayOfWeek(appointmentDate)
        val noDaysFromToday = FormatterClass().getDaysFromToday(appointmentDate)


        holder.tvDate.text = appointmentDate
        holder.tvAppointment.text = title
        holder.tvDay.text = dayOfWeek
        holder.tvNoOfDay.text = noDaysFromToday


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}