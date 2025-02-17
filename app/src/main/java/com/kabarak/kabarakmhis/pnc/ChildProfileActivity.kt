package com.kabarak.kabarakmhis.pnc


import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.auth.Login
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.NewMainActivity
import com.kabarak.kabarakmhis.new_designs.data_class.DbAncSchedule
import com.kabarak.kabarakmhis.new_designs.data_class.DbIdentifier
import com.kabarak.kabarakmhis.new_designs.data_class.DbObservationFhirData
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.ExpandableRecyclerAdapter
import kotlinx.android.synthetic.main.activity_patient_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer



class ChildProfileActivity : AppCompatActivity() {


    private val formatter = FormatterClass()
    private lateinit var fhirEngine: FhirEngine // Initialize later in onCreate
    private val childProfileViewModel: ChildProfileViewModel by viewModels {
        ChildProfileViewModel.ChildProfileViewModelFactory(application, fhirEngine)
    }
    private val formatterClass = FormatterClass()
    private lateinit var kabarakViewModel: KabarakViewModel
    private val viewModel: MainActivityViewModel by viewModels()

    val modelList = ArrayList<Model>()
    private var isPatient = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_dash)

        fhirEngine = FhirApplication.fhirEngine(this)

        kabarakViewModel = KabarakViewModel(this.application)

        // Get the CHILD_ID from the intent
        val childId = intent.getStringExtra("CHILD_ID")
        val childIdTextView: TextView = findViewById(R.id.text_bn_no)
        val childNameTextView: TextView = findViewById(R.id.text_child_name)
        val childGenderTextView: TextView = findViewById(R.id.text_child_gender)
        val childDobTextView: TextView = findViewById(R.id.text_child_dob) // New TextView for DOB

        


        // Display the ID temporarily while loading
        childIdTextView.text = "Child ID: $childId"

