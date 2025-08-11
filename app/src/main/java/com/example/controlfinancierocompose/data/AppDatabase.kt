package com.example.controlfinancierocompose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BankEntity::class,
        AccountEntity::class,
        InvestmentPlatformEntity::class,
        InvestmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun accountDao(): AccountDao
    abstract fun platformDao(): PlatformDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "financial_control_database"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
