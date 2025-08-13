package com.example.controlfinancierocompose.ui.credentials

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsScreen(onAccessGranted: () -> Unit) {
    val context = LocalContext.current
    var accessGranted by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val correctPin = "1234" // Cambia esto por tu lógica de almacenamiento seguro

    // Biometric check
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    LaunchedEffect(accessGranted) {
        if (accessGranted) onAccessGranted()
    }

    if (!accessGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Acceso a credenciales", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                if (canAuthenticate) {
                    Button(onClick = {
                        showBiometricPrompt(context) { success ->
                            if (success) accessGranted = true
                        }
                    }) {
                        Text("Acceder con huella")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(onClick = { showPinDialog = true }) {
                    Text("Acceder con código PIN")
                }
            }
        }
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Introduce tu código PIN") },
                text = {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (pinInput == correctPin) {
                            accessGranted = true
                            showPinDialog = false
                        } else {
                            pinInput = ""
                        }
                    }) {
                        Text("Acceder")
                    }
                },
                dismissButton = {
                    Button(onClick = { showPinDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}



fun showBiometricPrompt(context: Context, onResult: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación biométrica")
        .setSubtitle("Accede con tu huella dactilar")
        .setNegativeButtonText("Cancelar")
        .build()
    val activity = context as? FragmentActivity
    if (activity != null) {
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(true)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(false)
                }
                override fun onAuthenticationFailed() {
                    onResult(false)
                }
            })
        biometricPrompt.authenticate(promptInfo)
    } else {
        onResult(false)
    }
}