// Display the ID temporarily while loading
        childIdTextView.text = "Child ID: $childId"

        val immunizationButton: TextView = findViewById(R.id.btn_go_to_immunization)
        immunizationButton.setOnClickListener {
            val intent = Intent(this, ImmunizationAdd::class.java)
            intent.putExtra("PATIENT_ID", childId) // Pass the childId as patientId
            startActivity(intent)
        }

        if (childId != null) {
            // Fetch the child's profile
            childProfileViewModel.fetchChildProfile(childId)

            // Observe changes to the child's profile
            childProfileViewModel.childProfile.observe(this, Observer { childProfile ->
                if (childProfile != null) {
                    // Update the UI with the fetched details
                    childNameTextView.text = "Name: ${childProfile.name}"
                    childGenderTextView.text = "Gender: ${childProfile.gender}"


                    childDobTextView.text = "Date of Birth: ${childProfile.dob}" // Update DOB TextView


                } else {
                    // Display a message if the profile couldn't be loaded
                    childNameTextView.text = "Error: Child profile not found."
                }
            })
        } else {
            // Handle the case where no CHILD_ID is provided
            childNameTextView.text = "Error: No Child ID provided."
        }



        title = "Child Details"

        linearCall.setOnClickListener {
        }
        imgViewCall.setOnClickListener {

        }
        getData()

        initData()


    }


    private fun initData() {

        val dbChildProfileList = ArrayList<DbMaternalProfile>()

        // PNC Visit
        val dbMaternalPncVisitList = ArrayList<DbMaternalProfileChild>()
        val dbMaternalProfileChild19 = DbMaternalProfileChild(8.1,resources.getDrawable(R.drawable.childbirth), "ChildBirth")
        val dbMaternalProfileChild20 = DbMaternalProfileChild(8.2,resources.getDrawable(R.drawable.postnatalcare), "Early Identification of Congenital Abnormalities")
        val dbMaternalProfileChild21 = DbMaternalProfileChild(8.3,resources.getDrawable(R.drawable.childbirth), "Reproductive Organs Cancer Screening")
        val dbMaternalProfileChild22 = DbMaternalProfileChild(8.4,resources.getDrawable(R.drawable.chm), "Family Planning")
        val dbMaternalProfileChild23 = DbMaternalProfileChild(8.5, resources.getDrawable(R.drawable.chm), "POSTNATAL CARE - Mother")
        val dbMaternalProfileChild24 = DbMaternalProfileChild(8.6, resources.getDrawable(R.drawable.chm), "POSTNATAL CARE - Child")

        // Child Health Monitoring
        val dbMaternalProfileChild25 = DbMaternalProfileChild(8.7, resources.getDrawable(R.drawable.chm), "Civil Registration")
        val dbMaternalProfileChild26 = DbMaternalProfileChild(8.8, resources.getDrawable(R.drawable.chm), "Reason for Special Care")
        val dbMaternalProfileChild27 = DbMaternalProfileChild(8.9, resources.getDrawable(R.drawable.chm), "Other Problems as Reported by Parent or Guardian")
        val dbMaternalProfileChild28 = DbMaternalProfileChild(9.1, resources.getDrawable(R.drawable.chm), "Record of Baby Teeth Development")
        val dbMaternalProfileChild29 = DbMaternalProfileChild(9.2, resources.getDrawable(R.drawable.chm), "Broad clinical review at first contact below 6 months")
        val dbMaternalProfileChild30 = DbMaternalProfileChild(9.3, resources.getDrawable(R.drawable.chm), "Feeding information from parent/guardian")
        val dbMaternalProfileChild31 = DbMaternalProfileChild(9.4, resources.getDrawable(R.drawable.chm), "Developmental Milestones")
        val dbMaternalProfileChild32 = DbMaternalProfileChild(9.5, resources.getDrawable(R.drawable.chm), "Identification of early eye problems in an infant")
        val dbMaternalProfileChild33 = DbMaternalProfileChild(9.6, resources.getDrawable(R.drawable.chm), "Record of baby’s teeth development")
        val dbMaternalProfileChild34 = DbMaternalProfileChild(9.7, resources.getDrawable(R.drawable.chm), "Reason for Special Care")

        dbMaternalPncVisitList.addAll(listOf(dbMaternalProfileChild19, dbMaternalProfileChild20, dbMaternalProfileChild21, dbMaternalProfileChild22, dbMaternalProfileChild23, dbMaternalProfileChild24, dbMaternalProfileChild25, dbMaternalProfileChild26, dbMaternalProfileChild27, dbMaternalProfileChild28, dbMaternalProfileChild29, dbMaternalProfileChild30, dbMaternalProfileChild31, dbMaternalProfileChild32, dbMaternalProfileChild33, dbMaternalProfileChild34))

        // Immunization
        val immunizationList = ArrayList<DbMaternalProfileChild>()
        val immunization1 = DbMaternalProfileChild(10.1, resources.getDrawable(R.drawable.chm), "BCG Vaccine")
        val immunization2 = DbMaternalProfileChild(10.2, resources.getDrawable(R.drawable.chm), "Polio vaccine")
        val immunization3 = DbMaternalProfileChild(10.3, resources.getDrawable(R.drawable.chm), "IPV (Inactivated Polio Vaccine)")
        val immunization4 = DbMaternalProfileChild(10.4, resources.getDrawable(R.drawable.chm), "Diphtheria/Pertussis/Tetanus/Hepatitis\n" +
                "B/Haemophilus Influenza Type B")
        val immunization5 = DbMaternalProfileChild(10.5, resources.getDrawable(R.drawable.chm), "Pneumococcal Conjugate Vaccine")
        val immunization6 = DbMaternalProfileChild(10.6, resources.getDrawable(R.drawable.chm), "Rotavirus Vaccine")
        val immunization7 = DbMaternalProfileChild(10.7, resources.getDrawable(R.drawable.chm), "Measles Vaccine (MR)")
        val immunization8 = DbMaternalProfileChild(10.8, resources.getDrawable(R.drawable.chm), "Yellow Fever Vaccine")
        val immunization9 = DbMaternalProfileChild(10.9, resources.getDrawable(R.drawable.chm), "Meningococcal Vaccine")
        val immunization10 = DbMaternalProfileChild(11.1, resources.getDrawable(R.drawable.chm), "Other Vaccines")
        immunizationList.addAll(listOf(immunization1, immunization2, immunization3, immunization4, immunization5, immunization6, immunization7, immunization8, immunization9, immunization10))



        val dbMaternalPncVisit = DbMaternalProfile("PNC Visit", dbMaternalPncVisitList, isPatient)
        val dbImmunization = DbMaternalProfile("Immunization", immunizationList, isPatient)

        dbChildProfileList.addAll(listOf(dbMaternalPncVisit, dbImmunization))

        val adapter = ExpandableRecyclerAdapter(dbChildProfileList, this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)


    }

    private fun getData() {

        val status = formatterClass.retrieveSharedPreference(this, "spinnerClientValue")
        isPatient = if (status != null && status != "") {
            status != ReferralTypes.REFERRED.name
        }else{
            false
        }

//        viewModel.poll()

    }



    private fun changeVisibility(){
//        navigateMedicalHistory.visibility = View.GONE
//        linearRow1.visibility = View.GONE
//        linearRow2.visibility = View.GONE
//        linearRow3.visibility = View.GONE
//        linearRow4.visibility = View.GONE
//        linearRow5.visibility = View.GONE
//        linearRow6.visibility = View.GONE
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

    }





    private fun showClientDetails(
        patientLocalName: String,
        patientLocalDob: String?,
        patientLocalIdentifier: String?,
        kinName: String?,
        kinPhone: String?
    ) {

        tvName.text = patientLocalName

        if (patientLocalIdentifier != null) tvANCID.text = patientLocalIdentifier
        if (kinName != null) tvKinName.text = kinName
        if (kinPhone != null) tvKinDetails.text = kinPhone

        if (patientLocalDob != null) {
            val age = "${formatter.calculateAge(patientLocalDob)} years"
            tvAge.text = age
        }

        val edd = formatter.retrieveSharedPreference(this, "edd")
        if (edd != null){



        }


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }





    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, NewMainActivity::class.java)
        startActivity(intent)
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