package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.PredefinedMessagesUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.usecase.VoicePetitionUseCase
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.screen.ContactCentralFragmentDirections
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// STATE
// ------------------------------------------------------------

data class ContactCentralUiState(
    val buttons: ContactCentralButtonsState = ContactCentralButtonsState(),
    val showVoiceRequestDialog: Boolean = false,
)

data class ContactCentralButtonsState(
    val cancel: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        textRes = R.string.cancel,
        background = ButtonBackground.DEFAULT,
        style = ButtonStyle.ENABLE
    ),
    val shortBreak: ButtonUiState = ButtonUiState(
        visible = false,
        enabled = false,
        textRes = R.string.btn_short_break,
        background = ButtonBackground.GREEN,
        style = ButtonStyle.DISABLE
    ),
    val voiceCall: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        textRes = R.string.btn_voice_request,
        background = ButtonBackground.DEFAULT,
        style = ButtonStyle.ENABLE
    ),
    val messages: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = false,
        textRes = R.string.btn_messages,
        background = ButtonBackground.DEFAULT,
        style = ButtonStyle.DISABLE
    ),
    val information: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        textRes = R.string.btn_information,
        background = ButtonBackground.DEFAULT,
        style = ButtonStyle.ENABLE
    ),
)

data class ButtonUiState(
    val visible: Boolean,
    val enabled: Boolean,
    @StringRes val textRes: Int,
    val background: ButtonBackground,
    val style: ButtonStyle,
)

enum class ButtonBackground { DEFAULT, RED, GREEN }
enum class ButtonStyle { ENABLE, DISABLE, LOADING }

data class SharedContactCentralState(
    val shortBreakStatus: ShortBreakStatus? = null,
    val voiceValue: Boolean = true,
    val zone: String? = null,
    val hasShortBreak: Boolean = false,
    val currentShiftStatus: Int? = null,
    val isITopTaximeter: Boolean = false,
    val hasPredefinedMessages: Boolean = false,
)

// ------------------------------------------------------------
// EFFECTS
// ------------------------------------------------------------

sealed interface ContactCentralUiEffect {
    data object NavigateBack : ContactCentralUiEffect
    data class NavigateToPredefinedMessages(val directions: NavDirections) : ContactCentralUiEffect
    data class NavigateToInformationMessages(val directions: NavDirections) : ContactCentralUiEffect
    data object ShowShortBreakForcedToast : ContactCentralUiEffect
    data object TriggerShortBreakAction : ContactCentralUiEffect
    data class ShowVoiceRequestDialog(@StringRes val title: Int) : ContactCentralUiEffect
}

// ------------------------------------------------------------
// REDUCER
// ------------------------------------------------------------

data class ContactCentralReducerInput(
    val shortBreakStatus: ShortBreakStatus? = null,
    val voiceValue: Boolean = true,
    val zone: String? = null,
    val hasPredefinedMessages: Boolean = false,
    val hasShortBreak: Boolean = false,
    val currentShiftStatus: Int? = null,
    val isITopTaximeter: Boolean = false,
)

object ContactCentralButtonsReducer {

    fun reduce(
        previous: ContactCentralButtonsState,
        input: ContactCentralReducerInput,
        isHired: (Int) -> Boolean,
    ): ContactCentralButtonsState {
        val hasLocation = !input.zone.isNullOrBlank()

        val shortBreakButton = reduceShortBreakButton(
            shortBreakStatus = input.shortBreakStatus,
            hasLocation = hasLocation
        )

        val voiceButton = if (input.voiceValue) {
            ButtonUiState(
                visible = true,
                enabled = !input.isITopTaximeter,
                textRes = R.string.btn_voice_request,
                background = ButtonBackground.DEFAULT,
                style = if (input.isITopTaximeter) ButtonStyle.DISABLE else ButtonStyle.ENABLE
            )
        } else {
            ButtonUiState(
                visible = true,
                enabled = true,
                textRes = R.string.btn_cancel_voice_request,
                background = ButtonBackground.RED,
                style = ButtonStyle.ENABLE
            )
        }

        val messagesButton = previous.messages.copy(
            enabled = input.hasPredefinedMessages,
            style = if (input.hasPredefinedMessages) ButtonStyle.ENABLE else ButtonStyle.DISABLE
        )

        val cancelVisible = if (input.currentShiftStatus != null) {
            val hired = isHired(input.currentShiftStatus)
            !(!hired && input.hasShortBreak)
        } else {
            previous.cancel.visible
        }

        val shortBreakVisible = if (input.currentShiftStatus != null) {
            val hired = isHired(input.currentShiftStatus)
            !(!hired && input.hasShortBreak)
        } else {
            previous.shortBreak.visible
        }

        val finalVoiceButton = if (input.isITopTaximeter && input.voiceValue) {
            voiceButton.copy(enabled = false, style = ButtonStyle.DISABLE)
        } else {
            voiceButton
        }

        return previous.copy(
            cancel = previous.cancel.copy(visible = cancelVisible),
            shortBreak = shortBreakButton.copy(visible = shortBreakVisible),
            voiceCall = finalVoiceButton,
            messages = messagesButton,
            information = previous.information
        )
    }

