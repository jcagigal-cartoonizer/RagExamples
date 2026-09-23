package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.UserPreferencesUiState
import ifac.td.taxi.ui.screen.components.UserPreferencesDialogModel
import ifac.td.taxi.ui.screen.components.UserPreferencesButtonsState
import ifac.td.taxi.ui.screen.components.UserPreferencesUiState = copy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 576-3: import android.net.Uri
@Immutable
data class UserPreferencesUiState(
    val model: UserPreferences = UserPreferences(),
    val isLoggedIn: Boolean = false,
    val hasShifts: Boolean = false,
    val taximeterSkyGlass: Boolean = false,
    val lightOffVisible: Boolean = false,
    val fiscalLicensing: Boolean = false,
    val ingenicoInstalled: Boolean = false,
    val driverTurnMode: Int? = null,
    // section states
    val soundExpanded: Boolean = true,
    val invoicesExpanded: Boolean = true,
    val pinPadExpanded: Boolean = true,
    val shiftsExpanded: Boolean = true,
    val systemExpanded: Boolean = true,
    val locationExpanded: Boolean = true,
    val activeDialog: UserPreferencesDialogModel? = null,
) {
    fun deriveVisibility(): UserPreferencesUiState = copy()
}
sealed interface UserPreferencesUiEvent {
    data object ClickCancel : UserPreferencesUiEvent
    data object ClickAccept : UserPreferencesUiEvent
    data object ClickSecurePin : UserPreferencesUiEvent
    data object ClickFiscalPortugal : UserPreferencesUiEvent
    data object ClickSendLogs : UserPreferencesUiEvent
    data object ClickDeleteShifts : UserPreferencesUiEvent
    data object ToggleSound : UserPreferencesUiEvent
    data object ToggleInvoices : UserPreferencesUiEvent
    data object TogglePinPad : UserPreferencesUiEvent
    data object ToggleShifts : UserPreferencesUiEvent
    data object ToggleSystem : UserPreferencesUiEvent
    data object ToggleLocation : UserPreferencesUiEvent
    data class SetBeepNoBt(val value: Boolean) : UserPreferencesUiEvent
    data class SetVibrateNoBt(val value: Boolean) : UserPreferencesUiEvent
    data class SetUseExternalApp(val value: Boolean) : UserPreferencesUiEvent
    data class SetUseCustomPaymentTimerWithExternalApp(val value: Boolean) : UserPreferencesUiEvent
    data class SetPrintRedSysAlways(val value: Boolean) : UserPreferencesUiEvent
    data class SetUseTimeControl(val value: Boolean) : UserPreferencesUiEvent
    data class SetShowConfAccept(val value: Boolean) : UserPreferencesUiEvent
    data class SetShowConfReject(val value: Boolean) : UserPreferencesUiEvent
    data class SetUseFloatingWindow(val value: Boolean) : UserPreferencesUiEvent
    data class SetLightOffOnDispatched(val value: Boolean) : UserPreferencesUiEvent
    data class SetClosePendingTripsIfEmpty(val value: Boolean) : UserPreferencesUiEvent
    data object RequestPhoneCall : UserPreferencesUiEvent
    data object RequestBluetooth : UserPreferencesUiEvent
    data object RequestOverlay : UserPreferencesUiEvent
}
sealed interface UserPreferencesUiEffect {
    data object NavigateBack : UserPreferencesUiEffect
    data object NavigateToSecurePin : UserPreferencesUiEffect
    data object NavigateToPortugalSettings : UserPreferencesUiEffect
    data class NavigateToDeepLink(val uri: String) : UserPreferencesUiEffect
    data class ShowToast(val resId: Int) : UserPreferencesUiEffect
    data class OpenDialog(val dialog: UserPreferencesDialogModel) : UserPreferencesUiEffect
    data object CloseDialog : UserPreferencesUiEffect
    data object RequestPhonePermission : UserPreferencesUiEffect
    data object RequestBluetoothPermission : UserPreferencesUiEffect
    data object RequestOverlayPermission : UserPreferencesUiEffect
    data class OpenRingtonePicker(val intent: android.content.Intent, val type: Int) : UserPreferencesUiEffect
    data object OpenDatePickerDialog : UserPreferencesUiEffect
    data object KeepScreenOn : UserPreferencesUiEffect
    sealed interface DialogAction {
        data class Accept(val text: String? = null) : DialogAction
        data object Cancel : DialogAction
    }
}
@Immutable
data class UserPreferencesDialogModel(
    val title: String,
    val description: String? = null,
    val hint: String? = null,
    val text: String = "",
    val buttons: List<UserPreferencesDialogButton> = emptyList(),
    val isPin: Boolean = false,
    val showTextField: Boolean = false,
)
enum class UserPreferencesDialogButton { Cancel, Accept }
@Immutable
data class UserPreferencesButtonsState(
    val showControlHorario: Boolean,
    val showDeleteShiftsButton: Boolean,
    val showLightOffOnDispatched: Boolean,
    val showFiscalPortugalButton: Boolean,
    val showSecurePinButton: Boolean,
    val showSendLogsButton: Boolean,
    val showRedSysAlways: Boolean,
    val showPrintDriverSubscriber: Boolean,
    val cancelColors: ButtonColors,
    val acceptColors: ButtonColors,
    val secondaryColors: ButtonColors,
) {
    companion object {
        @androidx.compose.runtime.Composable
        fun from(state: UserPreferencesUiState): UserPreferencesButtonsState {
            return UserPreferencesButtonsState(
                showControlHorario = state.taximeterSkyGlass,
                showDeleteShiftsButton = state.hasShifts,
                showLightOffOnDispatched = state.lightOffVisible,
                showFiscalPortugalButton = state.fiscalLicensing,
                showSecurePinButton = true,
                showSendLogsButton = true,
                showRedSysAlways = state.model.pinPadSerialNumber.isNotBlank() || state.model.pinPadTypeId == 2,
                showPrintDriverSubscriber = true,
                cancelColors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C757D),
                    contentColor = Color.White
                ),
                acceptColors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D6EFD),
                    contentColor = Color.White
                ),
                secondaryColors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF198754),
                    contentColor = Color.White
                ),
            )
        }
    }
}
