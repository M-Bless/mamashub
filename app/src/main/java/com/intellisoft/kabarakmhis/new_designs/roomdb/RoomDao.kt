package com.intellisoft.kabarakmhis.new_designs.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData


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

}