package com.intellisoft.kabarakmhis.new_designs.chw.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.DbPatientDetails
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.chw.ChwClientDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DBEntry
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ChwPatientsListAdapter(private var entryList: List<DbPatientDetails>,
                             private val context: Context
) : RecyclerView.Adapter<ChwPatientsListAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvFieldName: TextView = itemView.findViewById(R.id.field_name)
        val tvId: TextView = itemView.findViewById(R.id.id)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].id

            FormatterClass().saveSharedPreference(context, "patientId", id)
            FormatterClass().saveSharedPreference(context, "FHIRID", id)

            context.startActivity(Intent(context, ChwClientDetails::class.java))

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val id = entryList[position].id
        val name = entryList[position].name

        val pos = "${position + 1}"

        holder.tvId.text = pos
        holder.tvName.text = name

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}