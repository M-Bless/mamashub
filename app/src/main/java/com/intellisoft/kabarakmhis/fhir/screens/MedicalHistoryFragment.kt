package com.intellisoft.kabarakmhis.fhir.screens

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.databinding.FragmentMedicalHistoryBinding
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.adapters.MaternityDetails
import com.intellisoft.kabarakmhis.fhir.data.Constants.MILK_CONSENT_FORM
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.kabarakmhis.helperclass.Steps

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MilkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MedicalHistoryFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: MedicalHistoryFragmentArgs by navArgs()

    private lateinit var unit: String
    private var _binding: FragmentMedicalHistoryBinding? = null
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
        _binding = FragmentMedicalHistoryBinding.inflate(inflater, container, false)
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
        val steps =
            Steps(fistIn = "Medical & Surgical History", lastIn = "Order Confirmation", secondButton = false)
        unit = "Medical & Surgical History"
        val adapter = MaternityDetails(
                this::consentFormClick,
                steps,
                true)

        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = unit
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData(false, args.code)
//        (activity as MainActivity).setDrawerEnabled(false)

    }
//    private fun encounterClick(encounter: EncounterItem) {
//        findNavController().navigate(
//            PhysicalExamFragmentDirections.navigateToObservations(
//                args.patientId,
//                encounter.id
//            )
//        )
//    }



    private fun consentFormClick() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                MILK_CONSENT_FORM
            )
        }
        findNavController().navigate(
            PhysicalExamFragmentDirections.navigateToScreening(
                args.patientId, "medical-surgical-history.json", "Physical Examination"
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hidden_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
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