package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.WelcomeViewModel
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
import ifac.td.taxi.ui.screen.WelcomeScreen
// # Block 122-2: import android.app.Application
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
    context: Application,
) : BaseViewModel(context) {
companion object {
    fun fromViewModel(viewModel: WelcomeViewModel): WelcomeComposeViewModel {
        val composeViewModel = WelcomeComposeViewModel(
            context = viewModel.context,
            bravoCentralConfigurationUseCase = viewModel.bravoCentralConfigurationUseCase,
            partialUseCase = viewModel.partialUseCase,
            ticketUseCase = viewModel.ticketUseCase,
            sessionUseCase = viewModel.sessionUseCase,
            startShiftUseCase = viewModel.startShiftUseCase,
            bluetoothLocalUseCase = viewModel.bluetoothLocalUseCase,
            userPreferencesUseCase = viewModel.userPreferencesUseCase,
            migrationV2UseCase = viewModel.migrationV2UseCase,
            licensingUseCase = viewModel.licensingUseCase,
            shiftUseCase = viewModel.shiftUseCase,
            loginDriverUseCase = viewModel.loginDriverUseCase,
            taximeterConnectUseCase = viewModel.taximeterConnectUseCase,
            permissionsUseCase = viewModel.permissionsUseCase,
            shiftStatusUseCase = viewModel.shiftStatusUseCase,
        )
        return composeViewModel
    }

}
    private val TAG = "WelcomeViewModel"
    lateinit var buttonsState : WelcomeButtonsState
    lateinit var dialogState : WelcomeDialogState
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<WelcomeUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    enum class SecurePinDestination {
        SHIFTS,
        STATISTICS,
    }
    fun onEvent(event: WelcomeUiEvent) {
        when (event) {
            WelcomeUiEvent.OnStart -> {
                checkClosuresPermission()
                makeMigrationV2()
            }
            WelcomeUiEvent.OnResume -> {
                checkUserLogIn()
                haveShifts()
                checkAllPermissions()
            }
            WelcomeUiEvent.OnInicioClicked -> checkAdditionalPermissions()
            WelcomeUiEvent.OnAjustesClicked -> clickSettings()
            WelcomeUiEvent.OnTurnosClicked -> clickShifts()
            WelcomeUiEvent.OnEstadisticasClicked -> clickStatistics()
            WelcomeUiEvent.OnConfiguracionClicked -> clickConfiguration()
            WelcomeUiEvent.OnPermisosClicked -> clickPermissions()
            WelcomeUiEvent.OnParcialesClicked -> clickParciales()
            WelcomeUiEvent.OnSalirClicked -> showExitDialog()
            WelcomeUiEvent.OnPrivacyAccepted -> onPrivacyAccepted()
            WelcomeUiEvent.OnPrivacyDeclined -> hidePrivacyDialog()
            is WelcomeUiEvent.OnSecurePinConfirmed -> {
                val destination = _uiState.value.securePinDestination ?: return
                verifyCurrentSecurePin(event.pin, destination)
            }
            WelcomeUiEvent.OnSecurePinDismissed -> hideSecurePinDialog()
            WelcomeUiEvent.OnExitConfirmed -> {
                viewModelScope.launch { _uiEffect.emit(WelcomeUiEffect.ExitApp) }
            }
        }
    }
    fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            val closuresEnabled = parameters?.isClosuresButton == true
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        parciales = it.buttons.parciales.copy(
                            visible = closuresEnabled,
                            enabled = closuresEnabled,
                            background = if (closuresEnabled) WelcomeButtonBackground.BLUE else WelcomeButtonBackground.ORANGE
                        )
                    )
                )
            }
        }
    }
    fun checkUserLogIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        parciales = it.buttons.parciales.copy(
                            enabled = loggedIn
                        )
                    )
                )
            }
            if (!loggedIn) {
                setButtonsUserLoggedOff()
            }
        }
    }
    fun haveShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getLastShift()
            val hasShifts = shifts != null
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        turnos = it.buttons.turnos.copy(
                            enabled = hasShifts,
                            background = if (hasShifts) WelcomeButtonBackground.BLUE else WelcomeButtonBackground.ORANGE
                        )
                    )
                )
            }
        }
    }
    fun checkAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasAllPermissions = permissionsUseCase.hasAllPermissions()
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        permisos = it.buttons.permisos.copy(
                            background = if (hasAllPermissions) WelcomeButtonBackground.BLUE else WelcomeButtonBackground.ORANGE
                        )
                    )
                )
            }
        }
    }
    fun hasITopMeterPaired(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()
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
    fun clickMenuLogin() {
        viewModelScope.launch {
            if (!sessionUseCase.isUserLoggedIn()) {
                navigateTo(R.id.action_welcomeFragment_to_loginUserFragment)
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
                        W2CLocation.setLocationAllowedByCentral(false)
                        sessionUseCase.updateWithCentralAllowed(false)
                        withContext(Dispatchers.IO) { ticketUseCase.loadTickets() }
                        startShiftUseCase.startShift()
                        shiftStatusUseCase.setStatus(ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                        _uiEffect.emit(WelcomeUiEffect.ConnectTaximeter)
                        navigateTo(R.id.action_welcomeFragment_to_homeFragment)
                    }
                    if ((conectionMode and 0x04) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", true)
                    }
                } else {
                    navigateTo(
                        WelcomeFragmentDirections.actionWelcomeFragmentToLoginDriverFragment()
                            .setConnectionMode(conectionMode)
                            .setStartTurnMode(startTurnMode)
                    )
                }
            }
        }
    }
    fun clickShifts() = requestSecurePinIfNeeded(SecurePinDestination.SHIFTS)
    fun clickStatistics() = requestSecurePinIfNeeded(SecurePinDestination.STATISTICS)
    fun clickSettings() {
        viewModelScope.launch { navigateTo(R.id.action_welcomeFragment_to_settingsFragment) }
    }
    fun clickConfiguration() {
        viewModelScope.launch { navigateTo(R.id.action_welcomeFragment_to_loginUserFragment) }
    }
    fun clickParciales() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                navigateTo(R.id.action_welcomeFragment_to_partials)
            } else {
                navigateTo(R.id.action_welcomeFragment_to_closed_partial)
            }
        }
    }
    fun clickPermissions() {
        viewModelScope.launch { navigateTo(R.id.action_welcomeFragment_to_permissionsFragment) }
    }
    fun makeMigrationV2() {
        viewModelScope.launch {
            val hasOldTrips = migrationV2UseCase.checkHasOldTrip()
            val hasOldShift = migrationV2UseCase.checkHasOldShift()
            if (hasOldShift || hasOldTrips) {
                val checkExistSession = sessionUseCase.getSession()
                if (checkExistSession == null) {
                    if (migrationV2UseCase.migrateSession()) {
                        Toast.makeText(context, "Start migration", Toast.LENGTH_LONG).show()
                        navigateTo(
                            WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                                .setAutoDownloadConfigurationFromMigration(true)
                        )
                    }
                }
            }
        }
    }
    fun checkAdditionalPermissions() {
        if (!hasITopMeterPaired()) {
            viewModelScope.launch {
                _uiEffect.emit(WelcomeUiEffect.ShowWelcomeSharedFlowBridgeWelcomeCustomDialog(
                    WelcomeDialogModel(
                        titleRes = R.string.dialog_no_permission_title,
                        descriptionRes = R.string.dialog_no_permission_desc,
                        buttons = listOf(WelcomeDialogButton.CANCEL, WelcomeDialogButton.ACCEPT)
                    )
                ))
            }
        } else {
            clickMenuLogin()
        }
    }
    fun requestSecurePinIfNeeded(destination: SecurePinDestination) {
        viewModelScope.launch {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            if (!currentSecurePin.isNullOrEmpty()) {
                _uiState.update { it.copy(securePinDestination = destination, showSecurePinDialog = true) }
            } else {
                navigateToDestination(destination)
            }
        }
    }
    fun verifyCurrentSecurePin(currentPin: String, destination: SecurePinDestination) {
        viewModelScope.launch {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) {
                hideSecurePinDialog()
                navigateToDestination(destination)
            } else {
                _uiState.update { it.copy(securePinErrorMessageRes = R.string.pin_incorrecto) }
                _uiEffect.emit(WelcomeUiEffect.ShowToast(R.string.pin_incorrecto))
            }
        }
    }
    private suspend fun navigateToDestination(destination: SecurePinDestination) {
        when (destination) {
            SecurePinDestination.SHIFTS ->
                navigateTo(R.id.action_welcomeFragment_to_shiftsFragment)
            SecurePinDestination.STATISTICS ->
                navigateTo(R.id.action_welcomeFragment_to_statisticsFragment)
        }
    }
    fun showExitDialog() {
        _uiState.update { it.copy(showExitDialog = true) }
    }
    fun hidePrivacyDialog() {
        _uiState.update { it.copy(showPrivacyDialog = false) }
    }
    fun hideSecurePinDialog() {
        _uiState.update { it.copy(showSecurePinDialog = false, securePinDestination = null) }
    }
    fun onPrivacyAccepted() {
        _uiState.update { it.copy(showPrivacyDialog = false) }
        viewModelScope.launch {
            _uiEffect.emit(WelcomeUiEffect.RequestPermissions)
        }
    }
    fun onPrivacyDialogNeeded() {
        _uiState.update { it.copy(showPrivacyDialog = true) }
    }
    fun setButtonsUserLoggedOff() {
        _uiState.update {
            it.copy(
                buttons = it.buttons.copy(
                    inicio = it.buttons.inicio.copy(enabled = true),
                    turnos = it.buttons.turnos.copy(enabled = false),
                    parciales = it.buttons.parciales.copy(enabled = false),
                    estadisticas = it.buttons.estadisticas.copy(enabled = true),
                )
            )
        }
    }
}
