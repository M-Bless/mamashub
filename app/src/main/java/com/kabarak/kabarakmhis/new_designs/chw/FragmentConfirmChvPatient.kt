package com.kabarak.kabarakmhis.new_designs.chw

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.chw.viewmodel.AddChwPatientViewModel
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import com.kabarak.kabarakmhis.new_designs.screens.FragmentConfirmDetails
import kotlinx.coroutines.*
import org.hl7.fhir.r4.model.QuestionnaireResponse


class FragmentConfirmChvPatient : Fragment(){

    private val formatter = FormatterClass()

    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var btnSave: Button

    private lateinit var fhirEngine: FhirEngine
    private val viewModel: AddChwPatientViewModel by viewModels()

    private lateinit var btnEditDetails: Button

    private var encounterDetailsList = ArrayList<DbConfirmDetails>()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {



        rootView = inflater.inflate(R.layout.frament_confirm, container, false)

        btnEditDetails = rootView.findViewById(R.id.btnEditDetails)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        btnEditDetails.setOnClickListener {
            //Go back to the previous activity
            requireActivity().onBackPressed()
        }

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

            val progressDialog= ProgressDialog(requireContext())
            progressDialog.setMessage("Saving...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            CoroutineScope(Dispatchers.Main).launch {

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    var clientName = ""
                    var dob = ""
                    val id = formatter.retrieveSharedPreference(requireContext(), "FHIRID").toString()


                    val dataQuantityList = ArrayList<QuantityObservation>()
                    val dataCodeList = ArrayList<CodingObservation>()

                    var  actionTaken= ""
                    val reasonCodeList = ArrayList<DbReasonCodeData>()
                    val mainReasonList = ArrayList<DbSupportingInfo>()

                    var providerName = ""

                    val observationList = kabarakViewModel.getAllObservations(requireContext())
                    observationList.forEach {

                        val title = it.title
                        val codeLabel = it.codeLabel
                        val value = it.value


                        if(codeLabel == DbObservationValues.COMMUNITY_HEALTH_UNIT.name){

                        }else if (codeLabel == DbObservationValues.COMMUNITY_HEALTH_LINK.name){

                        }else if (codeLabel == DbObservationValues.REFERRAL_REASON.name){
                            val codeValue = formatter.getCodes(codeLabel)
                            val dbObservationValues = DbReasonCodeData(value, codeValue, title)
                            reasonCodeList.add(dbObservationValues)
                        }else if (codeLabel == DbObservationValues.MAIN_PROBLEM.name){
                            val dbSupportingInfo = DbSupportingInfo(value, title)
                            mainReasonList.add(dbSupportingInfo)
                        }else if (codeLabel == DbObservationValues.CHW_INTERVENTION_GIVEN.name){
                            val codeValue = formatter.getCodes(codeLabel)
                            val dbObservationValues = DbReasonCodeData(value, codeValue, title)
                            reasonCodeList.add(dbObservationValues)
                        }else if (codeLabel == DbObservationValues.CHW_COMMENTS.name){
                            val codeValue = formatter.getCodes(codeLabel)
                            val dbObservationValues = DbReasonCodeData(value, codeValue, title)
                            reasonCodeList.add(dbObservationValues)
                        }else if (codeLabel == DbObservationValues.OFFICER_NAME.name){
                            providerName = value
                        }else if (codeLabel == DbObservationValues.ACTION_TAKEN.name){
                            actionTaken = value
                        }


                        when (codeLabel) {

                            //Patient resource
                            DbObservationValues.CLIENT_NAME.name -> { clientName = value }
                            DbObservationValues.DATE_OF_BIRTH.name -> { dob = value }

                            //Patient Observation resource
                            else ->{

                                val codeValue = formatter.getCodes(codeLabel)
                                val checkObservation = formatter.checkObservations(title)
                                if (codeValue != ""){

                                    if (checkObservation == ""){
                                        //Save as a value string

                                        val codingObservation = CodingObservation(
                                            codeValue,
                                            title,
                                            value)
                                        dataCodeList.add(codingObservation)

                                    }else{
                                        //Save as a value quantity
                                        val quantityObservation = QuantityObservation(
                                            codeValue,
                                            title,
                                            value,
                                            checkObservation
                                        )
                                        dataQuantityList.add(quantityObservation)

                                    }

                                }

                            }

                        }


                    }

                    val kmflCode = formatter.retrieveSharedPreference(requireContext(), "kmhflCode").toString()
                    val facilityName = formatter.retrieveSharedPreference(requireContext(), "facilityName").toString()
                    val userId = formatter.retrieveSharedPreference(requireContext(), "USERID").toString()

                    val dbChwDetails = DbChwDetails(userId, kmflCode)
                    val dbClinicianDetails = DbClinicianDetails("NURSE", providerName)

                    val dbLocation = DbLocation(facilityName, kmflCode)

                    val dbServiceReferralRequest = DbServiceReferralRequest(
                        "",
                        "",
                        "",
                        "",
                        reasonCodeList,
                        mainReasonList,
                        actionTaken,
                        dbChwDetails,
                        dbClinicianDetails,
                        dbLocation)


                    val dbChwData = DbChwData(id, clientName, dob, dataQuantityList, dataCodeList)

                    val encounterId = formatter.generateUuid()

                    val questionnaireFragment = childFragmentManager.findFragmentByTag(
                        QUESTIONNAIRE_FRAGMENT_TAG
                    ) as QuestionnaireFragment
                    savePatient(
                        dbChwData,
                        questionnaireFragment.getQuestionnaireResponse(),
                        encounterId,
                        dbServiceReferralRequest
                    )

                }.join()

                delay(7000)

                progressDialog.dismiss()

                val intent = Intent(requireContext(), PatientList::class.java)
                startActivity(intent)
                requireActivity().finish()

            }

        }


        return rootView
    }

    override fun onStart() {
        super.onStart()

        getConfirmDetails()
    }

    private fun savePatient(
        dbPatientFhirInformation: DbChwData,
        questionnaireResponse: QuestionnaireResponse,
        encounterId: String,
        dbServiceReferralRequest: DbServiceReferralRequest
    ) {
        viewModel.savePatient(
            dbPatientFhirInformation,
            questionnaireResponse,
            encounterId,
            dbServiceReferralRequest
            )
    }

    private fun updateArguments(){
        requireArguments()
            .putString(FragmentConfirmDetails.QUESTIONNAIRE_FILE_PATH_KEY, "patient.json")
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