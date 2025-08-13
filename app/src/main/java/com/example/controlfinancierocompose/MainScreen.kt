package com.example.controlfinancierocompose

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlfinancierocompose.navigation.Screen
import com.example.controlfinancierocompose.ui.accounts.AccountsScreen
import com.example.controlfinancierocompose.ui.accounts.AccountsViewModel
import com.example.controlfinancierocompose.ui.dashboard.DashboardScreen
import com.example.controlfinancierocompose.ui.dashboard.Movimiento
import com.example.controlfinancierocompose.ui.investments.InvestmentsScreen
import com.example.controlfinancierocompose.ui.investments.InvestmentsViewModel
import kotlinx.serialization.json.Json
import com.example.controlfinancierocompose.data.CalendarEventRepository

@Composable
fun MainScreen(
    accountsViewModel: AccountsViewModel,
    investmentsViewModel: InvestmentsViewModel,
    calendarEventRepository: CalendarEventRepository?,
    onReceiveQR: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) } // Ahora mostramos el dashboard por defecto
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
                    Triple("Inversiones", Icons.AutoMirrored.Filled.TrendingUp, "Inversiones"),
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
                Screen.DASHBOARD -> {
                    val banks by accountsViewModel.banks.collectAsState()
                    val investments by investmentsViewModel.investments.collectAsState()
                    val cuentas = banks.flatMap { it.accounts }
                    val saldoTotal = cuentas.sumOf { it.balance } + investments.sumOf { it.amount }
                    val cuentasTotal = cuentas.sumOf { it.balance }
                    val inversionesTotal = investments.sumOf { it.amount }
                    val movimientos = cuentas.map { Movimiento(it.name, it.balance, "-") } + investments.map { Movimiento(it.name, it.amount, it.date) }
                    val context = LocalContext.current
                    val calendarEventsState = remember { mutableStateOf<List<com.example.controlfinancierocompose.data.CalendarEventEntity>>(emptyList()) }
                    val credentialsState = remember { mutableStateOf<List<com.example.controlfinancierocompose.ui.credentials.Credential>>(emptyList()) }
                    var showQR by remember { mutableStateOf(false) }
                    var qrJson by remember { mutableStateOf("") }
                    // Cargar eventos y credenciales solo cuando se pulse exportar
                    Box(modifier = Modifier.fillMaxSize()) {
                        DashboardScreen(
                            saldoTotal = saldoTotal,
                            cuentas = cuentasTotal,
                            inversiones = inversionesTotal,
                            // deudas eliminado
                            ahorroMensual = 0.0,
                            gastosMensuales = 0.0,
                            ingresosMensuales = 0.0,
                            movimientos = movimientos,
                            onSendQR = {
                                // Recopilar datos y mostrar QR
                                val calendarEvents = try {
                                    calendarEventRepository?.let { repo ->
                                        kotlinx.coroutines.runBlocking { repo.getAllEventsList() }
                                    } ?: emptyList()
                                } catch (_: Exception) { emptyList() }
                                val credentials = try {
                                    com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.getAllCredentials(context)
                                } catch (_: Exception) { emptyList() }
                                calendarEventsState.value = calendarEvents
                                credentialsState.value = credentials
                                val exportData = com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                    banks = banks,
                                    accounts = cuentas,
                                    investmentPlatforms = investmentsViewModel.platforms.value,
                                    investments = investments,
                                    calendarEvents = calendarEvents,
                                    credentials = credentials
                                )
                                qrJson = Json.encodeToString(com.example.controlfinancierocompose.ui.dashboard.ExportData.serializer(), exportData)
                                showQR = true
                            },
                            onReceiveQR = onReceiveQR
                        )
                        if (showQR) {
                            com.example.controlfinancierocompose.ui.dashboard.QRDialog(qrJson) { showQR = false }
                        }
                    }
                }
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
                    // Calendario
                    com.example.controlfinancierocompose.ui.calendar.CalendarScreen()
                }
                Screen.CREDENTIALS -> {
                    // Credenciales con filtro de seguridad (PIN/huella)
                    var unlocked by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    if (!unlocked) {
                        com.example.controlfinancierocompose.ui.credentials.CredentialsUnlockScreen(
                            onUnlock = { unlocked = true }
                        )
                    } else {
                        val banks by accountsViewModel.banks.collectAsState()
                        val investmentPlatforms by investmentsViewModel.platforms.collectAsState()
                        com.example.controlfinancierocompose.ui.credentials.CredentialsListScreen(
                            banks = banks,
                            investmentPlatforms = investmentPlatforms,
                            getAccountsForBank = { bankId -> banks.find { it.id == bankId }?.accounts ?: emptyList() },
                            getHoldersForAccount = { account -> listOf(account.holder) },
                            getCredential = { platformId, accountId, holder ->
                                com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.getCredential(
                                    context,
                                    platformId,
                                    accountId,
                                    holder
                                )
                            },
                            onSaveCredential = { credential ->
                                com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveCredential(context, credential)
                            }
                        )
                    }
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
