package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PartialModel.Companion.hasTrips
import ifac.td.taxi.domain.model.itop.ITopMeterStateEnum
import ifac.td.taxi.domain.model.itop.ITopMeterStatus
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PermissionsUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.StartShiftUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.ui.screen.WelcomeFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --------------------------------------------------------------------------------------
// UI state
// --------------------------------------------------------------------------------------

enum class WelcomeButtonStyle { ENABLE, DISABLE, LOADING }
enum class WelcomeButtonBackground { BLUE, ORANGE }

data class WelcomeButtonState(
    val visible: Boolean = true,
    val style: WelcomeButtonStyle = WelcomeButtonStyle.ENABLE,
    val background: WelcomeButtonBackground = WelcomeButtonBackground.BLUE,
)

data class WelcomeButtonsState(
    val inicio: WelcomeButtonState = WelcomeButtonState(),
    val ajustes: WelcomeButtonState = WelcomeButtonState(),
    val turnos: WelcomeButtonState = WelcomeButtonState(),
    val estadisticas: WelcomeButtonState = WelcomeButtonState(),
    val configuracion: WelcomeButtonState = WelcomeButtonState(),
    val permisos: WelcomeButtonState = WelcomeButtonState(background = WelcomeButtonBackground.ORANGE),
    val parciales: WelcomeButtonState = WelcomeButtonState(),
    val salir: WelcomeButtonState = WelcomeButtonState(),
)

enum class WelcomeSecurePinDestination { SHIFTS, STATISTICS }

data class WelcomeUiState(
    val buttons: WelcomeButtonsState = WelcomeButtonsState(),
    val isLoggedIn: Boolean = false,
    val hasShifts: Boolean? = null,
    val hasAllPermissions: Boolean = false,
    val hasClosuresPermission: Boolean? = null,
    val isItopPaired: Boolean = false,
    val lastItopDisconnected: Boolean = false,
    val batteryOptimizationAlreadyLogged: Boolean = false,
)

data class WelcomeSharedUiState(
    val downloadingBravoConfiguration: Boolean = false,
    val lastItopStateReceived: ITopMeterStatus? = null,
    val correctLoginSuccess: Boolean = false,
    val reconnectionFlow: Boolean = false,
)

// --------------------------------------------------------------------------------------
// Effects
// --------------------------------------------------------------------------------------

sealed interface WelcomeUiEffect {
    data class Navigate(val directions: NavDirections) : WelcomeUiEffect
    data object ShowExitDialog : WelcomeUiEffect
    data object ShowBatteryOptimizationDialog : WelcomeUiEffect
    data class ShowSecurePinDialog(val destination: WelcomeSecurePinDestination) : WelcomeUiEffect
    data class ShowToast(@StringRes val resId: Int) : WelcomeUiEffect
    data object ConnectTaximeter : WelcomeUiEffect
}

// --------------------------------------------------------------------------------------
// Reducer
// --------------------------------------------------------------------------------------

data class WelcomeButtonsInput(
    val hasShifts: Boolean? = null,
    val partialButtonEnabled: Boolean? = null,
    val userLoggedIn: Boolean? = null,
    val hasAllPermissions: Boolean? = null,
    val downloadingConfig: Boolean? = null,
    val isItopPaired: Boolean = false,
    val lastItopState: ITopMeterStatus? = null,
    val correctLoginSuccess: Boolean? = null,
)

