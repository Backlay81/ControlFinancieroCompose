package com.example.controlfinancierocompose.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinUnlockScreen(onUnlock: () -> Unit, onMaxAttempts: () -> Unit) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var attemptsLeft by remember { mutableStateOf(5) }
    var showError by remember { mutableStateOf(false) }
    var showBiometric by remember { mutableStateOf(false) }

    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    val correctPin = prefs.getString("user_pin", "") ?: ""

    val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate()
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
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = "Acceso seguro",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Desbloquea tu app", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Introduce tu PIN de 8 dígitos o usa tu huella", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1976D2))
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showError) {
                    Text("PIN incorrecto. Intentos restantes: $attemptsLeft", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        if (pinInput == correctPin) {
                            onUnlock()
                        } else {
                            attemptsLeft--
                            showError = true
                            pinInput = ""
                            if (attemptsLeft <= 0) {
                                onMaxAttempts()
                            }
                        }
                    },
                    enabled = attemptsLeft > 0,
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
fun BiometricDialog(context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
    LaunchedEffect(Unit) {
        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Desbloquea la app con tu huella")
            .setNegativeButtonText("Cancelar")
            .build()
        // Obtener la actividad real desde el contexto de Compose
        fun findFragmentActivity(ctx: Context?): androidx.fragment.app.FragmentActivity? {
            var current = ctx
            while (current != null) {
                if (current is androidx.fragment.app.FragmentActivity) return current
                if (current is android.content.ContextWrapper) {
                    current = current.baseContext
                } else {
                    break
                }
            }
            return null
        }
        val activity = findFragmentActivity(context)
        if (activity != null) {
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        // Mensajes específicos según el código de error
                        val msg = when (errorCode) {
                            BiometricPrompt.ERROR_HW_UNAVAILABLE -> "El hardware biométrico no está disponible."
                            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> "No se pudo procesar la huella. Intenta de nuevo."
                            BiometricPrompt.ERROR_TIMEOUT -> "Tiempo de espera agotado."
                            BiometricPrompt.ERROR_NO_SPACE -> "No hay espacio suficiente para procesar la huella."
                            BiometricPrompt.ERROR_CANCELED -> "Autenticación cancelada."
                            BiometricPrompt.ERROR_LOCKOUT -> "Demasiados intentos fallidos. Intenta más tarde."
                            BiometricPrompt.ERROR_VENDOR -> "Error del sensor biométrico."
                            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "El sensor está bloqueado permanentemente."
                            BiometricPrompt.ERROR_USER_CANCELED -> "Cancelado por el usuario."
                            BiometricPrompt.ERROR_NO_BIOMETRICS -> "No hay huellas registradas. Configura una huella en ajustes."
                            BiometricPrompt.ERROR_HW_NOT_PRESENT -> "No hay hardware biométrico disponible."
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "Autenticación cancelada."
                            else -> errString.toString()
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        onError()
                    }
                    override fun onAuthenticationFailed() {
                        Toast.makeText(context, "Huella no reconocida. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                        onError()
                    }
                })
            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(context, "No se pudo iniciar la autenticación biométrica. (Contexto de Activity no encontrado)", Toast.LENGTH_LONG).show()
            onError()
        }
    }
}
