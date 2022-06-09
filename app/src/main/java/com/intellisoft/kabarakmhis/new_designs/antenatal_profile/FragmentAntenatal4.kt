package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.fragment_antenatal4.view.*
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHIVStatus
import kotlinx.android.synthetic.main.fragment_antenatal4.view.radioGrpHiv

import java.util.*


class FragmentAntenatal4 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private val antenatal4 = DbResourceViews.ANTENATAL_4.name

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal4, container, false)

        formatter.saveCurrentPage("4", requireContext())
        getPageDetails()

        rootView.btnSave.setOnClickListener {

//            formatter.saveSharedPreference(requireContext(), "FRAGMENT", antenatal4)
            val intent = Intent(requireContext(), AntenatalProfileView::class.java)
            startActivity(intent)

        }

        rootView.radioGrpHiv.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {

                } else {

                }
            }
        }
        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "R") {
                    changeVisibility(rootView.linearReactive, true)
                } else {
                    changeVisibility(rootView.linearReactive, false)
                }
            }
        }
        rootView.radioGrpReactive.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReferral, true)
                } else {
                    changeVisibility(rootView.linearReferral, false)
                }
            }
        }



        return rootView
    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }


}