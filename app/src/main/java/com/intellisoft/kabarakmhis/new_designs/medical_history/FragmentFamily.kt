package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_family.view.*
import kotlinx.android.synthetic.main.fragment_family.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentFamily : Fragment() , AdapterView.OnItemSelectedListener{

    private val formatter = FormatterClass()

    var relationshipList = arrayOf("","Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_family, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.radioGrpTwins.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTwins, true)
                } else {
                    changeVisibility(rootView.linearTwins, false)
                }

            }
        }
        rootView.radioGrpTb.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearTbRelation, true)
                } else {
                    changeVisibility(rootView.linearTbRelation, false)
                }

            }
        }
        rootView.radioGrpSameHouse.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(rootView.linearReferTbScreening, true)
                } else {
                    changeVisibility(rootView.linearReferTbScreening, false)
                }

            }
        }

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()
        initSpinner()

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Save"
        rootView.navigation.btnPrevious.text = "Previous"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        if (rootView.linearTwins.visibility == View.VISIBLE){
            val text = formatter.getRadioText(rootView.radioGrpTwinHistory)
            addData("Twins History",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpTwins)
            addData("Twins History",text)
        }

        if (rootView.linearTbRelation.visibility == View.VISIBLE){
            //Get Name of relative and relationship
            val text = rootView.etRelativeTbName.text.toString()
            addData("Family Member with TB ",text)
            addData("Family Member with TB Relationship",spinnerRshpValue)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpTb)
            addData("Tuberculosis History",text)
        }

        if (rootView.linearReferTbScreening.visibility == View.VISIBLE){
            //Refer for TB Screening
            val text = rootView.etTbScreening.text.toString()
            addData("Tuberculosis Screening",text)
        }else{
            val text = formatter.getRadioText(rootView.radioGrpSameHouse)
            addData("Was the patient sharing residence with TB person? ",text)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Family History", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)

        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.ANTENATAL_PROFILE.name)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.MEDICAL_HISTORY.name))
        ft.addToBackStack(null)
        ft.commit()
//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.MEDICAL_HISTORY.name)
//
//        startActivity(Intent(requireContext(), PatientProfile::class.java))

    }

    private fun addData(key: String, value: String) {
        observationList[key] = value
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

    private fun initSpinner() {


        val kinRshp = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationshipList)
        kinRshp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView.spinnerRelativeTbRshp!!.adapter = kinRshp

        rootView.spinnerRelativeTbRshp.onItemSelectedListener = this


    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.spinnerRelativeTbRshp -> { spinnerRshpValue = rootView.spinnerRelativeTbRshp.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }



}