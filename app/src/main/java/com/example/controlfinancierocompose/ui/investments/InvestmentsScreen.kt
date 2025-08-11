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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlfinancierocompose.data.InvestmentEntity
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity
import com.example.controlfinancierocompose.data.model.InvestmentType
import java.text.NumberFormat
import java.util.Locale

// Eliminada clase InvestmentPlatform, ahora se usa InvestmentPlatformEntity y InvestmentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    viewModel: InvestmentsViewModel,
    onNavigate: (Int) -> Unit
) {
    // Estados para confirmación de borrado
    val showDeletePlatformDialog = remember { mutableStateOf(false) }
    val platformToDelete = remember { mutableStateOf<Long?>(null) }
    val showDeleteInvestmentDialog = remember { mutableStateOf(false) }
    val investmentToDelete = remember { mutableStateOf<Pair<Long, Long>?>(null) }

    // Estados para edición de plataformas
    val showEditPlatformDialog = remember { mutableStateOf(false) }
    val platformToEdit = remember { mutableStateOf<Long?>(null) }
    val editPlatformName = remember { mutableStateOf("") }
    val editPlatformActive = remember { mutableStateOf(true) }

    // Estados para edición de inversiones
    val showEditInvestmentDialog = remember { mutableStateOf(false) }
    val investmentToEdit = remember { mutableStateOf<Pair<Long, Long>?>(null) }
    val editInvestmentName = remember { mutableStateOf("") }
    val editInvestmentAmount = remember { mutableStateOf("") }
    val editInvestmentShares = remember { mutableStateOf("") }
    val editInvestmentPrice = remember { mutableStateOf("") }
    val editInvestmentType = remember { mutableStateOf("") }
    val editInvestmentNotes = remember { mutableStateOf("") }
    // Eliminado campo de fecha
    val editInvestmentActive = remember { mutableStateOf(true) }
    // Estados para DatePickerDialog


    // Estados para añadir inversión
    val showAddInvestmentDialog = remember { mutableStateOf(false) }
    val addInvestmentPlatformId = remember { mutableStateOf<Long?>(null) }
    val addInvestmentName = remember { mutableStateOf("") }
    val addInvestmentAmount = remember { mutableStateOf("") }
    val addInvestmentShares = remember { mutableStateOf("") }
    val addInvestmentPrice = remember { mutableStateOf("") }
    val addInvestmentType = remember { mutableStateOf("") }
    val addInvestmentNotes = remember { mutableStateOf("") }
    val addInvestmentDate = remember { mutableStateOf("") }

    // Obtenemos la lista de plataformas del ViewModel
    val platforms by viewModel.platforms.collectAsState(initial = emptyList())
    val investments by viewModel.investments.collectAsState(initial = emptyList())
    val totalInvested = investments.filter { it.isActive }.sumOf { it.amount }
    val platformCount = platforms.size
    val investmentCount = investments.size
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    var amountsVisible = remember { mutableStateOf(true) }
    // Estados para el diálogo de añadir plataforma
    val showAddDialog = remember { mutableStateOf(false) }
    val newPlatformName = remember { mutableStateOf("") }

    // Estado resumen de inversiones
    val investmentState = remember(platforms to investments) {
        val totalAmount = investments.filter { it.isActive }.sumOf { it.amount }
        val activePlatforms = platforms.count { it.isActive }
        val activeInvestments = investments.count { it.isActive }
        object {
            val totalAmount = totalAmount
            val activePlatforms = activePlatforms
            val activeInvestments = activeInvestments
        }
    }

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
                onClick = { showAddDialog.value = true },
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar plataforma")
            }
        }
    ) { padding ->
        if (showAddDialog.value) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showAddDialog.value = false },
                title = { Text("Nueva plataforma de inversión") },
                text = {
                    Column {
                        TextField(
                            value = newPlatformName.value,
                            onValueChange = { newPlatformName.value = it },
                            label = { Text("Nombre de la plataforma") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPlatformName.value.isNotBlank()) {
                            viewModel.addPlatform(newPlatformName.value)
                            newPlatformName.value = ""
                            showAddDialog.value = false
                        }
                    }) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .heightIn(min = 70.dp, max = 130.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF1976D2)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Saldo total",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (amountsVisible.value) {
                                    val formatted = currencyFormatter.format(totalInvested)
                                    val clean = formatted.replace("€", "").trim()
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = clean,
                                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold),
                                        )
                                        Text(
                                            text = " €",
                                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "******",
                                        style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold),
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Inversiones",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    investmentCount.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                Text(
                                    if (amountsVisible.value) "Ocultar" else "Mostrar",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1976D2)),
                                )
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val sortedPlatforms = platforms.sortedBy { it.isActive.not() }
                    items(sortedPlatforms) { platform ->
                        val platformInvestments = investments.filter { it.platformId == platform.id }
                        PlatformCardRoom(
                            platform = platform,
                            investments = platformInvestments,
                            amountsVisible = amountsVisible.value,
                            currencyFormatter = currencyFormatter,
                            onTogglePlatformActive = {
                                viewModel.updatePlatform(platform.copy(isActive = !platform.isActive))
                            },
                            onEditPlatform = {
                                platformToEdit.value = platform.id
                                editPlatformName.value = platform.name
                                editPlatformActive.value = platform.isActive
                                showEditPlatformDialog.value = true
                            },
                            onDeletePlatform = {
                                platformToDelete.value = platform.id
                                showDeletePlatformDialog.value = true
                            },
                            onToggleInvestmentActive = { investmentId: Long ->
                                val investment = platformInvestments.find { it.id == investmentId }
                                if (investment != null) {
                                    viewModel.updateInvestment(investment.copy(isActive = !investment.isActive))
                                }
                            },
                            onEditInvestment = { investmentId: Long ->
                                val investment = platformInvestments.find { it.id == investmentId }
                                if (investment != null) {
                                    investmentToEdit.value = Pair(platform.id, investment.id)
                                    editInvestmentName.value = investment.name
                                    editInvestmentAmount.value = investment.amount.toString()
                                    editInvestmentShares.value = investment.shares.toString()
                                    editInvestmentPrice.value = investment.price.toString()
                                    editInvestmentType.value = investment.type
                                    editInvestmentNotes.value = investment.notes
                                    editInvestmentActive.value = investment.isActive
                                    showEditInvestmentDialog.value = true
                                }
                            },
                            onDeleteInvestment = { investmentId: Long ->
                                val investment = platformInvestments.find { it.id == investmentId }
                                if (investment != null) {
                                    investmentToDelete.value = Pair(platform.id, investment.id)
                                    showDeleteInvestmentDialog.value = true
                                }
                            },
                            onAddInvestment = { platformId ->
    addInvestmentPlatformId.value = platformId
    viewModel.selectPlatform(platformId)
    addInvestmentName.value = ""
    addInvestmentAmount.value = ""
    addInvestmentType.value = ""
    addInvestmentNotes.value = ""
    addInvestmentDate.value = ""
    showAddInvestmentDialog.value = true
                            }
                        )
                    }
                }
            }
            // Diálogo de edición de plataforma
            if (showEditPlatformDialog.value) {
                platformToEdit.value?.let { id ->
                    val platform = platforms.find { it.id == id }
                    if (platform != null) {
                        EditPlatformDialog(
                            platformName = editPlatformName.value,
                            onConfirm = { name ->
                                viewModel.updatePlatform(platform.copy(name = name))
                                showEditPlatformDialog.value = false
                            },
                            onDismiss = { showEditPlatformDialog.value = false },
                            onNameChange = { editPlatformName.value = it }
                        )
                    }
                }
            }

            // Diálogo de confirmación de borrado de plataforma
            if (showDeletePlatformDialog.value && platformToDelete.value != null) {
                val platform = platforms.find { it.id == platformToDelete.value }
                if (platform != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeletePlatformDialog.value = false },
                        title = { Text("Eliminar plataforma") },
                        text = { Text("¿Seguro que quieres eliminar esta plataforma? Se eliminarán todas sus inversiones.") },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.deletePlatform(platform)
                                showDeletePlatformDialog.value = false
                            }) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDeletePlatformDialog.value = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
            // Diálogo de edición de inversión
            if (showEditInvestmentDialog.value && investmentToEdit.value != null) {
                val (platformId, investmentId) = investmentToEdit.value!!
                val investment = investments.find { it.id == investmentId && it.platformId == platformId }
                if (investment != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showEditInvestmentDialog.value = false },
                        title = { Text("Editar inversión") },
                        text = {
                            Column {
                                TextField(
                                    value = editInvestmentName.value,
                                    onValueChange = { editInvestmentName.value = it },
                                    label = { Text("Nombre de la inversión") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = editInvestmentAmount.value,
                                    onValueChange = { editInvestmentAmount.value = it },
                                    label = { Text("Cantidad invertida total") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = editInvestmentShares.value,
                                    onValueChange = { editInvestmentShares.value = it },
                                    label = { Text("Número de acciones") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = editInvestmentPrice.value,
                                    onValueChange = { editInvestmentPrice.value = it },
                                    label = { Text("Precio de compra por acción") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = editInvestmentType.value,
                                    onValueChange = { editInvestmentType.value = it },
                                    label = { Text("Tipo de inversión") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = editInvestmentNotes.value,
                                    onValueChange = { editInvestmentNotes.value = it },
                                    label = { Text("Notas") },
                                    singleLine = false,
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    IconButton(
                                        onClick = { showEditInvestmentDialog.value = false },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFF757575), modifier = Modifier.size(32.dp))
                                    }
                                    Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateInvestment(
                                                investment.copy(
                                                    name = editInvestmentName.value,
                                                    amount = editInvestmentAmount.value.toDoubleOrNull() ?: 0.0,
                                                    shares = editInvestmentShares.value.toDoubleOrNull() ?: 0.0,
                                                    price = editInvestmentPrice.value.toDoubleOrNull() ?: 0.0,
                                                    type = editInvestmentType.value,
                                                    notes = editInvestmentNotes.value,
                                                    isActive = editInvestmentActive.value
                                                )
                                            )
                                            showEditInvestmentDialog.value = false
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Guardar", tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                                    }
                                    Text("Guardar", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    )
                }
            }

            // Diálogo de añadir inversión
            if (showAddInvestmentDialog.value && addInvestmentPlatformId.value != null) {
                // Limpiar campos al abrir el diálogo
                addInvestmentName.value = ""
                addInvestmentAmount.value = ""
                addInvestmentShares.value = ""
                addInvestmentPrice.value = ""
                addInvestmentType.value = ""
                addInvestmentNotes.value = ""
                addInvestmentDate.value = ""
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        showAddInvestmentDialog.value = false
                        // Limpiar campos al cerrar el diálogo
                        addInvestmentName.value = ""
                        addInvestmentAmount.value = ""
                        addInvestmentShares.value = ""
                        addInvestmentPrice.value = ""
                        addInvestmentType.value = ""
                        addInvestmentNotes.value = ""
                        addInvestmentDate.value = ""
                    },
                    title = { Text("Agregar inversión") },
                    text = {
                        Column {
                            TextField(
                                value = addInvestmentName.value,
                                onValueChange = { addInvestmentName.value = it },
                                label = { Text("Nombre de la inversión") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                            TextField(
                                value = addInvestmentAmount.value,
                                onValueChange = { addInvestmentAmount.value = it },
                                label = { Text("Cantidad invertida total") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                            TextField(
                                value = addInvestmentShares.value,
                                onValueChange = { addInvestmentShares.value = it },
                                label = { Text("Número de acciones") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                            TextField(
                                value = addInvestmentPrice.value,
                                onValueChange = { addInvestmentPrice.value = it },
                                label = { Text("Precio de compra por acción") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                            TextField(
                                value = addInvestmentType.value,
                                onValueChange = { addInvestmentType.value = it },
                                label = { Text("Tipo de inversión") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                            TextField(
                                value = addInvestmentNotes.value,
                                onValueChange = { addInvestmentNotes.value = it },
                                label = { Text("Notas") },
                                singleLine = false,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = { showAddInvestmentDialog.value = false },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFF757575), modifier = Modifier.size(32.dp))
                                }
                                Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = {
                                        val platformId = addInvestmentPlatformId.value!!
                                        val name = addInvestmentName.value
                                        val amount = addInvestmentAmount.value.toDoubleOrNull() ?: 0.0
                                        val shares = addInvestmentShares.value.toDoubleOrNull() ?: 0.0
                                        val price = addInvestmentPrice.value.toDoubleOrNull() ?: 0.0
                                        val type = addInvestmentType.value
                                        val notes = addInvestmentNotes.value
                                        val date = addInvestmentDate.value
                                        if (name.isNotBlank()) {
                                            // Get the max investment ID and increment by 1 for the new ID
                                            val investments = viewModel.investments.value
                                            val newId = (investments.maxOfOrNull { it.id } ?: 0) + 1
                                            viewModel.addInvestment(
                                                InvestmentEntity(
                                                    id = newId,
                                                    name = name,
                                                    amount = amount,
                                                    shares = shares,
                                                    price = price,
                                                    type = type,
                                                    notes = notes,
                                                    date = date,
                                                    isActive = true,
                                                    platformId = platformId
                                                )
                                            )
                                            showAddInvestmentDialog.value = false
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Guardar", tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                                }
                                Text("Guardar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                )
            }
            // Diálogo de confirmación de borrado de inversión
            if (showDeleteInvestmentDialog.value && investmentToDelete.value != null) {
                val (platformId, investmentId) = investmentToDelete.value!!
                val investment = investments.find { it.id == investmentId && it.platformId == platformId }
                if (investment != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteInvestmentDialog.value = false },
                        title = { Text("Eliminar inversión") },
                        text = { Text("¿Seguro que quieres eliminar esta inversión?") },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.deleteInvestment(investment)
                                showDeleteInvestmentDialog.value = false
                            }) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDeleteInvestmentDialog.value = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }

}


@Composable
fun PlatformCardRoom(
    platform: InvestmentPlatformEntity,
    investments: List<InvestmentEntity>,
    amountsVisible: Boolean,
    currencyFormatter: NumberFormat,
    onTogglePlatformActive: () -> Unit,
    onEditPlatform: () -> Unit,
    onDeletePlatform: () -> Unit,
    onToggleInvestmentActive: (Long) -> Unit,
    onEditInvestment: (Long) -> Unit,
    onDeleteInvestment: (Long) -> Unit,
    onAddInvestment: (Long) -> Unit
) {
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
            val showMenu = remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    platform.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
                    modifier = Modifier.weight(1f)
                )
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
                IconButton(
                    onClick = { showMenu.value = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(22.dp)
                    )
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(if (platform.isActive) "Marcar como inactivo" else "Marcar como activo") },
                        onClick = {
                            showMenu.value = false
                            onTogglePlatformActive()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (platform.isActive) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (platform.isActive) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Editar plataforma") },
                        onClick = {
                            showMenu.value = false
                            onEditPlatform()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1976D2))
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Eliminar plataforma") },
                        onClick = {
                            showMenu.value = false
                            onDeletePlatform()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                        }
                    )
                }
            }
            Text(
                text = "${investments.size} inversiones",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF757575)),
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )
            val platformTotal = investments.sumOf { it.amount }
            Text(
                text = "Total: ${if (amountsVisible) currencyFormatter.format(platformTotal) else "******"}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (expanded.value) {
                Spacer(modifier = Modifier.height(10.dp))
                val sortedInvestments = investments.sortedBy { it.isActive.not() }
                sortedInvestments.forEachIndexed { idx, investment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (idx == sortedInvestments.lastIndex) 0.dp else 8.dp)
                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = if (!investment.isActive) BorderStroke(2.dp, Color(0xFFD32F2F)) else if (!platform.isActive) BorderStroke(2.dp, Color(0xFFD32F2F)) else BorderStroke(2.dp, Color(0xFF1976D2))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = investment.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onToggleInvestmentActive(investment.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (investment.isActive) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = if (investment.isActive) "Activo" else "Inactivo",
                                        tint = if (investment.isActive) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onEditInvestment(investment.id)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar inversión", tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        onDeleteInvestment(investment.id)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar inversión", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                }
                            }
                            // Mostrar solo los datos que existan o sean relevantes
                            if (investment.shares > 0.0) {
                                Text(text = "Acciones: ${investment.shares}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (investment.price > 0.0) {
                                Text(text = "Precio: ${currencyFormatter.format(investment.price)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (investment.type.isNotBlank()) {
                                Text(text = "Tipo: ${investment.type}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (investment.date.isNotBlank()) {
                                Text(text = "Fecha: ${investment.date}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (investment.notes.isNotBlank()) {
                                Text(text = "Notas: ${investment.notes}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                            Text(text = "Cantidad: ${if (amountsVisible) currencyFormatter.format(investment.amount) else "******"}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF388E3C)), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onAddInvestment(platform.id) },
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Agregar inversión", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}



