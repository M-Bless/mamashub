package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import kotlinx.android.synthetic.main.fragment_family.view.*


class FragmentFamily : Fragment() {

    private val formatter = FormatterClass()

    var educationList = arrayOf("Dont know level of Education", "No Education", "Primary School", "Secondary School", "Higher Education")
    var relationshipList = arrayOf("Spouse", "Child (B)", "Child (R)", "Parent", "Relatives")
    private var spinnerEducationValue = educationList[0]
    private var spinnerRshpValue  = relationshipList[0]

    private lateinit var rootView: View


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_family, container, false)

        rootView.btnNext.setOnClickListener {

            startActivity(Intent(requireContext(), MedicalSurgicalHistoryView::class.java))


        }

        formatter.saveCurrentPage("3", requireContext())
        getPageDetails()


        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }



}