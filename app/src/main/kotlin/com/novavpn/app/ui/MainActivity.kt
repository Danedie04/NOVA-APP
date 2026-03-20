package com.novavpn.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Scaffold
import dagger.hilt.android.AndroidEntryPoint
import com.novavpn.app.ui.screens.MainScreen
import com.novavpn.app.ui.theme.NovaVpnTheme
import com.novavpn.app.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

/**
 * Main activity — hosts the Compose UI and handles VPN permission flow.
 *
 * Permission flow:
 *  1. User taps Connect
 *  2. ViewModel calls VpnService.prepare()
 *  3. If non-null intent returned → launch [vpnPermissionLauncher]
 *  4. On RESULT_OK → ViewModel.onVpnPermissionGranted() → connect
 *  5. On RESULT_CANCELED → ViewModel.onVpnPermissionDenied() → show error
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    /** Handles the system VPN permission dialog result */
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaVpnTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Collect UI events (permission requests, errors)
                LaunchedEffect(Unit) {
                    viewModel.uiEvents.collect { event ->
                        when (event) {
                            is VpnViewModel.UiEvent.RequestVpnPermission -> {
                                vpnPermissionLauncher.launch(event.intent)
                            }
                            is VpnViewModel.UiEvent.ShowError -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                            }
                            is VpnViewModel.UiEvent.VpnPermissionGranted -> { /* handled above */ }
                        }
                    }
                }

                val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
                val selectedConfig by viewModel.selectedConfig.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = com.novavpn.app.ui.theme.NovaBlack,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { _ ->
                    MainScreen(
                        vpnState = vpnState,
                        selectedConfig = selectedConfig,
                        servers = viewModel.servers,
                        onToggleConnection = viewModel::onToggleConnection,
                        onServerSelected = viewModel::selectServer
                    )
                }
            }
        }
    }
}
