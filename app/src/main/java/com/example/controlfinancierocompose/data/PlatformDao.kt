package com.example.controlfinancierocompose.data

import androidx.room.*

@Dao
interface PlatformDao {
    @Query("DELETE FROM investment_platforms")
    suspend fun deleteAllPlatforms()
    @Query("SELECT * FROM investment_platforms")
    fun getAllPlatforms(): kotlinx.coroutines.flow.Flow<List<InvestmentPlatformEntity>>

    @Insert
    suspend fun insertPlatform(platform: InvestmentPlatformEntity): Long

    @Update
    suspend fun updatePlatform(platform: InvestmentPlatformEntity)

    @Delete
    suspend fun deletePlatform(platform: InvestmentPlatformEntity)
}
