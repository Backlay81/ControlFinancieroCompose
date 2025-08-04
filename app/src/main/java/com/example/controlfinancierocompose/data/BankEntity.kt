package com.example.controlfinancierocompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.controlfinancierocompose.ui.accounts.Bank

@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = true
)

// Extension function to convert from entity to domain model
fun BankEntity.toBank(accounts: List<AccountEntity> = emptyList()): Bank {
    return Bank(
        id = id,
        name = name,
        accounts = accounts.map { it.toAccount() },
        isActive = isActive
    )
}

// Extension function to convert from domain model to entity
fun Bank.toBankEntity(): BankEntity {
    return BankEntity(
        id = id,
        name = name,
        isActive = isActive
    )
}
