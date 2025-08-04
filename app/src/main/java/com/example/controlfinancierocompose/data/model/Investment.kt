package com.example.controlfinancierocompose.data.model

data class Investment(
    val id: String,
    val name: String,
    val amount: Double,
    val type: InvestmentType,
    val date: String
)

enum class InvestmentType(val displayName: String) {
    SAVINGS("Ahorro"),
    FIXED_DEPOSIT("Depósito a plazo fijo"),
    STOCKS("Acciones"),
    MUTUAL_FUNDS("Fondos mutuos"),
    REAL_ESTATE("Bienes raíces"),
    CRYPTO("Criptomonedas"),
    OTHER("Otros")
}
