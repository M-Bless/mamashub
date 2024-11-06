package com.kabarak.kabarakmhis.new_designs.chw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.auth.Login
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.helperclass.DbChwPatientData
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.chw.adapter.ChwPatientsListAdapter
import com.kabarak.kabarakmhis.new_designs.chw.viewmodel.ChwPatientListViewModel
import kotlinx.android.synthetic.main.activity_patient_list.*
import kotlinx.android.synthetic.main.activity_patient_list.btnRegisterPatient
import kotlinx.android.synthetic.main.activity_patient_list.no_record
import kotlinx.android.synthetic.main.activity_patient_list.refreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class  PatientList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientListViewModel: ChwPatientListViewModel
    private lateinit var fhirEngine: FhirEngine

    private val viewModel: MainActivityViewModel by viewModels()

    private var formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        title = "Client List"

        fhirEngine = FhirApplication.fhirEngine(this)

        patientListViewModel = ChwPatientListViewModel(application, fhirEngine)
        patientListViewModel = ViewModelProvider(this,
            ChwPatientListViewModel.FhirFormatterClassViewModelFactory
                (application, fhirEngine)
        )[ChwPatientListViewModel::class.java]

        recyclerView = findViewById(R.id.patient_list)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        search_round.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // collapse the view ?
                //menu.findItem(R.id.menu_search).collapseActionView();
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

            getData("FACILITY_TO_FACILITY")
            refreshLayout.isRefreshing = false
        }


        btnRegisterPatient.setOnClickListener {

            startActivity(Intent(this@PatientList, CommunityHealthWorkerForm::class.java))

        }

        radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radio: RadioButton? = findViewById(checkedId)
            when (radio?.text) {
                resources.getString(R.string.facility_to_facility) -> {
                    getData("FACILITY_TO_FACILITY")
                }
                resources.getString(R.string.facility_from_facility) -> {
                    getData("FACILITY_FROM_FACILITY")
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()

        getData("FACILITY_TO_FACILITY")
    }

    private fun getData(selectedValue: String) {

        CoroutineScope(Dispatchers.IO).launch {
            formatter.saveSharedPreference(this@PatientList, "spinnerClientValue", selectedValue)

            val patientList = patientListViewModel.getPatientList()
            showPatients(patientList)


            formatter.saveSharedPreference(this@PatientList, "patientName", "")
            formatter.saveSharedPreference(this@PatientList, "identifier", "")
        }


    }

    private fun showPatients(patientList: List<DbChwPatientData>) {

        FormatterClass().nukeEncounters(this@PatientList)

        CoroutineScope(Dispatchers.Main).launch {

            if (patientList.isEmpty()) {
                no_record.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                no_record.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val dateTimeFormatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val result = patientList.sortedByDescending {
                    (if (it.referralDate != "----/--/--"){

                        try {
                            LocalDate.parse(it.referralDate, dateTimeFormatter)
                        }catch (e: Exception){
                            LocalDate.parse(it.referralDate, dateTimeFormatter1)
                        }

                    }else{
                        it.name
                    }).toString()

                }
                //Get referral date


                val adapter = ChwPatientsListAdapter(result, this@PatientList)
                recyclerView.adapter = adapter
            }

            viewModel.poll()
        }

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