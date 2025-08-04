package com.example.controlfinancierocompose.di

import android.content.Context
import com.example.controlfinancierocompose.data.AppDatabase
import com.example.controlfinancierocompose.data.FinancialRepository

object AppContainer {
    
    fun provideFinancialRepository(context: Context): FinancialRepository {
        val database = AppDatabase.getDatabase(context)
        return FinancialRepository(
            bankDao = database.bankDao(),
            accountDao = database.accountDao()
        )
    }
}
