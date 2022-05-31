package com.intellisoft.kabarakmhis.fhir.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.intellisoft.kabarakmhis.databinding.PatientDetailsHeaderBinding
import com.intellisoft.kabarakmhis.databinding.PatientListItemViewBinding
import com.intellisoft.kabarakmhis.fhir.viewmodels.ChildDetailOverview
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailData
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailOverview
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailRelation
import com.intellisoft.kabarakmhis.helperclass.RelatedPersonItem
import com.intellisoft.kabarakmhis.helperclass.Steps
import com.intellisoft.kabarakmhis.helperclass.allCornersRounded

class OverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
    private val maternityClick: () -> Unit,
    private val steps: Steps,
    val show: Boolean,
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailOverview).let { binding.title.text = it.patient.name }
        if (show) {
            binding.appRegistration.visibility = View.VISIBLE
            binding.screener.text = steps.lastIn
            binding.maternity.text = steps.fistIn
//            binding.screener.setOnClickListener { newBornClick() }
            binding.maternity.setOnClickListener { maternityClick() }
            if (!steps.secondButton!!) {
                binding.screener.visibility = View.INVISIBLE
            }
        }
        data.patient.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted
        }
    }
}

class RelatedOverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
    val show: Boolean,
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailOverview).let { binding.title.text = it.patient.name }
        if (show) {
            binding.appRegistration.visibility = View.VISIBLE
        }
        data.patient.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted
        }
    }
}


/**
 * Child Overview
 **/
class ChildOverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as ChildDetailOverview).let {
            binding.title.text = it.relation.name

        }
        data.relation.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted

        }
    }
}

class PatientDetailsRelationItemViewHolder(
    private val binding: PatientListItemViewBinding,
    val viewChildClick: (RelatedPersonItem) -> Unit
) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {

        (data as PatientDetailRelation).let {

            binding.name.text = it.relation.name
            binding.fieldName.text = it.relation.dob
            binding.root.setOnClickListener { viewChildClick(data.relation) }

        }

//        binding.status.visibility = View.GONE
        binding.id.visibility = View.GONE
        binding.tvView.visibility = View.VISIBLE
    }
}