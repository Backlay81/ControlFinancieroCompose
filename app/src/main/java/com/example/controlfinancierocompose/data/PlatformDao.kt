package com.example.controlfinancierocompose.data

import androidx.room.*

@Dao
interface PlatformDao {
    @Query("DELETE FROM investment_platforms")
    suspend fun deleteAllPlatforms()
    @Query("SELECT * FROM investment_platforms")
    fun getAllPlatforms(): List<InvestmentPlatformEntity>

    @Insert
    fun insertPlatform(platform: InvestmentPlatformEntity)

    @Update
    fun updatePlatform(platform: InvestmentPlatformEntity)

    @Delete
    fun deletePlatform(platform: InvestmentPlatformEntity)
}
