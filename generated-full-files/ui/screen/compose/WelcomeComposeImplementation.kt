package ifac.td.taxi.ui.screen.compose
import ifac.td.taxi.domain.model.itop.ITopMeterStatus
import ifac.td.taxi.domain.model.PartialModel.Companion.hasTrips
import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import android.content.res.Configuration
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import ifac.td.taxi.domain.usecase.SessionUseCase
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import android.os.Build
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material3.AlertDialog
import ifac.td.taxi.viewmodel.BaseViewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_NOTIFICATION
import ifac.td.taxi.domain.usecase.PermissionsUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import androidx.compose.foundation.lazy.grid.items
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.model.itop.ITopMeterStateEnum
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import ifac.td.taxi.domain.usecase.StartShiftUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import kotlinx.coroutines.withContext
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import androidx.compose.runtime.setValue
import android.os.PowerManager
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import androidx.compose.ui.platform.LocalConfiguration
import ifac.td.taxi.domain.usecase.PartialUseCase
import androidx.compose.runtime.getValue
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import androidx.navigation.NavDirections
import androidx.compose.foundation.background
import ifac.td.taxi.domain.usecase.ShiftUseCase
import androidx.compose.foundation.layout.systemBarsPadding
import android.provider.Settings
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_LOCATION
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.viewinterop.AndroidView
import android.app.Application
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R


// -----------------------------
// UI STATE / EFFECTS / EVENTS
// -----------------------------

data class WelcomeUiState(
    val userLoggedIn: Boolean = false,
    val hasShifts: Boolean? = null,
    val partialButtonEnabled: Boolean? = null,
    val hasAllPermissions: Boolean = false,
    val isITopPaired: Boolean = false,
    val isDownloadingBravoConfiguration: Boolean = false,
    val lastITopState: ITopMeterStatusUi? = null,
    val correctLoginSuccess: Boolean = false,
)

data class WelcomeButtonsState(
    val buttons: List<WelcomeButtonVisualState> = emptyList()
)

data class WelcomeButtonVisualState(
    val type: ButtonType,
    val text: String,
    val style: WelcomeButtonStyle,
    val background: WelcomeButtonBackground,
    val visible: Boolean = true,
)

enum class WelcomeButtonStyle { ENABLE, DISABLE, LOADING }
enum class WelcomeButtonBackground { GREEN, RED, BLUE, YELLOW, ORANGE, DEFAULT, BLACK, TRANSPARENT }

sealed interface WelcomeUiEvent {
    data object ScreenStarted : WelcomeUiEvent
    data object StartClicked : WelcomeUiEvent
    data object SettingsClicked : WelcomeUiEvent
    data object ShiftsClicked : WelcomeUiEvent
    data object StatisticsClicked : WelcomeUiEvent
    data object ConfigurationClicked : WelcomeUiEvent
    data object PermissionsClicked : WelcomeUiEvent
    data object ParcialesClicked : WelcomeUiEvent
    data object ExitClicked : WelcomeUiEvent
    data object BackPressed : WelcomeUiEvent
    data object Resume : WelcomeUiEvent
    data object CheckPermissions : WelcomeUiEvent
    data object CheckUserLogin : WelcomeUiEvent
    data object CheckShifts : WelcomeUiEvent
    data object CheckClosuresPermission : WelcomeUiEvent
    data object CheckAllPermissions : WelcomeUiEvent
    data object MakeMigrationV2 : WelcomeUiEvent
    data class VerifySecurePin(val pin: String, val destination: WelcomeSecurePinDestination) : WelcomeUiEvent
}

sealed interface WelcomeUiEffect {
    data class Navigate(val destination: Any) : WelcomeUiEffect
    data class ShowToast(val messageRes: Int) : WelcomeUiEffect
    data object ConnectTaximeter : WelcomeUiEffect
    data object ExitApp : WelcomeUiEffect
    data class RequestPermissions(val permissions: Array<String>) : WelcomeUiEffect
    data class OpenCustomDialog(
        val model: CustomDialog.CustomDialogModel,
        val callback: (CustomDialog.CustomDialogResponse) -> Unit
    ) : WelcomeUiEffect
}

sealed interface WelcomeDialogState {
    data object Hidden : WelcomeDialogState
    data class Visible(
        val model: CustomDialog.CustomDialogModel,
        val onConfirm: (String?) -> Unit,
        val onDismiss: () -> Unit
    ) : WelcomeDialogState
}

