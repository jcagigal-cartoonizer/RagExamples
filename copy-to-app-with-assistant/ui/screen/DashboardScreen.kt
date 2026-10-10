package ifac.td.taxi.ui.screen
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.DashboardScreen
// # Block 408-5: import androidx.activity.compose.BackHandler
@Composable
fun DashboardScreen(
    viewModel: DashboardComposeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToOnTrip: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                DashboardUiEffect.NavigateBack -> onNavigateBack()
                DashboardUiEffect.NavigateToHome -> onNavigateHome()
                DashboardUiEffect.NavigateToOnTrip -> onNavigateToOnTrip()
                DashboardUiEffect.OpenDelocateDialog -> dialogVisible = true
                DashboardUiEffect.CloseDialog -> dialogVisible = false
            }
        }
    }
    BackHandler {
        viewModel.onEvent(DashboardUiEvent.OnBackPressed)
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardUiEvent.OnResume)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(DashboardUiEvent.OnPause)
        }
    }
    Scaffold(
        topBar = {
            DashboardHeader(
                state = uiState.headerState
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DashboardButtonsRow(
                state = uiState.buttonsState,
                onShowAll = { viewModel.onEvent(DashboardUiEvent.OnShowAllTripsClicked) },
                onShowRecent = { viewModel.onEvent(DashboardUiEvent.OnShowRecentTripsClicked) },
                onCancel = { viewModel.onEvent(DashboardUiEvent.OnCancelClicked) },
                onClose = { viewModel.onEvent(DashboardUiEvent.OnCloseClicked) }
            )
            Spacer(Modifier.height(12.dp))
            Text("Pending trips: ${uiState.pendingTripsCount}")
            Spacer(Modifier.height(12.dp))
            DashboardZonesSection(
                title = "Nearby zones",
                zones = uiState.nearbyZones
            )
            DashboardZonesSection(
                title = "Far zones",
                zones = uiState.farZones
            )
            DashboardZonesSection(
                title = "Actual zones",
                zones = uiState.actualZones
            )
        }
    }
    if (dialogVisible) {
        DashboardCustomNotificationDialogDashboardCustomDialog(
            title = "Delocate",
            message = "Do you want to delocate the hired zone?",
            confirmText = "Yes",
            dismissText = "No",
            onConfirm = {
                dialogVisible = false
                viewModel.onEvent(DashboardUiEvent.OnConfirmDialog)
            },
            onDismiss = {
                dialogVisible = false
                viewModel.onEvent(DashboardUiEvent.OnDismissDialog)
            }
        )
    }
}
@Composable
fun DashboardHeader(state: DashboardHeaderState) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        if (state.showStand) Text(state.standText)
        if (state.showZone) Text(state.zoneText)
        if (state.showHired) Text(state.hiredText)
        if (state.showTrips) Text(state.tripsText)
    }
}
@Composable
fun DashboardZonesSection(
    title: String,
    zones: List<ZoneModel>
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(zones) { zone ->
                Text(text = zone.zone.nombreZone ?: "Zone")
            }
        }
    }
}
