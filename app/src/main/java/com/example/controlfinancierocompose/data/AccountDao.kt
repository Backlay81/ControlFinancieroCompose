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
interface AccountDao {
    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE bankId = :bankId ORDER BY name ASC")
    fun getAccountsByBankId(bankId: Long): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE bankId = :bankId ORDER BY name ASC")
    suspend fun getAccountsByBankIdSync(bankId: Long): List<AccountEntity>
    
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Long): AccountEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long
    
    @Update
    suspend fun updateAccount(account: AccountEntity)
    
    @Delete
    suspend fun deleteAccount(account: AccountEntity)
    
    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: Long)
    
    @Query("SELECT SUM(balance) FROM accounts WHERE currency = :currency")
    suspend fun getTotalBalanceByCurrency(currency: String): Double?
}
