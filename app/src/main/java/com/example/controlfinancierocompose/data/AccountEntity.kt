package com.example.controlfinancierocompose.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.controlfinancierocompose.ui.accounts.Account

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bankId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankId: Long,
    val holder: String,
    val name: String,
    val balance: Double,
    val currency: String = "USD",
    val type: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)

// Extension function to convert from entity to domain model
fun AccountEntity.toAccount(): Account {
    return Account(
        id = id,
        bankId = bankId,
        holder = holder,
        name = name,
        balance = balance,
        currency = currency,
        type = type,
        notes = notes,
        isActive = isActive
    )
}

// Extension function to convert from domain model to entity
fun Account.toAccountEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        bankId = bankId,
        holder = holder,
        name = name,
        balance = balance,
        currency = currency,
        type = type,
        notes = notes,
        isActive = isActive
    )
}
