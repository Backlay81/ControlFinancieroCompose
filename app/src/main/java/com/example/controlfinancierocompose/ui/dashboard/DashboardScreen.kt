package com.example.controlfinancierocompose.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.graphics.Color as ComposeColor

// Modelo de movimiento para el dashboard
data class Movimiento(val descripcion: String, val cantidad: Double, val fecha: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    saldoTotal: Double = 12500.0,
    cuentas: Double = 8000.0,
    inversiones: Double = 4000.0,
    deudas: Double = 500.0,
    ahorroMensual: Double = 300.0,
    gastosMensuales: Double = 1200.0,
    ingresosMensuales: Double = 1500.0,
    movimientos: List<Movimiento> = listOf(
        Movimiento("Compra supermercado", -45.0, "10/08/2025"),
        Movimiento("Ingreso nómina", 1500.0, "01/08/2025"),
        Movimiento("Transferencia ahorro", -300.0, "05/08/2025"),
        Movimiento("Pago alquiler", -600.0, "01/08/2025")
    ),
    onSendQR: () -> Unit = {},
    onReceiveQR: () -> Unit = {}
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
                    DropdownMenuItem(
                        text = { Text("Enviar datos") },
                        onClick = {
                            qrMenuExpanded = false
                            onSendQR()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recibir datos") },
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
                SummaryCard("Inversiones", inversiones, Icons.Default.TrendingUp, Color(0xFF0288D1), amountsVisible.value)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                SummaryCard("Deudas", deudas, Icons.Default.MoneyOff, Color(0xFFD32F2F), amountsVisible.value)
                Spacer(modifier = Modifier.height(20.dp))
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
                PieChartMock(cuentas, inversiones, deudas)
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
                    if (amountsVisible) "%.2f €".format(cantidad) else "•••••• €",
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
            Text("%.2f €".format(cantidad), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
        }
    }
}

@Composable
fun PieChartMock(cuentas: Double, inversiones: Double, deudas: Double) {
    // Aquí iría una gráfica real, pero mostramos una fila de barras de colores como mock
    val minWeight = 0.01f
    val total = cuentas + inversiones + deudas
    val cuentasWeight = if (total > 0) (cuentas / total).toFloat().coerceAtLeast(minWeight) else minWeight
    val inversionesWeight = if (total > 0) (inversiones / total).toFloat().coerceAtLeast(minWeight) else minWeight
    val deudasWeight = if (total > 0) (deudas / total).toFloat().coerceAtLeast(minWeight) else minWeight

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
        Box(modifier = Modifier
            .weight(deudasWeight)
            .fillMaxHeight()
            .background(Color(0xFFD32F2F), RoundedCornerShape(16.dp)))
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Cuentas", color = Color(0xFF388E3C), fontSize = 12.sp)
        Text("Inversiones", color = Color(0xFF0288D1), fontSize = 12.sp)
        Text("Deudas", color = Color(0xFFD32F2F), fontSize = 12.sp)
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
                        (if (it.cantidad >= 0) "+" else "") + "%.2f €".format(it.cantidad),
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
            Spacer(modifier = Modifier.height(16.dp))
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