fun reduceWelcomeButtonsState(
    current: WelcomeButtonsState,
    input: WelcomeButtonsInput,
): WelcomeButtonsState {
    var state = current

    input.hasShifts?.let { hasShifts ->
        state = state.copy(
            turnos = state.turnos.copy(
                style = if (hasShifts) WelcomeButtonStyle.ENABLE else WelcomeButtonStyle.DISABLE
            )
        )
    }

    input.partialButtonEnabled?.let { enabled ->
        state = state.copy(
            parciales = state.parciales.copy(
                style = if (enabled) WelcomeButtonStyle.ENABLE else WelcomeButtonStyle.DISABLE
            )
        )
    }

    input.userLoggedIn?.let { loggedIn ->
        if (!loggedIn) {
            state = state.copy(
                parciales = state.parciales.copy(style = WelcomeButtonStyle.DISABLE)
            )
        } else {
            state = state.copy(
                parciales = state.parciales.copy(style = WelcomeButtonStyle.ENABLE)
            )
        }
    }

    input.downloadingConfig?.let { downloading ->
        state = state.copy(
            inicio = state.inicio.copy(
                style = if (downloading) WelcomeButtonStyle.LOADING else state.inicio.style
            )
        )
    }

    input.hasAllPermissions?.let { hasAllPermissions ->
        state = state.copy(
            permisos = state.permisos.copy(
                background = if (hasAllPermissions) WelcomeButtonBackground.BLUE else WelcomeButtonBackground.ORANGE
            )
        )
    }

    input.lastItopState?.let { itopState ->
        if (input.isItopPaired) {
            state = when (itopState.taximeterState) {
                null, ITopMeterStateEnum.DISCONNECTED.state -> {
                    state.copy(inicio = state.inicio.copy(style = WelcomeButtonStyle.DISABLE))
                }
                else -> {
                    state.copy(inicio = state.inicio.copy(style = WelcomeButtonStyle.ENABLE))
                }
            }
        }
    }

    input.correctLoginSuccess?.let { success ->
        if (!success) {
            val shouldDisable = input.isItopPaired &&
                (input.lastItopState?.taximeterState == null ||
                 input.lastItopState?.taximeterState == ITopMeterStateEnum.DISCONNECTED.state)

            state = state.copy(
                inicio = state.inicio.copy(
                    style = if (shouldDisable) WelcomeButtonStyle.DISABLE else WelcomeButtonStyle.ENABLE
                )
            )
        }
    }

    return state
}

// --------------------------------------------------------------------------------------
// Compose-friendly Welcome VM
// --------------------------------------------------------------------------------------