enum class WelcomeSecurePinDestination { SHIFTS, STATISTICS }

data class ITopMeterStatusUi(
    val taximeterState: Int? = null,
    val driverCardData: String? = null
)

data class WelcomeButtonsReducerInput(
    val userLoggedIn: Boolean,
    val hasShifts: Boolean?,
    val partialButtonEnabled: Boolean?,
    val hasAllPermissions: Boolean,
    val isDownloadingBravoConfiguration: Boolean,
    val lastITopState: ITopMeterStatusUi?,
    val correctLoginSuccess: Boolean,
    val isITopPaired: Boolean,
)

// -----------------------------
// EXACT BUTTON REDUCER
// -----------------------------

fun reduceWelcomeButtonsState(input: WelcomeButtonsReducerInput): WelcomeButtonsState {
    val inicioStyle = when {
        input.isDownloadingBravoConfiguration -> WelcomeButtonStyle.LOADING
        input.isITopPaired && (input.lastITopState?.taximeterState == null ||
            input.lastITopState.taximeterState == ITopMeterStateEnum.DISCONNECTED.state) -> WelcomeButtonStyle.DISABLE
        else -> WelcomeButtonStyle.ENABLE
    }

    val turnosStyle = when (input.hasShifts) {
        true -> WelcomeButtonStyle.ENABLE
        false, null -> WelcomeButtonStyle.DISABLE
    }

    val parcialesStyle = when (input.partialButtonEnabled) {
        true -> WelcomeButtonStyle.ENABLE
        false, null -> WelcomeButtonStyle.DISABLE
    }

    val permisosBackground = if (input.hasAllPermissions) {
        WelcomeButtonBackground.BLUE
    } else {
        WelcomeButtonBackground.ORANGE
    }

    return WelcomeButtonsState(
        buttons = listOf(
            WelcomeButtonVisualState(ButtonType.INICIO, "INICIO", inicioStyle, WelcomeButtonBackground.GREEN),
            WelcomeButtonVisualState(ButtonType.AJUSTES_WELCOME, "AJUSTES", WelcomeButtonStyle.ENABLE, WelcomeButtonBackground.BLUE),
            WelcomeButtonVisualState(ButtonType.PARCIALES_CIERRE, "PARCIALES", parcialesStyle, WelcomeButtonBackground.BLUE),
            WelcomeButtonVisualState(ButtonType.TURNOS, "TURNOS", turnosStyle, WelcomeButtonBackground.BLUE),
            WelcomeButtonVisualState(ButtonType.ESTADISTICAS, "ESTADÍSTICAS", WelcomeButtonStyle.ENABLE, WelcomeButtonBackground.BLUE),
            WelcomeButtonVisualState(ButtonType.CONFIGURACION, "CONFIGURACIÓN", WelcomeButtonStyle.ENABLE, WelcomeButtonBackground.BLUE),
            WelcomeButtonVisualState(ButtonType.PERMISSIONS, "PERMISOS", WelcomeButtonStyle.ENABLE, permisosBackground),
            WelcomeButtonVisualState(ButtonType.SALIR, "SALIR", WelcomeButtonStyle.ENABLE, WelcomeButtonBackground.RED),
        )
    )
}

