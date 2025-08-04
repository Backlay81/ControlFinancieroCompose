package com.example.controlfinancierocompose.ui.investments

import java.time.LocalDate

data class Investment(
    val id: Long,
    val name: String,
    val type: InvestmentType,
    val amount: Double,
    val initialDate: LocalDate,
    val dueDate: LocalDate? = null,
    val interestRate: Double? = null,
    val currentValue: Double? = null,
    val currency: String = "EUR",
    val notes: String? = null
)

enum class InvestmentType {
    STOCKS, 
    BONDS, 
    MUTUAL_FUNDS, 
    CERTIFICATE_OF_DEPOSIT, 
    REAL_ESTATE, 
    CRYPTOCURRENCY, 
    OTHER;
    
    fun displayName(): String {
        return when(this) {
            STOCKS -> "Acciones"
            BONDS -> "Bonos"
            MUTUAL_FUNDS -> "Fondos de inversión"
            CERTIFICATE_OF_DEPOSIT -> "Depósito a plazo"
            REAL_ESTATE -> "Bienes raíces"
            CRYPTOCURRENCY -> "Criptomonedas"
            OTHER -> "Otros"
        }
    }
}
