package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCaseImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class InfoDispatchComposeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(InfoDispatchUiState())
    val uiState: StateFlow<InfoDispatchUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<InfoDispatchUiEffect>()
    val uiEffect: SharedFlow<InfoDispatchUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: InfoDispatchUiEvent) {
        when (event) {
            InfoDispatchUiEvent.OnBackPressed -> emitEffect(InfoDispatchUiEffect.NavigateToHome)
            InfoDispatchUiEvent.OnNavigateDirections -> emitEffect(InfoDispatchUiEffect.NavigateToDirections)
            InfoDispatchUiEvent.OnNavigateMeetingSign -> {
                val colors = _uiState.value.meetingSignColors
                emitEffect(InfoDispatchUiEffect.NavigateToMeetingSign(colors.first, colors.second))
            }
            InfoDispatchUiEvent.OnClickNotifications -> handleNotifications()
            InfoDispatchUiEvent.OnClickVoiceCall -> handleVoiceCall()
            InfoDispatchUiEvent.OnClickPrint -> { /* call print use case */ }
            InfoDispatchUiEvent.OnClickNoClient -> handleNoClient()
            InfoDispatchUiEvent.OnClickReturn -> { /* open dialog via effect */ }
            InfoDispatchUiEvent.OnClickFlightCode -> {
                val colors = _uiState.value.meetingSignColors
                emitEffect(InfoDispatchUiEffect.NavigateToMeetingSign(colors.first, colors.second))
            }
            is InfoDispatchUiEvent.OnTabSelected -> selectTab(event.index)
            is InfoDispatchUiEvent.OnDialogButtonClicked -> handleDialogAction(event.button)
        }
    }
    fun emitEffect(effect: InfoDispatchUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
    fun updateState(reducer: (InfoDispatchUiState) -> InfoDispatchUiState) {
        _uiState.update(reducer)
    }
    fun handleNotifications() {
        val dispatch = _uiState.value.dispatch ?: return
        val atDoor = dispatch.isAtDoorNotificationEnabled && !dispatch.isAtDoorNotificationSent
        val rider = dispatch.riderInCab && !dispatch.isRiderInCabNotificationSent
        when {
            atDoor && rider -> {
                emitEffect(
                    InfoDispatchUiEffect.OpenDialog(
                        InfoDispatchDialogState(
                            type = DialogType.SELECT_NOTIFICATION,
                            title = "Select notification",
                            description = buildDispatchDescription(dispatch),
                            buttons = listOf(
                                DialogButton(InfoDispatchDialogButtonType.AT_DOOR, "AT DOOR"),
                                DialogButton(InfoDispatchDialogButtonType.RIDER_IN_CAB, "RIDER IN CAB")
                            )
                        )
                    )
                )
            }
            atDoor -> {
                emitEffect(
                    InfoDispatchUiEffect.OpenDialog(
                        InfoDispatchDialogState(
                            type = DialogType.SELECT_NOTIFICATION,
                            title = "Select notification",
                            description = buildDispatchDescription(dispatch),
                            buttons = listOf(DialogButton(InfoDispatchDialogButtonType.AT_DOOR, "AT DOOR"))
                        )
                    )
                )
            }
            rider -> {
                emitEffect(
                    InfoDispatchUiEffect.OpenDialog(
                        InfoDispatchDialogState(
                            type = DialogType.SELECT_NOTIFICATION,
                            title = "Select notification",
                            description = buildDispatchDescription(dispatch),
                            buttons = listOf(DialogButton(InfoDispatchDialogButtonType.RIDER_IN_CAB, "RIDER IN CAB"))
                        )
                    )
                )
            }
        }
    }
    fun handleVoiceCall() {
        val dispatch = _uiState.value.dispatch ?: return
        if (dispatch.isBridgeCall) {
            when (_uiState.value.bridgeCallState) {
                0 -> updateState { it.copy(buttons = it.buttons.copy(voiceCall = ComposeButtonState.loading())) }
                1, 2 -> { /* cancel bridge call */ }
            }
        } else {
            if (dispatch.customerPhoneNumber.isBlank()) return
            emitEffect(InfoDispatchUiEffect.OpenExternalPhoneCall(dispatch.customerPhoneNumber))
        }
    }
    fun handleNoClient() {
        emitEffect(
            InfoDispatchUiEffect.OpenDialog(
                InfoDispatchDialogState(
                    type = DialogType.CONFIRM_NO_CLIENT,
                    title = "Confirm no client",
                    buttons = listOf(
                        DialogButton(InfoDispatchDialogButtonType.CANCEL, "CANCEL"),
                        DialogButton(InfoDispatchDialogButtonType.ACCEPT, "ACCEPT")
                    )
                )
            )
        )
    }
    fun handleDialogAction(button: InfoDispatchDialogButtonType) {
        when (button) {
            InfoDispatchDialogButtonType.CANCEL -> emitEffect(InfoDispatchUiEffect.CloseDialog)
            InfoDispatchDialogButtonType.ACCEPT -> emitEffect(InfoDispatchUiEffect.CloseDialog)
            InfoDispatchDialogButtonType.AT_DOOR -> emitEffect(InfoDispatchUiEffect.CloseDialog)
            InfoDispatchDialogButtonType.RIDER_IN_CAB -> emitEffect(InfoDispatchUiEffect.CloseDialog)
        }
    }
    fun selectTab(index: Int) {
        val dispatch = _uiState.value.multiDispatch.getOrNull(index) ?: return
        updateState { it.copy(selectedTabIndex = index) }
    }
    fun buildDispatchDescription(dispatch: InfoDispatchUiModel): String =
        listOf(dispatch.dispatchName.orEmpty(), dispatch.pickUpAdress.orEmpty(), dispatch.longDispatchNumber.orEmpty())
            .joinToString("\n")
}
data class InfoDispatchUiModel(
    val id: Long,
    val dispatchName: String? = null,
    val pickUpAdress: String? = null,
    val longDispatchNumber: String = "",
    val customerPhoneNumber: String = "",
    val isBridgeCall: Boolean = false,
    val isAtDoorNotificationEnabled: Boolean = false,
    val isAtDoorNotificationSent: Boolean = false,
    val riderInCab: Boolean = false,
    val isRiderInCabNotificationSent: Boolean = false
)
data class DispatchExtraUiModel(
    val externalTripId: Int = 0,
    val tolls: Int? = null,
    val customerRequirements: String? = null
)
