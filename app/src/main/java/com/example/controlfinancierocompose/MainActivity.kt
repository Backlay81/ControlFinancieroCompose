package com.example.controlfinancierocompose

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.controlfinancierocompose.ui.investments.InvestmentsScreen
import com.example.controlfinancierocompose.ui.investments.InvestmentsViewModel
import com.example.controlfinancierocompose.ui.dashboard.DashboardScreen
import com.example.controlfinancierocompose.ui.dashboard.Movimiento
import com.example.controlfinancierocompose.ui.theme.ControlFinancieroComposeTheme


class MainActivity : FragmentActivity() {
    private val accountsViewModel: AccountsViewModel by viewModels {
        val app = application as FinancialControlApplication
        AccountsViewModel.Factory(app.repository)
    }
    private val investmentsViewModel: InvestmentsViewModel by viewModels {
        val app = application as FinancialControlApplication
        InvestmentsViewModel.Factory(app.repository)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FinancialControlApplication
        app.loadSampleDataIfNeeded()
        enableEdgeToEdge()
        setContent {
            ControlFinancieroComposeTheme {
                val context = this
                var pinSet by remember { mutableStateOf(false) }
                var unlocked by remember { mutableStateOf(false) }
                var showUnlock by remember { mutableStateOf(false) }
                // Comprobar si hay PIN guardado
                LaunchedEffect(Unit) {
                    val prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                        "secure_prefs",
                        androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC),
                        context,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    pinSet = prefs.getString("user_pin", null)?.length == 8
                    showUnlock = pinSet
                }
                // Mostrar pantalla de configuración de PIN si no existe
                if (!pinSet) {
                    com.example.controlfinancierocompose.ui.auth.PinSetupScreen(onPinSet = {
                        pinSet = true
                        showUnlock = true
                    })
                } else if (showUnlock && !unlocked) {
                    com.example.controlfinancierocompose.ui.auth.PinUnlockScreen(
                        onUnlock = { unlocked = true },
                        onMaxAttempts = { /* Aquí podrías mostrar la tarjeta de coordenadas para recuperación */ }
                    )
                } else {
                    MainScreen(
                        accountsViewModel = accountsViewModel,
                        investmentsViewModel = investmentsViewModel
                    )
                }
                // Si quieres pedir el PIN al volver del background, deberás gestionar el estado en el ciclo de vida de la actividad (onResume), no con SideEffect ni LaunchedEffect aquí.
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
                Screen.DASHBOARD -> {
                    val banks by accountsViewModel.banks.collectAsState()
                    val investments by investmentsViewModel.investments.collectAsState()
                    val cuentas = banks.flatMap { it.accounts }
                    val saldoTotal = cuentas.sumOf { it.balance } + investments.sumOf { it.amount }
                    val cuentasTotal = cuentas.sumOf { it.balance }
                    val inversionesTotal = investments.sumOf { it.amount }
                    val deudas = cuentas.filter { it.balance < 0 }.sumOf { it.balance }
                    val movimientos = cuentas.map { Movimiento(it.name, it.balance, "-") } + investments.map { Movimiento(it.name, it.amount, it.date) }
                    // Puedes ajustar ahorroMensual, gastosMensuales, ingresosMensuales según tu lógica
                    DashboardScreen(
                        saldoTotal = saldoTotal,
                        cuentas = cuentasTotal,
                        inversiones = inversionesTotal,
                        deudas = deudas,
                        ahorroMensual = 0.0,
                        gastosMensuales = 0.0,
                        ingresosMensuales = 0.0,
                        movimientos = movimientos
                    )
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