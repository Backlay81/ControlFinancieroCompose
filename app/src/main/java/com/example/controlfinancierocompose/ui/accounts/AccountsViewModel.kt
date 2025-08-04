package com.example.controlfinancierocompose.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controlfinancierocompose.data.FinancialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(private val repository: FinancialRepository) : ViewModel() {
    val banks: StateFlow<List<Bank>> = repository.allBanks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBank(bank: Bank) {
        viewModelScope.launch {
            repository.insertBank(bank)
        }
    }

    fun addAccount(bankId: Long, account: Account) {
        viewModelScope.launch {
            val newAccount = account.copy(bankId = bankId)
            repository.insertAccount(newAccount)
        }
    }

    fun removeBank(bankId: Long) {
        viewModelScope.launch {
            repository.deleteBankById(bankId)
        }
    }

    fun removeAccount(bankId: Long, accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccountById(accountId)
        }
    }

    fun updateAccount(bankId: Long, account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun updateBank(bank: Bank) {
        viewModelScope.launch {
            repository.updateBank(bank)
        }
    }
    
    // Factory para crear el ViewModel con dependencias
    class Factory(private val repository: FinancialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountsViewModel::class.java)) {
                return AccountsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
