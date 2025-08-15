package com.example.controlfinancierocompose

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.serialization.json.jsonObject

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
    // Usar un flag persistente para no perder el estado si la app se pausa
    val sharedPreferences = context.getSharedPreferences("import_backup", 0)
    var pendingImportData by remember {
        mutableStateOf<com.example.controlfinancierocompose.ui.dashboard.ExportData?>(
            null
        )
    }
    // Declarar app, repository y calendarEventRepo al inicio para acceso global (solo una vez)
    val app = context.applicationContext as com.example.controlfinancierocompose.FinancialControlApplication
    val repository = app.repository
    val calendarEventRepo = app.calendarEventRepository
    // Al iniciar o volver a la app, si hay backup pendiente en prefs, cargarlo
    LaunchedEffect(Unit) {
        try {
            val json = sharedPreferences.getString("pending_import_json", null)
            if (json != null) {
                Log.e("IMPORT_DEBUG", "JSON leído: " + json.take(200))
                // Intentar primero abreviado, luego estándar, luego adaptar claves largas a cortas
                val importData = try {
                    com.example.controlfinancierocompose.ui.dashboard.fromAbbreviatedQrJson(json)
                } catch (e: Exception) {
                    Log.e("IMPORT_DEBUG", "Error abreviado: ${e.message}", e)
                    try {
                        Json.decodeFromString(
                            com.example.controlfinancierocompose.ui.dashboard.ExportData.serializer(),
                            json
                        )
                    } catch (e2: Exception) {
                        Log.e("IMPORT_DEBUG", "Error estándar: ${e2.message}", e2)
                        // --- Adaptar claves largas a cortas si es posible ---
                        try {
                            val jsonObj = kotlinx.serialization.json.Json.parseToJsonElement(json).jsonObject
                            // Si detecta claves largas, mapear a abreviadas
                            val keyMap = mapOf(
                                "banks" to "b", "accounts" to "a", "investmentPlatforms" to "p", "investments" to "i", "calendarEvents" to "e", "credentials" to "r",
                                "id" to "i", "name" to "n", "isActive" to "v", "bankId" to "b", "holder" to "h", "balance" to "l", "currency" to "c", "type" to "t", "notes" to "o",
                                "platformId" to "p", "accountId" to "a", "username" to "u", "password" to "w", "description" to "d", "date" to "f"
                            )
                            fun mapKeysDeep(element: kotlinx.serialization.json.JsonElement): kotlinx.serialization.json.JsonElement {
                                return when (element) {
                                    is kotlinx.serialization.json.JsonObject -> kotlinx.serialization.json.buildJsonObject {
                                        element.forEach { (k, v) ->
                                            val newKey = keyMap[k] ?: k
                                            put(newKey, mapKeysDeep(v))
                                        }
                                    }
                                    is kotlinx.serialization.json.JsonArray -> kotlinx.serialization.json.JsonArray(element.map { mapKeysDeep(it) })
                                    else -> element
                                }
                            }
                            val abreviado = mapKeysDeep(jsonObj)
                            com.example.controlfinancierocompose.ui.dashboard.fromAbbreviatedQrJson(abreviado.toString())
                        } catch (e3: Exception) {
                            Log.e("IMPORT_DEBUG", "Error adaptando claves: ${e3.message}", e3)
                            null
                        }
                    }
                }
                if (importData != null) {
                    pendingImportData = importData
                }
                sharedPreferences.edit().remove("pending_import_json").apply()
            }
        } catch (e: Exception) {
            Log.e("IMPORT_DEBUG", "Error global: ${e.message}", e)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        // Guardar el JSON en SharedPreferences para que sobreviva si la app se pausa
                        sharedPreferences.edit().putString("pending_import_json", json).apply()
                        exportMessage = "Backup listo para importar. Si la app pide autenticación, la importación continuará al volver."
                    } else {
                        exportMessage = "No se pudo leer el archivo."
                    }
                } catch (e: Exception) {
                    exportMessage = "Error al importar backup: ${e.message}"
                }
            } else {
                exportMessage = "Importación cancelada o URI nulo"
            }
        }
    )

    // Restaurar datos importados cuando pendingImportData cambie
    androidx.compose.runtime.LaunchedEffect(pendingImportData) {
        val importData = pendingImportData
        if (importData != null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
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

                    for (bank in importData.banks) {
                        repository.insertBank(bank)
                        for (account in bank.accounts) {
                            repository.insertAccount(account)
                        }
                    }
                    for (platform in importData.investmentPlatforms) {
                        repository.insertPlatform(platform)
                    }
                    for (investment in importData.investments) {
                        repository.insertInvestment(investment)
                    }
                    for (event in importData.calendarEvents) {
                        calendarEventRepo?.insertEvent(event)
                    }
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
                // Limpiar el flag persistente
                sharedPreferences.edit().remove("pending_import_json").apply()
            }
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
                    val banks = accountsViewModel.banks.collectAsState().value
                    val investments = investmentsViewModel.investments.collectAsState().value
                    val cuentas = banks.flatMap { bank -> bank.accounts }
                    val saldoTotal = cuentas.fold(0.0) { acc, cuenta -> acc + cuenta.balance } + 
                                    investments.fold(0.0) { acc, inv -> acc + inv.amount }
                    val cuentasTotal = cuentas.fold(0.0) { acc, cuenta -> acc + cuenta.balance }
                    val inversionesTotal = investments.fold(0.0) { acc, inv -> acc + inv.amount }
                    val movimientos = cuentas.map { cuenta ->
                        Movimiento(
                            cuenta.name,
                            cuenta.balance,
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
                    // --- Exportación de backup: declaración aquí para acceso a banks/cuentas/investments ---
                    var pendingExport by remember { mutableStateOf(false) }
                    val exportLauncher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
                        onResult = { uri ->
                            if (uri != null) {
                                try {
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
                                    val json = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                        outputStream.write(json.toByteArray())
                                    }
                                    exportMessage = "Backup exportado correctamente."
                                } catch (e: Exception) {
                                    exportMessage = "Error al exportar backup: ${e.message}"
                                }
                            } else {
                                exportMessage = "Exportación cancelada."
                            }
                            pendingExport = false
                        }
                    )
                    LaunchedEffect(pendingExport) {
                        if (pendingExport) {
                            val fileName = "controlfinanciero-backup-${System.currentTimeMillis()}.json"
                            exportLauncher.launch(fileName)
                        }
                    }
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
                            // ...existing code...
                            onExportFile = { pendingExport = true },
                            onReceiveQR = onReceiveQR,
                            onImportFile = {
                                importLauncher.launch("application/json")
                            }
                        )
                        // --- Exportación de backup: declaración fuera del callback ---
                        var pendingExport by remember { mutableStateOf(false) }
                        val exportLauncher = rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
                            onResult = { uri ->
                                if (uri != null) {
                                    try {
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
                                        val json = com.example.controlfinancierocompose.ui.dashboard.toAbbreviatedQrJson(exportData)
                                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                            outputStream.write(json.toByteArray())
                                        }
                                        exportMessage = "Backup exportado correctamente."
                                    } catch (e: Exception) {
                                        exportMessage = "Error al exportar backup: ${e.message}"
                                    }
                                } else {
                                    exportMessage = "Exportación cancelada."
                                }
                                pendingExport = false
                            }
                        )
                        LaunchedEffect(pendingExport) {
                            if (pendingExport) {
                                val fileName = "controlfinanciero-backup-${System.currentTimeMillis()}.json"
                                exportLauncher.launch(fileName)
                            }
                        }
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
                        val banks = accountsViewModel.banks.collectAsState().value
                        val investmentPlatforms = investmentsViewModel.platforms.collectAsState().value
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
