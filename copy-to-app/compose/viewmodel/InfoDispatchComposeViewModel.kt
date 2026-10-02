package ifac.td.taxi.compose.viewmodel
import  android.app.Application
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
// # Block 150-3: import android.app.Application
class InfoDispatchComposeViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val phoneCallUseCase: PhoneCallUseCase,
    private val dispatchNotificationUseCase: DispatchNotificationUseCase,
    private val bridgeCallUseCase: BridgeCallUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val ttsUseCase: TTSUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(InfoDispatchUiState())
    val uiState: StateFlow<InfoDispatchUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<InfoDispatchUiEffect>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<InfoDispatchUiEffect> = _uiEffect.asSharedFlow()
    fun onDispatchChanged(dispatch: InfoDispatchModel?) {
        _uiState.update { it.copy(dispatch = dispatch) }
        rebuildButtons()
    }
    fun onExtraDataChanged(extra: DispatchExtraDataUi?) {
        _uiState.update { it.copy(extraData = extra) }
        rebuildButtons()
    }
    fun onNoClientEnabledChanged(value: Boolean?) {
        _uiState.update { it.copy(noClientEnabled = value) }
        rebuildButtons()
    }
    fun onBridgeCallStateChanged(state: Int) {
        _uiState.update { it.copy(bridgeCallState = state) }
        rebuildButtons()
    }
    fun onCustomerCallAvailabilityChanged(customerAvailable: Boolean, buttonAvailable: Boolean) {
        _uiState.update {
            it.copy(
                customerCallAvailable = customerAvailable,
                customerCallButtonAvailable = buttonAvailable
            )
        }
        rebuildButtons()
    }
    fun onShiftStatusChanged(currentStatus: Int?, isManual: Boolean, showReturnButton: Boolean) {
        _uiState.update {
            it.copy(
                currentStatus = currentStatus,
                shiftIsManual = isManual,
                showReturnDispatchButton = showReturnButton
            )
        }
        rebuildButtons()
    }
    fun onTaximeterConnectionChanged(connected: Boolean) {
        _uiState.update { it.copy(taximeterConnected = connected) }
        rebuildButtons()
    }
    fun onIsCurrentBluetoothITopChanged(value: Boolean) {
        _uiState.update { it.copy(isCurrentBluetoothITop = value) }
        rebuildButtons()
    }
    fun onTimerTick(value: Int?) {
        _uiState.update { it.copy(timerText = value?.toString().orEmpty()) }
    }
    fun onMeetingSignColorsLoaded(pair: Pair<Int, Int>) {
        _uiState.update { it.copy(meetingSignColors = pair) }
    }
    fun onNotificationClicked() {
        val dispatch = uiState.value.dispatch ?: return
        val buttons = buildList {
            if (dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false) add(InfoDispatchDialogButton.AtDoor)
            if (dispatch.riderInCab == true && dispatch.isRiderInCabNotificationSent == false) add(InfoDispatchDialogButton.RiderInCab)
        }
        _uiEffect.tryEmit(
            InfoDispatchUiEffect.OpenDialog(
                InfoDispatchDialogState(
                    title = "Select notification",
                    description = buildDialogDescription(dispatch),
                    buttons = buttons,
                    type = InfoDispatchDialogType.Notification
                )
            )
        )
    }
    fun onVoiceCallClicked() {
        val dispatch = uiState.value.dispatch ?: return
        if (!checkPermissionRequired()) {
            _uiEffect.tryEmit(InfoDispatchUiEffect.RequestPhonePermission)
            return
        }
        val isBridgeCall = dispatch.customerPhoneNumberExit?.toIntOrNull() == 4 && dispatch.customerPhoneNumber?.length ?: 0 > 1
        if (isBridgeCall) {
            when (uiState.value.bridgeCallState) {
                0 -> startBridgeCall(dispatch.longDispatchNumber.orEmpty())
                1, 2 -> cancelBridgeCall(dispatch.longDispatchNumber.orEmpty())
                else -> Unit
            }
        } else {
            dispatch.customerPhoneNumber?.let {
                _uiEffect.tryEmit(InfoDispatchUiEffect.OpenExternalPhoneCall(it))
            }
        }
    }
    fun onNavigateClicked() {
        _uiEffect.tryEmit(InfoDispatchUiEffect.NavigateToDirections)
    }
    fun onPrintClicked() {
        uiState.value.dispatch?.let { dispatchUseCase.getCurrentDispatchInfo(it) }
    }
    fun onNoClientClicked() {
        val dispatch = uiState.value.dispatch ?: return
        _uiEffect.tryEmit(
            InfoDispatchUiEffect.OpenDialog(
                InfoDispatchDialogState(
                    title = "Confirm no client",
                    buttons = listOf(InfoDispatchDialogButton.Cancel, InfoDispatchDialogButton.Accept),
                    type = InfoDispatchDialogType.Confirm
                )
            )
        )
    }
    fun onDialogResult(button: InfoDispatchDialogButton) {
        val dispatch = uiState.value.dispatch
        when (button) {
            InfoDispatchDialogButton.AtDoor -> dispatch?.let { sendAtTheDoorNotification(it) }
            InfoDispatchDialogButton.RiderInCab -> dispatch?.let { sendInCabNotification(it) }
            InfoDispatchDialogButton.Accept -> Unit
            InfoDispatchDialogButton.Cancel -> Unit
        }
        _uiEffect.tryEmit(InfoDispatchUiEffect.CloseDialog)
    }
    fun rebuildButtons() {
        val s = uiState.value
        val dispatch = s.dispatch
        val isITop = s.isCurrentBluetoothITop
        val isManual = s.shiftIsManual
        val taximeterRestriction = isITop && !isManual
        val notificationState = when {
            dispatch == null -> InfoDispatchButtonUiState.Hidden
            taximeterRestriction -> InfoDispatchButtonUiState.Visible(false, "Notifications", ButtonKind.Notification, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
            dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false && dispatch.riderInCab == true && dispatch.isRiderInCabNotificationSent == false ->
                InfoDispatchButtonUiState.Visible(true, "Avisos", ButtonKind.Notification, ButtonBackground.Green, InfoDispatchButtonStyles.White)
            !dispatch.isAtDoorNotificationEnabled() && dispatch.riderInCab == true && dispatch.isRiderInCabNotificationSent == false ->
                InfoDispatchButtonUiState.Visible(true, "Rider in cab", ButtonKind.Notification, ButtonBackground.Green, InfoDispatchButtonStyles.White)
            dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false ->
                InfoDispatchButtonUiState.Visible(true, "At door", ButtonKind.Notification, ButtonBackground.Green, InfoDispatchButtonStyles.White)
            else -> InfoDispatchButtonUiState.Visible(false, "Notifications", ButtonKind.Notification, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
        }
        val voiceCallState = when {
            !s.customerCallAvailable -> InfoDispatchButtonUiState.Visible(false, "Call", ButtonKind.VoiceCall, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
            s.customerCallButtonAvailable && s.bridgeCallState == 2 ->
                InfoDispatchButtonUiState.Visible(true, "Calling", ButtonKind.VoiceCall, ButtonBackground.Red, InfoDispatchButtonStyles.White)
            s.customerCallButtonAvailable && s.bridgeCallState == 0 ->
                InfoDispatchButtonUiState.Visible(true, "Call", ButtonKind.VoiceCall, ButtonBackground.Green, InfoDispatchButtonStyles.White)
            s.bridgeCallState != 0 ->
                InfoDispatchButtonUiState.Visible(true, "Waiting", ButtonKind.VoiceCall, ButtonBackground.Orange, InfoDispatchButtonStyles.White)
            else -> InfoDispatchButtonUiState.Visible(false, "Call", ButtonKind.VoiceCall, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
        }
        val noClientState = when {
            taximeterRestriction -> InfoDispatchButtonUiState.Visible(false, "No client", ButtonKind.NoClient, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
            s.noClientEnabled == true -> InfoDispatchButtonUiState.Visible(true, "No client", ButtonKind.NoClient, ButtonBackground.Red, InfoDispatchButtonStyles.White)
            else -> InfoDispatchButtonUiState.Visible(false, "No client", ButtonKind.NoClient, ButtonBackground.Disabled, InfoDispatchButtonStyles.White)
        }
        val returnState = if (s.showReturnDispatchButton && !taximeterRestriction) {
            InfoDispatchButtonUiState.Visible(true, "Return", ButtonKind.Return, ButtonBackground.Red, InfoDispatchButtonStyles.White)
        } else {
            InfoDispatchButtonUiState.Hidden
        }
        val navigateState = InfoDispatchButtonUiState.Visible(true, "Navigate", ButtonKind.Navigate, ButtonBackground.Green, InfoDispatchButtonStyles.White)
        _uiState.update {
            it.copy(
                buttons = InfoDispatchButtonsState(
                    notification = notificationState,
                    voiceCall = voiceCallState,
                    print = InfoDispatchButtonUiState.Visible(true, "Print", ButtonKind.Print, ButtonBackground.Green, InfoDispatchButtonStyles.White),
                    noClient = noClientState,
                    returnTrip = returnState,
                    navigate = navigateState
                )
            )
        }
    }
    fun buildDialogDescription(dispatch: InfoDispatchModel): String {
        val name = dispatch.dispatchName.orEmpty()
        val address = dispatch.pickUpAdress.orEmpty()
        val numDispatch = dispatch.longDispatchNumber.orEmpty()
        return "$name\n$address\n$numDispatch"
    }
    fun checkPermissionRequired(): Boolean = true
    fun startBridgeCall(dispatchNumber: String) { /* same logic as existing VM */ }
    fun cancelBridgeCall(dispatchNumber: String) { /* same logic as existing VM */ }
}
