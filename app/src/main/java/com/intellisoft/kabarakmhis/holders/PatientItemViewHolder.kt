
package com.intellisoft.kabarakmhis.holders

import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.databinding.PatientListItemViewBinding
import com.intellisoft.kabarakmhis.helperclass.PatientItem
import java.time.LocalDate
import java.time.Period
import org.hl7.fhir.r4.model.codesystems.RiskProbability

class PatientItemViewHolder(binding: PatientListItemViewBinding) :
  RecyclerView.ViewHolder(binding.root) {
  private val nameView: TextView = binding.name
  private val ageView: TextView = binding.fieldName
  private val idView: TextView = binding.id

  @RequiresApi(Build.VERSION_CODES.O)
  fun bindTo(
    patientItem: PatientItem,
    onItemClicked: (PatientItem) -> Unit
  ) {
    this.nameView.text = patientItem.name
    this.ageView.text = getFormattedAge(patientItem, ageView.context.resources)
    this.idView.text = "Id: #---${getTruncatedId(patientItem)}"
    this.itemView.setOnClickListener { onItemClicked(patientItem) }

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getFormattedAge(
      patientItem: PatientItem,
      resources: Resources
  ): String {
    if (patientItem.dob.isEmpty()) return ""
    return Period.between(LocalDate.parse(patientItem.dob), LocalDate.now()).let {
      when {
        it.years > 0 -> resources.getQuantityString(R.plurals.ageYear, it.years, it.years)
        it.months > 0 -> resources.getQuantityString(R.plurals.ageMonth, it.months, it.months)
        else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
      }
    }
  }

  /** The new ui just shows shortened id with just last 3 characters. */
  private fun getTruncatedId(patientItem: PatientItem): String {
    return patientItem.resourceId.takeLast(3)
  }
}
