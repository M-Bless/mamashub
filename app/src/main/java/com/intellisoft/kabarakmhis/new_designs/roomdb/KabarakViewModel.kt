package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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

    fun getTittlePatientData(title: String, context: Context) = runBlocking {
        repository.getTittlePatientData(title, context)
    }




}