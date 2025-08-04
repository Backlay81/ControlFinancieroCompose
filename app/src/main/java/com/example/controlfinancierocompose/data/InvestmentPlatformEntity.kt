package com.example.controlfinancierocompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investment_platforms")
data class InvestmentPlatformEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = true
)
