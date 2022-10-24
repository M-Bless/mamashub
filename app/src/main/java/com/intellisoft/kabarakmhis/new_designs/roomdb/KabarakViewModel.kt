package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.intellisoft.kabarakmhis.new_designs.data_class.DbFhirEncounter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientData
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class KabarakViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KabarakRepository

    init {
        val roomDao = KabarakDatabase.getDatabase(application).roomDao()
        repository = KabarakRepository(roomDao)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */


    fun insertInfo(context: Context, dbPatientData: DbPatientData) {
        repository.insertPatientDataInfo(context, dbPatientData)
    }

    fun insertFhirEncounter(context: Context, dbFhirEncounter: DbFhirEncounter){
        repository.insertFhirEncounter(context, dbFhirEncounter)
    }

    fun getFhirEncounter(context: Context, encounterType: String)= runBlocking {
        repository.getFhirEncounter(context, encounterType)
    }

    fun insertCounty(context: Context) {
        repository.insertCounty(context)
    }

    fun getTittlePatientData(title: String, context: Context) = runBlocking {
        repository.getTittlePatientData(title, context)
    }

    fun deleteTypeTable(type: String, context: Context) = runBlocking {
        repository.deleteTypeTable(context, type)
    }
    fun deleteTitleTable(title: String, context: Context) = runBlocking {
        repository.deleteTitleTable(context, title)
    }

    fun getConfirmDetails(context: Context) = runBlocking {
        repository.getConfirmDetails(context)
    }
    fun getAllObservations(context: Context) = runBlocking {
        repository.getAllObservations(context)
    }
    fun deleteTitleTable(context: Context) = runBlocking {
        repository.deleteTitleTable(context)
    }

    fun getCounties() = runBlocking {
        repository.getCounties()
    }
    fun getSubCounty(countyId: Int) = runBlocking {
        repository.getSubCounty(countyId)
    }
    fun getWards(constituencyName: String) = runBlocking {
        repository.getWards(constituencyName)
    }
    fun getCountyNameData(countyName: String) = runBlocking {
        repository.getCountyNameData(countyName)
    }

    fun nukeTable() = runBlocking {
        repository.nukePatientDataTable()
    }



}