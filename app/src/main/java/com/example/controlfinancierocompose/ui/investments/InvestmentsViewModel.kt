package com.example.controlfinancierocompose.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controlfinancierocompose.data.FinancialRepository
import com.example.controlfinancierocompose.data.InvestmentEntity
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InvestmentsViewModel(private val repository: FinancialRepository) : ViewModel() {
    private val _platforms = MutableStateFlow<List<InvestmentPlatformEntity>>(emptyList())
    val platforms: StateFlow<List<InvestmentPlatformEntity>> = _platforms.asStateFlow()

    private val _investments = MutableStateFlow<List<InvestmentEntity>>(emptyList())
    val investments: StateFlow<List<InvestmentEntity>> = _investments.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.allPlatforms.collectLatest { _platforms.value = it }
        }
        viewModelScope.launch {
            repository.allInvestments.collectLatest { _investments.value = it }
        }
    }

    fun addPlatform(platform: InvestmentPlatformEntity) = viewModelScope.launch {
        repository.insertPlatform(platform)
    }

    fun updatePlatform(platform: InvestmentPlatformEntity) = viewModelScope.launch {
        repository.updatePlatform(platform)
    }

    fun deletePlatform(platform: InvestmentPlatformEntity) = viewModelScope.launch {
        repository.deletePlatform(platform)
    }

    fun addInvestment(investment: InvestmentEntity) = viewModelScope.launch {
        repository.insertInvestment(investment)
    }

    fun updateInvestment(investment: InvestmentEntity) = viewModelScope.launch {
        repository.updateInvestment(investment)
    }

    fun deleteInvestment(investment: InvestmentEntity) = viewModelScope.launch {
        repository.deleteInvestment(investment)
    }

    class Factory(private val repository: FinancialRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InvestmentsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InvestmentsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
