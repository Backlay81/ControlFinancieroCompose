
package com.example.controlfinancierocompose.ui.investments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import com.example.controlfinancierocompose.data.model.Investment
import com.example.controlfinancierocompose.data.model.InvestmentType
import com.example.controlfinancierocompose.navigation.Screen
import java.text.NumberFormat
import java.util.Locale

data class InvestmentPlatform(val id: Long, val name: String, val isActive: Boolean = true, val investments: List<Investment> = emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    viewModel: InvestmentsViewModel,
    onNavigate: (Int) -> Unit
) {
    // Datos de ejemplo
    val platforms = listOf(
        InvestmentPlatform(
            id = 1,
            name = "MyPlatform",
            isActive = true,
            investments = listOf(
                Investment(
                    id = "1", 
                    name = "Crypto", 
                    amount = 1000.0, 
                    type = InvestmentType.CRYPTO, 
                    date = "2023-01-01"
                ),
                Investment(
                    id = "2", 
                    name = "Stocks", 
                    amount = 500.0, 
                    type = InvestmentType.STOCKS, 
                    date = "2023-02-15"
                )
            )
        ),
        InvestmentPlatform(
            id = 2,
            name = "OtherPlatform",
            isActive = false,
            investments = listOf(
                Investment(
                    id = "3", 
                    name = "ETF", 
                    amount = 200.0, 
                    type = InvestmentType.MUTUAL_FUNDS, 
                    date = "2023-03-20"
                )
            )
        )
    )
    val totalInvested = platforms.flatMap { it.investments }.sumOf { it.amount }
    val platformCount = platforms.size
    val investmentCount = platforms.sumOf { it.investments.size }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    var amountsVisible = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Inversiones", 
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Acción agregar plataforma */ },
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar plataforma")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .heightIn(min = 70.dp, max = 130.dp)
                            .shadow(6.dp, RoundedCornerShape(22.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(2.dp, Color(0xFF1976D2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Total invertido", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)))
                                Spacer(modifier = Modifier.height(2.dp))
                                if (amountsVisible.value) {
                                    val formatted = currencyFormatter.format(totalInvested)
                                    val clean = formatted.replace("€", "").trim()
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                                        Text(text = clean, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold))
                                        Text(text = " €", style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold), modifier = Modifier.padding(start = 2.dp, bottom = 2.dp))
                                    }
                                } else {
                                    Text(text = "******", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold))
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Plataformas", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(platformCount.toString(), style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = { amountsVisible.value = !amountsVisible.value },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (amountsVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (amountsVisible.value) "Ocultar montos" else "Mostrar montos",
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(if (amountsVisible.value) "Ocultar" else "Mostrar", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1976D2)))
                            }
                        }
                    }
                }
                items(platforms.sortedBy { it.isActive.not() }) { platform ->
                    PlatformCard(platform = platform, amountsVisible = amountsVisible.value, currencyFormatter = currencyFormatter)
                }
            }
        }
    }
}

@Composable
fun PlatformCard(platform: InvestmentPlatform, amountsVisible: Boolean, currencyFormatter: NumberFormat) {
    val expanded = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(24.dp),
        border = if (!platform.isActive) BorderStroke(3.dp, Color(0xFFD32F2F)) else BorderStroke(1.dp, Color.Black)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(platform.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)), modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { expanded.value = !expanded.value },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (expanded.value) Icons.Default.Info else Icons.Default.Add,
                        contentDescription = if (expanded.value) "Colapsar" else "Expandir",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(text = "${platform.investments.size} inversiones", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF757575)), modifier = Modifier.padding(top = 2.dp, bottom = 2.dp))
            val platformTotal = platform.investments.sumOf { it.amount }
            Text(text = "Total: ${if (amountsVisible) currencyFormatter.format(platformTotal) else "******"}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 6.dp))
            if (expanded.value) {
                Spacer(modifier = Modifier.height(10.dp))
                val sortedInvestments = platform.investments
                sortedInvestments.forEachIndexed { idx, investment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (idx == platform.investments.lastIndex) 0.dp else 8.dp)
                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF1976D2))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = investment.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Cantidad: ${if (amountsVisible) currencyFormatter.format(investment.amount) else "******"}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF388E3C)), modifier = Modifier.padding(top = 4.dp))
                            // Aquí puedes agregar más detalles de la inversión si lo deseas
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Botón para agregar inversión (opcional)
                /*Button(
                    onClick = { /* TODO: Acción agregar inversión */ },
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Agregar inversión", style = MaterialTheme.typography.labelLarge)
                }*/
            }
        }
    }
}