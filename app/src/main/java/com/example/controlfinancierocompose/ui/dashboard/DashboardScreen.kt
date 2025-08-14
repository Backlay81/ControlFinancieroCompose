package com.example.controlfinancierocompose.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.controlfinancierocompose.data.CalendarEventEntity
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity
import com.example.controlfinancierocompose.ui.accounts.Account
import com.example.controlfinancierocompose.ui.accounts.Bank
import com.example.controlfinancierocompose.ui.credentials.Credential
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*




// Mapeo: id→i, name→n, accounts→a, isActive→v, bankId→b, holder→h, balance→l, currency→c, type→t, notes→o

// No usamos estas funciones, pero las dejamos como referencia del mapeo abreviado
// (El código actual usa una construcción directa de JsonObject y JsonArray)
/*
fun Bank.toAbbreviatedMap(): Map<String, Any?> = mapOf(
    "i" to id,
    "n" to name,
    "a" to accounts.map { it.toAbbreviatedMap() },
    "v" to isActive
)

fun Account.toAbbreviatedMap(): Map<String, Any?> = mapOf(
    "i" to id,
    "b" to bankId,
    "h" to holder,
    "n" to name,
    "l" to balance,
    "c" to currency,
    "t" to type,
    "o" to notes,
    "v" to isActive
)
*/

// --- Funciones para deserializar desde formato abreviado ---

/**
 * Deserializa un JSON con formato abreviado a un objeto ExportData.
 * Esta función convierte el JSON abreviado (claves cortas como "b", "c", etc.)
 * de vuelta a un objeto ExportData completo.
 */
fun fromAbbreviatedQrJson(json: String): ExportData {
    val parser = Json { ignoreUnknownKeys = true }
    val jsonObj = parser.parseToJsonElement(json).jsonObject
    
    // Extraer bancos (clave "b")
    val banks = if (jsonObj.containsKey("b")) {
        jsonObj["b"]?.jsonArray?.map { bankElement ->
            val bankObj = bankElement.jsonObject
            Bank(
                id = bankObj["i"]?.jsonPrimitive?.long ?: 0L,
                name = bankObj["n"]?.jsonPrimitive?.content ?: "",
                isActive = bankObj["v"]?.jsonPrimitive?.boolean ?: true,
                accounts = bankObj["a"]?.jsonArray?.map { accountElement ->
                    val accountObj = accountElement.jsonObject
                    Account(
                        id = accountObj["i"]?.jsonPrimitive?.long ?: 0L,
                        bankId = accountObj["b"]?.jsonPrimitive?.long ?: 0L,
                        holder = accountObj["h"]?.jsonPrimitive?.content ?: "",
                        name = accountObj["n"]?.jsonPrimitive?.content ?: "",
                        balance = accountObj["l"]?.jsonPrimitive?.double ?: 0.0,
                        currency = accountObj["c"]?.jsonPrimitive?.content ?: "USD",
                        type = accountObj["t"]?.jsonPrimitive?.contentOrNull,
                        notes = accountObj["o"]?.jsonPrimitive?.contentOrNull,
                        isActive = accountObj["v"]?.jsonPrimitive?.boolean ?: true
                    )
                } ?: emptyList()
            )
        } ?: emptyList()
    } else {
        emptyList()
    }
    
    // Para el resto de tipos, usar la deserialización normal
    val investmentPlatforms = if (jsonObj.containsKey("p")) {
        parser.decodeFromJsonElement(ListSerializer(InvestmentPlatformEntity.serializer()), jsonObj["p"]!!)
    } else {
        emptyList()
    }
    
    val investments = if (jsonObj.containsKey("i")) {
        parser.decodeFromJsonElement(ListSerializer(com.example.controlfinancierocompose.data.InvestmentEntity.serializer()), jsonObj["i"]!!)
    } else {
        emptyList()
    }
    
    val calendarEvents = if (jsonObj.containsKey("e")) {
        parser.decodeFromJsonElement(ListSerializer(CalendarEventEntity.serializer()), jsonObj["e"]!!)
    } else {
        emptyList()
    }
    
    val credentials = if (jsonObj.containsKey("r")) {
        parser.decodeFromJsonElement(ListSerializer(Credential.serializer()), jsonObj["r"]!!)
    } else {
        emptyList()
    }
    
    return ExportData(
        banks = banks,
        accounts = emptyList(), // No usamos accounts sueltas, las incluimos en banks
        investmentPlatforms = investmentPlatforms,
        investments = investments,
        calendarEvents = calendarEvents,
        credentials = credentials
    )
}


