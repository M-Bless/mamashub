package com.intellisoft.kabarakmhis.new_designs

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.intellisoft.kabarakmhis.helperclass.DbPatientDetails
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.PatientsListAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.new_patient.RegisterNewPatient
import kotlinx.android.synthetic.main.activity_new_main.*
import kotlinx.android.synthetic.main.activity_new_main.no_record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class NewMainActivity : AppCompatActivity()  , AdapterView.OnItemSelectedListener{

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var fhirEngine: FhirEngine

    private lateinit var etSearch : SearchView
    private val viewModel: MainActivityViewModel by viewModels()

    private var formatter = FormatterClass()

    var clientList = arrayOf("","All", "Referred", "Not referred")
    private var spinnerClientValue  = clientList[0]
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

        title = "Client List"

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel = ViewModelProvider(this@NewMainActivity,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, "patientId")
        )[PatientDetailsViewModel::class.java]

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

        getData()

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

                CoroutineScope(Dispatchers.IO).launch {
                    if (!TextUtils.isEmpty(txtSearch)) {
                        patientListViewModel.searchPatientsByName(txtSearch, spinnerClientValue)
                    } else {
                        val patientList = patientListViewModel.getPatientList()
                        showPatients(patientList)
                    }
                }



                return false
            }
        })


        refreshLayout.setOnRefreshListener{

            getData()
            refreshLayout.isRefreshing = false
        }


        initSpinner()
    }

    private fun initSpinner() {

        val filterValue =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, clientList)
        filterValue.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner!!.adapter = filterValue
        mySpinner.onItemSelectedListener = this

    }




    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.mySpinner -> {
                spinnerClientValue = mySpinner.selectedItem.toString()

            }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun showPatients(dbPatientDetailsList: List<DbPatientDetails>) {

        FormatterClass().nukeEncounters(this@NewMainActivity)

        CoroutineScope(Dispatchers.Main).launch {

            if (dbPatientDetailsList.isEmpty()) {
                no_record.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                no_record.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val result = dbPatientDetailsList.sortedBy {
                    (if (it.lastUpdated != "-"){
                        LocalDate.parse(it.lastUpdated, dateTimeFormatter)
                    }else{
                        it.name
                    }).toString()
                }

                val adapter = PatientsListAdapter(result, this@NewMainActivity)
                recyclerView.adapter = adapter
            }


        }

    }

    private fun getData() {


        patientListViewModel.liveSearchedPatients.observe(this) {
            showPatients(it)
        }

        formatter.saveSharedPreference(this@NewMainActivity, "patientName", "")
        formatter.saveSharedPreference(this@NewMainActivity, "identifier", "")

        viewModel.poll()
    }

    override fun onStart() {
        super.onStart()

        getData()
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