    private fun reduceShortBreakButton(
        shortBreakStatus: ShortBreakStatus?,
        hasLocation: Boolean
    ): ButtonUiState {
        return if (hasLocation) {
            when (shortBreakStatus) {
                ShortBreakStatus.IN_SHORT_BREAK,
                ShortBreakStatus.IN_SHORT_BREAK_FORCED -> ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_end_short_break,
                    background = ButtonBackground.RED,
                    style = ButtonStyle.ENABLE
                )

                ShortBreakStatus.CAN_START_SHORT_BREAK -> ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_short_break,
                    background = ButtonBackground.GREEN,
                    style = ButtonStyle.ENABLE
                )

                ShortBreakStatus.SHORT_BREAK_DISABLED -> ButtonUiState(
                    visible = true,
                    enabled = false,
                    textRes = R.string.btn_short_break,
                    background = ButtonBackground.GREEN,
                    style = ButtonStyle.DISABLE
                )

                null -> ButtonUiState(
                    visible = true,
                    enabled = false,
                    textRes = R.string.btn_short_break,
                    background = ButtonBackground.GREEN,
                    style = ButtonStyle.DISABLE
                )
            }
        } else {
            if (shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK &&
                shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK_FORCED
            ) {
                ButtonUiState(
                    visible = true,
                    enabled = false,
                    textRes = R.string.btn_short_break,
                    background = ButtonBackground.GREEN,
                    style = ButtonStyle.DISABLE
                )
            } else {
                ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_end_short_break,
                    background = ButtonBackground.RED,
                    style = ButtonStyle.ENABLE
                )
            }
        }
    }
}

// ------------------------------------------------------------
// SCREEN VIEWMODEL
// ------------------------------------------------------------

