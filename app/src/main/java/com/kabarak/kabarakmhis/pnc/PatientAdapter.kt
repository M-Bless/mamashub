package com.kabarak.kabarakmhis.pnc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import org.hl7.fhir.r4.model.Patient

class PatientAdapter : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    private var patients: List<Patient> = emptyList()

    fun setPatients(patientList: List<Patient>) {
        patients = patientList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.bind(patient)
    }

    override fun getItemCount(): Int = patients.size

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.patientNameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.patientIdTextView)

        fun bind(patient: Patient) {
            val name = patient.name?.firstOrNull()?.nameAsSingleString ?: "Unnamed"
            nameTextView.text = name
            idTextView.text = patient.id
        }
    }
}
