package com.intellisoft.kabarakmhis.new_designs

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.intellisoft.kabarakmhis.helperclass.DbPatientDetails
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.PatientsListAdapter
import com.intellisoft.kabarakmhis.new_designs.new_patient.RegisterNewPatient
import kotlinx.android.synthetic.main.activity_new_main.*
import kotlinx.android.synthetic.main.activity_new_main.no_record
import kotlinx.android.synthetic.main.activity_previous_pregnancy_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewMainActivity : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var fhirEngine: FhirEngine

    private lateinit var etSearch : SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

        title = "Patient List"

        etSearch = findViewById(R.id.search)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientListViewModel = PatientListViewModel(application, fhirEngine)
        patientListViewModel = ViewModelProvider(this,
            PatientListViewModel.FhirFormatterClassViewModelFactory
                (application, fhirEngine)
        )[PatientListViewModel::class.java]
        recyclerView = findViewById(R.id.patient_list);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        patientListViewModel.liveSearchedPatients.observe(this) {

            showPatients(it)
        }

        btnRegisterPatient.setOnClickListener {
            startActivity(Intent(this, RegisterNewPatient::class.java))
        }

        etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // collapse the view ?
                //menu.findItem(R.id.menu_search).collapseActionView();
                Log.e("queryText", query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // search goes here !!
                // listAdapter.getFilter().filter(query);

                val txtSearch = newText.toString()
                if (!TextUtils.isEmpty(txtSearch)) {
                    patientListViewModel.searchPatientsByName(txtSearch)
                } else {
                    val patientList = patientListViewModel.getPatientList()
                    showPatients(patientList)
                }

                return false
            }
        })


        refreshLayout.setOnRefreshListener{

            CoroutineScope(Dispatchers.IO).launch {  getData() }


            refreshLayout.isRefreshing = false
        }



    }

    private fun showPatients(patientList: List<DbPatientDetails>) {

        FormatterClass().nukeEncounters(this@NewMainActivity)

        if (patientList.isEmpty()) {
            no_record.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            no_record.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            val adapter = PatientsListAdapter(patientList, this@NewMainActivity)
            recyclerView.adapter = adapter
        }


    }

    private fun getData() {




//        val patientData = retrofitCallsFhir.getPatients(this@NewMainActivity)
//        val patientList = patientData.entry
//        if (patientList != null){
//
//            CoroutineScope(Dispatchers.Main).launch {
//
//                if (!patientList.isNullOrEmpty()){
//                    no_record.visibility = View.GONE
//                    recyclerView.visibility = View.VISIBLE
//                }else{
//                    no_record.visibility = View.VISIBLE
//                    recyclerView.visibility = View.GONE
//                }
//
//                val configurationListingAdapter = PatientsAdapter(
//                    patientList,this@NewMainActivity)
//                recyclerView.adapter = configurationListingAdapter
//
//                FormatterClass().nukeEncounters(this@NewMainActivity)
//            }
//
//
//        }
    }



    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch { getData() }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {

                startActivity(Intent(this, Login::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}