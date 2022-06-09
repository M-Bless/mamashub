package com.intellisoft.kabarakmhis.new_designs.roomdb.tables

import android.service.carrier.CarrierIdentifier
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_data")
data class PatientData(

        var code: String ,
        var value: String ,
        var type: String ,
        var identifier: String ,
        var title: String ,
        var fhirId: String ,
        var loggedUserId: String ,
        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}
