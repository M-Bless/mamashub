package com.intellisoft.kabarakmhis.new_designs.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.County
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.SubCounty


@Dao
interface RoomDao {

    @Insert
    suspend fun addPatientDataInfo(patientData: PatientData)

    @Query("SELECT EXISTS (SELECT 1 FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND type =:type AND code =:code)")
    suspend fun checkPatientDataInfo(userId: String, type: String, code:String, fhirId: String): Boolean

    @Query("DELETE FROM patient_data WHERE id =:id")
    suspend fun deletePatientDataInfo(id: Int)

    @Query("SELECT * from patient_data WHERE id =:id")
    suspend fun getPatientDataInfo(id: Int): PatientData?

    @Query("SELECT * from patient_data WHERE fhirId =:fhirId AND type =:type")
    suspend fun getPatientInfoType(type: String, fhirId: String): PatientData?

    @Query("SELECT * from patient_data WHERE id =:id AND type =:type")
    suspend fun getPatientInfoTypeId(id: Int, type: String): PatientData?


    @Query("SELECT * from patient_data WHERE fhirId =:fhirId AND title =:title")
    suspend fun getPatientInfoTitle(title: String, fhirId: String): List<PatientData>

    @Query("SELECT * from patient_data WHERE fhirId =:fhirId")
    suspend fun getPatientInfoFhir(fhirId: String): List<PatientData>

    @Query("DELETE FROM patient_data")
    suspend fun nukePatientDataTable()

    @Query("UPDATE patient_data SET value =:value WHERE id =:id")
    suspend fun updatePatientRecords(value: String, id: Int)

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND type =:type AND code =:code")
    suspend fun getPatientData(userId: String, type: String, code:String, fhirId: String): PatientData?

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND title =:title")
    suspend fun getPatientDataTitle(userId: String, title: String, fhirId: String): List<PatientData>

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND title =:title AND type =:type")
    suspend fun getPatientDataTitleType(userId: String, title: String, fhirId: String, type:String): List<PatientData>


    //County
    @Query("SELECT EXISTS (SELECT 1 FROM county WHERE countyName =:countyName)")
    suspend fun checkCounty(countyName: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM sub_county WHERE ward =:ward)")
    suspend fun checkSubCounty(ward: String): Boolean

    @Insert
    suspend fun addCounty(county: County)

    @Insert
    suspend fun addSubCounty(subCounty: SubCounty)

    @Query("SELECT * from county")
    suspend fun getCounties(): List<County>

    @Query("SELECT * from county WHERE countyName =:countyName")
    suspend fun getCountyNameData(countyName: String): County

    @Query("SELECT * from sub_county WHERE countyId =:countyId")
    suspend fun getSubCounty(countyId: Int): List<SubCounty>

    @Query("SELECT * from sub_county WHERE constituencyName =:constituencyName")
    suspend fun getWards(constituencyName: String): List<SubCounty>





}