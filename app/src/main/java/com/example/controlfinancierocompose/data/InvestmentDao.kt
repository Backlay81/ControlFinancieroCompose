package com.example.controlfinancierocompose.data

import androidx.room.*

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments WHERE platformId = :platformId")
    fun getInvestmentsForPlatform(platformId: Long): List<InvestmentEntity>

    @Insert
    fun insertInvestment(investment: InvestmentEntity)

    @Update
    fun updateInvestment(investment: InvestmentEntity)

    @Delete
    fun deleteInvestment(investment: InvestmentEntity)
}
