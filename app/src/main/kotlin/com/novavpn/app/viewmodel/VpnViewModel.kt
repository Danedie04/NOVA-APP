package com.novavpn.app.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novavpn.app.vpn.VpnConfig
import com.novavpn.app.vpn.VpnRepository
import com.novavpn.app.vpn.VpnState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main VPN screen.
 *
 * Bridges the UI and the VpnRepository.
 * Emits one-off UI events (permission requests, errors) via a SharedFlow.
 */
@HiltViewModel
class VpnViewModel @Inject constructor(
    private val repository: VpnRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Current VPN connection state — collected by Compose UI */
    val vpnState: StateFlow<VpnState> = repository.vpnState
        .stateIn(viewModelScope, SharingStarted.Eagerly, VpnState.Disconnected)

    /** Selected server configuration */
    private val _selectedConfig = MutableStateFlow(VpnConfig())
    val selectedConfig: StateFlow<VpnConfig> = _selectedConfig.asStateFlow()

    /** List of available servers */
    val servers: List<VpnConfig> = repository.getServers()

    // -------------------------------------------------------------------------
    // One-shot UI events
    // -------------------------------------------------------------------------

    sealed class UiEvent {
        data class RequestVpnPermission(val intent: Intent) : UiEvent()
        data class ShowError(val message: String) : UiEvent()
        object VpnPermissionGranted : UiEvent()
    }

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /**
     * Called when the user taps the Connect / Disconnect button.
     */
    fun onToggleConnection() {
        when (vpnState.value) {
            is VpnState.Connected,
            is VpnState.Connecting    -> disconnect()
            is VpnState.Disconnected,
            is VpnState.Error         -> requestConnect()
            else                      -> { /* Ignore taps during transitions */ }
        }
    }

    /**
     * Checks VPN permission; if already granted, connects immediately.
     * Otherwise, emits a permission request event to the UI.
     */
    private fun requestConnect() {
        viewModelScope.launch {
            val permissionIntent = repository.prepareVpn()
            if (permissionIntent == null) {
                // Permission already granted — connect
                connect()
            } else {
                // UI must launch this intent to request permission
                _uiEvents.emit(UiEvent.RequestVpnPermission(permissionIntent))
            }
        }
    }

    /**
     * Called by the UI after VPN permission is granted (Activity result).
     */
    fun onVpnPermissionGranted() {
        connect()
    }

    /**
     * Called by the UI when VPN permission is denied.
     */
    fun onVpnPermissionDenied() {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowError("VPN permission denied. Please allow NovaVPN to create a VPN connection."))
        }
    }

    /** Starts the VPN connection with the currently selected config. */
    private fun connect() {
        repository.connect(_selectedConfig.value)
    }

    /** Stops the VPN connection. */
    private fun disconnect() {
        repository.disconnect()
    }

    /** Updates the selected server. */
    fun selectServer(config: VpnConfig) {
        _selectedConfig.value = config
        // If currently connected, reconnect with new server
        if (vpnState.value is VpnState.Connected) {
            disconnect()
            viewModelScope.launch {
                // Brief delay to let disconnect complete
                kotlinx.coroutines.delay(500)
                connect()
            }
        }
    }
}
