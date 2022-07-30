package com.intellisoft.kabarakmhis.new_designs.counselling

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
import kotlinx.android.synthetic.main.fragment_couselling2.view.*
import kotlinx.android.synthetic.main.fragment_couselling2.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentCounselling2 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_couselling2, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)


        formatter.saveCurrentPage("2", requireContext())
        getPageDetails()

        handleNavigation()

        return rootView
    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Confirm"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val text1 = formatter.getRadioText(rootView.radioGrpInfantFeeding)
        val text2 = formatter.getRadioText(rootView.radioGrpColostrum)

        val paleMother = formatter.getRadioText(rootView.radioGrpMotherPale)
        val headAche = formatter.getRadioText(rootView.radioGrpHeadAche)
        val vaginalBleeding = formatter.getRadioText(rootView.radioGrpVaginalBleeding)
        val abdominalPain = formatter.getRadioText(rootView.radioGrpAbdominalPain)
        val babyMovement = formatter.getRadioText(rootView.radioGrpBabyMovement)
        val convulsions = formatter.getRadioText(rootView.radioGrpConvulsions)
        val waterBreaking = formatter.getRadioText(rootView.radioGrpWaterBreaking)
        val swollenFace = formatter.getRadioText(rootView.radioGrpSwollenFace)
        val motherFever = formatter.getRadioText(rootView.radioGrpMotherFever)

        addData("Was infant feeding counselling done",text1)
        addData("Was counselling on exclusive breastfeeding and benefits of colostrum done",text2)

        addData("Was the mother pale:",paleMother)
        addData("Does the mother have severe headache:",headAche)
        addData("Did the mother have vaginal bleeding:",vaginalBleeding)
        addData("Did the mother have abdominal pain:",abdominalPain)
        addData("Did the mother have reduced or no movement of the unborn baby:",babyMovement)
        addData("Did the mother have convulsions/fits:",convulsions)
        addData("Was the mother's water breaking:",waterBreaking)
        addData("Did the mother have swollen face and hands:",swollenFace)
        addData("Did the mother have a fever:",motherFever)

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Counselling", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.COUNSELLING.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, formatter.startFragmentConfirm(requireContext(), DbResourceViews.COUNSELLING.name))
        ft.addToBackStack(null)
        ft.commit()

//        formatter.saveToFhir(dbPatientData, requireContext(), DbResourceViews.COUNSELLING.name)
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