/**
 * Serializa ExportData a un JSON compacto y abreviado para QR.
 * - Claves de primer nivel abreviadas
 * - Omitir listas vacías
 * - Salida minificada
 */
fun toAbbreviatedQrJson(exportData: ExportData): String {
    val json = Json { encodeDefaults = false; prettyPrint = false }
    
    // Construir el objeto JSON principal
    val objMap = mutableMapOf<String, JsonElement>()
    
    // Serialización abreviada para bancos y cuentas
    if (exportData.banks.isNotEmpty()) {
        val banksList = JsonArray(exportData.banks.map { bank ->
            val bankMap = mutableMapOf<String, JsonElement>(
                "i" to JsonPrimitive(bank.id),
                "n" to JsonPrimitive(bank.name)
            )
            // Solo incluir 'v' si el banco NO está activo
            if (!bank.isActive) bankMap["v"] = JsonPrimitive(bank.isActive)
            // Solo incluir 'a' si hay cuentas
            if (bank.accounts.isNotEmpty()) {
                val accountsArray = JsonArray(bank.accounts.map { account ->
                    val accMap = mutableMapOf<String, JsonElement>(
                        "i" to JsonPrimitive(account.id),
                        "b" to JsonPrimitive(account.bankId),
                        "h" to JsonPrimitive(account.holder),
                        "n" to JsonPrimitive(account.name),
                        "l" to JsonPrimitive(account.balance)
                    )
                    if (account.currency != "USD") accMap["c"] = JsonPrimitive(account.currency)
                    if (account.type != null) accMap["t"] = JsonPrimitive(account.type)
                    if (account.notes != null) accMap["o"] = JsonPrimitive(account.notes)
                    if (!account.isActive) accMap["v"] = JsonPrimitive(account.isActive)
                    JsonObject(accMap)
                })
                bankMap["a"] = accountsArray
            }
            JsonObject(bankMap)
        })
        objMap["b"] = banksList
    }
    
    // Serialización normal para plataformas, eventos y credenciales
    if (exportData.investmentPlatforms.isNotEmpty()) {
        objMap["p"] = json.encodeToJsonElement(ListSerializer(InvestmentPlatformEntity.serializer()), exportData.investmentPlatforms)
    }
    if (exportData.investments.isNotEmpty()) {
        val investmentsList = JsonArray(exportData.investments.map { inv ->
            val invMap = mutableMapOf<String, JsonElement>(
                "i" to JsonPrimitive(inv.id),
                "n" to JsonPrimitive(inv.name)
            )
            if (inv.platformId != 0L) invMap["p"] = JsonPrimitive(inv.platformId)
            if (inv.amount != 0.0) invMap["a"] = JsonPrimitive(inv.amount)
            if (inv.shares != 0.0) invMap["s"] = JsonPrimitive(inv.shares)
            if (inv.price != 0.0) invMap["r"] = JsonPrimitive(inv.price)
            if (inv.date.isNotEmpty()) invMap["d"] = JsonPrimitive(inv.date)
            if (inv.type.isNotEmpty()) invMap["t"] = JsonPrimitive(inv.type)
            if (inv.notes.isNotEmpty()) invMap["o"] = JsonPrimitive(inv.notes)
            if (!inv.isActive) invMap["v"] = JsonPrimitive(inv.isActive)
            JsonObject(invMap)
        })
        objMap["i"] = investmentsList
    }
    if (exportData.calendarEvents.isNotEmpty()) {
        val eventsList = JsonArray(exportData.calendarEvents.map { ev ->
            val evMap = mutableMapOf<String, JsonElement>()
            if (ev.id != 0L) evMap["i"] = JsonPrimitive(ev.id)
            evMap["n"] = JsonPrimitive(ev.name)
            if (ev.description.isNotEmpty()) evMap["d"] = JsonPrimitive(ev.description)
            evMap["f"] = JsonPrimitive(ev.date)
            JsonObject(evMap)
        })
        objMap["e"] = eventsList
    }
    if (exportData.credentials.isNotEmpty()) {
        val credsList = JsonArray(exportData.credentials.map { cred ->
            val credMap = mutableMapOf<String, JsonElement>(
                "p" to JsonPrimitive(cred.platformId),
                "h" to JsonPrimitive(cred.holder)
            )
            if (cred.accountId != null) credMap["a"] = JsonPrimitive(cred.accountId)
            if (!cred.username.isNullOrEmpty()) credMap["u"] = JsonPrimitive(cred.username)
            if (!cred.password.isNullOrEmpty()) credMap["w"] = JsonPrimitive(cred.password)
            JsonObject(credMap)
        })
        objMap["r"] = credsList
    }
    
    // Serializar el objeto final
    return json.encodeToString(JsonObject(objMap))
}


