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
    fun addPatientDataInfo(patientData: PatientData)

    @Query("SELECT EXISTS (SELECT 1 FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND type =:type AND code =:code)")
    fun checkPatientDataInfo(userId: String, type: String, code:String, fhirId: String): Boolean

    @Query("DELETE FROM patient_data WHERE id =:id")
    fun deletePatientDataInfo(id: Int)

    @Query("SELECT * from patient_data WHERE id =:id")
    fun getPatientDataInfo(id: Int): PatientData?

    @Query("SELECT * from patient_data WHERE fhirId =:fhirId AND type =:type")
    fun getPatientInfoType(type: String, fhirId: String): PatientData?

    @Query("SELECT * from patient_data WHERE id =:id AND type =:type")
    fun getPatientInfoTypeId(id: Int, type: String): PatientData?


    @Query("SELECT * from patient_data WHERE fhirId =:fhirId AND title =:title")
    fun getPatientInfoTitle(title: String, fhirId: String): List<PatientData>

    @Query("SELECT * from patient_data WHERE fhirId =:fhirId")
    fun getPatientInfoFhir(fhirId: String): List<PatientData>

    @Query("DELETE FROM patient_data")
    fun nukePatientDataTable()

    @Query("DELETE FROM patient_data WHERE title =:title")
    fun deleteTitleTable(title: String)

    @Query("UPDATE patient_data SET value =:value WHERE id =:id")
    fun updatePatientRecords(value: String, id: Int)

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND type =:type AND code =:code")
    fun getPatientData(userId: String, type: String, code:String, fhirId: String): PatientData?

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND title =:title")
    fun getPatientDataTitle(userId: String, title: String, fhirId: String): List<PatientData>

    @Query("SELECT * FROM patient_data WHERE loggedUserId =:userId AND fhirId =:fhirId AND title =:title AND type =:type")
    fun getPatientDataTitleType(userId: String, title: String, fhirId: String, type:String): List<PatientData>


    //County
    @Query("SELECT EXISTS (SELECT 1 FROM county WHERE countyName =:countyName)")
    fun checkCounty(countyName: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM sub_county WHERE ward =:ward)")
    fun checkSubCounty(ward: String): Boolean

    @Insert
    fun addCounty(county: County)

    @Insert
    fun addSubCounty(subCounty: SubCounty)

    @Query("SELECT * from county")
    fun getCounties(): List<County>

    @Query("SELECT * from county WHERE countyName =:countyName")
    fun getCountyNameData(countyName: String): County

    @Query("SELECT * from sub_county WHERE countyId =:countyId")
    fun getSubCounty(countyId: Int): List<SubCounty>

    @Query("SELECT * from sub_county WHERE constituencyName =:constituencyName")
    fun getWards(constituencyName: String): List<SubCounty>





}