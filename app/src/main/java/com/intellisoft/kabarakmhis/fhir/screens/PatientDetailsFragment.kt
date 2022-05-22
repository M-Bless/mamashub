package com.intellisoft.kabarakmhis.fhir.screens

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.databinding.PatientDetailBinding
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.adapters.PatientDetailsRecyclerViewAdapter
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModelFactory


class PatientDetailsFragment : Fragment() {

    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: PatientDetailsFragmentArgs by navArgs()
    private var _binding: PatientDetailBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PatientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    args.patientId
                )
            )
                .get(PatientDetailsViewModel::class.java)
        val adapter = PatientDetailsRecyclerViewAdapter(::onAddScreenerClick, ::onMaternityClick)
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Patient Card"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData(true, args.code)

    }

    private fun onAddScreenerClick() {
        findNavController()
            .navigate(
                PatientDetailsFragmentDirections.actionPatientDetailsToScreenEncounterFragment(
                    args.patientId,"pregnancy-confirmation.json","Pregnancy Confirmation"
                )
            )
    }

    private fun onMaternityClick() {
        findNavController()
            .navigate(
                PatientDetailsFragmentDirections.actionPatientDetailsToScreenEncounterFragment(
                    args.patientId,"maternal-information.json","Maternity Registration"
                )
            )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.menu_patient_edit -> {
//                findNavController()
//                    .navigate(
//                        PatientDetailsFragmentDirections.navigateToEditPatient(
//                            args.patientId
//                        )
//                    )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
