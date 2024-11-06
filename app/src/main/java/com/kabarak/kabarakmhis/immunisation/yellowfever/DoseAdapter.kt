package com.kabarak.kabarakmhis.immunisation.yellowfever

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_yellowfever_dose.*
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.immunisation.data_class.DoseClass
import java.text.ParseException
import java.util.Locale

class DoseAdapter(
    private val dose: List<DoseClass>,
    private val onDoseClassClick: (String) -> Unit
) : RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_yellowfever_dose, parent, false)
        return DoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val doseClass = dose[position]
        holder.bind(doseClass)

        // Handle click events for each item
        holder.itemView.setOnClickListener {
            onDoseClassClick(doseClass.id) // Pass unique identifier if needed
        }
    }

    override fun getItemCount(): Int = dose.size

    class DoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)

        // Define the input format of the received date string
        private val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        // Define the desired output format
        private val outputDateFormat = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())

        fun bind(doseClass: DoseClass) {
            nameTextView.text = "Vaccine Name: ${doseClass.vaccineName}"

            // Parse and reformat the date
            dateTextView.text = try {
                val parsedDate = inputDateFormat.parse(doseClass.issuedDate)
                "Date Given: ${outputDateFormat.format(parsedDate)}"
            } catch (e: ParseException) {
                "Date Given: N/A"
            }
        }
    }
}