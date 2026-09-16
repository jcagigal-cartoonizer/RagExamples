package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.state.InfoDispatchDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.state.InfoDispatchUiEvent
import ifac.td.taxi.ui.screen.state.InfoDispatchUiState
import ifac.td.taxi.ui.screen.state.MessageUiState
import ifac.td.taxi.domain.usecase.PendingTripsUseCaseImpl
import ifac.td.taxi.ui.screen.state.DashboardDialogState
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.ui.screen.state.ComposeButtonState
import ifac.td.taxi.ui.screen.state.InfoDispatchUiEffect
// // ## `InfoDispatchComposeViewModel.kt`


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCaseImpl
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InfoDispatchComposeViewModel(
    application: Application,
    private val legacyVm: ifac.td.taxi.viewmodel.InfoDispatchViewModel
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InfoDispatchUiState())
    val uiState: StateFlow<InfoDispatchUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<InfoDispatchUiEffect>()
    val effects: SharedFlow<InfoDispatchUiEffect> = _effects.asSharedFlow()

    fun bindDispatch(dispatch: InfoDispatchModel?) {
        _uiState.update { current ->
            current.copy(
                dispatch = dispatch,
                buttons = current.buttons.copy(
                    notifications = current.buttons.notifications.copy(
                        visible = true
                    )
                )
            )
        }
        dispatch?.let { dispatch ->
            refreshButtons(dispatch)
            refreshDetails(dispatch)
        }
    }

    fun onEvent(event: InfoDispatchUiEvent) {
        when (event) {
            InfoDispatchUiEvent.NavigateClicked -> {
                viewModelScope.launch { _effects.emit(InfoDispatchUiEffect.NavigateToDirections) }
            }

            InfoDispatchUiEvent.VoiceCallClicked -> {
                val dispatch = uiState.value.dispatch ?: return
                legacyVm.stopTTS()
                legacyVm.startCall(dispatch.customerPhoneNumber ?: "")
            }

            InfoDispatchUiEvent.PrintClicked -> {
                uiState.value.dispatch?.let { legacyVm.printInfoDispatch(it) }
            }

            InfoDispatchUiEvent.NoClientClicked -> {
                _uiState.update { it.copy(dialogState = InfoDispatchDialogState(
                    visible = true,
                    title = "Confirm no client",
                    description = "",
                    buttons = listOf(InfoDispatchDialogAction.CANCEL, InfoDispatchDialogAction.ACCEPT)
                )) }
            }

            InfoDispatchUiEvent.ReturnClicked -> {
                _uiState.update { it.copy(dialogState = InfoDispatchDialogState(
                    visible = true,
                    title = "Return dispatch",
                    description = "Confirm change to hired",
                    buttons = listOf(InfoDispatchDialogAction.CANCEL, InfoDispatchDialogAction.ACCEPT)
                )) }
            }

            InfoDispatchUiEvent.NotificationsClicked -> {
                _uiState.update { it.copy(dialogState = InfoDispatchDialogState(
                    visible = true,
                    title = "Select notification",
                    description = "",
                    buttons = listOf(InfoDispatchDialogAction.AT_DOOR, InfoDispatchDialogAction.RIDER_IN_CAB)
                )) }
            }

            InfoDispatchUiEvent.DismissDialog -> {
                _uiState.update { it.copy(dialogState = InfoDispatchDialogState()) }
            }

            is InfoDispatchUiEvent.SelectDispatchTab -> Unit
            is InfoDispatchUiEvent.DialogAction -> onDialogAction(event.action)
        }
    }

    fun onDialogAction(action: InfoDispatchDialogAction) {
        val dispatch = uiState.value.dispatch
        when (action) {
            InfoDispatchDialogAction.CANCEL -> onEvent(InfoDispatchUiEvent.DismissDialog)
            InfoDispatchDialogAction.ACCEPT -> {
                // host decides depending on dialog context
                onEvent(InfoDispatchUiEvent.DismissDialog)
            }
            InfoDispatchDialogAction.AT_DOOR -> {
                dispatch?.let { legacyVm.sendAtTheDoorNotification(it) }
                onEvent(InfoDispatchUiEvent.DismissDialog)
            }
            InfoDispatchDialogAction.RIDER_IN_CAB -> {
                dispatch?.let { legacyVm.sendInCabNotification(it) }
                onEvent(InfoDispatchUiEvent.DismissDialog)
            }
        }
    }

    private fun refreshButtons(dispatch: InfoDispatchModel) {
        val hasAtDoorAction = dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false
        val hasRiderInCabAction = (dispatch.riderInCab ?: false) && dispatch.isRiderInCabNotificationSent == false

        val notificationsState = when {
            hasAtDoorAction && hasRiderInCabAction -> ButtonUiState(true, true, ButtonStyleUi.Enabled, ButtonBackgroundUi.Blue, "AVISOS")
            !hasAtDoorAction && hasRiderInCabAction -> ButtonUiState(true, true, ButtonStyleUi.Enabled, ButtonBackgroundUi.Green, "RIDER IN CAB")
            hasAtDoorAction -> ButtonUiState(true, true, ButtonStyleUi.Enabled, ButtonBackgroundUi.Orange, "AT DOOR")
            else -> ButtonUiState(true, false, ButtonStyleUi.Disabled, ButtonBackgroundUi.Gray, "AVISOS")
        }

        _uiState.update {
            it.copy(
                buttons = it.buttons.copy(
                    notifications = notificationsState,
                    noClient = it.buttons.noClient.copy(visible = true),
                    voiceCall = it.buttons.voiceCall.copy(visible = true),
                    returnDispatch = it.buttons.returnDispatch.copy(visible = true),
                    print = it.buttons.print.copy(visible = true),
                    navigate = it.buttons.navigate.copy(visible = true),
                )
            )
        }
    }

    private fun refreshDetails(dispatch: InfoDispatchModel) {
        _uiState.update { it.copy(details = listOfNotNull(dispatch.pickUpAdress, dispatch.destinyAdress?.joinToString("\n"))) }
    }
}


// // # 9) Navigation preservation
