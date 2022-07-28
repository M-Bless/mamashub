package com.intellisoft.kabarakmhis.new_designs.screens

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel


class FragmentConfirmDetails : Fragment(){

    private val formatter = FormatterClass()

    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.frament_confirm, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        recyclerView = rootView.findViewById(R.id.confirmList);
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)


        return rootView
    }

    override fun onStart() {
        super.onStart()

        getConfirmDetails()
    }

    private fun getConfirmDetails() {

        //Get the data from the previous screen
        //Use fhirId, loggedIn User, and title

        val confirmList = kabarakViewModel.getConfirmDetails(requireContext())
        val confirmParentAdapter = ConfirmParentAdapter(confirmList,requireContext())
        recyclerView.adapter = confirmParentAdapter



    }


}