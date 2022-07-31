package com.intellisoft.kabarakmhis.new_designs.pmtct

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.fragment_pmtct2.view.*
import kotlinx.android.synthetic.main.fragment_pmtct2.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentPmtct2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_pmtct2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        rootView.radioGrpRegimen.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReason, true)
                } else {
                    changeVisibility(rootView.linearReason, false)
                }

            }
        }

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Confirm"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun checkedText(checkBox: CheckBox){

        if (checkBox.isChecked){
            val value = checkBox.text.toString()
            addData("Reason for regiment change",value)
        }

    }
    private fun saveData() {

        if(rootView.linearReason.visibility == View.VISIBLE){
            checkedText(rootView.checkboxViralLoad)
            checkedText(rootView.checkboxAdverseReactions)
            checkedText(rootView.checkboxInteraction)
            checkedText(rootView.checkboxTrimester)
            val otherRegimen = rootView.etOther.text.toString()

            if (!TextUtils.isEmpty(otherRegimen)){
                addData("Other Regimen Applied",otherRegimen)
            }
        }

        val artAmount = rootView.etDosageAmount.text.toString()

        if (!TextUtils.isEmpty(artAmount)){
            addData("ART Amount",artAmount)
        }
        val frequency = rootView.etFrequency.text.toString()

        if (!TextUtils.isEmpty(frequency)){
            addData("ART Frequency",frequency)
        }



        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Dosage", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.PMTCT.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)


        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.PMTCT.name))
        ft.addToBackStack(null)
        ft.commit()

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.PMTCT.name)
//
//        startActivity(Intent(requireContext(), PatientProfile::class.java))


    }



    private fun addData(key: String, value: String) {
        if (key != ""){
            observationList[key] = value
        }

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