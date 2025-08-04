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
    
    private val _investments = MutableStateFlow<List<Investment>>(emptyList())
    val investments: StateFlow<List<Investment>> = _investments.asStateFlow()
    
    // Temporarily using in-memory storage
    // In a real app, this would use a Repository and database
    
    fun addInvestment(name: String, amount: Double, type: InvestmentType, date: String) {
        val newInvestment = Investment(
            id = UUID.randomUUID().toString(),
            name = name,
            amount = amount,
            type = type,
            date = date
        )
        
        _investments.update { currentList ->
            currentList + newInvestment
        }
    }
    
    fun updateInvestment(id: String, name: String, amount: Double, type: InvestmentType, date: String) {
        _investments.update { currentList ->
            currentList.map { investment ->
                if (investment.id == id) {
                    investment.copy(
                        name = name,
                        amount = amount,
                        type = type,
                        date = date
                    )
                } else {
                    investment
                }
            }
        }
    }
    
    fun deleteInvestment(id: String) {
        _investments.update { currentList ->
            currentList.filter { it.id != id }
        }
    }
}
