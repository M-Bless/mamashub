package com.kabarak.kabarakmhis.pnc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.databinding.FragmentChildListBinding
import timber.log.Timber

class ChildListFragment : Fragment() {

    private var _binding: FragmentChildListBinding? = null
    private val binding get() = _binding!!

    private lateinit var fhirEngine: FhirEngine
    private lateinit var childListViewModel: ChildListViewModel
    private lateinit var childListAdapter: ChildListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FhirEngine using FhirEngineProvider (similar to Activity)
        fhirEngine = FhirEngineProvider.getInstance(requireContext())

        // Retrieve the identifier passed from the activity
        val identifier = arguments?.getString("identifier") ?: ""

        // Initialize ViewModel and pass the identifier
        childListViewModel = ViewModelProvider(
            this,
            ChildListViewModel.ChildListViewModelFactory(requireActivity().application, fhirEngine, identifier)
        ).get(ChildListViewModel::class.java)

        // Set up RecyclerView
        setupRecyclerView()

        // Observe LiveData for children list
        childListViewModel.liveSearchedChildren.observe(viewLifecycleOwner) { children ->
            Timber.d("Submitting ${children.size} child records")
            childListAdapter.submitList(children) // Update RecyclerView

            // Notify activity to update the visibility of no_record view
            (activity as? ChildViewActivity)?.setNoRecordVisibility(children.isNotEmpty())
        }

        // Optionally observe child count and update UI
        childListViewModel.childCount.observe(viewLifecycleOwner) { count ->
            binding.childCountTextView.text = "Total Children: $count"
        }
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = binding.recyclerViewChildList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        childListAdapter = ChildListAdapter { childItem ->
            onChildItemClicked(childItem) // Handle child item click
        }
        recyclerView.adapter = childListAdapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun onChildItemClicked(childItem: ChildListViewModel.ChildItem) {
        Timber.d("Child clicked: ${childItem.name}")
        // TODO: Navigate to child details screen
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
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
