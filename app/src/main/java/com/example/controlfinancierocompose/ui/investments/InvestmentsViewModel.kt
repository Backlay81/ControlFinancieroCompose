package com.example.controlfinancierocompose.ui.investments

import androidx.lifecycle.ViewModel
import com.example.controlfinancierocompose.data.model.Investment
import com.example.controlfinancierocompose.data.model.InvestmentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class InvestmentsViewModel : ViewModel() {
    
    // StateFlow para las plataformas
    private val _platforms = MutableStateFlow<List<InvestmentPlatform>>(emptyList())
    val platforms: StateFlow<List<InvestmentPlatform>> = _platforms.asStateFlow()
    
    // StateFlow para las inversiones (mantenemos por compatibilidad)
    private val _investments = MutableStateFlow<List<Investment>>(emptyList())
    val investments: StateFlow<List<Investment>> = _investments.asStateFlow()
    
    // Inicialización con datos de ejemplo
    init {
        _platforms.value = listOf(
            InvestmentPlatform(
                id = 1,
                name = "MyPlatform",
                isActive = true,
                investments = listOf(
                    Investment(
                        id = "1", 
                        name = "Crypto", 
                        amount = 1000.0, 
                        type = InvestmentType.CRYPTO, 
                        date = "2023-01-01",
                        isActive = true
                    ),
                    Investment(
                        id = "2", 
                        name = "Stocks", 
                        amount = 500.0, 
                        type = InvestmentType.STOCKS, 
                        date = "2023-02-15",
                        isActive = true
                    )
                )
            ),
            InvestmentPlatform(
                id = 2,
                name = "OtherPlatform",
                isActive = false,
                investments = listOf(
                    Investment(
                        id = "3", 
                        name = "ETF", 
                        amount = 200.0, 
                        type = InvestmentType.MUTUAL_FUNDS, 
                        date = "2023-03-20",
                        isActive = true
                    )
                )
            )
        )
    }
    
    // Funciones para gestionar plataformas
    
    fun togglePlatformActiveState(platformId: Long) {
        _platforms.update { currentList ->
            currentList.map { platform ->
                if (platform.id == platformId) {
                    platform.copy(isActive = !platform.isActive)
                } else {
                    platform
                }
            }
        }
    }
    
    fun addPlatform(name: String) {
        val newPlatform = InvestmentPlatform(
            id = (_platforms.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            isActive = true
        )
        
        _platforms.update { currentList ->
            currentList + newPlatform
        }
    }
    
    fun updatePlatform(platformId: Long, name: String, isActive: Boolean) {
        _platforms.update { currentList ->
            currentList.map { platform ->
                if (platform.id == platformId) {
                    platform.copy(name = name, isActive = isActive)
                } else {
                    platform
                }
            }
        }
    }
    
    fun deletePlatform(platformId: Long) {
        _platforms.update { currentList ->
            currentList.filter { it.id != platformId }
        }
    }
    
    // Funciones para gestionar inversiones
    
    fun toggleInvestmentActiveState(platformId: Long, investmentId: String) {
        _platforms.update { platformList ->
            platformList.map { platform ->
                if (platform.id == platformId) {
                    // Encontrar la plataforma correcta
                    val updatedInvestments = platform.investments.map { investment ->
                        if (investment.id == investmentId) {
                            // Actualizar el estado de la inversión
                            investment.copy(isActive = !investment.isActive)
                        } else {
                            investment
                        }
                    }
                    platform.copy(investments = updatedInvestments)
                } else {
                    platform
                }
            }
        }
    }
    
    fun addInvestment(platformId: Long, name: String, amount: Double, type: InvestmentType, date: String) {
        val newInvestment = Investment(
            id = UUID.randomUUID().toString(),
            name = name,
            amount = amount,
            type = type,
            date = date,
            isActive = true
        )
        
        _platforms.update { platformList ->
            platformList.map { platform ->
                if (platform.id == platformId) {
                    // Añadir la inversión a la plataforma correcta
                    platform.copy(investments = platform.investments + newInvestment)
                } else {
                    platform
                }
            }
        }
    }
    
    fun updateInvestment(platformId: Long, investmentId: String, name: String, amount: Double, type: InvestmentType, date: String, isActive: Boolean) {
        _platforms.update { platformList ->
            platformList.map { platform ->
                if (platform.id == platformId) {
                    // Encontrar la plataforma correcta
                    val updatedInvestments = platform.investments.map { investment ->
                        if (investment.id == investmentId) {
                            // Actualizar la inversión
                            investment.copy(
                                name = name,
                                amount = amount,
                                type = type,
                                date = date,
                                isActive = isActive
                            )
                        } else {
                            investment
                        }
                    }
                    platform.copy(investments = updatedInvestments)
                } else {
                    platform
                }
            }
        }
    }
    
    fun deleteInvestment(platformId: Long, investmentId: String) {
        _platforms.update { platformList ->
            platformList.map { platform ->
                if (platform.id == platformId) {
                    // Eliminar la inversión de la plataforma correcta
                    platform.copy(investments = platform.investments.filter { it.id != investmentId })
                } else {
                    platform
                }
            }
        }
    }
}