// Formateador para cantidades en formato español
fun formatCantidadES(cantidad: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "ES"))
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    return nf.format(cantidad) + " €"
}

// Modelo de movimiento para el dashboard
data class Movimiento(val descripcion: String, val cantidad: Double, val fecha: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    saldoTotal: Double = 12500.0,
    cuentas: Double = 8000.0,
    inversiones: Double = 4000.0,
    ahorroMensual: Double = 300.0,
    gastosMensuales: Double = 1200.0,
    ingresosMensuales: Double = 1500.0,
    movimientos: List<Movimiento> = listOf(
        Movimiento("Compra supermercado", -45.0, "10/08/2025"),
        Movimiento("Ingreso nómina", 1500.0, "01/08/2025"),
        Movimiento("Transferencia ahorro", -300.0, "05/08/2025"),
        Movimiento("Pago alquiler", -600.0, "01/08/2025")
    ),
    onSendQRBank: () -> Unit = {},
    onSendQRInvestment: () -> Unit = {},
    onSendQRCalendar: () -> Unit = {},
    onSendQRCredentials: () -> Unit = {},
    onExportFile: () -> Unit = {},
    onReceiveQR: () -> Unit = {},
    onImportFile: () -> Unit = {}
) {
    val amountsVisible = remember { mutableStateOf(false) }
    var qrMenuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFF1976D2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { amountsVisible.value = !amountsVisible.value },
                ) {
                    Icon(
                        imageVector = if (amountsVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (amountsVisible.value) "Ocultar montos" else "Mostrar montos",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { qrMenuExpanded = true },
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones QR", tint = Color.White)
                }
                DropdownMenu(
                    expanded = qrMenuExpanded,
                    onDismissRequest = { qrMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    // Grupo: Enviar por QR
                    Text(
                        "Enviar por QR",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    DropdownMenuItem(
                        text = { Text("Cuentas") },
                        onClick = {
                            qrMenuExpanded = false
                            onSendQRBank()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Inversiones") },
                        onClick = {
                            qrMenuExpanded = false
                            onSendQRInvestment()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Calendario") },
                        onClick = {
                            qrMenuExpanded = false
                            onSendQRCalendar()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Credenciales") },
                        onClick = {
                            qrMenuExpanded = false
                            onSendQRCredentials()
                        }
                    )
                    Divider()
                    // Grupo: Archivo
                    Text(
                        "Archivo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    DropdownMenuItem(
                        text = { Text("Exportar archivo") },
                        onClick = {
                            qrMenuExpanded = false
                            onExportFile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Importar archivo") },
                        onClick = {
                            qrMenuExpanded = false
                            onImportFile()
                        }
                    )
                    Divider()
                    // Grupo: Recibir QR
                    DropdownMenuItem(
                        text = { Text("Recibir datos (QR)") },
                        onClick = {
                            qrMenuExpanded = false
                            onReceiveQR()
                        }
                    )
                }
            }
        }
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                SummaryCard("Saldo total", saldoTotal, Icons.Default.AccountBalance, Color(0xFF1976D2), amountsVisible.value)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                SummaryCard("Cuentas", cuentas, Icons.Default.Savings, Color(0xFF388E3C), amountsVisible.value)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                SummaryCard("Inversiones", inversiones, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF0288D1), amountsVisible.value)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text("Indicadores clave", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IndicatorCard("Ahorro mensual", ahorroMensual, Color(0xFF388E3C))
                    IndicatorCard("Gastos", gastosMensuales, Color(0xFFD32F2F))
                    IndicatorCard("Ingresos", ingresosMensuales, Color(0xFF1976D2))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Text("Distribución de activos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                PieChartMock(cuentas, inversiones)
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                MovimientosList(movimientos)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SummaryCard(titulo: String, cantidad: Double, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, amountsVisible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0.12f), Color.White)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .padding(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = titulo, tint = color, modifier = Modifier.size(36.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = color)
                Text(
                    if (amountsVisible) formatCantidadES(cantidad) else "•••••• €",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = color
                )
            }
        }
    }
}

@Composable
fun IndicatorCard(titulo: String, cantidad: Double, color: Color) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(60.dp)
            .background(
                color.copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(1.dp, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = color)
            Text(formatCantidadES(cantidad), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
        }
    }
}

@Composable
fun PieChartMock(cuentas: Double, inversiones: Double) {
    // Aquí iría una gráfica real, pero mostramos una fila de barras de colores como mock
    val minWeight = 0.01f
    val total = cuentas + inversiones
    val cuentasWeight = if (total > 0) (cuentas / total).toFloat().coerceAtLeast(minWeight) else minWeight
    val inversionesWeight = if (total > 0) (inversiones / total).toFloat().coerceAtLeast(minWeight) else minWeight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color.White, RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .weight(cuentasWeight)
            .fillMaxHeight()
            .background(Color(0xFF388E3C), RoundedCornerShape(16.dp)))
        Box(modifier = Modifier
            .weight(inversionesWeight)
            .fillMaxHeight()
            .background(Color(0xFF0288D1), RoundedCornerShape(16.dp)))
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Cuentas", color = Color(0xFF388E3C), fontSize = 12.sp)
        Text("Inversiones", color = Color(0xFF0288D1), fontSize = 12.sp)
    }
}

