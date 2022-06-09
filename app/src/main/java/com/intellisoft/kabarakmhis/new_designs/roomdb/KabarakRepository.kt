package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.content.Context
import android.util.Log
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbTypeDataValue
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData

import kotlinx.coroutines.*


class KabarakRepository(private val roomDao: RoomDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    private fun getSharedPref(context: Context, sharedKey: String): String? {
        return FormatterClass().retrieveSharedPreference(context, sharedKey)
    }


    fun insertPatientDataInfo(context: Context, dbPatientData: DbPatientData){

        CoroutineScope(Dispatchers.IO).launch {

            val loggedInUser = getSharedPref(context, "USERID").toString()
            val fhirId = getSharedPref(context, "FHIRID").toString()

            val title = dbPatientData.title
            val dbDataDetailsList = dbPatientData.data

            for (dbDataDetails in dbDataDetailsList){

                val dbDataList = dbDataDetails.data_value

                for (dbData in dbDataList){

                    val code = dbData.code
                    val value = dbData.value
                    val type = dbData.type
                    val identifier = dbData.identifier


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

    suspend fun getTittlePatientData(title:String, context: Context):ArrayList<DbTypeDataValue>{

        val fhirId = getSharedPref(context, "FHIRID").toString()
        val dataList = roomDao.getPatientInfoTitle(title, fhirId)

        val dbObservationDataList = ArrayList<DbTypeDataValue>()

        for (items in dataList){


            val id = items.id
            val type = items.type


            if (id != null){

                val patientData = roomDao.getPatientInfoTypeId(id, type)

                if (patientData != null){

                    val typeData = patientData.type

                    if (type == typeData){

                        val code = patientData.code
                        val value = patientData.value

                        val dbObserveValue = DbObserveValue(code, value)
                        val dbTypeDataValue = DbTypeDataValue(type, dbObserveValue)

                        dbObservationDataList.add(dbTypeDataValue)

                    }

                }

            }


        }





        Log.e("-0-0-0-0 ", dbObservationDataList.toString())

        return dbObservationDataList
    }





}