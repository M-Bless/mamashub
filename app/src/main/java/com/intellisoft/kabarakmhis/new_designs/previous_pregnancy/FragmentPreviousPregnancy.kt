package com.intellisoft.kabarakmhis.new_designs.previous_pregnancy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.*
import kotlinx.android.synthetic.main.fragment_prev_pregnancy.view.*


class FragmentPreviousPregnancy : Fragment(), AdapterView.OnItemSelectedListener {

    private val formatter = FormatterClass()

    var pregnancyOrderList = arrayOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th")
    private var spinnerPregnancyValue  = pregnancyOrderList[0]


    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_prev_pregnancy, container, false)

        rootView.btnSave.setOnClickListener {
            saveData()
        }

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()


        return rootView
    }

    private fun saveData() {

        val year = etYear.text.toString()
        val ancTime = etVisitTime.text.toString()
        val birthPlace = etPlaceOfChildBirth.text.toString()
        val gestation = etGestation.text.toString()
        val duration = etDuration.text.toString()

        val babyWeight = etBabyWeight.text.toString()
        val abnormal = etAbnormal.text.toString()




        startActivity(Intent(requireContext(), PreviousPregnancyView::class.java))

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pregnancyOrderList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerPregOrder!!.adapter = kinRshp

        rootView.spinnerPregOrder.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRshp -> { spinnerPregnancyValue = rootView.spinnerPregOrder.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}