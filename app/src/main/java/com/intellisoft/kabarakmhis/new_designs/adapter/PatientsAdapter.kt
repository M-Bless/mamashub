package com.intellisoft.kabarakmhis.new_designs.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DBEntry
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PatientsAdapter(private var entryList: List<DBEntry>,
                      private val context: Context
) : RecyclerView.Adapter<PatientsAdapter.Pager2ViewHolder>() {

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
            val id = entryList[pos].resource.id
            val dob = entryList[pos].resource.birthDate
            val name = entryList[pos].resource.name[0].family

            CoroutineScope(Dispatchers.IO).launch {
                coroutineScope {
                    launch(Dispatchers.IO) {

                        val contactList = entryList[position].resource.contact
                        if (contactList != null) {
                            for (items in contactList){

                                val relationShip = items.relationship?.get(0)?.text
                                val kinName = items.name.family
                                val phoneNumber = items.telecom[0].value

                                if (relationShip != null) {
                                    FormatterClass().saveSharedPreference(context, "kinRelationShip", relationShip)
                                }
                                FormatterClass().saveSharedPreference(context, "kinName", kinName)
                                FormatterClass().saveSharedPreference(context, "kinPhoneNumber", phoneNumber)

                            }
                        }

                        FormatterClass().saveSharedPreference(context, "patientId", id)
                        FormatterClass().saveSharedPreference(context, "FHIRID", id)

                        FormatterClass().saveSharedPreference(context, "dob", dob)
                        FormatterClass().saveSharedPreference(context, "name", name)

                        RetrofitCallsFhir().getPatientEncounters(context)

                    }
                }
            }


            context.startActivity(Intent(context, PatientProfile::class.java))

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

        val id = entryList[position].resource.id
        val name = entryList[position].resource.name
        val birthDate = entryList[position].resource.birthDate

        val pos = "${position + 1}"


        holder.tvId.text = pos
        holder.tvName.text = name[0].family
        holder.tvFieldName.text = birthDate


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}