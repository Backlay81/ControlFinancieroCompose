package com.example.controlfinancierocompose.ui.accounts

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(accountsViewModel: AccountsViewModel, onNavigate: (Int) -> Unit = {}) {
    val banks by accountsViewModel.banks.collectAsState()
    // Ordenar bancos: activos primero, inactivos al final
    val sortedBanks = banks.sortedWith(compareBy<Bank> { it.isActive.not() }
        .thenByDescending { it.accounts.sumOf { acc -> acc.balance } })
    val amountsVisible = remember { mutableStateOf(false) }
    val totalBalance = banks.flatMap { it.accounts }.sumOf { it.balance }
    val accountCount = banks.sumOf { it.accounts.size }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    var showAddBankDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var selectedBankId by remember { mutableStateOf<Long?>(null) }
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var showDeleteBankDialog by remember { mutableStateOf(false) }
    var bankToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    var bankToEdit by remember { mutableStateOf<Bank?>(null) }
    var showEditBankDialog by remember { mutableStateOf(false) }

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
                    "Bancos y Cuentas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBankDialog = true },
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar banco")
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
                                        val formatted = currencyFormatter.format(totalBalance)
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
                                        "Cuentas",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        accountCount.toString(),
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
                }
                
                // Mensaje cuando no hay bancos (ahora después de la tarjeta de resumen)
                if (sortedBanks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(40.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                "Sin bancos",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Añade tu primer banco para comenzar a gestionar tus cuentas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                
                items(sortedBanks) { bank ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = if (!bank.isActive) {
                            androidx.compose.foundation.BorderStroke(
                                width = 3.dp,
                                color = Color(0xFFD32F2F)
                            )
                        } else {
                            androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color.Black
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            // Menú de opciones agrupadas (overflow menu)
                            val showMenu = remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = bank.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
                                    modifier = Modifier.weight(1f)
                                )
                                // Expandir/colapsar
                                IconButton(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.Info else Icons.Default.Add,
                                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                // Menú de tres puntos
                                IconButton(
                                    onClick = { showMenu.value = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
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
                                        text = { Text(if (bank.isActive) "Marcar como inactivo" else "Marcar como activo") },
                                        onClick = {
                                            showMenu.value = false
                                            val updated = bank.copy(isActive = !bank.isActive)
                                            accountsViewModel.updateBank(updated)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (bank.isActive) Icons.Default.Close else Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (bank.isActive) Color(0xFFD32F2F) else Color(0xFF388E3C)
                                            )
                                        }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Editar banco") },
                                        onClick = {
                                            showMenu.value = false
                                            bankToEdit = bank
                                            showEditBankDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1976D2))
                                        }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Eliminar banco") },
                                        onClick = {
                                            showMenu.value = false
                                            bankToDelete = bank.id
                                            showDeleteBankDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                                        }
                                    )
                                }
                            }
                            Text(
                                text = "${bank.accounts.size} cuentas",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF757575)),
                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                            )
                            
                            // Mostrar saldo total del banco
                            val bankTotalBalance = bank.accounts.sumOf { it.balance }
                            Text(
                                text = "Saldo total: ${if (amountsVisible.value) currencyFormatter.format(bankTotalBalance) else "******"}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF388E3C),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            if (expanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                // Ordenar cuentas: activas primero, inactivas al final
                                val sortedAccounts = bank.accounts.sortedBy { it.isActive.not() }
                                sortedAccounts.forEachIndexed { idx, account ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (idx == bank.accounts.lastIndex) 0.dp else 8.dp)
                                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                        elevation = CardDefaults.cardElevation(1.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 2.dp,
                                            color = if (!account.isActive) Color(0xFFD32F2F) else Color(0xFF1976D2)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            // Titulares (en negrita) con botones de acción en la misma fila que el primer titular
                                            val holders = account.holder.split(", ")
                                            
                                            // Mostrar todos los titulares, cada uno en una línea, el primero con los botones de acción
                                            holders.forEachIndexed { idx, holder ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Titular: $holder",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (idx == 0) {
                                                        // Tick/Cross primero
                                                        IconButton(
                                                            onClick = {
                                                                val updated = account.copy(isActive = !account.isActive)
                                                                accountsViewModel.updateAccount(account.bankId, updated)
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (account.isActive) Icons.Default.Check else Icons.Default.Close,
                                                                contentDescription = if (account.isActive) "Activo" else "Inactivo",
                                                                tint = if (account.isActive) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                        // Botones de acción después
                                                        IconButton(
                                                            onClick = {
                                                                accountToEdit = account
                                                                showEditAccountDialog = true
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Editar cuenta", tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                accountToDelete = Pair(bank.id, account.id)
                                                                showDeleteAccountDialog = true
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar cuenta", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Tipo de cuenta (si existe)
                                            if (account.type?.isNotBlank() == true) {
                                                Text(
                                                    text = "Tipo: ${account.type}", 
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                            
                                            // Número de cuenta
                                            Text(
                                                text = "Cuenta: ${account.name}", 
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            
                                            // Saldo
                                            Text(
                                                text = "Saldo: ${if (amountsVisible.value) currencyFormatter.format(account.balance) else "******"} ${account.currency}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF388E3C)),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            
                                            // Notas (si existen)
                                            if (account.notes?.isNotBlank() == true) {
                                                Text(
                                                    text = "Notas: ${account.notes}", 
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        selectedBankId = bank.id
                                        showAddAccountDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Agregar cuenta", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            // Diálogos fuera del LazyColumn
            if (showAddBankDialog) {
                AddBankDialog(
                    onAdd = { bankName ->
                        val newId = (banks.maxOfOrNull { it.id } ?: 0) + 1
                        accountsViewModel.addBank(Bank(newId, bankName))
                        showAddBankDialog = false
                    },
                    onDismiss = { showAddBankDialog = false }
                )
            }
            if (showAddAccountDialog && selectedBankId != null) {
                AddAccountDialog(
                    onAdd = { holder, accountNumber, balance, type, currency, notes ->
                        val bankId = selectedBankId!!
                        val newId = (banks.flatMap { it.accounts }.maxOfOrNull { it.id } ?: 0) + 1
                        accountsViewModel.addAccount(
                            bankId,
                            Account(
                                id = newId,
                                bankId = bankId,
                                holder = holder,
                                name = accountNumber,
                                balance = balance,
                                currency = currency,
                                type = type,
                                notes = notes
                            )
                        )
                        showAddAccountDialog = false
                        selectedBankId = null
                    },
                    onDismiss = {
                        showAddAccountDialog = false
                        selectedBankId = null
                    }
                )
            }
            if (showEditAccountDialog && accountToEdit != null) {
                EditAccountDialog(
                    account = accountToEdit!!,
                    onEdit = { editedAccount ->
                        accountsViewModel.updateAccount(editedAccount.bankId, editedAccount)
                        showEditAccountDialog = false
                        accountToEdit = null
                    },
                    onDismiss = {
                        showEditAccountDialog = false
                        accountToEdit = null
                    }
                )
            }
            if (showDeleteBankDialog && bankToDelete != null) {
                ConfirmDialog(
                    title = "Eliminar banco",
                    message = "¿Seguro que quieres eliminar este banco y todas sus cuentas?",
                    onConfirm = {
                        accountsViewModel.removeBank(bankToDelete!!)
                        showDeleteBankDialog = false
                        bankToDelete = null
                    },
                    onDismiss = {
                        showDeleteBankDialog = false
                        bankToDelete = null
                    }
                )
            }
            if (showDeleteAccountDialog && accountToDelete != null) {
                ConfirmDialog(
                    title = "Eliminar cuenta",
                    message = "¿Seguro que quieres eliminar esta cuenta?",
                    onConfirm = {
                        accountsViewModel.removeAccount(accountToDelete!!.first, accountToDelete!!.second)
                        showDeleteAccountDialog = false
                        accountToDelete = null
                    },
                    onDismiss = {
                        showDeleteAccountDialog = false
                        accountToDelete = null
                    }
                )
            }
        }
    }
}

@Composable
fun AddBankDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Banco") },
        text = {
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Nombre del banco") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bankName.isNotBlank()) {
                        onAdd(bankName)
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    onAdd: (String, String, Double, String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
val holders = remember { mutableStateListOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showOtherTypeField by remember { mutableStateOf(false) }
    var otherType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val currency = "EUR" // Default currency
    val typeOptions = listOf("Corriente", "Ahorros", "Tarjeta de crédito", "Inversión", "Otros")
    val typeFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cuenta") },
        text = {
            Column {
                holders.forEachIndexed { idx, value ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { holders[idx] = it },
                            label = { Text("Titular ${idx + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(bottom = 4.dp)
                        )
                        if (holders.size > 1) {
                            IconButton(
                                onClick = { holders.removeAt(idx) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar titular", tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Número de cuenta") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Saldo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo de cuenta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    singleLine = false
                )
                // Moneda por defecto: EUR
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFF757575), modifier = Modifier.size(32.dp))
                    }
                    Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = {
                            val bal = balance.toDoubleOrNull() ?: 0.0
                            val finalType = if (type == "Otros") otherType else type
                            val joinedHolders = holders.filter { it.isNotBlank() }.joinToString(", ")
                            if (joinedHolders.isNotBlank() && accountNumber.isNotBlank()) {
                                onAdd(joinedHolders, accountNumber, bal, finalType, currency, if (notes.isBlank()) null else notes)
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Guardar", tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                    }
                    Text("Guardar", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = { holders.add("") },
                        enabled = holders.size < 10,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir titular", tint = Color(0xFF388E3C), modifier = Modifier.size(32.dp))
                    }
                    Text("Añadir titular", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(account: Account, onEdit: (Account) -> Unit, onDismiss: () -> Unit) {
    val holders = remember { mutableStateListOf<String>().apply { addAll(account.holder.split(", ").map { it.trim() }) } }
    var accountNumber by remember { mutableStateOf(account.name) }
    var balance by remember { mutableStateOf(account.balance.toString()) }
    var type by remember { mutableStateOf(account.type ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var showOtherTypeField by remember { mutableStateOf(false) }
    var otherType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(account.notes ?: "") }
    val currency = "EUR" // Default currency
    val typeOptions = listOf("Corriente", "Ahorros", "Tarjeta de crédito", "Inversión", "Otros")
    val typeFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cuenta") },
        text = {
            Column {
                holders.forEachIndexed { idx, value ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { holders[idx] = it },
                            label = { Text("Titular ${idx + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(bottom = 4.dp)
                        )
                        if (holders.size > 1) {
                            IconButton(
                                onClick = { holders.removeAt(idx) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar titular", tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Número de cuenta") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Saldo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo de cuenta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    singleLine = false
                )
                // Moneda por defecto: EUR
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFF757575), modifier = Modifier.size(32.dp))
                    }
                    Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = {
                            val bal = balance.toDoubleOrNull() ?: 0.0
                            val finalType = if (type == "Otros") otherType else type
                            val joinedHolders = holders.filter { it.isNotBlank() }.joinToString(", ")
                            if (joinedHolders.isNotBlank() && accountNumber.isNotBlank()) {
                                onEdit(account.copy(
                                    holder = joinedHolders, 
                                    name = accountNumber, 
                                    balance = bal, 
                                    currency = currency,
                                    type = finalType,
                                    notes = if (notes.isBlank()) null else notes
                                ))
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Guardar", tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                    }
                    Text("Guardar", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = { holders.add("") },
                        enabled = holders.size < 10,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir titular", tint = Color(0xFF388E3C), modifier = Modifier.size(32.dp))
                    }
                    Text("Añadir titular", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Confirmar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
