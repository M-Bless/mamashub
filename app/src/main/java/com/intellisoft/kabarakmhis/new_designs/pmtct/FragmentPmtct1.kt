package com.intellisoft.kabarakmhis.new_designs.pmtct

import android.app.Application
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.*
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_birthplan1.view.*
import kotlinx.android.synthetic.main.fragment_pmtct1.*
import kotlinx.android.synthetic.main.fragment_pmtct1.view.*
import kotlinx.android.synthetic.main.fragment_pmtct1.view.navigation
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class FragmentPmtct1 : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View
    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0
    private var lifeART = false

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    var regimentList = arrayOf("Please select a regimen",
        "Dolutegravir (DTG)",
        "Emtricitabine (FTC)",
        "Tenofovir alafenamide fumarate (TDF)",
        "Zidovudine (AZT)",
        "Lamivudine (3TC)",
        "Nevirapine (NVP)",
        "Efavirenz (EFV)",
        "Other")

    private var dbPMTCTRegimenList = ArrayList<DbPMTCTRegimen>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_pmtct1, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()


        rootView.checkboxART.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                changeVisibility(rootView.linearART, true)
            } else {
                changeVisibility(rootView.linearART, false)
            }
        }
        rootView.btnRegimen.setOnClickListener {

            initDialog()

        }


        handleNavigation()

        return rootView
    }

    private fun initDialog() {

        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.regimen_pmtct)
        val btnSaveRegimen = dialog.findViewById<Button>(R.id.btnSaveRegimen)
        val spinnerRegimen = dialog.findViewById<Spinner>(R.id.spinnerRegimen)
        val etRegimen = dialog.findViewById<EditText>(R.id.etRegimen)

        val etAmount = dialog.findViewById<EditText>(R.id.etAmount)
        val etDosageAmount = dialog.findViewById<EditText>(R.id.etDosageAmount)
        val etFrequency = dialog.findViewById<EditText>(R.id.etFrequency)

        val radioGroupDosage = dialog.findViewById<RadioGroup>(R.id.radioGroupDosage)

        val radioDosage1 = dialog.findViewById<RadioButton>(R.id.radioDosage1)
        val radioDosage2 = dialog.findViewById<RadioButton>(R.id.radioDosage2)

        val rowDosage = dialog.findViewById<TableRow>(R.id.rowDosage)

        radioGroupDosage.setOnCheckedChangeListener { _, checkedId ->
            //Check for null
            if (checkedId != -1) {
                val radio: RadioButton = dialog.findViewById(checkedId)
                val radioText = radio.text.toString()
                etDosageAmount.setText(radioText)
            }

        }


        spinnerRegimen.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, regimentList)
        spinnerRegimen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Check if the user selected the last item
                if (position == regimentList.size - 1) {
                    //If the user selected the last item, show the dialog to add a new item
                    etRegimen.isEnabled = true
                    etFrequency.isEnabled = true
                } else {
                    //If the user selected an item from the list, disable the edit text
                    if (position != 0){
                        val spinnerSelectedRegimen = regimentList[position]
                        etRegimen.setText(spinnerSelectedRegimen)
                    }
                    etRegimen.isEnabled = false
                    etFrequency.isEnabled = false
                }
                radioGroupDosage.clearCheck()
                etDosageAmount.setText("")
                //Get the selected position and add Dosages
                when (position) {

                    1 -> {
                        rowDosage.visibility = View.VISIBLE
                        radioDosage1.text = "50"
                        radioDosage2.text = "100"
                    }
                    2 -> {
                        rowDosage.visibility = View.GONE
                        etDosageAmount.setText("50")
                    }
                    3 -> {
                        rowDosage.visibility = View.GONE
                        etDosageAmount.setText("300")
                    }
                    4 -> {
                        rowDosage.visibility = View.GONE
                        etDosageAmount.setText("150")
                    }
                    5 -> {
                        rowDosage.visibility = View.VISIBLE
                        radioDosage1.text = "150"
                        radioDosage2.text = "300"
                    }
                    6 -> {
                        rowDosage.visibility = View.GONE
                        etDosageAmount.setText("200")
                    }
                    7 -> {
                        rowDosage.visibility = View.VISIBLE
                        radioDosage1.text = "200"
                        radioDosage2.text = "400"
                    }
                    else -> {
                        rowDosage.visibility = View.GONE
                        etDosageAmount.setText("")
                    }
                }

                Log.e("Spinner", "Selected item: $position")

            }

        }

        btnSaveRegimen.setOnClickListener {

            val amount = etAmount.text.toString()
            val dosageAmount = etDosageAmount.text.toString()
            val frequency = etFrequency.text.toString()

            val selectedRegimen = etRegimen.text.toString()
            if(!TextUtils.isEmpty(selectedRegimen) && !TextUtils.isEmpty(amount)
                && !TextUtils.isEmpty(dosageAmount) && !TextUtils.isEmpty(frequency)){

                val dbPMTCTRegimen = DbPMTCTRegimen(selectedRegimen, amount.toDouble(), dosageAmount.toDouble(), frequency)
                dbPMTCTRegimenList.add(dbPMTCTRegimen)

                showRegimenList()

                dialog.dismiss()
            }else{

                if (TextUtils.isEmpty(selectedRegimen)) etRegimen.error = "Please enter a regimen"
                if (TextUtils.isEmpty(amount)) etAmount.error = "Please enter an amount"
                if (TextUtils.isEmpty(dosageAmount)) etDosageAmount.error = "Please enter a dosage amount"
                if (TextUtils.isEmpty(frequency)) etFrequency.error = "Please enter a frequency"
            }

        }

        dialog.show()
    }

    private fun showRegimenList() {

        val regimenPmtctAdapter = RegimenPmtctAdapter(dbPMTCTRegimenList, requireContext())
        recyclerView.adapter = regimenPmtctAdapter

    }



    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }



    private fun saveData() {

        val dbDataList = ArrayList<DbDataList>()
        val errorList = ArrayList<String>()

        val interventionGivenList = ArrayList<String>()
        if (rootView.checkboxART.isChecked) interventionGivenList.add("ART for life")
        if (rootView.checkboxVL.isChecked) interventionGivenList.add("Viral Load Sample")

        if (interventionGivenList.isNotEmpty()){
            addData("Intervention Given", interventionGivenList.joinToString(separator = ", "), DbObservationValues.INTERVENTION_GIVEN.name)
        }else{
            errorList.add("Intervention Given is required")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_INTERVENTION_GIVEN.name, DbResourceType.Observation.name ,label)
            dbDataList.add(data)

        }
        observationList.clear()

        val checkBoxList = ArrayList<String>()
        if(rootView.linearART.visibility == View.VISIBLE){

            dbPMTCTRegimenList.forEach {

                val regimen = it.regimen
                val amount = it.amount
                val dosageAmount = it.dosage
                val frequency = it.frequency

                val regimenData = "Regimen: $regimen, Amount: $amount, Dosage: $dosageAmount, Frequency: $frequency"
                checkBoxList.add(regimenData)

            }

            if (checkBoxList.isNotEmpty()){
                addData("Regimen Given", checkBoxList.joinToString(separator = ", "), DbObservationValues.REGIMEN.name)
            }


            for (items in observationList){

                val key = items.key
                val dbObservationLabel = observationList.getValue(key)

                val value = dbObservationLabel.value
                val label = dbObservationLabel.label

                val data = DbDataList(key, value, DbSummaryTitle.B_ART_FOR_LIFE.name, DbResourceType.Observation.name, label)
                dbDataList.add(data)

            }
            observationList.clear()

        }

        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PMTCT.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPmtct2())
            ft.addToBackStack(null)
            ft.commit()


        }else{
            formatter.showErrorDialog(errorList, requireContext())
        }




    }



    private fun addData(key: String, value: String, codeLabel: String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
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

    override fun onStart() {
        super.onStart()
        showRegimenList()
        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(), DbResourceViews.PMTCT.name)
                if (encounterId != null){

                    val interventionGiven = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.INTERVENTION_GIVEN.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (interventionGiven.isNotEmpty()){
                            val value = interventionGiven[0].value
                            val valueList = formatter.stringToWords(value)
                            val checkBoxList = ArrayList<CheckBox>()
                            checkBoxList.addAll(listOf(
                                rootView.checkboxART,
                                rootView.checkboxVL))
                            valueList.forEach {

                                val valueData = it.replace(" ", "").toLowerCase()
                                checkBoxList.forEach { checkBox ->
                                    if (checkBox.text.toString().replace(" ", "").lowercase() == valueData){
                                        checkBox.isChecked = true
                                    }
                                }

                            }

                        }

                    }

                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}