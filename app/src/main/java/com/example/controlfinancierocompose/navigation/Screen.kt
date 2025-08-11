package com.example.controlfinancierocompose.navigation

// Enumeración que representa las pantallas de la aplicación
enum class Screen(val index: Int) {
    DASHBOARD(0),
    ACCOUNTS(1),
    INVESTMENTS(2),
    CALENDAR(3),
    CREDENTIALS(4),
    SETTINGS(5);

    companion object {
        fun fromIndex(index: Int): Screen {
            return values().find { it.index == index } ?: ACCOUNTS
        }
    }
}
