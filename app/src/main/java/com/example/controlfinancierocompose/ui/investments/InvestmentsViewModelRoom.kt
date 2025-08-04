package com.example.controlfinancierocompose.ui.investments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlfinancierocompose.data.AppDatabase
import com.example.controlfinancierocompose.data.InvestmentPlatformEntity
import com.example.controlfinancierocompose.data.InvestmentEntity
import com.example.controlfinancierocompose.data.PlatformDao
import com.example.controlfinancierocompose.data.InvestmentDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvestmentsViewModelRoom(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    private val platformDao: PlatformDao = db.platformDao()
    private val investmentDao: InvestmentDao = db.investmentDao()

    private val _platforms = MutableStateFlow<List<InvestmentPlatformEntity>>(emptyList())
    val platforms: StateFlow<List<InvestmentPlatformEntity>> = _platforms

    private val _investments = MutableStateFlow<List<InvestmentEntity>>(emptyList())
    val investments: StateFlow<List<InvestmentEntity>> = _investments

    private var selectedPlatformId: Long? = null

    init {
        viewModelScope.launch {
            _platforms.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.getAllPlatforms()
            }
        }
    }

    fun addPlatform(name: String) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val entity = InvestmentPlatformEntity(name = name)
                platformDao.insertPlatform(entity)
            }
            _platforms.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.getAllPlatforms()
            }
        }
    }

    fun updatePlatform(platform: InvestmentPlatformEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.updatePlatform(platform)
            }
            _platforms.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.getAllPlatforms()
            }
        }
    }

    fun deletePlatform(platform: InvestmentPlatformEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.deletePlatform(platform)
            }
            _platforms.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                platformDao.getAllPlatforms()
            }
            // If deleted platform was selected, clear investments
            if (selectedPlatformId == platform.id) {
                _investments.value = emptyList()
                selectedPlatformId = null
            }
        }
    }

    fun selectPlatform(platformId: Long) {
        selectedPlatformId = platformId
        viewModelScope.launch {
            _investments.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                investmentDao.getInvestmentsForPlatform(platformId)
            }
        }
    }

    fun addInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                investmentDao.insertInvestment(investment)
            }
            selectedPlatformId?.let {
                _investments.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    investmentDao.getInvestmentsForPlatform(it)
                }
            }
        }
    }

    fun updateInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                investmentDao.updateInvestment(investment)
            }
            selectedPlatformId?.let {
                _investments.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    investmentDao.getInvestmentsForPlatform(it)
                }
            }
        }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                investmentDao.deleteInvestment(investment)
            }
            selectedPlatformId?.let {
                _investments.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    investmentDao.getInvestmentsForPlatform(it)
                }
            }
        }
    }
}
