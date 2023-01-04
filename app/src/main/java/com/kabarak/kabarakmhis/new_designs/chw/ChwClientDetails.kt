package com.kabarak.kabarakmhis.new_designs.chw

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.chw.viewmodel.ChwDetailsViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ConfirmParentAdapter
import kotlinx.android.synthetic.main.activity_patient_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.stream.Stream

class ChwClientDetails : AppCompatActivity() {

    private val formatter = FormatterClass()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientDetailsViewModel: ChwDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chw_client_details)

        title = "Client Details"

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            ChwDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[ChwDetailsViewModel::class.java]
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
    }


    override fun onStart() {
        super.onStart()

        getPatientDetails()
    }

    private fun getPatientDetails() {

        try {
            
            patientDetailsViewModel.getPatientData()

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@ChwClientDetails)
                progressDialog.setTitle("Please wait..")
                progressDialog.setMessage("Loading patient details")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {


                    //Get Client Details
                    val observationDataList = patientDetailsViewModel.getPatientData()


                    CoroutineScope(Dispatchers.Main).launch {

                        progressDialog.dismiss()

                        CoroutineScope(Dispatchers.Main).launch {

                            progressDialog.dismiss()

                            if (observationDataList.isNotEmpty()) {
                                no_record.visibility = View.GONE
                                recycler_view.visibility = View.VISIBLE
                            } else {
                                no_record.visibility = View.VISIBLE
                                recycler_view.visibility = View.GONE
                            }

                            val confirmParentAdapter = ConfirmParentAdapter(observationDataList,this@ChwClientDetails)
                            recyclerView.adapter = confirmParentAdapter

                    }

                    }.join()
                }
            }



        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun <T> merge(first: List<T>, second: List<T>, third: List<T>, fourth:List<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        Stream.of(first, second, third, fourth).forEach { item: List<T>? -> list.addAll(item!!) }
        return list
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {


                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}