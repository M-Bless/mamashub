package com.intellisoft.kabarakmhis.new_designs.antenatal_profile

import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.fragment_antenatal3.view.*

import java.util.*


class FragmentAntenatal3 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal3, container, false)

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()

        rootView.btnNext.setOnClickListener {

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentAntenatal4())
            ft.addToBackStack(null)
            ft.commit()
        }

        rootView.radioGrpHiv.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTestDate, true)
                    changeVisibility(rootView.linearNo, false)
                } else {
                    changeVisibility(rootView.linearTestDate, false)
                    changeVisibility(rootView.linearNo, true)
                }
            }
        }
        rootView.radioGrpHIVStatus.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "NR") {
                    changeVisibility(rootView.linearNR, true)
                } else {
                    changeVisibility(rootView.linearNR, false)
                }
            }
        }
        rootView.radioGrpSyphilis.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearSyphTestDate, true)
                    changeVisibility(rootView.linearSyphNo, false)
                } else {
                    changeVisibility(rootView.linearSyphTestDate, false)
                    changeVisibility(rootView.linearSyphNo, true)
                }
            }
        }
        rootView.radioGrpHepatitis.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearHepatitis, true)
                    changeVisibility(rootView.linearHepaNo, false)
                } else {
                    changeVisibility(rootView.linearHepatitis, false)
                    changeVisibility(rootView.linearHepaNo, true)
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