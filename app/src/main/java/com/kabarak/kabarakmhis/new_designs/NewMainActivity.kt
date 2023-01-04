package com.kabarak.kabarakmhis.new_designs

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.auth.Login
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.kabarak.kabarakmhis.helperclass.DbPatientDetails
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.helperclass.ReferralTypes
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.adapter.PatientsListAdapter
import com.kabarak.kabarakmhis.new_designs.new_patient.RegisterNewPatient
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.activity_new_main.btnRegisterPatient
import kotlinx.android.synthetic.main.activity_new_main.no_record
import kotlinx.android.synthetic.main.activity_new_main.radioGroup
import kotlinx.android.synthetic.main.activity_new_main.refreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class NewMainActivity : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var fhirEngine: FhirEngine

    private lateinit var etSearch : SearchView
    private val viewModel: MainActivityViewModel by viewModels()

    private var formatter = FormatterClass()


    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var kabarakViewModel: KabarakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

        title = "Client List"
        kabarakViewModel = KabarakViewModel(applicationContext as Application)

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

        getData(ReferralTypes.CLIENT_RECORDS.name)

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

                        val patientList = patientListViewModel.getPatientList()
                        //Filtering the list based on the search text
                        val filteredList = patientList.filter {
                            it.name.contains(txtSearch, true)
                        }
                        showPatients(filteredList)
                    } else {
                        val patientList = patientListViewModel.getPatientList()
                        showPatients(patientList)
                    }

//                    if (!TextUtils.isEmpty(txtSearch)) {
//                        patientListViewModel.searchPatientsByName(txtSearch)
//                    } else {
//                        val patientList = patientListViewModel.getPatientList()
//                        showPatients(patientList)
//                    }
                }

                return false
            }
        })


        refreshLayout.setOnRefreshListener{

            getData(ReferralTypes.CLIENT_RECORDS.name)
            refreshLayout.isRefreshing = false
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radio: RadioButton? = findViewById(checkedId)
            when (radio?.text) {
                resources.getString(R.string.clients_records) -> {
                    getData(ReferralTypes.CLIENT_RECORDS.name)
                }
                resources.getString(R.string.referrals_to_facility) -> {
                    getData(ReferralTypes.REFERRED.name)
                }
            }

        }

    }




    private fun showPatients(dbPatientDetailsList: List<DbPatientDetails>) {

        FormatterClass().nukeEncounters(this@NewMainActivity)
        kabarakViewModel.nukeTable()

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

            viewModel.poll()
        }



    }

    private fun getData(selectedValue: String) {

        CoroutineScope(Dispatchers.IO).launch {


            formatter.saveSharedPreference(this@NewMainActivity, "spinnerClientValue", selectedValue)

            val patientList = patientListViewModel.getPatientList()
            showPatients(patientList)

            formatter.saveSharedPreference(this@NewMainActivity, "patientName", "")
            formatter.saveSharedPreference(this@NewMainActivity, "identifier", "")

        }


    }

    override fun onStart() {
        super.onStart()

        getData(ReferralTypes.CLIENT_RECORDS.name)
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