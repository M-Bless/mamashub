package com.intellisoft.kabarakmhis.new_designs.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.AntenatalProfile
import com.intellisoft.kabarakmhis.new_designs.antenatal_profile.AntenatalProfileView
import com.intellisoft.kabarakmhis.new_designs.birth_plan.BirthPlan
import com.intellisoft.kabarakmhis.new_designs.birth_plan.BirthPlanView
import com.intellisoft.kabarakmhis.new_designs.clinical_notes.ClinicalNotesList
import com.intellisoft.kabarakmhis.new_designs.malaria_propylaxis.MalariaProphylaxis
import com.intellisoft.kabarakmhis.new_designs.matenal_serology.MaternalSerology
import com.intellisoft.kabarakmhis.new_designs.medical_history.MedicalHistory
import com.intellisoft.kabarakmhis.new_designs.medical_history.MedicalSurgicalHistoryView
import com.intellisoft.kabarakmhis.new_designs.new_patient.PatientDetailsView
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExamination
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExaminationList
import com.intellisoft.kabarakmhis.new_designs.physical_examination.PhysicalExaminationView
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyAdd
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyList
import com.intellisoft.kabarakmhis.new_designs.present_pregnancy.PresentPregnancyView
import com.intellisoft.kabarakmhis.new_designs.preventive_service.PreventiveService
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancy
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancyList
import com.intellisoft.kabarakmhis.new_designs.previous_pregnancy.PreviousPregnancyView
import com.intellisoft.kabarakmhis.new_designs.weight_monitoring.WeightMonitoringChart
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatientProfile : AppCompatActivity() {

    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)


        title = "Patient Details"


        linearLayoutCall.setOnClickListener {

            val txtPhone = tvKinDetails.text.toString()
            if (!TextUtils.isEmpty(txtPhone)){
                calluser(txtPhone)
            }

        }

        navigate()

    }

    private fun calluser(value: String){
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$value")
        startActivity(dialIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        getPatientData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPatientData() {

        CoroutineScope(Dispatchers.IO).launch { RetrofitCallsFhir().getPatientEncounters(this@PatientProfile) }

        val patientName = formatter.retrieveSharedPreference(this, "name")
        val dob = formatter.retrieveSharedPreference(this, "dob")

        //Calculate Age
        val age = "${formatter.calculateAge(dob.toString())} years"

        val kinRelationShip = formatter.retrieveSharedPreference(this, "kinRelationShip")
        val kinName = formatter.retrieveSharedPreference(this, "kinName")
        val kinPhoneNumber = formatter.retrieveSharedPreference(this, "kinPhoneNumber")

        if (kinRelationShip != null && kinName != null && kinPhoneNumber != null){

            val kinDetails = "$kinName \n$kinPhoneNumber"
            tvKinDetails.text = kinPhoneNumber
            tvKinName.text = kinName

        }

        tvName.text = patientName
        tvAge.text = age

    }

    fun navigatePreviousPreg(view: View) {
        val intent = Intent(this, PreviousPregnancy::class.java)
        startActivity(intent)
    }

    fun navigateAntenatalProfile(view: View) {
        val intent = Intent(this, AntenatalProfile::class.java)
        startActivity(intent)
    }
    fun medicalHistory(view: View) {
        val intent = Intent(this, MedicalHistory::class.java)
        startActivity(intent)
    }
    fun navigatePhysical(view: View) {
        val intent = Intent(this, PhysicalExamination::class.java)
        startActivity(intent)
    }

    fun navigatePatientDetails(view: View) {
//        val intent = Intent(this, PatientDetailsView::class.java)
//        startActivity(intent)
    }

    private fun navigate() {
        navigateClinicalNotes.setOnClickListener { startActivity(Intent(this, ClinicalNotesList::class.java))}
        navigateBirthPlan.setOnClickListener { startActivity(Intent(this, BirthPlanView::class.java))}
        navigatePresent.setOnClickListener { startActivity(Intent(this, PresentPregnancyList::class.java))}
        navigatePhysicalExam.setOnClickListener { startActivity(Intent(this, PhysicalExaminationList::class.java))}
        navigateWeight.setOnClickListener { startActivity(Intent(this, WeightMonitoringChart::class.java))}
        navigateMedicalHistory.setOnClickListener { startActivity(Intent(this, MedicalSurgicalHistoryView::class.java))}
        navigatePreviousPreg.setOnClickListener { startActivity(Intent(this, PreviousPregnancyList::class.java))}
        navigateAntenatal.setOnClickListener { startActivity(Intent(this, AntenatalProfileView::class.java))}

        navigatePreventiveService.setOnClickListener { startActivity(Intent(this, PreventiveService::class.java))}
        navigateMalariaProphylaxis.setOnClickListener { startActivity(Intent(this, MalariaProphylaxis::class.java))}
        navigateMaternalSerology.setOnClickListener { startActivity(Intent(this, MaternalSerology::class.java))}

    }
}