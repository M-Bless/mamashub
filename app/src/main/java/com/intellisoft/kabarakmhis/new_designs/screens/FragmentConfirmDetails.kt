package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.kabarakmhis.MainActivity
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.AddPatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.NewMainActivity
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Reference


class FragmentConfirmDetails : Fragment(){

    private val formatter = FormatterClass()

    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var btnSave: Button

    private var encounterDetailsList = ArrayList<DbConfirmDetails>()

    private lateinit var fhirEngine: FhirEngine
    private val viewModel: AddPatientDetailsViewModel by viewModels()
    private lateinit var patientId : String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        rootView = inflater.inflate(R.layout.frament_confirm, container, false)

        patientId = formatter.retrieveSharedPreference(requireContext(), "FHIRID").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        recyclerView = rootView.findViewById(R.id.confirmList);
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        btnSave = rootView.findViewById(R.id.btnSave)

        btnSave.setOnClickListener {

            val encounter = formatter.retrieveSharedPreference(requireContext(), "encounterTitle")
            if (encounter != null){

                val progressDialog= ProgressDialog(requireContext())
                progressDialog.setMessage("Saving...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                CoroutineScope(Dispatchers.IO).launch {

                    delay(1000)

                    if (encounterDetailsList.isNotEmpty()){

                        val encounterId = formatter.generateUuid()
                        val patientReference = Reference("Patient/$patientId")

                        val questionnaireFragment =
                            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
                        val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()

                        val dataQuantityList = ArrayList<QuantityObservation>()
                        val dataCodeList = ArrayList<CodingObservation>()

                        encounterDetailsList.forEach {observation ->

                            val observationList = observation.detailsList
                            observationList.forEach {

                                val code = it.title
                                val value = it.value

                                val checkObservation = formatter.checkObservations(code)
                                if (checkObservation == ""){
                                    //Save as a value string

                                    val codingObservation = CodingObservation(
                                        "8338-6",
                                        code,
                                        value)
                                    dataCodeList.add(codingObservation)

                                }else{
                                    //Save as a value quantity
                                    val quantityObservation = QuantityObservation(
                                        "8338-6",
                                        code,
                                        value,
                                        checkObservation
                                    )
                                    dataQuantityList.add(quantityObservation)

                                }



                            }


                        }

                        viewModel.createEncounter(
                            patientReference,
                            encounterId,
                            questionnaireResponse,
                            dataCodeList,
                            dataQuantityList,
                            encounter
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(requireContext(), "The data has been collected successfully. PLease wait as its being saved.", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }


                        kabarakViewModel.deleteTitleTable(requireContext())

                        if (encounter == DbResourceViews.PATIENT_INFO.name){
                            val intent = Intent(requireContext(), NewMainActivity::class.java)
                            startActivity(intent)
                            activity?.finish()

                        }else{
                            val intent = Intent(requireContext(), PatientProfile::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }



                    }else{
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(requireContext(), "No data to save", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }

                }



            }




        }


        return rootView
    }

    override fun onStart() {
        super.onStart()
        getConfirmDetails()


    }

    private fun updateArguments(){
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "client.json")
    }

    private fun addQuestionnaireFragment(){
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }

    private fun getConfirmDetails() {

        //Get the data from the previous screen
        //Use fhirId, loggedIn User, and title



        encounterDetailsList = kabarakViewModel.getConfirmDetails(requireContext())
        val confirmParentAdapter = ConfirmParentAdapter(encounterDetailsList,requireContext())
        recyclerView.adapter = confirmParentAdapter



    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}