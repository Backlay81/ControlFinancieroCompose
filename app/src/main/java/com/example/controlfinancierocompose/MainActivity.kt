package com.example.controlfinancierocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.controlfinancierocompose.ui.accounts.AccountsScreen
import com.example.controlfinancierocompose.ui.accounts.AccountsViewModel
import com.example.controlfinancierocompose.ui.investments.InvestmentsScreen
import com.example.controlfinancierocompose.ui.investments.InvestmentsViewModel
import com.example.controlfinancierocompose.ui.theme.ControlFinancieroComposeTheme

class MainActivity : ComponentActivity() {
    private val accountsViewModel: AccountsViewModel by viewModels {
        val app = application as FinancialControlApplication
        AccountsViewModel.Factory(app.repository)
    }
    
    private val investmentsViewModel: InvestmentsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Añadir datos de prueba si es necesario
        val app = application as FinancialControlApplication
        app.loadSampleDataIfNeeded()
        
        enableEdgeToEdge()
        setContent {
            ControlFinancieroComposeTheme {
                MainScreen(
                    accountsViewModel = accountsViewModel,
                    investmentsViewModel = investmentsViewModel
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    accountsViewModel: AccountsViewModel,
    investmentsViewModel: InvestmentsViewModel
) {
    var currentScreen by remember { mutableStateOf(1) } // Por defecto mostramos la pantalla de cuentas
    
    when (currentScreen) {
        0 -> {
            // Dashboard (por implementar)
            AccountsScreen(
                accountsViewModel = accountsViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
        1 -> {
            // Cuentas
        AccountsScreen(
            accountsViewModel = accountsViewModel,
            onNavigate = { screen -> currentScreen = screen }
        )
        }
        2 -> {
            // Inversiones
            InvestmentsScreen(
                viewModel = investmentsViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
        3 -> {
            // Calendario (por implementar)
            AccountsScreen(
                accountsViewModel = accountsViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
        4 -> {
            // Credenciales (por implementar)
            AccountsScreen(
                accountsViewModel = accountsViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
        else -> {
            AccountsScreen(
                accountsViewModel = accountsViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
    }
}