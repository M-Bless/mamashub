package com.kabarak.kabarakmhis.pnc.diphtheria

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.data_class.Diphtheria

class DiphtheriaAdapter(
    private val diphtherias: List<Diphtheria>,
    private val onDiphtheriaClick: (String) -> Unit // Lambda function to handle diphtheria click
) : RecyclerView.Adapter<DiphtheriaAdapter.DiphtheriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiphtheriaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_diphtheria, parent, false) // view is a LinearLayout
        return DiphtheriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiphtheriaViewHolder, position: Int) {
        val diphtheria = diphtherias[position]
        holder.bind(diphtheria)

        // Set click listener to pass the diphtheria's responseId (id)
        holder.itemView.setOnClickListener {
            onDiphtheriaClick(diphtheria.id) // Pass the diphtheria's ID (responseId) to the lambda function
        }
    }

    override fun getItemCount(): Int {
        return diphtherias.size
    }

    class DiphtheriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val doseTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaDose)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaDate)
        private val nextdateTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaNextDate)
        private  val batchTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaBatch)
        private val lotNumberTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaLotNumber)
        private val manufacturerTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaManufacturer)
        private val expiryDateTextView: TextView = itemView.findViewById(R.id.tvDiphtheriaExpiryDate)

        fun bind(diphtheria: Diphtheria) {
            doseTextView.text = diphtheria.dose
            dateTextView.text = diphtheria.date
            nextdateTextView.text = diphtheria.nextDate
            batchTextView.text = diphtheria.batch
            lotNumberTextView.text = diphtheria.lotnumber
            manufacturerTextView.text = diphtheria.manufacturer
            expiryDateTextView.text = diphtheria.expiryDate
        }
    }
}