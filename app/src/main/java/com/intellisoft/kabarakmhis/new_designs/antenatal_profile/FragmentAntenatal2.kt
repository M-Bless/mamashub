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
import kotlinx.android.synthetic.main.activity_register_new_patient.*
import kotlinx.android.synthetic.main.fragment_antenatal2.view.*

import java.util.*


class FragmentAntenatal2 : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_antenatal2, container, false)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.btnNext.setOnClickListener {

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentAntenatal3())
            ft.addToBackStack(null)
            ft.commit()
        }

        rootView.radioGrpTb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTB, true)
                } else {
                    changeVisibility(rootView.linearTB, false)

                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpTbResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Positive") {
                    changeVisibility(rootView.linearPositive, true)
                    changeVisibility(rootView.linearNegative, false)
                } else if (checkedBtn == "Negative")  {
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, true)
                }else{
                    changeVisibility(rootView.linearPositive, false)
                    changeVisibility(rootView.linearNegative, false)
                }
            }
        }
        rootView.radioGrpUltrasound1.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearDate, true)
                } else {
                    changeVisibility(rootView.linearDate, false)
                }
            }
        }
        rootView.radioGrpUltrasound2.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linear2ndUltra, true)
                } else {
                    changeVisibility(rootView.linear2ndUltra, false)
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