package com.intellisoft.kabarakmhis.new_designs.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.County
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.FhirEncounter
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.PatientData
import com.intellisoft.kabarakmhis.new_designs.roomdb.tables.SubCounty


@Database(
        entities = [
            PatientData::class,
            County::class,
            SubCounty::class,
            FhirEncounter::class,
        ],
        version = 2,
        exportSchema = false)
public abstract class KabarakDatabase : RoomDatabase() {

    abstract fun roomDao() : RoomDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: KabarakDatabase? = null

        fun getDatabase(context: Context): KabarakDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        KabarakDatabase::class.java,
                        "kabarak_database"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}