package com.intellisoft.kabarakmhis.new_designs.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObservationValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import kotlinx.android.synthetic.main.activity_medical_history.*


class MedicalHistory : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private var hashSetFamily = HashSet<String>()
    private val hashSetSurgicalHistory = HashSet<String>()
    private val hashSetMedicalHistory = HashSet<String>()


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

        btnSave.setOnClickListener {

            saveMedicalHistory()

        }
    }

    private fun saveMedicalHistory() {

        val dbObservationValueList = HashSet<DbObservationData>()

        if (hashSetSurgicalHistory.isNotEmpty()){
            val dbObservationData = DbObservationData("Surgical History", hashSetSurgicalHistory)
            dbObservationValueList.add(dbObservationData)
        }
        if (hashSetMedicalHistory.isNotEmpty()){
            val dbObservationData = DbObservationData("Medical History", hashSetMedicalHistory)
            dbObservationValueList.add(dbObservationData)
        }
        if (hashSetFamily.isNotEmpty()){
            val dbObservationData = DbObservationData("Family History", hashSetFamily)
            dbObservationValueList.add(dbObservationData)
        }

        val dbObservationValue = DbObservationValue(dbObservationValueList)


        retrofitCallsFhir.createFhirEncounter(this, dbObservationValue, DbResourceViews.MEDICAL_HISTORY.name)



    }

    private fun initFamily() {

        checkboxTwins.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetFamily.add(selectedValue)
            }else{
                hashSetFamily.remove(selectedValue)
            }
        }
        checkboxHistTb.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetFamily.add(selectedValue)
            }else{
                hashSetFamily.remove(selectedValue)
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
                hashSetMedicalHistory.add(selectedValue)
            }else{
                hashSetMedicalHistory.remove(selectedValue)
            }
        }
        checkboxHypertension.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetMedicalHistory.add(selectedValue)
            }else{
                hashSetMedicalHistory.remove(selectedValue)
            }
        }
        checkboxBlood.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetMedicalHistory.add(selectedValue)
            }else{
                hashSetMedicalHistory.remove(selectedValue)
            }
        }
        checkboxTb.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetMedicalHistory.add(selectedValue)
            }else{
                hashSetMedicalHistory.remove(selectedValue)
            }
        }

    }

    private fun initSurgicalCheckBox() {


        checkboxNoPast.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxNoKnowledge.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxDilation.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxMyomectomy.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxRemoval.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxOophorectomy.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxSalpi.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }
        checkboxCervical.setOnCheckedChangeListener { buttonView, isChecked ->

            val selectedValue = buttonView?.text.toString()
            if (isChecked){
                hashSetSurgicalHistory.add(selectedValue)
            }else{
                hashSetSurgicalHistory.remove(selectedValue)
            }
        }

    }


}