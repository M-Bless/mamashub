package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.android.synthetic.main.activity_patient_profile.imgViewCall
import kotlinx.android.synthetic.main.activity_patient_profile.linearCall
import kotlinx.android.synthetic.main.activity_patient_profile.tvANCID
import kotlinx.android.synthetic.main.activity_patient_profile.tvAge
import kotlinx.android.synthetic.main.activity_patient_profile.tvKinDetails
import kotlinx.android.synthetic.main.activity_patient_profile.tvKinName
import kotlinx.android.synthetic.main.activity_patient_profile.tvName
import kotlinx.android.synthetic.main.activity_summary.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Summary : AppCompatActivity() {

    private val formatter = FormatterClass()
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        linearCall.setOnClickListener {
            val txtCall = tvKinDetails.text.toString()
            calluser(txtCall)
        }
        imgViewCall.setOnClickListener {
            val txtCall = tvKinDetails.text.toString()
            calluser(txtCall)
        }
    }

    private fun calluser(value: String){
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$value")
        startActivity(dialIntent)
    }

    override fun onStart() {
        super.onStart()
        getSavedData()
    }

    private fun getSavedData() {

        viewModel.poll()

        try {

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@Summary)
                progressDialog.setTitle("Please wait..")
                progressDialog.setMessage("Fetching data...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    val patientLocalName = formatter.retrieveSharedPreference(this@Summary, "patientName")
                    val patientLocalDob = formatter.retrieveSharedPreference(this@Summary, "dob")
                    val patientLocalIdentifier = formatter.retrieveSharedPreference(this@Summary, "identifier")

                    val patientLocalKinName = formatter.retrieveSharedPreference(this@Summary, "kinName")
                    val patientLocalKinPhone = formatter.retrieveSharedPreference(this@Summary, "kinPhone")

                    if (patientLocalName != "") tvName.text = patientLocalName
                    if (patientLocalIdentifier != "") tvANCID.text = patientLocalIdentifier
                    if (patientLocalKinName != "") tvKinName.text = patientLocalKinName
                    if (patientLocalKinPhone != "") tvKinDetails.text = patientLocalKinPhone
                    if (patientLocalDob != null) {
                        val age = "${formatter.calculateAge(patientLocalDob)} years"
                        tvAge.text = age
                    }

                    /**
                     * Get the patient's Summary
                     */

                    //Physical Exam
                    val physicalExam = formatter.retrieveSharedPreference(this@Summary,
                        "${DbResourceViews.PHYSICAL_EXAMINATION.name}_SUMMARY")
                    if (physicalExam != null){
                        val physicalExamPair = formatter.getProgress(physicalExam)
                        val physicalExamStatus = physicalExamPair.first
                        val physicalExamTotal = physicalExamPair.second
                        progressBarPhysical.setProgress(physicalExamStatus, true)
                        progressBarPhysical.max = physicalExamTotal
                        tvPhysicalExam.text = physicalExam
                    }

                    //Present Pregnancy
                    val presentPregnancy = formatter.retrieveSharedPreference(this@Summary,
                        "${DbResourceViews.PRESENT_PREGNANCY.name}_SUMMARY")
                    if (presentPregnancy != null){
                        val presentPregnancyPair = formatter.getProgress(presentPregnancy)
                        val presentPregnancyStatus = presentPregnancyPair.first
                        val presentPregnancyTotal = presentPregnancyPair.second
                        progressBarPresentPregnancy.setProgress(presentPregnancyStatus, true)
                        progressBarPresentPregnancy.max = presentPregnancyTotal
                        tvPresentPregnancy.text = presentPregnancy
                    }

                    //Tetatnus
                    val tetanus = formatter.retrieveSharedPreference(this@Summary,
                        "${DbResourceViews.TETENUS_DIPTHERIA.name}_SUMMARY")
                    if (tetanus != null){
                        val tetanusPair = formatter.getProgress(tetanus)
                        val tetanusStatus = tetanusPair.first
                        val tetanusTotal = tetanusPair.second
                        progressBarTetanusDiphtheria.setProgress(tetanusStatus, true)
                        progressBarTetanusDiphtheria.max = tetanusTotal
                        tvTetanusDiphtheria.text = tetanus
                    }

                    //Malaria
                    val malaria = formatter.retrieveSharedPreference(this@Summary,
                        "${DbResourceViews.MALARIA_PROPHYLAXIS.name}_SUMMARY")
                    if (malaria != null){
                        val malariaPair = formatter.getProgress(malaria)
                        val malariaStatus = malariaPair.first
                        val malariaTotal = malariaPair.second
                        progressBarMalariaProphylaxis.setProgress(malariaStatus, true)
                        progressBarMalariaProphylaxis.max = malariaTotal
                        tvMalariaProphylaxis.text = malaria
                    }

                    //IFAS
                    val ifas = formatter.retrieveSharedPreference(this@Summary,
                        "${DbResourceViews.IFAS.name}_SUMMARY")
                    if (ifas != null){
                        val ifasPair = formatter.getProgress(ifas)
                        val ifasStatus = ifasPair.first
                        val ifasTotal = ifasPair.second
                        progressIFAS.setProgress(ifasStatus, true)
                        progressIFAS.max = ifasTotal
                        tvIfas.text = ifas
                    }


                }.join()
                progressDialog.dismiss()


            }


        }catch (e: Exception) {
            e.printStackTrace()
        }

    }
}