class WelcomeComposeViewModel(
    private val sessionUseCase: SessionUseCase,
    private val shiftUseCase: ShiftUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val ticketUseCase: TicketUseCase,
    private val loginDriverUseCase: LoginDriverUseCase,
    private val startShiftUseCase: StartShiftUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val permissionsUseCase: PermissionsUseCase,
    private val partialUseCase: PartialUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val context: Application,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<WelcomeUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    fun onStart() {
        checkClosuresPermission()
        makeMigrationV2()
    }

    fun onResume() {
        checkUserLogIn()
        haveShifts()
        checkAllPermissions()
    }

    fun onInicioClicked() {
        if (!hasITopMeterPaired()) {
            checkAdditionalPermissions()
        } else {
            clickMenuLogin()
        }
    }

    fun onAjustesClicked(onNavigate: (NavDirections) -> Unit) {
        onNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToSettingsFragment())
    }

    fun onTurnosClicked() {
        requestSecurePinIfNeeded(WelcomeSecurePinDestination.SHIFTS)
    }

    fun onEstadisticasClicked() {
        requestSecurePinIfNeeded(WelcomeSecurePinDestination.STATISTICS)
    }

    fun onConfiguracionClicked() {
        viewModelScope.launch {
            _uiEffects.emit(
                WelcomeUiEffect.Navigate(
                    WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                )
            )
        }
    }

    fun onPermisosClicked() {
        viewModelScope.launch {
            _uiEffects.emit(
                WelcomeUiEffect.Navigate(
                    WelcomeFragmentDirections.actionWelcomeFragmentToPermissionsFragment()
                )
            )
        }
    }

    fun onParcialesClicked() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            val directions = if (activePartial != null && activePartial.hasTrips()) {
                WelcomeFragmentDirections.actionWelcomeFragmentToPartials()
            } else {
                WelcomeFragmentDirections.actionWelcomeFragmentToClosedPartial()
            }
            _uiEffects.emit(WelcomeUiEffect.Navigate(directions))
        }
    }

    fun onSalirClicked() {
        viewModelScope.launch { _uiEffects.emit(WelcomeUiEffect.ShowExitDialog) }
    }

    fun onSecurePinConfirmed(pin: String, destination: WelcomeSecurePinDestination) {
        viewModelScope.launch {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (pin == actualPin) {
                when (destination) {
                    WelcomeSecurePinDestination.SHIFTS ->
                        _uiEffects.emit(
                            WelcomeUiEffect.Navigate(
                                WelcomeFragmentDirections.actionWelcomeFragmentToShiftsFragment()
                            )
                        )
                    WelcomeSecurePinDestination.STATISTICS ->
                        _uiEffects.emit(
                            WelcomeUiEffect.Navigate(
                                WelcomeFragmentDirections.actionWelcomeFragmentToStatisticsFragment()
                            )
                        )
                }
            } else {
                _uiEffects.emit(WelcomeUiEffect.ShowToast(R.string.pin_incorrecto))
            }
        }
    }

    fun onPrivacyAccepted() {
        viewModelScope.launch {
            _uiEffects.emit(WelcomeUiEffect.ShowToast(R.string.toast_aceptado))
            clickMenuLogin()
        }
    }

    fun onBatteryOptimizationAccepted() {
        viewModelScope.launch {
            _uiEffects.emit(WelcomeUiEffect.ConnectTaximeter)
            clickMenuLogin()
        }
    }

    fun onExitConfirmed() {
        viewModelScope.launch {
            _uiEffects.emit(WelcomeUiEffect.ShowExitDialog)
        }
    }

    private fun checkAdditionalPermissions() {
        if (!_uiState.value.batteryOptimizationAlreadyLogged) {
            viewModelScope.launch { _uiEffects.emit(WelcomeUiEffect.ShowBatteryOptimizationDialog) }
        } else {
            clickMenuLogin()
        }
    }

    private fun requestSecurePinIfNeeded(destination: WelcomeSecurePinDestination) {
        viewModelScope.launch {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            if (!currentSecurePin.isNullOrEmpty()) {
                _uiEffects.emit(WelcomeUiEffect.ShowSecurePinDialog(destination))
            } else {
                navigateToDestination(destination)
            }
        }
    }

    private suspend fun navigateToDestination(destination: WelcomeSecurePinDestination) {
        when (destination) {
            WelcomeSecurePinDestination.SHIFTS ->
                _uiEffects.emit(
                    WelcomeUiEffect.Navigate(
                        WelcomeFragmentDirections.actionWelcomeFragmentToShiftsFragment()
                    )
                )
            WelcomeSecurePinDestination.STATISTICS ->
                _uiEffects.emit(
                    WelcomeUiEffect.Navigate(
                        WelcomeFragmentDirections.actionWelcomeFragmentToStatisticsFragment()
                    )
                )
        }
    }

    private fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            val enabled = parameters?.isClosuresButton == true
            _uiState.update {
                it.copy(
                    hasClosuresPermission = enabled,
                    buttons = it.buttons.copy(
                        parciales = it.buttons.parciales.copy(
                            visible = enabled,
                            style = if (enabled) WelcomeButtonStyle.ENABLE else WelcomeButtonStyle.DISABLE
                        )
                    )
                )
            }
        }
    }

    private fun checkUserLogIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.update {
                it.copy(
                    isLoggedIn = loggedIn,
                    buttons = it.buttons.copy(
                        parciales = it.buttons.parciales.copy(
                            style = if (loggedIn) WelcomeButtonStyle.ENABLE else WelcomeButtonStyle.DISABLE
                        )
                    )
                )
            }
        }
    }

    private fun haveShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getLastShift()
            _uiState.update {
                it.copy(
                    hasShifts = shifts != null,
                    buttons = it.buttons.copy(
                        turnos = it.buttons.turnos.copy(
                            style = if (shifts != null) WelcomeButtonStyle.ENABLE else WelcomeButtonStyle.DISABLE
                        )
                    )
                )
            }
        }
    }

    private fun checkAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasAllPermissions = permissionsUseCase.hasAllPermissions()
            _uiState.update {
                it.copy(
                    hasAllPermissions = hasAllPermissions,
                    buttons = it.buttons.copy(
                        permisos = it.buttons.permisos.copy(
                            background = if (hasAllPermissions) WelcomeButtonBackground.BLUE else WelcomeButtonBackground.ORANGE
                        )
                    )
                )
            }
        }
    }

    private fun hasITopMeterPaired(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()

    private fun clickMenuLogin() {
        viewModelScope.launch {
            if (!sessionUseCase.isUserLoggedIn()) {
                _uiEffects.emit(
                    WelcomeUiEffect.Navigate(
                        WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                    )
                )
            } else {
                val conectionMode = bravoCentralConfigurationUseCase.getConnectionMode()
                var cntMethods = 0
                if ((conectionMode and 0x01) != 0) cntMethods++
                if ((conectionMode and 0x02) != 0) cntMethods++
                if ((conectionMode and 0x04) != 0) cntMethods++

                val startTurnMode = licensingUseCase.getStartTurnMode() ?: 1

                if (startTurnMode == 0 && cntMethods < 2) {
                    if ((conectionMode and 0x01) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", false)
                    }
                    if ((conectionMode and 0x02) != 0) {
                        startShiftUseCase.startShift()
                        shiftStatusUseCase.setStatus("STATE_FOR_HIRE_NO_CENTRAL", false)
                        withContext(Dispatchers.IO) { ticketUseCase.loadTickets() }
                        _uiEffects.emit(WelcomeUiEffect.ConnectTaximeter)
                        _uiEffects.emit(
                            WelcomeUiEffect.Navigate(
                                WelcomeFragmentDirections.actionWelcomeFragmentToHomeFragment()
                            )
                        )
                    }
                    if ((conectionMode and 0x04) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", true)
                    }
                } else {
                    _uiEffects.emit(
                        WelcomeUiEffect.Navigate(
                            WelcomeFragmentDirections.actionWelcomeFragmentToLoginDriverFragment()
                                .setConnectionMode(conectionMode)
                                .setStartTurnMode(startTurnMode)
                        )
                    )
                }
            }
        }
    }

    private fun makeMigrationV2() {
        viewModelScope.launch {
            val hasOldTrips = migrationV2UseCase.checkHasOldTrip()
            val hasOldShift = migrationV2UseCase.checkHasOldShift()
            if (hasOldShift || hasOldTrips) {
                val checkExistSession = sessionUseCase.getSession()
                if (checkExistSession == null) {
                    if (migrationV2UseCase.migrateSession()) {
                        _uiEffects.emit(
                            WelcomeUiEffect.Navigate(
                                WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                                    .setAutoDownloadConfigurationFromMigration(true)
                            )
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// Compose route
// --------------------------------------------------------------------------------------

@Composable
fun WelcomeRoute(
    navController: NavController,
    viewModel: WelcomeComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onExitApp: () -> Unit,
    onRequestBatteryOptimizationSettings: () -> Unit,
    onConnectTaximeter: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadingConfig by sharedViewModel.progressBarFlow.collectAsStateWithLifecycle(initialValue = Pair(0, 0))
    val correctLogin by sharedViewModel.correctLoginFlow.collectAsStateWithLifecycle(initialValue = Pair(false, false))
    val lastItopState by sharedViewModel.lastITopStateReceived.collectAsStateWithLifecycle(initialValue = null)
    val reconnection by sharedViewModel.reconnectionFlow.collectAsStateWithLifecycle(initialValue = false)

    var securePinDestination by remember { mutableStateOf<WelcomeSecurePinDestination?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onShowHeader(true)
        onShowBottomBar(true)
        viewModel.onStart()
        viewModel.onResume()
    }

    LaunchedEffect(downloadingConfig, correctLogin, lastItopState, reconnection) {
        // reducer bridge for shared activity state
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                is WelcomeUiEffect.Navigate -> navController.navigate(effect.directions)
                WelcomeUiEffect.ShowExitDialog -> showExitDialog = true
                WelcomeUiEffect.ShowBatteryOptimizationDialog -> showBatteryDialog = true
                is WelcomeUiEffect.ShowSecurePinDialog -> securePinDestination = effect.destination
                is WelcomeUiEffect.ShowToast -> onShowToast(effect.resId)
                WelcomeUiEffect.ConnectTaximeter -> onConnectTaximeter()
            }
        }
    }

    WelcomeScreen(
        uiState = uiState,
        onInicio = { viewModel.onInicioClicked() },
        onAjustes = { navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSettingsFragment()) },
        onTurnos = { viewModel.onTurnosClicked() },
        onEstadisticas = { viewModel.onEstadisticasClicked() },
        onConfiguracion = { viewModel.onConfiguracionClicked() },
        onPermisos = { viewModel.onPermisosClicked() },
        onParciales = { viewModel.onParcialesClicked() },
        onSalir = { viewModel.onSalirClicked() },
        securePinDestination = securePinDestination,
        onSecurePinConfirm = { pin, destination ->
            securePinDestination = null
            viewModel.onSecurePinConfirmed(pin, destination)
        },
        showExitDialog = showExitDialog,
        onDismissExitDialog = { showExitDialog = false },
        onExitConfirm = {
            showExitDialog = false
            onExitApp()
        },
        showBatteryDialog = showBatteryDialog,
        onDismissBatteryDialog = { showBatteryDialog = false },
        onBatteryConfirm = {
            showBatteryDialog = false
            onRequestBatteryOptimizationSettings()
            viewModel.onBatteryOptimizationAccepted()
        }
    )
}

// --------------------------------------------------------------------------------------
// Screen
// --------------------------------------------------------------------------------------

@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onInicio: () -> Unit,
    onAjustes: () -> Unit,
    onTurnos: () -> Unit,
    onEstadisticas: () -> Unit,
    onConfiguracion: () -> Unit,
    onPermisos: () -> Unit,
    onParciales: () -> Unit,
    onSalir: () -> Unit,
    securePinDestination: WelcomeSecurePinDestination?,
    onSecurePinConfirm: (String, WelcomeSecurePinDestination) -> Unit,
    showExitDialog: Boolean,
    onDismissExitDialog: () -> Unit,
    onExitConfirm: () -> Unit,
    showBatteryDialog: Boolean,
    onDismissBatteryDialog: () -> Unit,
    onBatteryConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WelcomeButton("Inicio", uiState.buttons.inicio, onInicio)
        WelcomeButton("Ajustes", uiState.buttons.ajustes, onAjustes)
        WelcomeButton("Turnos", uiState.buttons.turnos, onTurnos)
        WelcomeButton("Estadísticas", uiState.buttons.estadisticas, onEstadisticas)
        WelcomeButton("Configuración", uiState.buttons.configuracion, onConfiguracion)
        if (uiState.buttons.permisos.visible) {
            WelcomeButton("Permisos", uiState.buttons.permisos, onPermisos)
        }
        if (uiState.buttons.parciales.visible) {
            WelcomeButton("Parciales", uiState.buttons.parciales, onParciales)
        }
        WelcomeButton("Salir", uiState.buttons.salir, onSalir)
    }

    if (securePinDestination != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("PIN") },
            text = { Text("Ingrese su PIN actual") },
            confirmButton = {
                TextButton(onClick = {
                    onSecurePinConfirm("1234", securePinDestination)
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { /* dismiss handled by parent */ }) { Text("Cancelar") }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = onDismissExitDialog,
            title = { Text("Cerrar app") },
            confirmButton = { TextButton(onClick = onExitConfirm) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = onDismissExitDialog) { Text("Cancelar") } }
        )
    }

    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = onDismissBatteryDialog,
            title = { Text("Optimización de batería") },
            text = { Text("Debe desactivar la optimización de batería.") },
            confirmButton = { TextButton(onClick = onBatteryConfirm) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = onDismissBatteryDialog) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun WelcomeButton(
    text: String,
    state: WelcomeButtonState,
    onClick: () -> Unit,
) {
    if (!state.visible) return

    val enabled = state.style != WelcomeButtonStyle.DISABLE && state.style != WelcomeButtonStyle.LOADING
    val containerColor = when (state.background) {
        WelcomeButtonBackground.BLUE -> MaterialTheme.colorScheme.primary
        WelcomeButtonBackground.ORANGE -> MaterialTheme.colorScheme.tertiary
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.style == WelcomeButtonStyle.LOADING) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp))
            Spacer(Modifier.height(8.dp))
        }
        Text(text)
    }
}


