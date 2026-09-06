package com.geovoice.app.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovoice.app.core.permission.PermissionManager
import com.geovoice.app.data.preferences.PreferencesRepository
import com.geovoice.app.service.LocationForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HomeUiState(
    val isTracking: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val canOfferResume: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        viewModelScope.launch {
            val wasActive = preferences.wasTrackingActive.first()
            // Section 41 : on ne relance jamais automatiquement, on propose seulement.
            _uiState.value = _uiState.value.copy(canOfferResume = wasActive)
        }
    }

    fun refreshPermissionState() {
        val context = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = PermissionManager.hasAnyLocation(context)
        )
    }

    fun startTracking() {
        val context = getApplication<Application>()
        if (!PermissionManager.hasAnyLocation(context)) {
            refreshPermissionState()
            return
        }
        LocationForegroundService.start(context)
        _uiState.value = _uiState.value.copy(isTracking = true, canOfferResume = false)
    }

    fun stopTracking() {
        val context = getApplication<Application>()
        LocationForegroundService.stop(context)
        _uiState.value = _uiState.value.copy(isTracking = false)
    }
}
