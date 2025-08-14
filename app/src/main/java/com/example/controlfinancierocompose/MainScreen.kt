package com.example.controlfinancierocompose

import androidx.activity.compose.rememberLauncherForActivityResult
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
    // Límite recomendado para QR (en caracteres)

    accountsViewModel: AccountsViewModel,
    investmentsViewModel: InvestmentsViewModel,
    calendarEventRepository: CalendarEventRepository?,
    onReceiveQR: () -> Unit
) {
    val QR_CHAR_LIMIT = 1800
    val context = LocalContext.current
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportData by remember {
        mutableStateOf<com.example.controlfinancierocompose.ui.dashboard.ExportData?>(
            null
        )
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        val importData = Json.decodeFromString(
                            com.example.controlfinancierocompose.ui.dashboard.ExportData.serializer(),
                            json
                        )
                        pendingImportData = importData
                    } else {
                        exportMessage = "No se pudo leer el archivo."
                    }
                } catch (e: Exception) {
                    exportMessage = "Error al importar backup: ${e.message}"
                }
            }
        }
    )

    // Restaurar datos importados cuando pendingImportData cambie
    val app =
        context.applicationContext as com.example.controlfinancierocompose.FinancialControlApplication
    val repository = app.repository
    val calendarEventRepo = app.calendarEventRepository
    androidx.compose.runtime.LaunchedEffect(pendingImportData) {
        val importData = pendingImportData
        if (importData != null) {
            try {
                // Borrar todo
                repository.deleteAllInvestments()
                repository.deleteAllPlatforms()
                repository.deleteAllAccounts()
                repository.deleteAllBanks()
                repository.deleteAllCalendarEvents()
                com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveAllCredentials(
                    context,
                    emptyList()
                )

                // Importar bancos y cuentas
                importData.banks.forEach { bank ->
                    repository.insertBank(bank)
                    bank.accounts.forEach { account ->
                        repository.insertAccount(account)
                    }
                }
                // Importar plataformas de inversión
                importData.investmentPlatforms.forEach { platform ->
                    repository.insertPlatform(platform)
                }
                // Importar inversiones
                importData.investments.forEach { investment ->
                    repository.insertInvestment(investment)
                }
                // Importar eventos de calendario
                importData.calendarEvents.forEach { event ->
                    calendarEventRepo?.insertEvent(event)
                }
                // Importar credenciales
                com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveAllCredentials(
                    context,
                    importData.credentials
                )

                // Refrescar los ViewModels para que la UI muestre los datos correctos
                accountsViewModel.refresh()
                investmentsViewModel.refresh()
                exportMessage = "Backup importado correctamente."
            } catch (e: Exception) {
                exportMessage = "Error al importar backup: ${e.message}"
            }
            pendingImportData = null
        }
    }
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
                    val movimientos = cuentas.map {
                        Movimiento(
                            it.name,
                            it.balance,
                            "-"
                        )
                    } + investments.map { Movimiento(it.name, it.amount, it.date) }
                    val context = LocalContext.current
                    val calendarEventsState = remember {
                        mutableStateOf<List<com.example.controlfinancierocompose.data.CalendarEventEntity>>(
                            emptyList()
                        )
                    }
                    val credentialsState = remember {
                        mutableStateOf<List<com.example.controlfinancierocompose.ui.credentials.Credential>>(
                            emptyList()
                        )
                    }
                    var showQR by remember { mutableStateOf(false) }
                    var qrJson by remember { mutableStateOf("") }
                    var exportMessage by remember { mutableStateOf<String?>(null) }
                    // Cargar eventos y credenciales solo cuando se pulse exportar
                    Box(modifier = Modifier.fillMaxSize()) {
                        DashboardScreen(
                            saldoTotal = saldoTotal,
                            cuentas = cuentasTotal,
                            inversiones = inversionesTotal,
                            ahorroMensual = 0.0,
                            gastosMensuales = 0.0,
                            ingresosMensuales = 0.0,
                            movimientos = movimientos,
                            onSendQRBank = {
                                val exportData = com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                    banks = banks,
                                    accounts = emptyList(),
                                    investmentPlatforms = emptyList(),
                                    investments = emptyList(),
                                    calendarEvents = emptyList(),
                                    credentials = emptyList()
                                )
                                val qrString = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                if (qrString.length > QR_CHAR_LIMIT) {
                                    exportMessage = "Demasiados datos para QR (bancos). Usa exportar archivo."
                                } else {
                                    qrJson = qrString
                                    showQR = true
                                }
                            },
                            // Eliminado onSendQRAccount (QR solo cuentas)
                            onSendQRInvestment = {
                                val exportData = com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                    banks = emptyList(),
                                    accounts = emptyList(),
                                    investmentPlatforms = investmentsViewModel.platforms.value,
                                    investments = investments,
                                    calendarEvents = emptyList(),
                                    credentials = emptyList()
                                )
                                val qrString = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                if (qrString.length > QR_CHAR_LIMIT) {
                                    exportMessage = "Demasiados datos para QR (inversiones). Usa exportar archivo."
                                } else {
                                    qrJson = qrString
                                    showQR = true
                                }
                            },
                            onSendQRCalendar = {
                                val calendarEvents = try {
                                    calendarEventRepository?.let { repo ->
                                        kotlinx.coroutines.runBlocking { repo.getAllEventsList() }
                                    } ?: emptyList()
                                } catch (_: Exception) {
                                    emptyList()
                                }
                                val exportData = com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                    banks = emptyList(),
                                    accounts = emptyList(),
                                    investmentPlatforms = emptyList(),
                                    investments = emptyList(),
                                    calendarEvents = calendarEvents,
                                    credentials = emptyList()
                                )
                                val qrString = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                if (qrString.length > QR_CHAR_LIMIT) {
                                    exportMessage = "Demasiados datos para QR (eventos). Usa exportar archivo."
                                } else {
                                    qrJson = qrString
                                    showQR = true
                                }
                            },
                            onSendQRCredentials = {
                                val credentials = try {
                                    com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.getAllCredentials(
                                        context
                                    )
                                } catch (_: Exception) {
                                    emptyList()
                                }
                                val exportData = com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                    banks = emptyList(),
                                    accounts = emptyList(),
                                    investmentPlatforms = emptyList(),
                                    investments = emptyList(),
                                    calendarEvents = emptyList(),
                                    credentials = credentials
                                )
                                val qrString = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                if (qrString.length > QR_CHAR_LIMIT) {
                                    exportMessage = "Demasiados datos para QR (credenciales). Usa exportar archivo."
                                } else {
                                    qrJson = qrString
                                    showQR = true
                                }
                            },
                            onExportFile = {
                                val calendarEvents = try {
                                    calendarEventRepository?.let { repo ->
                                        kotlinx.coroutines.runBlocking { repo.getAllEventsList() }
                                    } ?: emptyList()
                                } catch (_: Exception) {
                                    emptyList()
                                }
                                val credentials = try {
                                    com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.getAllCredentials(
                                        context
                                    )
                                } catch (_: Exception) {
                                    emptyList()
                                }
                                val exportData =
                                    com.example.controlfinancierocompose.ui.dashboard.ExportData(
                                        banks = banks,
                                        accounts = cuentas,
                                        investmentPlatforms = investmentsViewModel.platforms.value,
                                        investments = investments,
                                        calendarEvents = calendarEvents,
                                        credentials = credentials
                                    )
                                try {
                                    val json = Json.encodeToString(
                                        com.example.controlfinancierocompose.ui.dashboard.ExportData.serializer(),
                                        exportData
                                    )
                                    val fileName =
                                        "controlfinanciero-backup-${System.currentTimeMillis()}.json"
                                    val downloads =
                                        android.os.Environment.getExternalStoragePublicDirectory(
                                            android.os.Environment.DIRECTORY_DOWNLOADS
                                        )
                                    val file = java.io.File(downloads, fileName)
                                    file.writeText(json)
                                    exportMessage = "Backup exportado en: ${file.absolutePath}"
                                } catch (e: Exception) {
                                    exportMessage = "Error al exportar backup: ${e.message}"
                                }
                            },
                            onReceiveQR = onReceiveQR,
                            onImportFile = {
                                importLauncher.launch("application/json")
                            }
                        )
                        if (showQR) {
                            com.example.controlfinancierocompose.ui.dashboard.QRDialog(qrJson) {
                                showQR = false
                            }
                        }
                        if (exportMessage != null) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { exportMessage = null },
                                title = { Text("Backup") },
                                text = { Text(exportMessage ?: "") },
                                confirmButton = {
                                    androidx.compose.material3.TextButton(onClick = {
                                        exportMessage = null
                                    }) {
                                        Text("OK")
                                    }
                                }
                            )
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
                            getAccountsForBank = { bankId ->
                                banks.find { it.id == bankId }?.accounts ?: emptyList()
                            },
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
                                com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveCredential(
                                    context,
                                    credential
                                )
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
