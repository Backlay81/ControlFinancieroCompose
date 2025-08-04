package com.example.controlfinancierocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlfinancierocompose.navigation.Screen
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
    var currentScreen by remember { mutableStateOf(Screen.ACCOUNTS) } // Por defecto mostramos la pantalla de cuentas
    var selectedSection by remember { mutableStateOf(currentScreen.index) }
    
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 4.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val items = listOf(
                    Triple("Dashboard", Icons.Filled.Home, "Dashboard"),
                    Triple("Cuentas", Icons.Filled.AccountCircle, "Cuentas"),
                    Triple("Inversiones", Icons.Filled.TrendingUp, "Inversiones"),
                    Triple("Calendario", Icons.Filled.CalendarToday, "Calendario"),
                    Triple("Credenciales", Icons.Filled.VpnKey, "Credenciales")
                )
                items.forEachIndexed { index, item ->
                    val selected = selectedSection == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                selectedSection = index
                                currentScreen = Screen.fromIndex(index)
                            }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            item.second,
                            contentDescription = item.first,
                            modifier = Modifier
                                .size(if (selected) 32.dp else 28.dp)
                                .padding(bottom = 2.dp),
                            tint = if (selected) Color(0xFF1976D2) else Color(0xFF757575)
                        )
                        Text(
                            item.third,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selected) Color(0xFF1976D2) else Color(0xFF757575),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            maxLines = 1
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(32.dp)
                                    .background(
                                        color = Color(0xFF1976D2),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.ACCOUNTS -> {
                    // Cuentas
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        onNavigate = { screenIndex -> 
                            selectedSection = screenIndex
                            currentScreen = Screen.fromIndex(screenIndex) 
                        }
                    )
                }
                Screen.INVESTMENTS -> {
                    // Inversiones
                    InvestmentsScreen(
                        viewModel = investmentsViewModel,
                        onNavigate = { screenIndex -> 
                            selectedSection = screenIndex
                            currentScreen = Screen.fromIndex(screenIndex) 
                        }
                    )
                }
                Screen.CALENDAR -> {
                    // Calendario (por implementar)
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        onNavigate = { screenIndex -> 
                            selectedSection = screenIndex
                            currentScreen = Screen.fromIndex(screenIndex) 
                        }
                    )
                }
                Screen.CREDENTIALS -> {
                    // Credenciales (por implementar)
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        onNavigate = { screenIndex -> 
                            selectedSection = screenIndex
                            currentScreen = Screen.fromIndex(screenIndex) 
                        }
                    )
                }
                Screen.SETTINGS -> {
                    // Ajustes (por implementar)
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        onNavigate = { screenIndex -> 
                            selectedSection = screenIndex
                            currentScreen = Screen.fromIndex(screenIndex) 
                        }
                    )
                }
            }
        }
    }
}