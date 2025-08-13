package com.example.controlfinancierocompose.ui.auth

import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

// Helper para capturar el bitmap de la vista actual
fun getBitmapFromView(view: android.view.View): android.graphics.Bitmap {
    // Método moderno para crear un bitmap a partir de una vista
    val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(onPinSet: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var showCard by remember { mutableStateOf(false) }
    var card by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    fun savePinAndCard(pin: String, card: List<Pair<String, String>>) {
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
        prefs.edit().putString("user_pin", pin).apply()
        prefs.edit().putString("card_coords", card.joinToString("|") { "${it.first},${it.second}" }).apply()
    }

    fun generateCard(): List<Pair<String, String>> {
        // Genera una tarjeta de coordenadas 4x4 (A-D, 1-4)
        val letters = listOf("A", "B", "C", "D")
        val numbers = listOf("1", "2", "3", "4")
        return letters.flatMap { l ->
            numbers.map { n ->
                val value = Random.nextInt(1000, 9999).toString()
                "$l$n" to value
            }
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
            if (!showCard) {
                PinSetupFields(
                    pin = pin,
                    onPinChange = { value -> if (value.length <= 8) pin = value.filter { c -> c.isDigit() } },
                    pinConfirm = pinConfirm,
                    onPinConfirmChange = { value -> if (value.length <= 8) pinConfirm = value.filter { c -> c.isDigit() } },
                    onCreatePin = {
                        if (pin.length == 8 && pin == pinConfirm) {
                            card = generateCard()
                            savePinAndCard(pin, card)
                            showCard = true
                        } else {
                            Toast.makeText(context, "El PIN debe tener 8 dígitos y coincidir", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                val view = LocalView.current
                PinCardView(card = card, view = view, context = context, onPinSet = onPinSet)
            }
        }
    }

}

@Composable
private fun PinSetupFields(
    pin: String,
    onPinChange: (String) -> Unit,
    pinConfirm: String,
    onPinConfirmChange: (String) -> Unit,
    onCreatePin: () -> Unit
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.VpnKey,
            contentDescription = "Configurar PIN",
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Configura tu PIN de 8 dígitos", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
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
        OutlinedTextField(
            value = pinConfirm,
            onValueChange = onPinConfirmChange,
            label = { Text("Confirmar PIN") },
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreatePin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
        ) {
            Text("Crear PIN y tarjeta", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PinCardView(card: List<Pair<String, String>>, view: View, context: android.content.Context, onPinSet: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.VpnKey,
            contentDescription = "Tarjeta generada",
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tarjeta de coordenadas generada", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                card.chunked(4).forEach { row ->
                    Row {
                        row.forEach { (coord, value) ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(coord, style = MaterialTheme.typography.labelMedium)
                                Text(value, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Guarda esta tarjeta en un lugar seguro. La necesitarás para recuperar tu PIN si lo olvidas.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Descargar la tarjeta como imagen PNG
                try {
                    val bitmap = getBitmapFromView(view)
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                    val fileName = "tarjeta_coordenadas_${sdf.format(Date())}.png"
                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Coordenadas")
                        }
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        val out = resolver.openOutputStream(uri)
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            out.close()
                            Toast.makeText(context, "Tarjeta guardada en la galería", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error al guardar la tarjeta", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Error al guardar la tarjeta", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar la tarjeta", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C), contentColor = Color.White)
        ) {
            Text("Descargar tarjeta", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onPinSet,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
        ) {
            Text("Continuar", style = MaterialTheme.typography.titleMedium)
        }
    }
}