class ContactCentralComposeViewModel(
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val voicePetitionUseCase: VoicePetitionUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ContactCentralUiState())
    val uiState: StateFlow<ContactCentralUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ContactCentralUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ContactCentralUiEffect> = _uiEffect.asSharedFlow()

    fun initialize() {
        viewModelScope.launch {
            val hasMessages = predefinedMessagesUseCase.getPredefinedMessages().isNotEmpty()
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        messages = it.buttons.messages.copy(
                            enabled = hasMessages,
                            style = if (hasMessages) ButtonStyle.ENABLE else ButtonStyle.DISABLE
                        )
                    )
                )
            }
        }
    }

    fun checkITopTaximeter() {
        viewModelScope.launch {
            val isITop = bluetoothLocalUseCase.isCurrentBluetoothItop()
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        voiceCall = it.buttons.voiceCall.copy(
                            enabled = !isITop,
                            style = if (isITop) ButtonStyle.DISABLE else it.buttons.voiceCall.style
                        )
                    )
                )
            }
        }
    }

    fun onCancelClick() {
        emitEffect(ContactCentralUiEffect.NavigateBack)
    }

    fun onShortBreakClick(status: ShortBreakStatus, hasLocation: Boolean) {
        if (status == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
            emitEffect(ContactCentralUiEffect.ShowShortBreakForcedToast)
            return
        }

        _uiState.update {
            it.copy(
                buttons = it.buttons.copy(
                    shortBreak = it.buttons.shortBreak.copy(style = ButtonStyle.LOADING)
                )
            )
        }
        emitEffect(ContactCentralUiEffect.TriggerShortBreakAction)
    }

    fun onVoiceCallClick(voiceValue: Boolean) {
        if (voiceValue) {
            _uiState.update { it.copy(showVoiceRequestDialog = true) }
            emitEffect(ContactCentralUiEffect.ShowVoiceRequestDialog(R.string.btn_voice_request))
        } else {
            voicePetitionUseCase.voiceRequest(false)
            emitEffect(ContactCentralUiEffect.NavigateBack)
        }
    }

    fun onVoiceRequestDialogAccept() {
        _uiState.update { it.copy(showVoiceRequestDialog = false) }
        voicePetitionUseCase.voiceRequest(true)
        emitEffect(ContactCentralUiEffect.NavigateBack)
    }

    fun onVoiceRequestDialogDismiss() {
        _uiState.update { it.copy(showVoiceRequestDialog = false) }
    }

    fun onMessagesClick() {
        emitEffect(
            ContactCentralUiEffect.NavigateToPredefinedMessages(
                ContactCentralFragmentDirections.actionContactCentralFragmentToPredefinedMessageFragment(
                    -1,
                    true
                )
            )
        )
    }

    fun onInformationClick() {
        emitEffect(
            ContactCentralUiEffect.NavigateToInformationMessages(
                ContactCentralFragmentDirections.actionContactCentralFragmentToInformationMessageFragment()
            )
        )
    }

    fun reduceFromSharedState(shared: SharedContactCentralState) {
        val input = ContactCentralReducerInput(
            shortBreakStatus = shared.shortBreakStatus,
            voiceValue = shared.voiceValue,
            zone = shared.zone,
            hasPredefinedMessages = shared.hasPredefinedMessages,
            hasShortBreak = shared.hasShortBreak,
            currentShiftStatus = shared.currentShiftStatus,
            isITopTaximeter = shared.isITopTaximeter
        )

        _uiState.update { current ->
            current.copy(
                buttons = ContactCentralButtonsReducer.reduce(
                    previous = current.buttons,
                    input = input,
                    isHired = { status -> shiftStatusUseCase.isHired(status) }
                )
            )
        }
    }

    private fun emitEffect(effect: ContactCentralUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}

// ------------------------------------------------------------
// SHARED VM ADAPTER FOR YOUR REAL MainActivityViewModel
// ------------------------------------------------------------

class ContactCentralMainActivityViewModelAdapter(
    private val mainActivityViewModel: MainActivityViewModel,
) : ViewModel() {

    private val _sharedState = MutableStateFlow(SharedContactCentralState())
    val sharedState: StateFlow<SharedContactCentralState> = _sharedState.asStateFlow()

    fun bind() {
        viewModelScope.launch {
            launch {
                mainActivityViewModel.shortBreakStatus.collectLatest { status ->
                    _sharedState.update { it.copy(shortBreakStatus = status) }
                }
            }
            launch {
                mainActivityViewModel.bravoStateFlow.collectLatest { bravo ->
                    _sharedState.update { it.copy(voiceValue = bravo.voiceValue) }
                }
            }
            launch {
                mainActivityViewModel.zoneFlow.collectLatest { zone ->
                    _sharedState.update { it.copy(zone = zone) }
                }
            }
            launch {
                mainActivityViewModel.shiftStatusFlow.collectLatest { shift ->
                    _sharedState.update { it.copy(currentShiftStatus = shift?.currentStatus) }
                }
            }
            launch {
                mainActivityViewModel.locationType.collectLatest {
                    // If you want to derive hasLocation from locationType instead of zone,
                    // map it here. Kept intentionally no-op to match your existing fragment.
                }
            }
        }
    }

    fun setHasShortBreak(value: Boolean) {
        _sharedState.update { it.copy(hasShortBreak = value) }
    }

    fun setIsITopTaximeter(value: Boolean) {
        _sharedState.update { it.copy(isITopTaximeter = value) }
    }

    fun setHasPredefinedMessages(value: Boolean) {
        _sharedState.update { it.copy(hasPredefinedMessages = value) }
    }
}

// ------------------------------------------------------------
// COMPOSE UI
// ------------------------------------------------------------

data class ContactCentralDialogState(
    @StringRes val title: Int
)

