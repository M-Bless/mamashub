package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.content.Context
import android.provider.SyncStateContract
import android.util.Log
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DbConfirmDetails
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbPatientData
import com.intellisoft.kabarakmhis.new_designs.data_class.DbTypeDataValue
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.County
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.SubCounty
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


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

                    }else{

                        val patientData = roomDao.getPatientData(loggedInUser, type, code, fhirId)
                        if (patientData != null){
                            val id = patientData.id
                            if (id != null) {
                                roomDao.updatePatientRecords(value, id)
                            }
                        }

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

    //get availabale types from the database
    suspend fun getTypes(context: Context):HashSet<String>{

        val fhirId = getSharedPref(context, "FHIRID").toString()
        val loggedInUser = getSharedPref(context, "USERID").toString()
        val encounterTitle = getSharedPref(context, "encounterTitle").toString()

        val detailsList = roomDao.getPatientDataTitle(loggedInUser, encounterTitle, fhirId)
        val hashSet = HashSet<String>()
        detailsList.forEach {
            val type = it.type
            hashSet.add(type)
        }

        return hashSet

    }

    suspend fun getConfirmDetails(context: Context):ArrayList<DbConfirmDetails>{

        val dbConfirmDetailsList = ArrayList<DbConfirmDetails>()

        val fhirId = getSharedPref(context, "FHIRID").toString()
        val loggedInUser = getSharedPref(context, "USERID").toString()
        val encounterTitle = getSharedPref(context, "encounterTitle").toString()

        val typeSetList = getTypes(context)
        typeSetList.forEach { type ->

            val typeDataList = roomDao.getPatientDataTitleType(loggedInUser, encounterTitle, fhirId, type)

            val dbObserveValueList = ArrayList<DbObserveValue>()

            typeDataList.forEach {

                val code = it.code
                val value = it.value

                val dbObserveValue = DbObserveValue(code, value)
                dbObserveValueList.add(dbObserveValue)
            }

            val dbConfirmDetails =  DbConfirmDetails(type, dbObserveValueList)
            dbConfirmDetailsList.add(dbConfirmDetails)
        }

        return dbConfirmDetailsList

    }

    fun insertCounty(context: Context){

        CoroutineScope(Dispatchers.IO).launch { importCSV(context) }


    }

    class CSVReader(var context: Context, var fileName: String) {
        var rows: MutableList<Array<String>> = ArrayList()
        @Throws(IOException::class)
        fun readCSV(): List<Array<String>> {
            val `is`: InputStream = context.assets.open(fileName)
            val isr = InputStreamReader(`is`)
            val br = BufferedReader(isr)
            var line: String? = null
            val csvSplitBy = ","
            br.readLine()
            while (br.readLine().also {

                    if (it != null){
                        line = it
                    }
//                    line = it
            } != null) {
                if (line != null){
                    val row = line!!.split(csvSplitBy).toTypedArray()
                    rows.add(row)
                }

            }
            return rows
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun importCSV(context: Context){
        val csvReaderCounty = CSVReader(context, "county.csv")/* path of local storage (it should be your csv file locatioin)*/
        val csvReaderSubCounty = CSVReader(context, "subcounty.csv")/* path of local storage (it should be your csv file locatioin)*/

        addCsvCounty(csvReaderCounty)
        addCsvSubCounty(csvReaderSubCounty)

    }


    private fun addCsvCounty(csvReaderCounty: CSVReader){
        var rows: List<Array<String>> = ArrayList()
        GlobalScope.launch(Dispatchers.IO) {

            try {
                rows = csvReaderCounty.readCSV()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (i in rows.indices) {

                val countyName = rows[i][1]
                addCounty(countyName)
            }

        }
    }

    private suspend fun addCounty(countyName: String){
        if (!roomDao.checkCounty(countyName)){
            val county = County(countyName)
            roomDao.addCounty(county)
        }
    }
    private suspend fun addSubCounty(
        countyId: String,
        constituencyName: String,
        wardName: String){
        if (!roomDao.checkSubCounty(wardName)){
            val subCounty = SubCounty(countyId, constituencyName, wardName, "")
            roomDao.addSubCounty(subCounty)
        }
    }

    private fun addCsvSubCounty(csvReader: CSVReader){
        var rows: List<Array<String>> = ArrayList()
        GlobalScope.launch(Dispatchers.IO) {

            try {
                rows = csvReader.readCSV()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (i in rows.indices) {

                val countyId = rows[i][1]
                val constituencyName = rows[i][2]
                val ward = rows[i][3]

                addSubCounty(countyId, constituencyName, ward)


            }

        }
    }

    public suspend fun getCounties(): ArrayList<County>{

        var countyDataList = ArrayList<County>()


        val countyList = roomDao.getCounties()
        countyDataList = countyList as ArrayList<County>


        return countyDataList
    }
    public suspend fun getSubCounty(countyId: Int): List<SubCounty>{


        return roomDao.getSubCounty(countyId)
    }
    public suspend fun getWards(constituencyName: String): List<SubCounty>{
        return roomDao.getWards(constituencyName)
    }
    public suspend fun getCountyNameData(countyName: String): County?{
        return roomDao.getCountyNameData(countyName)
    }

}