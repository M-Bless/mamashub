package com.kabarak.kabarakmhis.immunisation.vitamin_a_supplimentary

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import java.text.ParseException
import java.util.Locale
import com.kabarak.kabarakmhis.pnc.data_class.VitaminASup

class VitaminASupplimentaryAdapter(
    private val patients: List<VitaminASup>,
    private val onVitaminASupClick: (String) -> Unit
) : RecyclerView.Adapter<VitaminASupplimentaryAdapter.VitaminASupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitaminASupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_vitamin_a_sup, parent, false)
        return VitaminASupViewHolder(view)
    }

    override fun onBindViewHolder(holder: VitaminASupViewHolder, position: Int) {
        val vitaminASup = patients[position]
        holder.bind(vitaminASup)

        // Handle click events for each item
        holder.itemView.setOnClickListener {
            onVitaminASupClick(vitaminASup.id) // Pass unique identifier if needed
        }
    }

    override fun getItemCount(): Int = patients.size

    class VitaminASupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dosenameTextView: TextView = itemView.findViewById(R.id.tvDoses)
        private val ageTextView: TextView = itemView.findViewById(R.id.tvAge)
        private val nextvisitTextView: TextView = itemView.findViewById(R.id.tvNextVisit)
        private  val dateGivenTextView: TextView = itemView.findViewById(R.id.tvDateGiven)

        // Define the input and output date formats
        private val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        private val outputDateFormat = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())

        fun bind(vitaminASup: VitaminASup) {
            dosenameTextView.text = "Dose: ${vitaminASup.dose}"
            ageTextView.text = "Given at: ${vitaminASup.age}"


            // Parse and format the `nextVisit`
            nextvisitTextView.text = if (!vitaminASup.nextVisit.isNullOrEmpty()) {
                try {
                    val parsedDate = inputDateFormat.parse(vitaminASup.nextVisit)
                    "Next Visit: ${outputDateFormat.format(parsedDate)}"
                } catch (e: ParseException) {
                    "Next Visit: N/A"
                }
            } else {
                "Next Visit: N/A"
            }

            dateGivenTextView.text = if (!vitaminASup.nextVisit.isNullOrEmpty()) {
                try {
                    val parsedDate = inputDateFormat.parse(vitaminASup.dateGiven)
                    "Given on: ${outputDateFormat.format(parsedDate)}"
                } catch (e: ParseException) {
                    "Given on: N/A"
                }
            } else {
                "Given on: N/A"
            }
        }
    }
}