// -----------------------------
// COMPOSE SCREEN
// -----------------------------

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    sharedViewModel: WelcomeMainActivityViewModel,
    onNavigate: (Any) -> Unit,
    onBackPressed: () -> Unit,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onConnectTaximeter: () -> Unit,
    onExitApp: () -> Unit,
    onRequestPermissions: (Array<String>) -> Unit,
    onOpenDialog: (CustomDialog.CustomDialogModel, (CustomDialog.CustomDialogResponse) -> Unit) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val buttonsState by viewModel.buttonsState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val sharedState by sharedViewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onScreenStarted()
        sharedViewModel.onScreenStarted()
    }

    LaunchedEffect(configuration.orientation) {
        viewModel.onConfigurationChanged(configuration.orientation)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            handleWelcomeEffect(
                effect = effect,
                onNavigate = onNavigate,
                onShowToast = onShowToast,
                onConnectTaximeter = onConnectTaximeter,
                onExitApp = onExitApp,
                onRequestPermissions = onRequestPermissions,
                onOpenDialog = onOpenDialog
            )
        }
    }

    LaunchedEffect(Unit) {
        sharedViewModel.effects.collect { effect ->
            handleWelcomeEffect(
                effect = effect,
                onNavigate = onNavigate,
                onShowToast = onShowToast,
                onConnectTaximeter = onConnectTaximeter,
                onExitApp = onExitApp,
                onRequestPermissions = onRequestPermissions,
                onOpenDialog = onOpenDialog
            )
        }
    }

    LaunchedEffect(sharedState) {
        viewModel.syncFromSharedState(sharedState)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(1.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(buttonsState.buttons) { button ->
                WelcomeMenuButton(
                    button = button,
                    onClick = {
                        when (button.type) {
                            ButtonType.INICIO -> viewModel.onStartClicked()
                            ButtonType.AJUSTES_WELCOME -> viewModel.onSettingsClicked()
                            ButtonType.PARCIALES_CIERRE -> viewModel.onParcialesClicked()
                            ButtonType.TURNOS -> viewModel.onShiftsClicked()
                            ButtonType.ESTADISTICAS -> viewModel.onStatisticsClicked()
                            ButtonType.CONFIGURACION -> viewModel.onConfigurationClicked()
                            ButtonType.PERMISSIONS -> viewModel.onPermissionsClicked()
                            ButtonType.SALIR -> viewModel.onExitClicked()
                            else -> Unit
                        }
                    }
                )
            }
        }

        when (val d = dialogState) {
            is WelcomeDialogState.Hidden -> Unit
            is WelcomeDialogState.Visible -> {
                WelcomeCustomDialog(
                    state = d,
                    onDismiss = { viewModel.onDialogDismissed() },
                    onConfirm = { input -> viewModel.onDialogConfirmed(input) }
                )
            }
        }
    }
}

@Composable
private fun WelcomeMenuButton(
    button: WelcomeButtonVisualState,
    onClick: () -> Unit
) {
    if (!button.visible) return

    val style = when (button.style) {
        WelcomeButtonStyle.ENABLE -> CustomButton.StyleButton.ENABLE
        WelcomeButtonStyle.DISABLE -> CustomButton.StyleButton.DISABLE
        WelcomeButtonStyle.LOADING -> CustomButton.StyleButton.LOADING
    }

    val bg = when (button.background) {
        WelcomeButtonBackground.GREEN -> CustomButton.BackgroundButtonColor.GREEN
        WelcomeButtonBackground.RED -> CustomButton.BackgroundButtonColor.RED
        WelcomeButtonBackground.BLUE -> CustomButton.BackgroundButtonColor.BLUE
        WelcomeButtonBackground.YELLOW -> CustomButton.BackgroundButtonColor.YELLOW
        WelcomeButtonBackground.ORANGE -> CustomButton.BackgroundButtonColor.ORANGE
        WelcomeButtonBackground.DEFAULT -> CustomButton.BackgroundButtonColor.DEFAULT
        WelcomeButtonBackground.BLACK -> CustomButton.BackgroundButtonColor.BLACK
        WelcomeButtonBackground.TRANSPARENT -> CustomButton.BackgroundButtonColor.TRANSPARENT
    }

    AndroidView(
        factory = { context ->
            CustomButton(context).apply {
                setButtonType(button.type.value)
                changeText(button.text)
                setButtonStyle(style)
                changeBackground(bg)
                setAction(onClick)
            }
        },
        update = { view ->
            view.setButtonType(button.type.value)
            view.changeText(button.text)
            view.setButtonStyle(style)
            view.changeBackground(bg)
            view.setAction(onClick)
        }
    )
}