@Composable
fun ContactCentralScreen(
    navController: NavController,
    viewModel: ContactCentralComposeViewModel,
    sharedUiState: SharedContactCentralState,
    onShortBreakButtonPressed: () -> Unit,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.initialize()
    }

    LaunchedEffect(sharedUiState) {
        viewModel.reduceFromSharedState(sharedUiState)
    }

    val dialogState = remember { mutableStateOf<ContactCentralDialogState?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                ContactCentralUiEffect.NavigateBack -> navController.popBackStack()

                is ContactCentralUiEffect.NavigateToPredefinedMessages ->
                    navController.navigate(effect.directions)

                is ContactCentralUiEffect.NavigateToInformationMessages ->
                    navController.navigate(effect.directions)

                ContactCentralUiEffect.ShowShortBreakForcedToast ->
                    onShowToast(R.string.short_break_forced)

                is ContactCentralUiEffect.ShowVoiceRequestDialog ->
                    dialogState.value = ContactCentralDialogState(title = effect.title)

                ContactCentralUiEffect.TriggerShortBreakAction -> Unit
            }
        }
    }

    ContactCentralDialogHost(
        dialogState = dialogState.value,
        onDismiss = {
            dialogState.value = null
            viewModel.onVoiceRequestDialogDismiss()
        },
        onAccept = {
            dialogState.value = null
            viewModel.onVoiceRequestDialogAccept()
        }
    )

    ContactCentralContent(
        state = uiState,
        sharedUiState = sharedUiState,
        onCancel = { viewModel.onCancelClick() },
        onShortBreak = {
            val status = sharedUiState.shortBreakStatus
            if (status == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                onShowToast(R.string.short_break_forced)
            } else {
                viewModel.onShortBreakClick(
                    status = status ?: ShortBreakStatus.SHORT_BREAK_DISABLED,
                    hasLocation = !sharedUiState.zone.isNullOrBlank()
                )
                onShortBreakButtonPressed()
            }
        },
        onVoiceCall = { viewModel.onVoiceCallClick(sharedUiState.voiceValue) },
        onMessages = { viewModel.onMessagesClick() },
        onInformation = { viewModel.onInformationClick() }
    )
}

@Composable
fun ContactCentralContent(
    state: ContactCentralUiState,
    sharedUiState: SharedContactCentralState,
    onCancel: () -> Unit,
    onShortBreak: () -> Unit,
    onVoiceCall: () -> Unit,
    onMessages: () -> Unit,
    onInformation: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ContactCentralButton(
            style = state.buttons.cancel,
            onClick = onCancel
        )

        Spacer(Modifier.height(12.dp))

        if (state.buttons.shortBreak.visible) {
            ContactCentralButton(
                style = state.buttons.shortBreak,
                onClick = onShortBreak
            )
        }

        Spacer(Modifier.height(12.dp))

        ContactCentralButton(
            style = state.buttons.voiceCall,
            onClick = onVoiceCall
        )

        Spacer(Modifier.height(12.dp))

        ContactCentralButton(
            style = state.buttons.messages,
            onClick = onMessages
        )

        Spacer(Modifier.height(12.dp))

        ContactCentralButton(
            style = state.buttons.information,
            onClick = onInformation
        )
    }
}

@Composable
fun ContactCentralButton(
    style: ButtonUiState,
    onClick: () -> Unit,
) {
    if (!style.visible) return

    val backgroundColor = when (style.background) {
        ButtonBackground.DEFAULT -> Color(0xFF2D2D2D)
        ButtonBackground.RED -> Color(0xFFD32F2F)
        ButtonBackground.GREEN -> Color(0xFF388E3C)
    }

    val alpha = when (style.style) {
        ButtonStyle.DISABLE -> 0.4f
        ButtonStyle.LOADING -> 0.7f
        ButtonStyle.ENABLE -> 1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(backgroundColor.copy(alpha = alpha))
            .clickable(enabled = style.enabled && style.style != ButtonStyle.LOADING) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (style.style) {
                ButtonStyle.LOADING -> "..."
                else -> androidx.compose.ui.res.stringResource(style.textRes).uppercase()
            },
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContactCentralDialogHost(
    dialogState: ContactCentralDialogState?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    if (dialogState != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = androidx.compose.ui.res.stringResource(dialogState.title)) },
            text = { Text(text = androidx.compose.ui.res.stringResource(dialogState.title)) },
            confirmButton = {
                TextButton(onClick = onAccept) {
                    Text(text = androidx.compose.ui.res.stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = androidx.compose.ui.res.stringResource(android.R.string.cancel))
                }
            }
        )
    }
}


