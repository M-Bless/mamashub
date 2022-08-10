package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
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
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_surgical.view.*
import kotlinx.android.synthetic.main.fragment_surgical.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentSurgical : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_surgical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.checkboxNoPast.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked){
                rootView.linearSurgeries.visibility = View.GONE
            }else{
                if (!rootView.checkboxNoKnowledge.isChecked){
                    rootView.linearSurgeries.visibility = View.VISIBLE
                }else{
                    rootView.linearSurgeries.visibility = View.GONE
                }

            }
        }
        rootView.checkboxNoKnowledge.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked){
                rootView.linearSurgeries.visibility = View.GONE
            }else{
                if (!rootView.checkboxNoPast.isChecked){
                    rootView.linearSurgeries.visibility = View.VISIBLE
                }else{
                    rootView.linearSurgeries.visibility = View.GONE
                }

            }
        }


        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val surgicalHistoryList = ArrayList<String>()

        if (rootView.checkboxNoPast.isChecked) surgicalHistoryList.add(rootView.checkboxNoPast.text.toString())
        if (rootView.checkboxNoKnowledge.isChecked) surgicalHistoryList.add(rootView.checkboxNoKnowledge.text.toString())
        if (rootView.checkboxDilation.isChecked) surgicalHistoryList.add(rootView.checkboxDilation.text.toString())
        if (rootView.checkboxMyomectomy.isChecked) surgicalHistoryList.add(rootView.checkboxMyomectomy.text.toString())
        if (rootView.checkboxRemoval.isChecked) surgicalHistoryList.add(rootView.checkboxRemoval.text.toString())
        if (rootView.checkboxOophorectomy.isChecked) surgicalHistoryList.add(rootView.checkboxOophorectomy.text.toString())
        if (rootView.checkboxSalpi.isChecked) surgicalHistoryList.add(rootView.checkboxSalpi.text.toString())
        if (rootView.checkboxCervical.isChecked) surgicalHistoryList.add(rootView.checkboxCervical.text.toString())

        addData("Surgical History", surgicalHistoryList.toString(), DbObservationValues.SURGICAL_HISTORY.name)

        val otherGyna = rootView.etOtherGyna.text.toString()
        val otherSurgeries = rootView.etOtherSurgery.text.toString()

        if (!TextUtils.isEmpty(otherGyna)){
            addData("Other Gynecological Procedures",otherGyna, DbObservationValues.SURGICAL_HISTORY.name)
        }
        if (!TextUtils.isEmpty(otherSurgeries)){
            addData("Other Surgeries",otherSurgeries, DbObservationValues.SURGICAL_HISTORY.name)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, "Medical and Surgical History", DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentMedical())
        ft.addToBackStack(null)
        ft.commit()

    }

    private fun addData(key: String, value: String, codeLabel: String) {

        val dbObservationLabel = DbObservationLabel(value, codeLabel)
        observationList[key] = dbObservationLabel
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