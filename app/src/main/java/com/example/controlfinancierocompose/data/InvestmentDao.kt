package com.example.controlfinancierocompose.data

import androidx.room.*

@Dao
interface InvestmentDao {
    @Query("DELETE FROM investments")
    suspend fun deleteAllInvestments()
    @Query("SELECT * FROM investments WHERE platformId = :platformId")
    suspend fun getInvestmentsForPlatform(platformId: Long): List<InvestmentEntity>

    @Query("SELECT * FROM investments")
    suspend fun getAllInvestments(): List<InvestmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentEntity): Long

    @Update
    suspend fun updateInvestment(investment: InvestmentEntity)

    @Delete
    suspend fun deleteInvestment(investment: InvestmentEntity)
}