@Composable
fun WelcomeCustomDialog(
    state: WelcomeDialogState.Visible,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.model.title) },
        text = {
            if (state.model.editTextTypePin == true) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(state.model.hint.orEmpty()) }
                )
            } else {
                Text(state.model.description.orEmpty())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("ACEPTAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

private fun handleWelcomeEffect(
    effect: WelcomeUiEffect,
    onNavigate: (Any) -> Unit,
    onShowToast: (Int) -> Unit,
    onConnectTaximeter: () -> Unit,
    onExitApp: () -> Unit,
    onRequestPermissions: (Array<String>) -> Unit,
    onOpenDialog: (CustomDialog.CustomDialogModel, (CustomDialog.CustomDialogResponse) -> Unit) -> Unit,
) {
    when (effect) {
        is WelcomeUiEffect.Navigate -> onNavigate(effect.destination)
        is WelcomeUiEffect.ShowToast -> onShowToast(effect.messageRes)
        is WelcomeUiEffect.ConnectTaximeter -> onConnectTaximeter()
        is WelcomeUiEffect.ExitApp -> onExitApp()
        is WelcomeUiEffect.RequestPermissions -> onRequestPermissions(effect.permissions)
        is WelcomeUiEffect.OpenCustomDialog -> onOpenDialog(effect.model, effect.callback)
    }
}

// -----------------------------
// COMPOSE WELCOME VIEWMODEL
// -----------------------------

class WelcomeViewModel(
    val sessionUseCase: SessionUseCase,
    val shiftUseCase: ShiftUseCase,
    val licensingUseCase: LicensingUseCase,
    val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    val ticketUseCase: TicketUseCase,
    val loginDriverUseCase: LoginDriverUseCase,
    val startShiftUseCase: StartShiftUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val permissionsUseCase: PermissionsUseCase,
    val partialUseCase: PartialUseCase,
    val taximeterConnectUseCase: TaximeterConnectUseCase,
    val bluetoothLocalUseCase: BluetoothLocalUseCase,
    val migrationV2UseCase: MigrationV2UseCase,
    val userPreferencesUseCase: UserPreferencesUseCase,
    application: Application,
) : BaseViewModel(application) {

    val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    val _buttonsState = MutableStateFlow(WelcomeButtonsState())
    val buttonsState = _buttonsState.asStateFlow()

    val _dialogState = MutableStateFlow<WelcomeDialogState>(WelcomeDialogState.Hidden)
    val dialogState = _dialogState.asStateFlow()

    val _effects = MutableSharedFlow<WelcomeUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onScreenStarted() {
        viewModelScope.launch {
            checkClosuresPermission()
            makeMigrationV2()
            checkUserLogIn()
            haveShifts()
            checkAllPermissions()
            syncITopPaired()
            reduceButtonsFromCurrentState()
        }
    }

    fun onConfigurationChanged(orientation: Int) {
        Logs.d("WELCOME", "onConfigurationChanged: $orientation")
    }

    fun syncFromSharedState(shared: WelcomeUiState) {
        _uiState.value = _uiState.value.copy(
            isDownloadingBravoConfiguration = shared.isDownloadingBravoConfiguration,
            lastITopState = shared.lastITopState,
            correctLoginSuccess = shared.correctLoginSuccess
        )
        reduceButtonsFromCurrentState()
    }

    private fun syncITopPaired() {
        _uiState.value = _uiState.value.copy(isITopPaired = hasITopMeterPaired())
    }

    private fun reduceButtonsFromCurrentState() {
        _buttonsState.value = reduceWelcomeButtonsState(
            WelcomeButtonsReducerInput(
                userLoggedIn = _uiState.value.userLoggedIn,
                hasShifts = _uiState.value.hasShifts,
                partialButtonEnabled = _uiState.value.partialButtonEnabled,
                hasAllPermissions = _uiState.value.hasAllPermissions,
                isDownloadingBravoConfiguration = _uiState.value.isDownloadingBravoConfiguration,
                lastITopState = _uiState.value.lastITopState,
                correctLoginSuccess = _uiState.value.correctLoginSuccess,
                isITopPaired = _uiState.value.isITopPaired,
            )
        )
    }

    fun onStartClicked() {
        if (!hasITopMeterPaired()) checkAdditionalPermissions() else clickMenuLogin()
    }

    fun onSettingsClicked() = clickSettings()
    fun onShiftsClicked() = clickShifts()
    fun onStatisticsClicked() = clickStatistics()
    fun onConfigurationClicked() = clickConfiguration()
    fun onPermissionsClicked() = clickPermissions()
    fun onParcialesClicked() = clickParciales()
    fun onExitClicked() = showExitDialog()

    fun onDialogDismissed() {
        _dialogState.value = WelcomeDialogState.Hidden
    }

    fun onDialogConfirmed(input: String?) {
        val current = _dialogState.value
        if (current is WelcomeDialogState.Visible) {
            current.onConfirm(input)
        }
        _dialogState.value = WelcomeDialogState.Hidden
    }

    fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            _uiState.value = _uiState.value.copy(partialButtonEnabled = parameters?.isClosuresButton)
            reduceButtonsFromCurrentState()
        }
    }

    fun checkUserLogIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.value = _uiState.value.copy(userLoggedIn = loggedIn)
            reduceButtonsFromCurrentState()
        }
    }

    fun haveShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getLastShift()
            _uiState.value = _uiState.value.copy(hasShifts = shifts != null)
            reduceButtonsFromCurrentState()
        }
    }

    fun checkAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasAll = permissionsUseCase.hasAllPermissions()
            _uiState.value = _uiState.value.copy(hasAllPermissions = hasAll)
            reduceButtonsFromCurrentState()
        }
    }

    fun hasITopMeterPaired(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()

    fun clickSettings() = emitNavigate(R.id.action_welcomeFragment_to_settingsFragment)
    fun clickConfiguration() = emitNavigate(R.id.action_welcomeFragment_to_loginUserFragment)
    fun clickPermissions() = emitNavigate(R.id.action_welcomeFragment_to_permissionsFragment)

    fun clickParciales() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                emitNavigate(R.id.action_welcomeFragment_to_partials)
            } else {
                emitNavigate(R.id.action_welcomeFragment_to_closed_partial)
            }
        }
    }

    fun clickShifts() = requestSecurePinIfNeeded(WelcomeSecurePinDestination.SHIFTS)
    fun clickStatistics() = requestSecurePinIfNeeded(WelcomeSecurePinDestination.STATISTICS)

    fun clickMenuLogin() {
        viewModelScope.launch {
            if (!sessionUseCase.isUserLoggedIn()) {
                emitNavigate(R.id.action_welcomeFragment_to_loginUserFragment)
            } else {
                val connectionMode = bravoCentralConfigurationUseCase.getConnectionMode()
                var cntMethods = 0
                if ((connectionMode and 0x01) != 0) cntMethods++
                if ((connectionMode and 0x02) != 0) cntMethods++
                if ((connectionMode and 0x04) != 0) cntMethods++

                val startTurnMode = licensingUseCase.getStartTurnMode() ?: 1

                if (startTurnMode == 0 && cntMethods < 2) {
                    if ((connectionMode and 0x01) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", false)
                    }
                    if ((connectionMode and 0x02) != 0) {
                        startShiftUseCase.startShift()
                        shiftStatusUseCase.setStatus(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                        _effects.emit(WelcomeUiEffect.ConnectTaximeter)
                    }
                    if ((connectionMode and 0x04) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", true)
                    }
                } else {
                    emitNavigate(
                        WelcomeFragmentDirections.actionWelcomeFragmentToLoginDriverFragment()
                            .setConnectionMode(connectionMode)
                            .setStartTurnMode(startTurnMode)
                    )
                }
            }
        }
    }

    fun tryLoginDriver(lastITopCom: ITopMeterStatus?) {
        viewModelScope.launch {
            lastITopCom?.driverCardData?.let {
                if (it.length > 18) {
                    val cardData = it.substring(12, 17)
                    loginDriverUseCase.loginDriver(cardData, cardData, false)
                }
            }
        }
    }

    fun makeMigrationV2() {
        viewModelScope.launch {
            val hasOldTrips = migrationV2UseCase.checkHasOldTrip()
            val hasOldShift = migrationV2UseCase.checkHasOldShift()
            if (hasOldShift || hasOldTrips) {
                val checkExistSession = sessionUseCase.getSession()
                if (checkExistSession == null) {
                    if (migrationV2UseCase.migrateSession()) {
                        emitNavigate(
                            WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                                .setAutoDownloadConfigurationFromMigration(true)
                        )
                    }
                }
            }
        }
    }

    private fun requestSecurePinIfNeeded(destination: WelcomeSecurePinDestination) {
        viewModelScope.launch {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            if (!currentSecurePin.isNullOrEmpty()) {
                _dialogState.value = WelcomeDialogState.Visible(
                    model = CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.user_preferences_btn_secure_pin),
                        hint = context.getString(R.string.pin_actual_hint),
                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                        editTextTypePin = true,
                    ),
                    onConfirm = { pin -> verifyCurrentSecurePin(pin.orEmpty(), destination) },
                    onDismiss = { _dialogState.value = WelcomeDialogState.Hidden }
                )
            } else {
                navigateToDestination(destination)
            }
        }
    }

    fun verifyCurrentSecurePin(currentPin: String, destination: WelcomeSecurePinDestination) {
        viewModelScope.launch {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) {
                navigateToDestination(destination)
            } else {
                _effects.emit(WelcomeUiEffect.ShowToast(R.string.pin_incorrecto))
            }
        }
    }

    private suspend fun navigateToDestination(destination: WelcomeSecurePinDestination) {
        when (destination) {
            WelcomeSecurePinDestination.SHIFTS ->
                emitNavigate(R.id.action_welcomeFragment_to_shiftsFragment)

            WelcomeSecurePinDestination.STATISTICS ->
                emitNavigate(R.id.action_welcomeFragment_to_statisticsFragment)
        }
    }

    private fun showExitDialog() {
        viewModelScope.launch {
            _dialogState.value = WelcomeDialogState.Visible(
                model = CustomDialog.CustomDialogModel(
                    title = context.getString(R.string.close_app),
                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                ),
                onConfirm = { _ -> _effects.emit(WelcomeUiEffect.ExitApp) },
                onDismiss = { _dialogState.value = WelcomeDialogState.Hidden }
            )
        }
    }

    private fun checkAdditionalPermissions() {
        viewModelScope.launch {
            if (!isBatteryOptimizationDisabled()) {
                _dialogState.value = WelcomeDialogState.Visible(
                    model = CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.dialog_no_permission_title),
                        description = context.getString(R.string.dialog_no_permission_desc),
                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                    ),
                    onConfirm = {
                        requestIgnoreBatteryOptimizations()
                        clickMenuLogin()
                    },
                    onDismiss = { _dialogState.value = WelcomeDialogState.Hidden }
                )
            } else {
                clickMenuLogin()
            }
        }
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(getApplication<Application>().packageName) == true
    }

    private fun requestIgnoreBatteryOptimizations() {
        viewModelScope.launch {
            _effects.emit(
                WelcomeUiEffect.Navigate(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                )
            )
        }
    }

    private fun emitNavigate(destination: Any) {
        viewModelScope.launch { _effects.emit(WelcomeUiEffect.Navigate(destination)) }
    }
}

