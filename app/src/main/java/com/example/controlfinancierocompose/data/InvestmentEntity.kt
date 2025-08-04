package com.example.controlfinancierocompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.controlfinancierocompose.data.model.InvestmentType

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platformId: Long,
    val name: String,
    val amount: Double,
    val type: InvestmentType,
    val date: String,
    val isActive: Boolean = true
)
