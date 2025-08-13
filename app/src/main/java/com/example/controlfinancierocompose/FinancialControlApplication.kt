package com.example.controlfinancierocompose

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.example.controlfinancierocompose.data.FinancialRepository
import com.example.controlfinancierocompose.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinancialControlApplication : Application() {
    // Expose CalendarEventRepository
    val calendarEventRepository: com.example.controlfinancierocompose.data.CalendarEventRepository? by lazy {
        try {
            val db = com.example.controlfinancierocompose.data.AppDatabase.getDatabase(this)
            com.example.controlfinancierocompose.data.CalendarEventRepository(db.calendarEventDao())
        } catch (_: Exception) { null }
    }
    // Lazy initialization of the repository
    val repository: FinancialRepository by lazy {
        AppContainer.provideFinancialRepository(this)
    }
    
    // Flag para evitar cargar datos de muestra múltiples veces
    private var sampleDataLoaded = false
    
    // Carga datos de muestra si la base de datos está vacía
    fun loadSampleDataIfNeeded() {
        if (sampleDataLoaded) return
        
        // Lanzamos una corrutina para cargar datos de muestra si es necesario
        kotlinx.coroutines.MainScope().launch {
            val banks = repository.allBanks.firstOrNull()
            
            // Si no hay bancos, añadimos datos de muestra
            if (banks.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    repository.addSampleData()
                }
            }
            
            sampleDataLoaded = true
        }
    }
}
