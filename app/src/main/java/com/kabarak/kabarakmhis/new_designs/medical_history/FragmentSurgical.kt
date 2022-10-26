package com.kabarak.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import kotlinx.android.synthetic.main.fragment_antenatal1.view.*
import kotlinx.android.synthetic.main.fragment_surgical.view.*
import kotlinx.android.synthetic.main.fragment_surgical.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class FragmentSurgical : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_surgical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

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

    override fun onStart() {
        super.onStart()

        getPastData()
    }

    private fun getPastData() {

        try {
            //Check if encounter id is available

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(
                    requireContext(),
                    DbResourceViews.MEDICAL_HISTORY.name
                )

                if (encounterId != null){

                    val checkBoxList = mutableListOf<CheckBox>()
                    checkBoxList.addAll(listOf(
                        rootView.checkboxNoPast,
                        rootView.checkboxNoKnowledge,
                        rootView.checkboxDilation,
                        rootView.checkboxMyomectomy,
                        rootView.checkboxRemoval,
                        rootView.checkboxOophorectomy,
                        rootView.checkboxSalpi,
                        rootView.checkboxCervical,))

                    val surgicalHist = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.SURGICAL_HISTORY.name), encounterId)
                    val otherGynecological = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.OTHER_GYNAECOLOGICAL_HISTORY.name), encounterId)
                    val otherSurgery = patientDetailsViewModel.getObservationsPerCodeFromEncounter(formatter.getCodes(DbObservationValues.OTHER_SURGICAL_HISTORY.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if(surgicalHist.isNotEmpty()){

                            val surgicalHistValue = surgicalHist[0].value
                            val surgicalList = formatter.stringToWords(surgicalHistValue)
                            surgicalList.forEach {

                                checkBoxList.forEach { checkBox ->
                                    if (checkBox.text.toString() == it){
                                        checkBox.isChecked = true
                                    }
                                }

                            }

                        }
                        if (otherGynecological.isNotEmpty()){
                            val otherGynecologicalValue = otherGynecological[0].value
                            rootView.etOtherGyna.setText(otherGynecologicalValue)
                        }
                        if (otherSurgery.isNotEmpty()){
                            val otherSurgeryValue = otherSurgery[0].value
                            rootView.etOtherSurgery.setText(otherSurgeryValue)
                        }


                    }


                }

            }

        }catch (e: Exception){
            println(e)
        }

    }



    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }

    private fun saveData() {

        val surgicalHistoryList = ArrayList<String>()

        if (rootView.checkboxNoPast.isChecked){
            surgicalHistoryList.clear()
            surgicalHistoryList.add(rootView.checkboxNoPast.text.toString())
        }

        if (rootView.checkboxNoKnowledge.isChecked){
            surgicalHistoryList.clear()
            surgicalHistoryList.add(rootView.checkboxNoKnowledge.text.toString())
        }

        if(rootView.linearSurgeries.visibility == View.VISIBLE){

            if (rootView.checkboxDilation.isChecked) surgicalHistoryList.add(rootView.checkboxDilation.text.toString())
            if (rootView.checkboxMyomectomy.isChecked) surgicalHistoryList.add(rootView.checkboxMyomectomy.text.toString())
            if (rootView.checkboxRemoval.isChecked) surgicalHistoryList.add(rootView.checkboxRemoval.text.toString())
            if (rootView.checkboxOophorectomy.isChecked) surgicalHistoryList.add(rootView.checkboxOophorectomy.text.toString())
            if (rootView.checkboxSalpi.isChecked) surgicalHistoryList.add(rootView.checkboxSalpi.text.toString())
            if (rootView.checkboxCervical.isChecked) surgicalHistoryList.add(rootView.checkboxCervical.text.toString())

            val otherGyna = rootView.etOtherGyna.text.toString()
            val otherSurgeries = rootView.etOtherSurgery.text.toString()

            if (!TextUtils.isEmpty(otherGyna)){
                addData("Other Gynecological Procedures", otherGyna, DbObservationValues.OTHER_GYNAECOLOGICAL_HISTORY.name)
            }
            if (!TextUtils.isEmpty(otherSurgeries)){
                addData("Other Surgeries",otherSurgeries, DbObservationValues.OTHER_SURGICAL_HISTORY.name)
            }


        }
        addData("Surgical History", surgicalHistoryList.joinToString(separator = ","), DbObservationValues.SURGICAL_HISTORY.name)



        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_SURGICAL_HISTORY.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }

        if (dbDataList.size > 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentMedical())
            ft.addToBackStack(null)
            ft.commit()

        }else{

            val errorList = ArrayList<String>()
            errorList.add("Please select at least one option")
            formatter.showErrorDialog(errorList, requireContext())
        }



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