// -----------------------------
// COMPOSE SHARED VIEWMODEL BRIDGE
// -----------------------------

class WelcomeMainActivityViewModel(
    application: Application
) : BaseComposeViewModel(application) {

    val _uiState = MutableStateFlow(
        WelcomeUiState(
            userLoggedIn = false,
            hasShifts = null,
            partialButtonEnabled = null,
            hasAllPermissions = false,
            isITopPaired = false,
            isDownloadingBravoConfiguration = false,
            lastITopState = null,
            correctLoginSuccess = false
        )
    )
    val uiState = _uiState.asStateFlow()

    val _effects = MutableSharedFlow<WelcomeUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onScreenStarted() {
        // bridge placeholder for fragment-like shared state initialization
    }

    fun setDownloadingBravoConfiguration(value: Boolean) {
        _uiState.value = _uiState.value.copy(isDownloadingBravoConfiguration = value)
    }

    fun setLastITopState(value: ITopMeterStatus?) {
        _uiState.value = _uiState.value.copy(
            lastITopState = value?.let {
                ITopMeterStatusUi(
                    taximeterState = it.taximeterState,
                    driverCardData = it.driverCardData
                )
            }
        )
    }

    fun setCorrectLoginFlow(success: Boolean) {
        _uiState.value = _uiState.value.copy(correctLoginSuccess = success)
    }

    fun setReconnectionFlow(value: Boolean) {
        // keep for top bar logic if needed
    }

    fun emitConnectTaximeter() {
        viewModelScope.launch { _effects.emit(WelcomeUiEffect.ConnectTaximeter) }
    }
}

// -----------------------------
// BASE COMPOSE VIEWMODEL
// -----------------------------

open class BaseComposeViewModel(application: Application) : ViewModel() {
    protected val app: Application = application
}

// -----------------------------
// OPTIONAL DESTINATION MAP VIEWMODEL
// -----------------------------

class DestinationMapViewModel(
    val dispatchUseCase: DispatchUseCase,
    val navigatorUseCase: NavigatorUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "DestinationMapViewModel"

    val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    fun openNavigatorApp(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")

            val navigatorIntent = when {
                !coordinates.isNullOrEmpty() && !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                !coordinates.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
                !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorApp(street)
                else ->
                    navigatorUseCase.openNavigatorApp()
            }

            _navigatorFlow.emit(navigatorIntent)
        }
    }
}

// -----------------------------
// BASE VIEWMODEL PLACEHOLDER
// -----------------------------

open class BaseViewModel(context: Application) : ViewModel() {
    protected val application: Application = context
    protected fun getApplication(): Application = application
}
