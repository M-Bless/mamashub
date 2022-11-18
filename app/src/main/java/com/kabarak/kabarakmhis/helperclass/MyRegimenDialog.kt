package com.kabarak.kabarakmhis.helperclass

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.kabarak.kabarakmhis.R

class MyRegimenDialog : DialogFragment() {

    lateinit var btn_login: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);

        val rootView = inflater.inflate(R.layout.regimen_pmtct, container, false)

        btn_login = rootView.findViewById(R.id.btnSaveRegimen)
        btn_login.setOnClickListener {
            Log.e("------" ,"Clicked")
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}