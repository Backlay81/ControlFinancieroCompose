
package com.example.controlfinancierocompose.ui.accounts

import kotlinx.serialization.Serializable

@Serializable
data class Bank(
    val id: Long,
    val name: String,
    val accounts: List<Account> = emptyList(),
    val isActive: Boolean = true
)

@Serializable
data class Account(
    val id: Long,
    val bankId: Long,
    val holder: String,
    val name: String,
    val balance: Double,
    val currency: String = "USD",
    val type: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)
