package com.intellisoft.kabarakmhis.new_designs.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_medical_history.*


class MedicalHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_history)

        initSurgicalCheckBox()
        initMedical()
        initOtherAllergy()
        initFamily()

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            val radioButton= findViewById<RadioButton>(checkedId)
            val value = radioButton.text
            if (value == "Yes"){
                tableOtherAllergy.visibility = View.VISIBLE
            }else{
                tableOtherAllergy.visibility = View.GONE
            }

        }
    }

    private fun initFamily() {

        checkboxTwins.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxHistTb.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }

    }

    private fun initOtherAllergy() {

        checkboxAlbendazole.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxAluminium.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxCalcium.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxFolic.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxIron.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxMagnesium.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxSulfa.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxMebendazole.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxPenicilin.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxTDF.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }

    }

    private fun initMedical() {

        checkboxDiabetes.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxHypertension.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxBlood.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxTb.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }

    }

    private fun initSurgicalCheckBox() {

        checkboxNoPast.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxNoKnowledge.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxDilation.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxMyomectomy.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxRemoval.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxOophorectomy.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxSalpi.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }
        checkboxCervical.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){

            }else{

            }
        }

    }
}