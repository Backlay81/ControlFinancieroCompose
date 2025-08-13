package com.example.controlfinancierocompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var isUnlocked: Boolean by mutableStateOf(false)
}
