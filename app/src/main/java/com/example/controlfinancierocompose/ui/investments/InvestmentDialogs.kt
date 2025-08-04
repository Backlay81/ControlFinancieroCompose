package com.example.controlfinancierocompose.ui.investments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.controlfinancierocompose.data.model.Investment
import com.example.controlfinancierocompose.data.model.InvestmentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditPlatformDialog(
    platformName: String,
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Editar plataforma") },
        text = {
            androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                androidx.compose.material3.OutlinedTextField(
                    value = platformName,
                    onValueChange = onNameChange,
                    label = { androidx.compose.material3.Text("Nombre de la plataforma") },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = { onConfirm(platformName) }, enabled = platformName.isNotBlank()) {
                androidx.compose.material3.Text("Guardar")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Cancelar")
            }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, type: InvestmentType, date: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InvestmentType.SAVINGS) }
    
    // Current date as default
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    var date by remember { mutableStateOf(currentDate) }
    
    // Type dropdown state
    var expanded by remember { mutableStateOf(false) }
    
    // Field validation
    val isNameValid = name.isNotBlank()
    val isAmountValid = amountString.isNotBlank() && amountString.toDoubleOrNull() != null && amountString.toDoubleOrNull()!! > 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar inversión") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isNotBlank() && !isNameValid,
                    supportingText = {
                        if (name.isNotBlank() && !isNameValid) {
                            Text("El nombre no puede estar vacío")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount field
                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountString.isNotBlank() && !isAmountValid,
                    supportingText = {
                        if (amountString.isNotBlank() && !isAmountValid) {
                            Text("Ingrese una cantidad válida mayor a 0")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Tipo de inversión") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        InvestmentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date field
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountString.toDoubleOrNull() ?: 0.0
                    onConfirm(name, amount, selectedType, date)
                },
                enabled = isNameValid && isAmountValid
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvestmentDialog(
    investment: Investment,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, type: InvestmentType, date: String) -> Unit
) {
    var name by remember { mutableStateOf(investment.name) }
    var amountString by remember { mutableStateOf(investment.amount.toString()) }
    var selectedType by remember { mutableStateOf(investment.type) }
    var date by remember { mutableStateOf(investment.date) }
    
    // Type dropdown state
    var expanded by remember { mutableStateOf(false) }
    
    // Field validation
    val isNameValid = name.isNotBlank()
    val isAmountValid = amountString.isNotBlank() && amountString.toDoubleOrNull() != null && amountString.toDoubleOrNull()!! > 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar inversión") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isNotBlank() && !isNameValid,
                    supportingText = {
                        if (name.isNotBlank() && !isNameValid) {
                            Text("El nombre no puede estar vacío")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount field
                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountString.isNotBlank() && !isAmountValid,
                    supportingText = {
                        if (amountString.isNotBlank() && !isAmountValid) {
                            Text("Ingrese una cantidad válida mayor a 0")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Tipo de inversión") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        InvestmentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date field
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountString.toDoubleOrNull() ?: 0.0
                    onConfirm(name, amount, selectedType, date)
                },
                enabled = isNameValid && isAmountValid
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
