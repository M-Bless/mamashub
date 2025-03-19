package com.kabarak.kabarakmhis.new_designs.screens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.new_designs.data_class.DbConfirmDetails
import java.util.*

class ConfirmParentAdapter(private var entryList: List<DbConfirmDetails>?,
                           private val context: Context
) : RecyclerView.Adapter<ConfirmParentAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.childrenList)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            adapterPosition



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.confirm_parent_details,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val titleData = entryList?.get(position)?.titleData
        val detailsList = entryList?.get(position)?.detailsList

        val titleValue = titleData?.drop(2).toString().lowercase(Locale.getDefault())
            .replaceFirstChar {
                if (it.isLowerCase())
                    it.titlecase(Locale.getDefault())
                else it.toString() }.replace("_", " ")

        holder.tvTitle.text = titleValue

        val confirmParentAdapter = ConfirmChildAdapter(detailsList,context)
        holder.recyclerView.adapter = confirmParentAdapter



    }

    override fun getItemCount(): Int {
        return entryList!!.size
    }

}