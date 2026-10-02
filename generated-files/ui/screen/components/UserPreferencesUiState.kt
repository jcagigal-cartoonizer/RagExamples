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
