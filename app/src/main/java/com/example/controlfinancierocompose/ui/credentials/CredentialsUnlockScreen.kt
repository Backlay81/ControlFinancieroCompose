package com.example.controlfinancierocompose.ui.credentials

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsUnlockScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showBiometric by remember { mutableStateOf(false) }

    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    val correctPin = prefs.getString("user_pin", "") ?: ""

    val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    val canAuthenticate = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
    var biometricErrorMsg = ""
    if (!canAuthenticate) {
        biometricErrorMsg = when (biometricStatus) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Tu dispositivo no tiene hardware biométrico."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "El hardware biométrico no está disponible."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No hay huellas registradas. Configura una huella en ajustes."
            else -> "La autenticación biométrica no está disponible."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1976D2), Color(0xFF64B5F6)),
                    startY = 0f, endY = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.92f),
            elevation = CardDefaults.cardElevation(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Acceso credenciales",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Desbloquea tus credenciales", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Introduce tu PIN de acceso o usa tu huella", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8) pinInput = it.filter { c -> c.isDigit() } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFF90CAF9)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showError) {
                    Text("PIN incorrecto", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        if (pinInput == correctPin) {
                            onUnlock()
                        } else {
                            showError = true
                            pinInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
                ) {
                    Text("Desbloquear", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (canAuthenticate) {
                    IconButton(
                        onClick = { showBiometric = true },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF1976D2), shape = RoundedCornerShape(32.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Desbloquear con huella",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text("Desbloquear con huella", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1976D2))
                } else if (biometricErrorMsg.isNotEmpty()) {
                    Text(biometricErrorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (showBiometric) {
            BiometricDialog(
                context = context,
                onSuccess = {
                    showBiometric = false
                    onUnlock()
                },
                onError = {
                    showBiometric = false
                    Toast.makeText(context, "No se pudo autenticar con huella", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun BiometricDialog(context: android.content.Context, onSuccess: () -> Unit, onError: () -> Unit) {
    LaunchedEffect(Unit) {
        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Desbloquea las credenciales con tu huella")
            .setNegativeButtonText("Cancelar")
            .build()
        val activity = (context as? android.app.Activity) as? androidx.fragment.app.FragmentActivity
        if (activity != null) {
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onError()
                    }
                    override fun onAuthenticationFailed() {
                        onError()
                    }
                })
            biometricPrompt.authenticate(promptInfo)
        } else {
            onError()
        }
    }
}
