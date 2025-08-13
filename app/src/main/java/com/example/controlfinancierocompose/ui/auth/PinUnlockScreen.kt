package com.example.controlfinancierocompose.ui.auth

import android.content.Context
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
import androidx.compose.material.icons.filled.VpnKey
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
fun PinUnlockScreen(onUnlock: () -> Unit, onMaxAttempts: () -> Unit) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var attemptsLeft by remember { mutableStateOf(5) }
    var showError by remember { mutableStateOf(false) }
    var showBiometric by remember { mutableStateOf(false) }
    var lockCount by remember { mutableStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockTimeLeft by remember { mutableStateOf(0L) }
    var showCardDialog by remember { mutableStateOf(false) }
    val lockDurations = listOf(30L, 60L, 120L) // segundos
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
    val prefsEditor = prefs.edit()

    // Recuperar estado persistente
    LaunchedEffect(Unit) {
        lockCount = prefs.getInt("lock_count", 0)
        val unlockAt = prefs.getLong("unlock_at", 0L)
        val now = System.currentTimeMillis() / 1000L
        if (unlockAt > now) {
            isLocked = true
            lockTimeLeft = unlockAt - now
        }
    }

    // Temporizador de desbloqueo
    LaunchedEffect(isLocked, lockTimeLeft) {
        if (isLocked && lockTimeLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            lockTimeLeft--
            if (lockTimeLeft <= 0) {
                isLocked = false
                attemptsLeft = 5
                prefsEditor.putLong("unlock_at", 0L).apply()
            }
        }
    }

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
                    onValueChange = { value: String -> if (value.length <= 8) pinInput = value.filter { c -> c.isDigit() } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFF90CAF9)
                    ),
                    enabled = !isLocked && !showCardDialog,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showError) {
                    Text("PIN incorrecto. Intentos restantes: $attemptsLeft", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isLocked) {
                    Text("Demasiados intentos fallidos. Espera $lockTimeLeft segundos para el siguiente intento.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (showCardDialog) {
                    Text("Por seguridad, introduce tu tarjeta de coordenadas.", color = Color(0xFF1976D2), style = MaterialTheme.typography.bodyMedium)
                    // Aquí podrías poner el UI para la tarjeta de coordenadas
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        if (showCardDialog) {
                            // Aquí iría la validación de la tarjeta de coordenadas
                            onUnlock()
                        } else if (pinInput == correctPin) {
                            onUnlock()
                            attemptsLeft = 5
                            lockCount = 0
                            prefsEditor.putInt("lock_count", 0).apply()
                        } else {
                            attemptsLeft--
                            showError = true
                            pinInput = ""
                            if (attemptsLeft <= 0) {
                                lockCount++
                                prefsEditor.putInt("lock_count", lockCount).apply()
                                if (lockCount >= 3) {
                                    showCardDialog = true
                                    attemptsLeft = 0
                                } else {
                                    val now = System.currentTimeMillis() / 1000L
                                    val waitTime = lockDurations.getOrElse(lockCount - 1) { 120L }
                                    prefsEditor.putLong("unlock_at", now + waitTime).apply()
                                    lockTimeLeft = waitTime
                                    isLocked = true
                                    attemptsLeft = 5
                                }
                            }
                            if (lockCount >= 5) {
                                showCardDialog = true
                                attemptsLeft = 0
                            }
                        }
                    },
                    enabled = attemptsLeft > 0 && !isLocked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
                ) {
                    Text(if (showCardDialog) "Validar tarjeta" else "Desbloquear", style = MaterialTheme.typography.titleMedium)
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
