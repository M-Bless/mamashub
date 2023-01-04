package com.kabarak.kabarakmhis.new_designs.counselling

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_couselling1.view.*
import kotlinx.android.synthetic.main.fragment_couselling1.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentCounselling1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_couselling1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

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

        if (text1 != "" && text2 != "" && text3 != "" && text4 != "" && text5 != "" && text6 != ""
            && text7 != "" && text8 != "" && text9 != "" && text10 != "" && text11 != "" && text12 != "") {

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

        }else{

           Toast.makeText(requireContext(), "Please make sure you have selected all the fields", Toast.LENGTH_SHORT).show()

        }



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


    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(), DbResourceViews.COUNSELLING.name)
                if (encounterId != null){

                    val dangerSigns = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DANGER_SIGNS.name), encounterId)
                    val dentalHealth = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DENTAL_HEALTH.name), encounterId)
                    val birthPlan = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BIRTH_PLAN.name), encounterId)
                    val rhNegative = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.RH_NEGATIVE.name), encounterId)
                    val eatOneMeal = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EAT_ONE_MEAL.name), encounterId)
                    val eatMoreMeals = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.EAT_MORE_MEALS.name), encounterId)
                    val drinkWater = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DRINK_WATER.name), encounterId)
                    val takeIfas = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TAKE_IFAS.name), encounterId)
                    val avoidWork = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.AVOID_HEAVY_WORK.name), encounterId)
                    val sleepLLIn = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SLEEP_UNDER_LLIN.name), encounterId)
                    val goAnc = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.GO_FOR_ANC.name), encounterId)
                    val nonStrenous = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NON_STRENUOUS_ACTIVITY.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (dangerSigns.isNotEmpty()){
                            val value = dangerSigns[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpDanger.check(R.id.radioYesDanger)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpDanger.check(R.id.radioNoDanger)
                        }
                        if (dentalHealth.isNotEmpty()){
                            val value = dentalHealth[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpDental.check(R.id.radioYesDental)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpDental.check(R.id.radioNoDental)
                        }
                        if (birthPlan.isNotEmpty()){
                            val value = birthPlan[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpBirth.check(R.id.radioYesBirth)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpBirth.check(R.id.radioNoBirth)
                        }
                        if (rhNegative.isNotEmpty()){
                            val value = rhNegative[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpRh.check(R.id.radioYesRh)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpRh.check(R.id.radioNoRh)
                        }
                        if (eatOneMeal.isNotEmpty()){
                            val value = eatOneMeal[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpExtraMeal.check(R.id.radioYesExtraMeal)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpExtraMeal.check(R.id.radioNoExtraMeal)
                        }
                        if (eatMoreMeals.isNotEmpty()){
                            val value = eatMoreMeals[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpEveryDay.check(R.id.radioYesEveryDay)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpEveryDay.check(R.id.radioNoEveryDay)
                        }
                        if (drinkWater.isNotEmpty()){
                            val value = drinkWater[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpWater.check(R.id.radioYesWater)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpWater.check(R.id.radioNoWater)
                        }
                        if (takeIfas.isNotEmpty()){
                            val value = takeIfas[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpIFAS.check(R.id.radioYesIFAS)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpIFAS.check(R.id.radioNoIFAS)
                        }
                        if (avoidWork.isNotEmpty()){
                            val value = avoidWork[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHeavyWork.check(R.id.radioYesHeavyWork)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHeavyWork.check(R.id.radioNoHeavyWork)
                        }
                        if (sleepLLIn.isNotEmpty()){
                            val value = sleepLLIn[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpHeavyLLIN.check(R.id.radioYesLLIN)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpHeavyLLIN.check(R.id.radioNoLLIN)
                        }
                        if (goAnc.isNotEmpty()){
                            val value = goAnc[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpAncVisits.check(R.id.radioYesAncVisits)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpAncVisits.check(R.id.radioNoAncVisits)
                        }
                        if (nonStrenous.isNotEmpty()){
                            val value = nonStrenous[0].value
                            if (value.contains("Yes", ignoreCase = true)) rootView.radioGrpAncNonStrenuous.check(R.id.radioYesNonStrenuous)
                            if (value.contains("No", ignoreCase = true)) rootView.radioGrpAncNonStrenuous.check(R.id.radioNoNonStrenuous)
                        }

                    }



                }


            }

        }catch (e: Exception){
         println(e)
        }

    }

}