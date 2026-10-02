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
// # Block 6-1: import android.app.Activity
@Composable
fun UserPreferencesScreen(
    navController: NavController,
    viewModel: UserPreferencesComposeViewModel,
    onRequestPhonePermission: () -> Unit = {},
    onRequestBluetoothPermission: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<UserPreferencesButtonStylesCustomDialog.UserPreferencesButtonStylesCustomDialogModel?>(null) }
    var ringtoneRequestType by remember { mutableIntStateOf(-1) }
    val ringtoneLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.onRingtoneOrNotificationPicked(result.data, ringtoneRequestType)
            }
        }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UserPreferencesUiEffect.NavigateBack -> navController.popBackStack()
                is UserPreferencesUiEffect.NavigateToDeepLink -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri(effect.uri.toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.NavigateToSecurePin -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri("android-app://ifac.td.taxi/changeDriverPinFragment/".toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.NavigateToPortugalSettings -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri("android-app://ifac.td.taxi/portugalSettingsFragment/".toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.ShowToast -> {
                    // Host-side toast/snackbar handling
                }
                is UserPreferencesUiEffect.OpenDialog -> {
                    dialogState = effect.dialog
                }
                is UserPreferencesUiEffect.CloseDialog -> {
                    dialogState = null
                }
                is UserPreferencesUiEffect.RequestPhonePermission -> onRequestPhonePermission()
                is UserPreferencesUiEffect.RequestBluetoothPermission -> onRequestBluetoothPermission()
                is UserPreferencesUiEffect.RequestOverlayPermission -> onRequestOverlayPermission()
                is UserPreferencesUiEffect.OpenRingtonePicker -> {
                    ringtoneRequestType = effect.type
                    ringtoneLauncher.launch(effect.intent)
                }
                is UserPreferencesUiEffect.KeepScreenOn -> {
                    // Optional host action
                }
            }
        }
    }
    if (dialogState != null) {
        UserPreferencesButtonStylesUserPreferencesCustomDialog(
            dialog = dialogState!!,
            onDismiss = {
                viewModel.onDialogAction(
                    UserPreferencesUiEffect.DialogAction.Cancel
                )
                dialogState = null
            },
            onAction = { action ->
                viewModel.onDialogAction(action)
                if (action is UserPreferencesUiEffect.DialogAction.Accept) {
                    dialogState = null
                }
            }
        )
    }
    UserPreferencesContent(
        state = state,
        buttonsState = UserPreferencesButtonsState.from(state),
        onAction = viewModel::onAction
    )
}
@Composable
fun UserPreferencesContent(
    state: UserPreferencesUiState,
    buttonsState: UserPreferencesButtonsState,
    onAction: (UserPreferencesUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "User Preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // Cards / sections
        SectionCard(
            title = "Sound",
            expanded = state.soundExpanded,
            onToggle = { onAction(UserPreferencesUiEvent.ToggleSound) }
        ) {
            PreferenceCheckRow(
                text = "Sound disabled on TX",
                checked = state.model.beepNoBt,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetBeepNoBt(it)) }
            )
            PreferenceCheckRow(
                text = "Vibration and sound",
                checked = state.model.vibrateNoBT,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetVibrateNoBt(it)) }
            )
        }
        SectionCard(
            title = "Invoices",
            expanded = state.invoicesExpanded,
            enabled = state.isLoggedIn,
            onToggle = { onAction(UserPreferencesUiEvent.ToggleInvoices) }
        ) {
            PreferenceCheckRow(
                text = "Use external app",
                checked = state.model.useExternalApp,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetUseExternalApp(it)) }
            )
            if (state.model.useExternalApp) {
                PreferenceCheckRow(
                    text = "Use custom payment timer",
                    checked = state.model.useCustomPaymentTimerWithExternalApp,
                    onCheckedChange = {
                        onAction(UserPreferencesUiEvent.SetUseCustomPaymentTimerWithExternalApp(it))
                    }
                )
            }
            if (buttonsState.showRedSysAlways) {
                PreferenceCheckRow(
                    text = "Always print RedSys ticket",
                    checked = state.model.printRedSysCommerceTicketAlways,
                    onCheckedChange = { onAction(UserPreferencesUiEvent.SetPrintRedSysAlways(it)) }
                )
            }
        }
        SectionCard(
            title = "Pin Pad",
            expanded = state.pinPadExpanded,
            onToggle = { onAction(UserPreferencesUiEvent.TogglePinPad) }
        ) {
            // Replace with Compose dropdown/spinner implementation
            Text("PinPad serial: ${state.model.pinPadSerialNumber}")
        }
        SectionCard(
            title = "Shifts",
            expanded = state.shiftsExpanded,
            onToggle = { onAction(UserPreferencesUiEvent.ToggleShifts) }
        ) {
            PreferenceCheckRow(
                text = "Time control",
                checked = state.model.useTimeControl,
                visible = buttonsState.showControlHorario,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetUseTimeControl(it)) }
            )
            if (buttonsState.showDeleteShiftsButton) {
                Button(
                    onClick = { onAction(UserPreferencesUiEvent.ClickDeleteShifts) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete shifts by date")
                }
            }
        }
        SectionCard(
            title = "System",
            expanded = state.systemExpanded,
            onToggle = { onAction(UserPreferencesUiEvent.ToggleSystem) }
        ) {
            PreferenceCheckRow(
                text = "Show confirmation on accept",
                checked = state.model.showConfAcceptDispatch,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetShowConfAccept(it)) }
            )
            PreferenceCheckRow(
                text = "Show confirmation on reject",
                checked = state.model.showConfRejectDispatch,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetShowConfReject(it)) }
            )
            PreferenceCheckRow(
                text = "Floating window on background",
                checked = state.model.useFloatingWindow,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetUseFloatingWindow(it)) }
            )
            PreferenceCheckRow(
                text = "Light off on dispatched",
                checked = state.model.lightOffOnDispatched,
                visible = buttonsState.showLightOffOnDispatched,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetLightOffOnDispatched(it)) }
            )
            if (buttonsState.showSendLogsButton) {
                Button(
                    onClick = { onAction(UserPreferencesUiEvent.ClickSendLogs) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send logs")
                }
            }
        }
        SectionCard(
            title = "Location",
            expanded = state.locationExpanded,
            onToggle = { onAction(UserPreferencesUiEvent.ToggleLocation) }
        ) {
            PreferenceCheckRow(
                text = "Close pending trips if empty",
                checked = state.model.closePendingTripsIfEmpty,
                onCheckedChange = { onAction(UserPreferencesUiEvent.SetClosePendingTripsIfEmpty(it)) }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onAction(UserPreferencesUiEvent.ClickCancel) },
                modifier = Modifier.weight(1f),
                colors = buttonsState.cancelColors
            ) { Text("Cancel") }
            Button(
                onClick = { onAction(UserPreferencesUiEvent.ClickAccept) },
                modifier = Modifier.weight(1f),
                colors = buttonsState.acceptColors
            ) { Text("Accept") }
        }
        if (buttonsState.showFiscalPortugalButton) {
            Button(
                onClick = { onAction(UserPreferencesUiEvent.ClickFiscalPortugal) },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonsState.secondaryColors
            ) { Text("Fiscal Portugal") }
        }
        if (buttonsState.showSecurePinButton) {
            Button(
                onClick = { onAction(UserPreferencesUiEvent.ClickSecurePin) },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonsState.secondaryColors
            ) { Text("Secure Pin") }
        }
    }
}
@Composable
fun SectionCard(
    title: String,
    expanded: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title)
                Text(if (expanded) "▲" else "▼")
            }
            if (expanded) {
                Column(Modifier.padding(16.dp), content = content)
            }
        }
    }
}
@Composable
fun PreferenceCheckRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    visible: Boolean = true,
) {
    if (!visible) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text)
    }
}
