package com.kabarak.kabarakmhis.immunisation.Pconjugate

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.immunisation.data_class.Conjugate
import java.text.ParseException
import java.util.Locale

class PconjugateAdapter(
    private val patients: List<Conjugate>,
    private val onConjugateClick: (String) -> Unit
) : RecyclerView.Adapter<PconjugateAdapter.ConjugateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConjugateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_conjugate, parent, false)
        return ConjugateViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConjugateViewHolder, position: Int) {
        val conjugate = patients[position]
        holder.bind(conjugate)

        // Handle click events for each item
        holder.itemView.setOnClickListener {
            onConjugateClick(conjugate.id) // Pass unique identifier if needed
        }
    }

    override fun getItemCount(): Int = patients.size

    class ConjugateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dosenameTextView: TextView = itemView.findViewById(R.id.tvDoses)
        private val dategivenTextView: TextView = itemView.findViewById(R.id.tvDateGiven)
        private val nextvisitTextView: TextView = itemView.findViewById(R.id.tvNextVisit)

        // Define the input and output date formats
        private val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        private val outputDateFormat = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())

        fun bind(conjugate: Conjugate) {
            dosenameTextView.text = "${conjugate.doseName}"

            // Parse and format the `dateGiven`
            dategivenTextView.text = if (!conjugate.dateGiven.isNullOrEmpty()) {
                try {
                    val parsedDate = inputDateFormat.parse(conjugate.dateGiven)
                    "Date Given: ${outputDateFormat.format(parsedDate)}"
                } catch (e: ParseException) {
                    "Date Given: N/A"
                }
            } else {
                "Date Given: N/A"
            }

            // Parse and format the `nextVisit`
            nextvisitTextView.text = if (!conjugate.nextVisit.isNullOrEmpty()) {
                try {
                    val parsedDate = inputDateFormat.parse(conjugate.nextVisit)
                    "Next Visit: ${outputDateFormat.format(parsedDate)}"
                } catch (e: ParseException) {
                    "Next Visit: N/A"
                }
            } else {
                "Next Visit: N/A"
            }
        }
    }
}