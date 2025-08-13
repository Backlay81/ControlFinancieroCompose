package com.example.controlfinancierocompose


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.controlfinancierocompose.ui.accounts.AccountsViewModel
import com.example.controlfinancierocompose.ui.dashboard.ExportData
import com.example.controlfinancierocompose.ui.investments.InvestmentsViewModel
import com.example.controlfinancierocompose.ui.theme.ControlFinancieroComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : FragmentActivity() {
    override fun onResume() {
        super.onResume()
        // Siempre forzar autenticación al volver a primer plano
        SessionManager.isUnlocked = false
    }
    
    private val accountsViewModel: AccountsViewModel by viewModels {
        val app = application as FinancialControlApplication
        AccountsViewModel.Factory(app.repository)
    }
    
    private val investmentsViewModel: InvestmentsViewModel by viewModels {
        val app = application as FinancialControlApplication
        InvestmentsViewModel.Factory(app.repository)
    }
    
    // Definición del importador de datos
    private fun importDataFromQR(exportData: ExportData) {
        lifecycleScope.launch {
            val app = application as FinancialControlApplication
            val repository = app.repository
            // Borrar todo
            repository.deleteAllInvestments()
            repository.deleteAllPlatforms()
            repository.deleteAllAccounts()
            repository.deleteAllBanks()
            repository.deleteAllCalendarEvents()
            // Borrar credenciales (sobrescribir todo)
            com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveAllCredentials(this@MainActivity, emptyList())

            // Importar bancos y cuentas
            exportData.banks.forEach { bank ->
                repository.insertBank(bank)
                bank.accounts.forEach { account ->
                    repository.insertAccount(account)
                }
            }

            // Importar plataformas de inversión
            exportData.investmentPlatforms.forEach { platform ->
                repository.insertPlatform(platform)
            }

            // Importar inversiones
            exportData.investments.forEach { investment ->
                repository.insertInvestment(investment)
            }

            // Importar eventos de calendario
            val calendarEventRepo = app.calendarEventRepository
            exportData.calendarEvents.forEach { event ->
                calendarEventRepo?.insertEvent(event)
            }

            // Importar credenciales
            com.example.controlfinancierocompose.ui.credentials.CredentialsStorage.saveAllCredentials(this@MainActivity, exportData.credentials)

            // Refrescar los ViewModels para que la UI muestre los datos correctos
            accountsViewModel.refresh()
            investmentsViewModel.refresh()
        }
    }
    
    // Scanner para QR
    private val scanQrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanResult = result.data?.getStringExtra("SCAN_RESULT")
            if (scanResult != null) {
                try {
                    // Decodificar los datos JSON
                    val exportData = Json.decodeFromString(ExportData.serializer(), scanResult)
                    
                    // Procesar los datos recibidos
                    importDataFromQR(exportData)
                    
                    // Mostrar mensaje de éxito
                    Toast.makeText(this, "Datos importados correctamente", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    // Error al procesar los datos
                    Toast.makeText(this, "Error al procesar los datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
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
                LaunchedEffect(Unit) {
                    val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                        .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    val prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                        context,
                        "secure_prefs",
                        masterKey,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    pinSet = prefs.getString("user_pin", null)?.length == 8
                }
                val app = application as FinancialControlApplication
                val calendarEventRepository = app.calendarEventRepository
                if (!pinSet) {
                    com.example.controlfinancierocompose.ui.auth.PinSetupScreen(onPinSet = {
                        pinSet = true
                    })
                } else if (!SessionManager.isUnlocked) {
                    com.example.controlfinancierocompose.ui.auth.PinUnlockScreen(
                        onUnlock = {
                            SessionManager.isUnlocked = true
                        },
                        onMaxAttempts = { /* Aquí podrías mostrar la tarjeta de coordenadas para recuperación */ }
                    )
                } else {
                    MainScreen(
                        accountsViewModel = accountsViewModel,
                        investmentsViewModel = investmentsViewModel,
                        calendarEventRepository = calendarEventRepository,
                        onReceiveQR = { 
                            val intent = Intent(context, com.example.controlfinancierocompose.ui.scanner.CodeScannerActivity::class.java)
                            scanQrLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}


