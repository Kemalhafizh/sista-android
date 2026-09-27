package com.sultanagung1.sista.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sultanagung1.sista.data.local.dao.FeatureUsageDao
import com.sultanagung1.sista.data.local.dao.UserDao
import com.sultanagung1.sista.data.local.entity.FeatureUsageEntity
import com.sultanagung1.sista.data.local.entity.UserEntity

// Version 2 (FASE 76.4) adds feature_usage. Until then this database was never
// opened anywhere in the app (0 callers of getInstance), so no installed device
// has a version-1 file to migrate; the destructive fallback below cannot lose
// real data on this upgrade.
@Database(entities = [UserEntity::class, FeatureUsageEntity::class], version = 2, exportSchema = false)
abstract class SistaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun featureUsageDao(): FeatureUsageDao

    companion object {
        @Volatile
        private var INSTANCE: SistaDatabase? = null

        fun getInstance(context: Context): SistaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SistaDatabase::class.java,
                    "sista_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
