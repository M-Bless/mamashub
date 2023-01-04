package com.kabarak.kabarakmhis.new_designs.roomdb.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_data")
data class PatientData(

        var code: String ,
        var value: String ,
        var type: String ,
        var codeLabel: String ,
        var identifier: String ,
        var title: String ,
        var fhirId: String ,
        var loggedUserId: String ,
        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}
@Entity(tableName = "county")
data class County(

        var countyName: String ,

        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}
@Entity(tableName = "sub_county")
data class SubCounty(

        var countyId: String ,
        var constituencyName: String ,
        var ward: String ,
        var alias: String

        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}
@Entity(tableName = "fhir_encounter")
data class FhirEncounter(

        var encounterId: String ,
        var encounterName: String ,
        var encounterType: String ,
        var encounterDate: String ,
        var fhirId: String ,
        var loggedUserId: String ,

        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}
