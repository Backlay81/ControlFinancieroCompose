package com.example.controlfinancierocompose.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlfinancierocompose.data.CalendarEventEntity
import com.example.controlfinancierocompose.data.AppDatabase
import com.example.controlfinancierocompose.data.CalendarEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Context

class CalendarViewModel(context: Context) : ViewModel() {
    private val repository: CalendarEventRepository = CalendarEventRepository(
        AppDatabase.getDatabase(context).calendarEventDao()
    )
    private val _events = MutableStateFlow<List<CalendarEventEntity>>(emptyList())
    val events: StateFlow<List<CalendarEventEntity>> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEvents().collect { list ->
                _events.value = list
            }
        }
    }

    fun getEventsForDate(date: LocalDate): List<CalendarEventEntity> {
        val isoDate = date.format(DateTimeFormatter.ISO_DATE)
        return _events.value.filter { it.date == isoDate }
    }

    fun addEvent(name: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            val isoDate = date.format(DateTimeFormatter.ISO_DATE)
            repository.insertEvent(CalendarEventEntity(name = name, description = description, date = isoDate))
        }
    }

    fun updateEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
}
