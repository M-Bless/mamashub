package com.kabarak.kabarakmhis.pnc.vaccines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.Vaccines

class VaccinesAdapter(
    private val vaccines: MutableList<Vaccines>,
    private val onVaccineClick: (String) -> Unit // Lambda function to handle vaccine click
) : RecyclerView.Adapter<VaccinesAdapter.VaccineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_vaccines, parent, false) // view is a LinearLayout
        return VaccineViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val vaccine = vaccines[position]
        holder.bind(vaccine)

        // Set click listener to pass the vaccine's id
        holder.itemView.setOnClickListener {
            onVaccineClick(vaccine.id) // Pass the vaccine's ID to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return vaccines.size
    }

    class VaccineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvVaccineName)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvVaccineDate)

        fun bind(vaccine: Vaccines) {
            nameTextView.text = vaccine.VaccineName
            dateTextView.text = vaccine.VaccineDate
        }
    }
}
