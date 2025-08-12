package com.example.controlfinancierocompose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import com.example.controlfinancierocompose.navigation.Screen
import com.example.controlfinancierocompose.ui.scanner.QRScannerActivity
import com.example.controlfinancierocompose.ui.dashboard.ExportData
import androidx.activity.viewModels
import com.example.controlfinancierocompose.ui.accounts.AccountsScreen
import com.example.controlfinancierocompose.ui.accounts.AccountsViewModel
import com.example.controlfinancierocompose.ui.investments.InvestmentsScreen
import com.example.controlfinancierocompose.ui.investments.InvestmentsViewModel
import com.example.controlfinancierocompose.ui.dashboard.DashboardScreen
import com.example.controlfinancierocompose.ui.dashboard.Movimiento
import com.example.controlfinancierocompose.ui.theme.ControlFinancieroComposeTheme
import kotlinx.serialization.builtins.serializer


class MainActivity : FragmentActivity() {
    
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
        // Importar bancos y cuentas
        exportData.banks.forEach { bank ->
            // Verificar si el banco ya existe por nombre
            val existingBankWithSameName = accountsViewModel.banks.value.find { it.name == bank.name }
            
            if (existingBankWithSameName == null) {
                // Si no existe, añadir el banco
                accountsViewModel.addBank(bank)
            } else {
                // Si existe, actualizar el banco y sus cuentas
                accountsViewModel.updateBank(bank.copy(id = existingBankWithSameName.id))
                
                // Procesar las cuentas del banco
                bank.accounts.forEach { account ->
                    val existingAccount = existingBankWithSameName.accounts.find { it.name == account.name }
                    
                    if (existingAccount == null) {
                        // Si la cuenta no existe, añadirla
                        accountsViewModel.addAccount(existingBankWithSameName.id, 
                            account.copy(bankId = existingBankWithSameName.id))
                    } else {
                        // Si existe, actualizarla
                        accountsViewModel.updateAccount(existingBankWithSameName.id, 
                            account.copy(id = existingAccount.id, bankId = existingBankWithSameName.id))
                    }
                }
            }
        }
        
        // Importar inversiones
        exportData.investments.forEach { investment ->
            // Verificar si la inversión ya existe por nombre
            val existingInvestment = investmentsViewModel.investments.value.find { it.name == investment.name }
            
            if (existingInvestment == null) {
                // Si no existe, añadirla
                investmentsViewModel.addInvestment(investment)
            } else {
                // Si existe, actualizarla
                investmentsViewModel.updateInvestment(investment.copy(id = existingInvestment.id))
            }
        }
        
        // Aquí podrías procesar también calendarEvents y credentials cuando se implementen
    }
    
    // Scanner para QR
    private val scanQrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
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
                        investmentsViewModel = investmentsViewModel,
                        onReceiveQR = { 
                            val intent = Intent(context, QRScannerActivity::class.java)
                            scanQrLauncher.launch(intent)
                        }
                    )
                }
                // Si quieres pedir el PIN al volver del background, deberás gestionar el estado en el ciclo de vida de la actividad (onResume), no con SideEffect ni LaunchedEffect aquí.
            }
        }
    }
}