@Composable
fun MovimientosList(movimientos: List<Movimiento>) {
    Column {
        movimientos.forEach {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .background(Color(0xFFF0F4FF), RoundedCornerShape(8.dp))
                    .shadow(1.dp, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(it.descripcion, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(it.fecha, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(
                        (if (it.cantidad >= 0) "+" else "") + formatCantidadES(it.cantidad),
                        fontWeight = FontWeight.Bold,
                        color = if (it.cantidad >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Serializable

data class ExportData(
    val banks: List<Bank>,
    val accounts: List<Account>,
    val investmentPlatforms: List<InvestmentPlatformEntity>,
    val investments: List<com.example.controlfinancierocompose.data.InvestmentEntity>,
    val calendarEvents: List<CalendarEventEntity>,
    val credentials: List<Credential>
)

@Composable
fun QRDialog(json: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(ComposeColor.White, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Datos para compartir", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            QRCodeImage(json, size = 240.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("${json.length} caracteres", color = ComposeColor.DarkGray, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Escanea este código en el otro dispositivo", color = ComposeColor.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Composable
fun QRCodeImage(content: String, size: Dp) {
    val bitmap = remember(content) { generateQRCodeBitmap(content, size) }
    androidx.compose.foundation.Image(
        bitmap = bitmap,
        contentDescription = "QR",
        modifier = Modifier.size(size)
    )
}

fun generateQRCodeBitmap(content: String, size: Dp): ImageBitmap {
    val pxSize = size.value.toInt() * 3 // Ajuste para densidad
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, pxSize, pxSize)
    val bitmap = android.graphics.Bitmap.createBitmap(pxSize, pxSize, android.graphics.Bitmap.Config.ARGB_8888)
    for (x in 0 until pxSize) {
        for (y in 0 until pxSize) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap.asImageBitmap()
}
