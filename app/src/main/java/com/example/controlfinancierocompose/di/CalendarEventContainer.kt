package com.example.controlfinancierocompose.di

import android.content.Context
import com.example.controlfinancierocompose.data.AppDatabase
import com.example.controlfinancierocompose.data.CalendarEventRepository

object CalendarEventContainer {
    fun provideCalendarEventRepository(context: Context): CalendarEventRepository {
        val database = AppDatabase.getDatabase(context)
        return CalendarEventRepository(database.calendarEventDao())
    }
}
