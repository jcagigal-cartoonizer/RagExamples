package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 11-1: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.ui.screen.state.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
@Composable
fun MacroZoningScreen(
    uiState: MacroZoningUiState,
    onEvent: (MacroZoningUiEvent) -> Unit,
    uiEffects: Flow<MacroZoningUiEffect>,
    onNavigateToZoning: (macroZoneId: Int) -> Unit,
    onNavigateToPendingTrips: () -> Unit,
    onNavigateToPreReservationTrips: () -> Unit,
    onShowToast: (String) -> Unit,
    onShowDialog: (CustomDialogState) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            uiEffects.collectLatest { effect ->
                when (effect) {
                    is MacroZoningUiEffect.NavigateToZoning -> onNavigateToZoning(effect.macroZoneId)
                    is MacroZoningUiEffect.NavigateToPendingTrips -> onNavigateToPendingTrips()
                    is MacroZoningUiEffect.NavigateToPreReservationTrips -> onNavigateToPreReservationTrips()
                    is MacroZoningUiEffect.ShowToast -> onShowToast(effect.message)
                    is MacroZoningUiEffect.ShowDialog -> onShowDialog(effect.dialogState)
                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        MacroZoningHeader(
            buttonsState = uiState.buttonsState,
            onHeaderClick = { onEvent(MacroZoningUiEvent.OnOrderSelected(it)) },
            onFilterClick = { onEvent(MacroZoningUiEvent.OnFilterClick) },
            onPendingClick = { onEvent(MacroZoningUiEvent.OnPendingClick) },
            onPreReservationClick = { onEvent(MacroZoningUiEvent.OnPreReservationClick) },
        )
        if (uiState.isLoading && uiState.macroZones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            MacroZonesList(
                items = uiState.macroZones,
                scrollMode = uiState.scrollMode,
                onZoneClick = { onEvent(MacroZoningUiEvent.OnMacroZoneClick(it)) }
            )
        }
    }
    if (uiState.dialogState != null && uiState.dialogState.visible) {
        CustomDialogComposable(
            state = uiState.dialogState,
            onDismiss = { onEvent(MacroZoningUiEvent.OnDialogDismiss) },
            onConfirm = { onEvent(MacroZoningUiEvent.OnDialogConfirm) }
        )
    }
}
@Composable
fun MacroZoningHeader(
    buttonsState: MacroZoningButtonsState,
    onHeaderClick: (OrderOptions) -> Unit,
    onFilterClick: () -> Unit,
    onPendingClick: () -> Unit,
    onPreReservationClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSortItem(
                title = buttonsState.placeholderText,
                visible = true,
                active = buttonsState.activeOrder != OrderOptions.NONE,
                onClick = { /* placeholder tap optional */ }
            )
            Text(
                text = stringResource(R.string.filter),
                modifier = Modifier.clickable { onFilterClick() },
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (buttonsState.onStopVisible) {
                SortChip(
                    text = stringResource(R.string.stand),
                    selected = buttonsState.activeOrder == OrderOptions.IN_STAND,
                    onClick = { onHeaderClick(OrderOptions.IN_STAND) }
                )
            }
            if (buttonsState.onZoneVisible) {
                SortChip(
                    text = stringResource(R.string.zone_tts),
                    selected = buttonsState.activeOrder == OrderOptions.IN_ZONE,
                    onClick = { onHeaderClick(OrderOptions.IN_ZONE) }
                )
            }
            if (buttonsState.hiredVisible) {
                SortChip(
                    text = buttonsState.hiredLabel,
                    selected = buttonsState.activeOrder == OrderOptions.HIRED,
                    onClick = { onHeaderClick(OrderOptions.HIRED) }
                )
            }
            if (buttonsState.tripsVisible) {
                SortChip(
                    text = buttonsState.tripsLabel,
                    selected = buttonsState.activeOrder == OrderOptions.TRIPS,
                    onClick = { onHeaderClick(OrderOptions.TRIPS) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CustomActionButton(
                text = stringResource(R.string.pending),
                state = buttonsState.pendingButton,
                onClick = onPendingClick
            )
            CustomActionButton(
                text = stringResource(R.string.pre_reservation),
                state = buttonsState.preReservationButton,
                onClick = onPreReservationClick
            )
        }
    }
}
@Composable
fun MacroZonesList(
    items: List<MacroZoneModelUi>,
    scrollMode: ScrollModeEnum,
    onZoneClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            MacroZoneRow(item = item, onClick = { onZoneClick(item.idMacrozone) })
        }
    }
}
@Composable
fun MacroZoneRow(item: MacroZoneModelUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.name)
        if (item.isSelected) Text(text = "•", color = MaterialTheme.colorScheme.primary)
    }
}
