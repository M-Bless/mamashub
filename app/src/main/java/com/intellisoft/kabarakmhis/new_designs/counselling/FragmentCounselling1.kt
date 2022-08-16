package com.intellisoft.kabarakmhis.new_designs.counselling

import android.app.Application
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
import com.intellisoft.kabarakmhis.helperclass.DbObservationLabel
import com.intellisoft.kabarakmhis.helperclass.DbObservationValues
import com.intellisoft.kabarakmhis.helperclass.DbSummaryTitle
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_couselling1.view.*
import kotlinx.android.synthetic.main.fragment_couselling1.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*


class FragmentCounselling1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_couselling1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

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
        val dbDataList = ArrayList<DbDataList>()

        val text12 = formatter.getRadioText(rootView.radioGrpDanger)
        val text11 = formatter.getRadioText(rootView.radioGrpDental)
        val text10 = formatter.getRadioText(rootView.radioGrpBirth)
        val text9 = formatter.getRadioText(rootView.radioGrpRh)
        val text8 = formatter.getRadioText(rootView.radioGrpExtraMeal)
        val text7 = formatter.getRadioText(rootView.radioGrpEveryDay)
        val text6 = formatter.getRadioText(rootView.radioGrpWater)
        val text5 = formatter.getRadioText(rootView.radioGrpIFAS)
        val text4 = formatter.getRadioText(rootView.radioGrpHeavyWork)
        val text3 = formatter.getRadioText(rootView.radioGrpHeavyLLIN)
        val text2 = formatter.getRadioText(rootView.radioGrpAncVisits)
        val text1 = formatter.getRadioText(rootView.radioGrpAncNonStrenuous)

        addData("Danger signs couselling",text12, DbObservationValues.DANGER_SIGNS.name)
        addData("Dental health for mother",text11, DbObservationValues.DENTAL_HEALTH.name)
        addData("Birth plan counselling",text10, DbObservationValues.BIRTH_PLAN.name)
        addData("Rh negative counselling",text9, DbObservationValues.RH_NEGATIVE.name)
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_COUNSELLING_DONE.name, DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()

        addData("Advised to eat one extra meal a day",text8, DbObservationValues.EAT_ONE_MEAL.name)
        addData("Advised to eat at least 5 of the 10 food groups everyday",text7, DbObservationValues.EAT_MORE_MEALS.name)
        addData("Advised to drink plenty of water",text6, DbObservationValues.DRINK_WATER.name)
        addData("Advised to take IFAS",text5, DbObservationValues.TAKE_IFAS.name)
        addData("Advised to avoid heavy work, rest more",text4, DbObservationValues.AVOID_HEAVY_WORK.name)
        addData("advised to sleep under a long lasting insecticidal net (LLIN)",text3, DbObservationValues.SLEEP_UNDER_LLIN.name)
        addData("Advised to go for ANC visit as soon as possible and attend 8 times during pregnancy",text2, DbObservationValues.GO_FOR_ANC.name)
        addData("Advised against no-strenuous exercise",text1, DbObservationValues.NON_STRENUOUS_ACTIVITY.name)
        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.B_PREGNANCY_COUNSELLING.name, DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()



        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.COUNSELLING.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentCounselling2())
        ft.addToBackStack(null)
        ft.commit()

    }



    private fun addData(key: String, value: String,codeLabel: String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
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