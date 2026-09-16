package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.state.DashboardDialogState
import ifac.td.taxi.ui.screen.state.DashboardButtons
import ifac.td.taxi.compose.viewmodel.DashboardComposeViewModel
import ifac.td.taxi.ui.screen.DashboardScreen
import ifac.td.taxi.ui.screen.components.DashboardCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.state.DashboardUiEffect
import ifac.td.taxi.ui.screen.state.DashboardUiEvent
import ifac.td.taxi.ui.screen.state.DashboardUiState
import ifac.td.taxi.ui.screen.state.ActionButtonState
import ifac.td.taxi.ui.screen.state.ButtonBackground
import ifac.td.taxi.ui.screen.state.DashboardHeaderState
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import ifac.td.taxi.ui.screen.state.DashboardButtonConfig
// // ## 4) Compose `DashboardScreen`

// This replaces the fragment and preserves navigation via callbacks or `NavController`.


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.ui.model.ZoneModel

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var dialogState by remember { mutableStateOf<DashboardDialogState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardUiEffect.NavigateToZoneDetail -> {
                    // Preserve Jetpack navigation here
                    // navController.navigate(...)
                }
                DashboardUiEffect.NavigateBack -> navController.navigateUp()
                is DashboardUiEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is DashboardUiEffect.OpenDialog -> dialogState = effect.dialog
                DashboardUiEffect.CloseDialog -> dialogState = null
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardUiEvent.OnResume)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(DashboardUiEvent.OnPause) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DashboardHeader(buttonsState = uiState.buttonsState)

            DashboardButtonsRow(
                buttonsState = uiState.buttonsState,
                pendingTripsCount = uiState.pendingTripsCount,
                onLocateClick = { viewModel.onEvent(DashboardUiEvent.OnLocateOnHiredClick) }
            )

            DashboardZoneLists(
                nearbyZones = uiState.nearbyZones,
                farZones = uiState.farZones,
                actualZones = uiState.actualZones,
                onZoneClick = { viewModel.onEvent(DashboardUiEvent.OnZoneClick(it)) }
            )
        }
    }

    dialogState?.let { dialog ->
        DashboardCustomDialog(
            title = dialog.title,
            message = dialog.message,
            confirmText = dialog.confirmText,
            dismissText = dialog.dismissText,
            onConfirm = { viewModel.onEvent(DashboardUiEvent.OnDialogConfirm) },
            onDismiss = { viewModel.onEvent(DashboardUiEvent.OnDialogDismiss) }
        )
    }
}


// ## 5) Composable UI pieces

