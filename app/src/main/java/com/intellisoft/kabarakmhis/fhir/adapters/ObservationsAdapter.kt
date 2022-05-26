package com.intellisoft.kabarakmhis.fhir.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.kabarakmhis.databinding.PatientListItemViewBinding
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailCondition
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailData
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailObservation
import com.intellisoft.kabarakmhis.helperclass.*


class ObservationsAdapter :

    ListAdapter<PatientDetailData, PatientDetailItemViewHolder>(PatientDetailDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientDetailItemViewHolder {
        return when (ObservationViewTypes.from(viewType)) {

            ObservationViewTypes.OBSERVATION ->
                PatientDetailsObservationItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ObservationViewTypes.CONDITION ->
                PatientDetailsConditionItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: PatientDetailItemViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
        if (holder is PatientDetailsHeaderItemViewHolder) return

        holder.itemView.background =
            if (model.firstInGroup && model.lastInGroup) {
                allCornersRounded()
            } else if (model.firstInGroup) {
                topCornersRounded()
            } else if (model.lastInGroup) {
                bottomCornersRounded()
            } else {
                noCornersRounded()
            }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is PatientDetailObservation -> ObservationViewTypes.OBSERVATION
            is PatientDetailCondition -> ObservationViewTypes.CONDITION
            else -> {
                throw IllegalArgumentException("Undefined Item type")
            }
        }.ordinal
    }
}