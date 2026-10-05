package ifac.td.taxi.ui.screen.components
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
// # Block 10-1: import androidx.compose.foundation.layout.*
@Composable
fun DashboardRoute(
    navController: NavController,
    viewModel: DashboardComposeViewModel,
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    onOpenZoneDetails: (ZoneModel) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onOpenZoneDetails = onOpenZoneDetails
    )
    DashboardEffectsCollector(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onOpenZoneDetails = onOpenZoneDetails
    )
}
@Composable
fun DashboardEffectsCollector(
    viewModel: DashboardComposeViewModel,
    onNavigateBack: () -> Unit,
    onOpenZoneDetails: (ZoneModel) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DashboardUiEffect.NavigateBack -> onNavigateBack()
                is DashboardUiEffect.NavigateToZoneDetails -> onOpenZoneDetails(effect.zone)
                DashboardUiEffect.HideDialog -> Unit
                DashboardUiEffect.ShowDialog -> Unit
            }
        }
    }
}
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenZoneDetails: (ZoneModel) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    TextButton(onClick = { onNavigateBack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DashboardHeader(
                buttonsState = uiState.buttonsState,
                onOnStopClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.ON_STOP)) },
                onOnZoneClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.ON_ZONE)) },
                onHiredClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.HIRED)) },
                onTripsClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.TRIPS)) },
            )
            Spacer(Modifier.height(12.dp))
            PendingTripsSection(
                size = uiState.pendingTrips.size,
                trips = uiState.pendingTrips,
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Nearby Zones",
                zones = uiState.nearbyZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Far Zones",
                zones = uiState.farZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Actual Zone",
                zones = uiState.actualZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
        }
        if (uiState.dialogState.visible) {
            DashboardCustomDialogCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(DashboardUiEvent.DismissDialog) },
                onPrimaryAction = { onEvent(DashboardUiEvent.ConfirmDialog) },
                onSecondaryAction = { onEvent(DashboardUiEvent.CancelDialog) }
            )
        }
    }
}
@Composable
fun DashboardHeader(
    buttonsState: DashboardButtonsState,
    onOnStopClick: () -> Unit,
    onOnZoneClick: () -> Unit,
    onHiredClick: () -> Unit,
    onTripsClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DashboardHeaderRow(
            label = buttonsState.onStop.text,
            visible = buttonsState.onStop.visible,
            style = buttonsState.onStop.style,
            onClick = onOnStopClick
        )
        DashboardHeaderRow(
            label = buttonsState.onZone.text,
            visible = buttonsState.onZone.visible,
            style = buttonsState.onZone.style,
            onClick = onOnZoneClick
        )
        DashboardHeaderRow(
            label = buttonsState.hired.text,
            visible = buttonsState.hired.visible,
            style = buttonsState.hired.style,
            onClick = onHiredClick
        )
        DashboardHeaderRow(
            label = buttonsState.trips.text,
            visible = buttonsState.trips.visible,
            style = buttonsState.trips.style,
            onClick = onTripsClick
        )
    }
}
@Composable
fun DashboardHeaderRow(
    label: String,
    visible: Boolean,
    style: DashboardButtonStyle,
    onClick: () -> Unit,
) {
    if (!visible) return
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.contentColor
        ),
        shape = style.shape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = style.elevation)
    ) {
        Text(label)
    }
}
@Composable
fun PendingTripsSection(size: Int, trips: List<Any>) {
    Column {
        Text("Pending trips: $size")
        // Replace with LazyColumn + your row layout
    }
}
@Composable
fun ZonesSection(
    title: String,
    zones: List<ZoneModel>,
    onZoneClick: (ZoneModel) -> Unit,
    onZoneLongClick: (ZoneModel) -> Unit,
) {
    Column {
        Text(title)
        LazyColumn(
            modifier = Modifier.heightIn(max = 180.dp)
        ) {
            items(zones) { zone ->
                TextButton(
                    onClick = { onZoneClick(zone) }
                ) {
                    Text(zone.zone.nombreZone ?: "Zone")
                }
            }
        }
    }
}
