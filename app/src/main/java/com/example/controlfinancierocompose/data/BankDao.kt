package com.example.controlfinancierocompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("DELETE FROM banks")
    suspend fun deleteAllBanks()
    @Query("SELECT * FROM banks ORDER BY name ASC")
    fun getAllBanks(): Flow<List<BankEntity>>
    
    @Query("SELECT * FROM banks WHERE id = :bankId")
    suspend fun getBankById(bankId: Long): BankEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: BankEntity): Long
    
    @Update
    suspend fun updateBank(bank: BankEntity)
    
    @Delete
    suspend fun deleteBank(bank: BankEntity)
    
    @Query("DELETE FROM banks WHERE id = :bankId")
    suspend fun deleteBankById(bankId: Long)
}
