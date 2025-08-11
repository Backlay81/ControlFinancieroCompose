package com.example.controlfinancierocompose.data

import kotlinx.coroutines.flow.Flow
class CalendarEventRepository(
    private val dao: CalendarEventDao
) {
    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>> = dao.getEventsForDate(date)
    fun getAllEvents(): Flow<List<CalendarEventEntity>> = dao.getAllEvents()
    suspend fun insertEvent(event: CalendarEventEntity) = dao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEventEntity) = dao.updateEvent(event)
    suspend fun deleteEvent(event: CalendarEventEntity) = dao.deleteEvent(event)
}
