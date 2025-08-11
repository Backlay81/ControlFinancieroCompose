package com.example.controlfinancierocompose.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.controlfinancierocompose.data.CalendarEventEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val viewModel = remember { CalendarViewModel(context) }
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // 0=Sunday
    val days = (1..daysInMonth).toList()

    var selectedDay by remember { mutableStateOf(today.dayOfMonth) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showEditEventDialog by remember { mutableStateOf(false) }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var editEventIndex by remember { mutableStateOf(-1) }
    // Estado para confirmación de borrado
    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<CalendarEventEntity?>(null) }

    val selectedDate = currentMonth.atDay(selectedDay.coerceIn(1, daysInMonth))
    val isoSelectedDate = selectedDate.format(DateTimeFormatter.ISO_DATE)
    val allEvents by viewModel.events.collectAsState()
    val eventsForSelectedDay = allEvents.filter { it.date == isoSelectedDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // TopAppBar estilo cuentas/inversiones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFF1976D2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Calendario",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Navegación de meses
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minusMonths(1)
                selectedDay = 1
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior", tint = Color(0xFF1976D2))
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier,
                color = Color(0xFF1976D2)
            )
            IconButton(onClick = {
                currentMonth = currentMonth.plusMonths(1)
                selectedDay = 1
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente", tint = Color(0xFF1976D2))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Calendar grid
        val totalCells = dayOfWeekOffset + daysInMonth
        val weeks = (totalCells / 7) + if (totalCells % 7 != 0) 1 else 0
        var dayIndex = 0
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                for (d in 0..6) {
                    val cellIndex = week * 7 + d
                    if (cellIndex < dayOfWeekOffset || dayIndex >= daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(40.dp)) {}
                    } else {
                        val day = days[dayIndex]
                        val date = currentMonth.atDay(day)
                        val isoDate = date.format(DateTimeFormatter.ISO_DATE)
                        val hasEvents = allEvents.any { it.date == isoDate }
                        val isToday = today.dayOfMonth == day && today.month == currentMonth.month && today.year == currentMonth.year
                        val isSelected = selectedDay == day
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    when {
                                        isToday -> Color(0xFF1976D2)
                                        isSelected -> Color(0xFF90CAF9)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isToday -> Color.White
                                        isSelected -> Color.Black
                                        else -> Color.Black
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (hasEvents) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF1976D2), shape = MaterialTheme.shapes.small)
                                    )
                                }
                            }
                        }
                        dayIndex++
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showAddEventDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Añadir evento")
        }

        // Lista de eventos del día seleccionado
        Text(
            "Eventos del ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        if (eventsForSelectedDay.isEmpty()) {
            Text(
                "No hay eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        } else {
            eventsForSelectedDay.forEachIndexed { idx, event ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.name, style = MaterialTheme.typography.titleSmall)
                            if (event.description.isNotBlank()) {
                                Text(event.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                            }
                        }
                        IconButton(onClick = {
                            // Editar evento
                            eventName = event.name
                            eventDescription = event.description
                            editEventIndex = idx
                            showEditEventDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1976D2))
                        }
                        IconButton(onClick = {
                            // Mostrar confirmación de borrado
                            eventToDelete = event
                            showDeleteEventDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                        }
        // Diálogo de confirmación para eliminar evento
        if (showDeleteEventDialog && eventToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteEventDialog = false
                    eventToDelete = null
                },
                title = { Text("¿Eliminar evento?") },
                text = { Text("¿Estás seguro de que quieres eliminar el evento '${eventToDelete?.name}'?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteEvent(eventToDelete!!)
                        showDeleteEventDialog = false
                        eventToDelete = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteEventDialog = false
                        eventToDelete = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
                    }
                }
            }
        }

        // Diálogo para añadir evento
        if (showAddEventDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddEventDialog = false
                    eventName = ""
                    eventDescription = ""
                },
                title = { Text("Añadir evento") },
                text = {
                    Column {
                        Text("Día seleccionado: ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = { Text("Nombre del evento") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = { Text("Descripción") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventName.isNotBlank()) {
                                viewModel.addEvent(eventName, eventDescription, selectedDate)
                                showAddEventDialog = false
                                eventName = ""
                                eventDescription = ""
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showAddEventDialog = false
                        eventName = ""
                        eventDescription = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para editar evento
        if (showEditEventDialog && editEventIndex >= 0) {
            val eventToEdit = eventsForSelectedDay.getOrNull(editEventIndex)
            AlertDialog(
                onDismissRequest = {
                    showEditEventDialog = false
                    eventName = ""
                    eventDescription = ""
                    editEventIndex = -1
                },
                title = { Text("Editar evento") },
                text = {
                    Column {
                        Text("Día seleccionado: ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = { Text("Nombre del evento") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = { Text("Descripción") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventName.isNotBlank() && eventToEdit != null) {
                                viewModel.updateEvent(eventToEdit.copy(name = eventName, description = eventDescription))
                                showEditEventDialog = false
                                eventName = ""
                                eventDescription = ""
                                editEventIndex = -1
                            }
                        }
                    ) {
                        Text("Guardar cambios")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showEditEventDialog = false
                        eventName = ""
                        eventDescription = ""
                        editEventIndex = -1
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
