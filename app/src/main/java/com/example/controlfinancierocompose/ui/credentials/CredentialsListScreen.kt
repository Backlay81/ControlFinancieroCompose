package com.example.controlfinancierocompose.ui.credentials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.controlfinancierocompose.ui.accounts.Bank
import com.example.controlfinancierocompose.ui.accounts.Account
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsListScreen(
    banks: List<Bank>,
    investmentPlatforms: List<InvestmentPlatformEntity>,
    getAccountsForBank: (Long) -> List<Account>,
    getHoldersForAccount: (Account) -> List<String>,
    getCredential: (platformId: Long, accountId: Long?, holder: String) -> Credential?,
    onSaveCredential: (Credential) -> Unit
) {
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
                    "Credenciales",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    ) { innerPadding ->
        var selectedTab by remember { mutableStateOf(0) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bancos") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Bancos") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Inversiones") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Inversiones") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(banks) { bank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "Banco",
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        bank.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFF1976D2), fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                
                                val accounts = getAccountsForBank(bank.id)
                                val holders = accounts.flatMap { getHoldersForAccount(it) }.distinct()
                                holders.forEach { holder ->
                                    // Estado para controlar la expansión del titular
                                    var expanded by remember { mutableStateOf(false) }
                                    // Estado para controlar si se muestra la contraseña
                                    var showPassword by remember { mutableStateOf(false) }
                                    val credential = getCredential(bank.id, null, holder)
                                    var username by remember { mutableStateOf(credential?.username ?: "") }
                                    var password by remember { mutableStateOf(if (credential?.password == null || credential.password == "null") "" else credential.password) }
                                    
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        elevation = CardDefaults.cardElevation(2.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                                        onClick = { expanded = !expanded }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.AccountCircle,
                                                        contentDescription = "Titular",
                                                        tint = Color(0xFF1976D2),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        holder,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                }
                                                // Icono para indicar expansión
                                                Icon(
                                                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (expanded) "Contraer" else "Expandir",
                                                    tint = Color(0xFF1976D2)
                                                )
                                            }
                                            
                                            // Contenido expandible
                                            if (expanded) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFFE3F2FD),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.AccountCircle,
                                                        contentDescription = "Usuario",
                                                        tint = Color(0xFF1976D2),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    TextField(
                                                        value = username,
                                                        onValueChange = { newUser ->
                                                            username = newUser
                                                            onSaveCredential(
                                                                Credential(
                                                                    bank.id,
                                                                    null,
                                                                    holder,
                                                                    newUser,
                                                                    password
                                                                )
                                                            )
                                                        },
                                                        placeholder = { Text("Usuario") },
                                                        singleLine = true,
                                                        colors = TextFieldDefaults.colors(
                                                            unfocusedContainerColor = Color.Transparent,
                                                            focusedContainerColor = Color.Transparent,
                                                            unfocusedIndicatorColor = Color.Transparent,
                                                            focusedIndicatorColor = Color.Transparent
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFFE3F2FD),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.VpnKey,
                                                        contentDescription = "Contraseña",
                                                        tint = Color(0xFF1976D2),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    TextField(
                                                        value = password,
                                                        onValueChange = { newPass ->
                                                            password = newPass
                                                            onSaveCredential(
                                                                Credential(
                                                                    bank.id,
                                                                    null,
                                                                    holder,
                                                                    username,
                                                                    newPass
                                                                )
                                                            )
                                                        },
                                                        placeholder = { Text("Contraseña") },
                                                        singleLine = true,
                                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                                        trailingIcon = {
                                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                                Icon(
                                                                    if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                                    contentDescription = "Mostrar/ocultar",
                                                                    tint = Color(0xFF1976D2)
                                                                )
                                                            }
                                                        },
                                                        colors = TextFieldDefaults.colors(
                                                            unfocusedContainerColor = Color.Transparent,
                                                            focusedContainerColor = Color.Transparent,
                                                            unfocusedIndicatorColor = Color.Transparent,
                                                            focusedIndicatorColor = Color.Transparent
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(investmentPlatforms) { platform ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = "Plataforma",
                                        tint = Color(0xFF388E3C),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        platform.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFF388E3C),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                
                                val holder = "Titular"
                                // Estado para controlar la expansión del titular
                                var expanded by remember { mutableStateOf(false) }
                                // Estado para controlar si se muestra la contraseña
                                var showPassword by remember { mutableStateOf(false) }
                                val credential = getCredential(platform.id, null, holder)
                                
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    onClick = { expanded = !expanded }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AccountCircle, 
                                                    contentDescription = "Titular", 
                                                    tint = Color(0xFF388E3C), 
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    holder, 
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                                )
                                            }
                                            // Icono para indicar expansión
                                            Icon(
                                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (expanded) "Contraer" else "Expandir",
                                                tint = Color(0xFF388E3C)
                                            )
                                        }
                                        
                                        // Contenido expandible
                                        if (expanded) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.AccountCircle, 
                                                    contentDescription = "Usuario", 
                                                    tint = Color(0xFF388E3C), 
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                TextField(
                                                    value = credential?.username ?: "",
                                                    onValueChange = { newUser ->
                                                        onSaveCredential(Credential(platform.id, null, holder, newUser, credential?.password))
                                                    },
                                                    placeholder = { Text("Usuario") },
                                                    singleLine = true,
                                                    colors = TextFieldDefaults.colors(
                                                        unfocusedContainerColor = Color.Transparent,
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent,
                                                        focusedIndicatorColor = Color.Transparent
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.VpnKey, 
                                                    contentDescription = "Contraseña", 
                                                    tint = Color(0xFF388E3C), 
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                TextField(
                                                    value = credential?.password ?: "",
                                                    onValueChange = { newPass ->
                                                        onSaveCredential(Credential(platform.id, null, holder, credential?.username, newPass))
                                                    },
                                                    placeholder = { Text("Contraseña") },
                                                    singleLine = true,
                                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                                    trailingIcon = {
                                                        IconButton(onClick = { showPassword = !showPassword }) {
                                                            Icon(
                                                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                                                                contentDescription = "Mostrar/ocultar", 
                                                                tint = Color(0xFF388E3C)
                                                            )
                                                        }
                                                    },
                                                    colors = TextFieldDefaults.colors(
                                                        unfocusedContainerColor = Color.Transparent,
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent,
                                                        focusedIndicatorColor = Color.Transparent
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
