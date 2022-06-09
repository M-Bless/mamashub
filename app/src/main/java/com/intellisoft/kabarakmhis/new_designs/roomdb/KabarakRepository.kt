package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.content.Context
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientData
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData

import kotlinx.coroutines.*


class KabarakRepository(private val roomDao: RoomDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    private fun getSharedPref(context: Context, sharedKey: String): String? {
        return FormatterClass().retrieveSharedPreference(context, sharedKey)
    }


    fun insertPatientDataInfo(context: Context, list: List<DbPatientData>){

        CoroutineScope(Dispatchers.IO).launch {

            val loggedInUser = getSharedPref(context, "USERID").toString()
            val fhirId = getSharedPref(context, "FHIRID").toString()

            for (items in list){

                val title = items.title
                val type = items.type
                val dataList = items.dataList
                for (data in dataList){

                    val code = data.code
                    val value = data.value
                    val identifier = data.identifier

                    val isData = roomDao.checkPatientDataInfo(loggedInUser, type, code, fhirId)
                    if (!isData){

                        val patientData = PatientData(code, value, type, identifier, title, fhirId, loggedInUser)
                        roomDao.addPatientDataInfo(patientData)
                    }


                }


            }

        }

    }

    suspend fun nukePatientDataTable(){
        return roomDao.nukePatientDataTable()
    }

    suspend fun getTittlePatientData(title:String, context: Context):List<PatientData>{

        val fhirId = getSharedPref(context, "FHIRID").toString()
        return roomDao.getPatientInfoTitle(title, fhirId)
    }





}