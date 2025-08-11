package com.example.controlfinancierocompose.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controlfinancierocompose.data.FinancialRepository
import com.example.controlfinancierocompose.data.InvestmentEntity
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvestmentsViewModel(private val repository: FinancialRepository) : ViewModel() {
    private val _platforms = MutableStateFlow<List<InvestmentPlatformEntity>>(emptyList())
    val platforms: StateFlow<List<InvestmentPlatformEntity>> = _platforms

    private val _investments = MutableStateFlow<List<InvestmentEntity>>(emptyList())
    val investments: StateFlow<List<InvestmentEntity>> = _investments

    private var selectedPlatformId: Long? = null

    init {
        viewModelScope.launch {
            loadPlatforms()
            loadAllInvestments()
        }
    }

    private suspend fun loadPlatforms() {
        _platforms.value = repository.getAllPlatforms()
    }

    
    private suspend fun loadAllInvestments() {
        _investments.value = repository.getAllInvestments()
    }
    fun addPlatform(name: String) {
        viewModelScope.launch {
            repository.insertPlatform(InvestmentPlatformEntity(name = name))
            loadPlatforms()
        }
    }

    fun updatePlatform(platform: InvestmentPlatformEntity) {
        viewModelScope.launch {
            repository.updatePlatform(platform)
            loadPlatforms()
        }
    }

    fun deletePlatform(platform: InvestmentPlatformEntity) {
        viewModelScope.launch {
            repository.deletePlatform(platform)
            loadPlatforms()
            
            // Si la plataforma eliminada era la seleccionada, limpiar inversiones
            if (selectedPlatformId == platform.id) {
                _investments.value = emptyList()
                selectedPlatformId = null
            }
        }
    }

    fun selectPlatform(platformId: Long) {
        selectedPlatformId = platformId
        viewModelScope.launch {
            loadAllInvestments()
        }
    }
    

    fun addInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            repository.insertInvestment(investment)
            loadAllInvestments()
        }
    }

    fun updateInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            repository.updateInvestment(investment)
            loadAllInvestments()
        }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
            loadAllInvestments()
        }
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
