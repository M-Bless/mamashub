package com.kabarak.kabarakmhis.new_designs.screens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.helperclass.DbMaternalProfile
import kotlinx.android.synthetic.main.activity_patient_profile.*

class ExpandableRecyclerAdapter (val modelList: List<DbMaternalProfile>, val context: Context):
    RecyclerView.Adapter<ExpandableRecyclerAdapter.AdapterVH>()
{
    class AdapterVH(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle : TextView = itemView.findViewById(R.id.tvTitle)
        var recyclerView : RecyclerView = itemView.findViewById(R.id.recyclerView)

        var linearLayout : LinearLayout = itemView.findViewById(R.id.linearLayout)
        var expendableLayout : RelativeLayout = itemView.findViewById(R.id.expandable_layout)
        var imgArrow : ImageView = itemView.findViewById(R.id.imgArrow)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterVH {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_expandable,parent,false)
        return AdapterVH(view)
    }

    override fun onBindViewHolder(holder: AdapterVH, position: Int) {
        val model  = modelList[position]
        val title = model.title
        val childrenList = model.childrenList
        val isPatient = model.isPatient

        holder.tvTitle.text = title
        val maternalProfileChildrenAdapter = MaternalProfileChildrenAdapter(childrenList, context)
        holder.recyclerView.adapter = maternalProfileChildrenAdapter
        holder.recyclerView.setHasFixedSize(true)

        val isExpandable: Boolean = modelList[position].expandable
        holder.expendableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE
        holder.imgArrow.background = (if (isExpandable) context.getDrawable(R.drawable.ic_arrow_up) else context.getDrawable(R.drawable.ic_arrow_down))

        holder.linearLayout.isEnabled = isPatient


        holder.linearLayout.setOnClickListener{
            val version = modelList[position]
            version.expandable = !model.expandable
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }
}