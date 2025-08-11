package com.example.controlfinancierocompose.di

import android.content.Context
import com.example.controlfinancierocompose.data.AppDatabase
import com.example.controlfinancierocompose.data.FinancialRepository
import com.example.controlfinancierocompose.data.InvestmentDao
import com.example.controlfinancierocompose.data.PlatformDao

object AppContainer {
    
    fun provideFinancialRepository(context: Context): FinancialRepository {
        val database = AppDatabase.getDatabase(context)
        return FinancialRepository(
            bankDao = database.bankDao(),
            accountDao = database.accountDao(),
            platformDao = database.platformDao(),
            investmentDao = database.investmentDao()
        )
    }
    
    fun providePlatformDao(context: Context): PlatformDao {
        return AppDatabase.getDatabase(context).platformDao()
    }
    
    fun provideInvestmentDao(context: Context): InvestmentDao {
        return AppDatabase.getDatabase(context).investmentDao()
    }
}
