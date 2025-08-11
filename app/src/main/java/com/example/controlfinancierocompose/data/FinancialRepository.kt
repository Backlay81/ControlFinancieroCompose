package com.example.controlfinancierocompose.data

import com.example.controlfinancierocompose.ui.accounts.Account
import com.example.controlfinancierocompose.ui.accounts.Bank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FinancialRepository(
    private val bankDao: BankDao,
    private val accountDao: AccountDao,
    private val platformDao: PlatformDao,
    private val investmentDao: InvestmentDao
) {
    // Bank operations
    // Combinamos los flujos de bancos y cuentas
    val allBanks: Flow<List<Bank>> = kotlinx.coroutines.flow.combine(
        bankDao.getAllBanks(),
        accountDao.getAllAccounts()
    ) { banks, accounts ->
        // Para cada banco, encontramos sus cuentas asociadas
        banks.map { bankEntity ->
            // Filtramos las cuentas que pertenecen a este banco
            val bankAccounts = accounts.filter { it.bankId == bankEntity.id }
            // Creamos el objeto Bank con sus cuentas
            bankEntity.toBank(bankAccounts)
        }
    }
    
    suspend fun getBankWithAccounts(bankId: Long): Bank? {
        val bank = bankDao.getBankById(bankId) ?: return null
        val bankAccounts = accountDao.getAccountsByBankIdSync(bankId)
        return bank.toBank(bankAccounts)
    }
    
    suspend fun insertBank(bank: Bank): Long {
        return bankDao.insertBank(bank.toBankEntity())
    }
    
    suspend fun updateBank(bank: Bank) {
        bankDao.updateBank(bank.toBankEntity())
    }
    
    suspend fun deleteBank(bank: Bank) {
        bankDao.deleteBank(bank.toBankEntity())
    }
    
    suspend fun deleteBankById(bankId: Long) {
        bankDao.deleteBankById(bankId)
    }
    
    // Account operations
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts().map { accountEntities ->
        accountEntities.map { accountEntity ->
            accountEntity.toAccount()
        }
    }
    
    fun getAccountsByBankId(bankId: Long): Flow<List<Account>> {
        return accountDao.getAccountsByBankId(bankId).map { accountEntities ->
            accountEntities.map { accountEntity ->
                accountEntity.toAccount()
            }
        }
    }
    
    suspend fun getAccountById(accountId: Long): Account? {
        return accountDao.getAccountById(accountId)?.toAccount()
    }
    
    suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account.toAccountEntity())
    }
    
    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toAccountEntity())
    }
    
    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account.toAccountEntity())
    }
    
    suspend fun deleteAccountById(accountId: Long) {
        accountDao.deleteAccountById(accountId)
    }
    
    suspend fun getTotalBalanceByCurrency(currency: String): Double {
        return accountDao.getTotalBalanceByCurrency(currency) ?: 0.0
    }
    
    // Operaciones de plataformas de inversión
    suspend fun getAllPlatforms(): List<InvestmentPlatformEntity> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            platformDao.getAllPlatforms()
        }
    }
    
    suspend fun insertPlatform(platform: InvestmentPlatformEntity): Long {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            platformDao.insertPlatform(platform)
            platform.id ?: -1
        }
    }
    
    suspend fun updatePlatform(platform: InvestmentPlatformEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            platformDao.updatePlatform(platform)
        }
    }
    
    suspend fun deletePlatform(platform: InvestmentPlatformEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            platformDao.deletePlatform(platform)
        }
    }
    
    // Operaciones de inversiones
    suspend fun getInvestmentsForPlatform(platformId: Long): List<InvestmentEntity> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            investmentDao.getInvestmentsForPlatform(platformId)
        }
    }

    suspend fun getAllInvestments(): List<InvestmentEntity> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            investmentDao.getAllInvestments()
        }
    }
    
    suspend fun insertInvestment(investment: InvestmentEntity): Long {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val id = investmentDao.insertInvestment(investment)
            id
        }
    }
    
    suspend fun updateInvestment(investment: InvestmentEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            investmentDao.updateInvestment(investment)
        }
    }
    
    suspend fun deleteInvestment(investment: InvestmentEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            investmentDao.deleteInvestment(investment)
        }
    }
    
    // Función para agregar datos de prueba (útil para depuración)
    suspend fun addSampleData() {
        // La función queda vacía para no añadir datos de ejemplo
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // No se añaden bancos ni cuentas de ejemplo
        }
    }
}
