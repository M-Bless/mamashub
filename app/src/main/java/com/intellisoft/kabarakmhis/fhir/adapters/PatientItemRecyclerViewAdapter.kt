package com.intellisoft.kabarakmhis.fhir.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.kabarakmhis.databinding.PatientListItemViewBinding
import com.intellisoft.kabarakmhis.helperclass.PatientItem
import com.intellisoft.kabarakmhis.holders.PatientItemViewHolder


/** UI Controller helper class to monitor Patient viewmodel and display list of patients. */
class PatientItemRecyclerViewAdapter(
    private val onItemClicked: (PatientItem) -> Unit
) :
    ListAdapter<PatientItem, PatientItemViewHolder>(PatientItemDiffCallback()) {

    class PatientItemDiffCallback : DiffUtil.ItemCallback<PatientItem>() {
        override fun areItemsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.id == newItem.id && oldItem.risk == newItem.risk
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder {
        return PatientItemViewHolder(
            PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}
