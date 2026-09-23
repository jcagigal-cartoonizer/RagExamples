package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ContactCentralUiState
import ifac.td.taxi.ui.screen.components.ContactCentralUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 97-2: import android.app.Application
class ContactCentralComposeViewModel(
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val voicePetitionUseCase: VoicePetitionUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ContactCentralUiState())
    val uiState: StateFlow<ContactCentralUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ContactCentralUiEffect>()
    val uiEffect: SharedFlow<ContactCentralUiEffect> = _uiEffect.asSharedFlow()
    private var hasShortBreak: Boolean = false
    private var hasPredefinedMessages: Boolean = false
    private var isITopTaximeter: Boolean = false
    fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            hasPredefinedMessages = predefinedMessagesUseCase.getPredefinedMessages().isNotEmpty()
            hasShortBreak = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.hasShortBreak ?: false
            isITopTaximeter = bluetoothLocalUseCase.isCurrentBluetoothItop()
            reduceButtons()
        }
    }
    fun onCancelClick() {
        emitEffect(ContactCentralUiEffect.NavigateBack)
    }
    fun onShortBreakClick(shortBreakStatus: ShortBreakStatus, hasLocation: Boolean) {
        if (shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
            emitEffect(ContactCentralUiEffect.ShowShortBreakForcedToast)
            return
        }
        _uiState.update {
            it.copy(
                buttons = it.buttons.copy(
                    shortBreak = it.buttons.shortBreak.copy(loading = true, enabled = false)
                ),
                isLoadingShortBreakAction = true
            )
        }
        // Preserves old fragment behavior: action was delegated to activity
    }
    fun onVoiceCallClick(voiceValue: Boolean) {
        if (voiceValue) {
            emitEffect(ContactCentralUiEffect.ShowVoiceRequestDialog(
                title = getApplication<Application>().getString(R.string.btn_voice_request)
            ))
        } else {
            voicePetitionUseCase.voiceRequest(false)
            emitEffect(ContactCentralUiEffect.NavigateBack)
        }
    }
    fun onVoiceRequestDialogAccept() {
        voicePetitionUseCase.voiceRequest(true)
        emitEffect(ContactCentralUiEffect.NavigateBack)
    }
    fun onMessagesClick() {
        emitEffect(ContactCentralUiEffect.NavigateToPredefinedMessages)
    }
    fun onInformationClick() {
        emitEffect(ContactCentralUiEffect.NavigateToInformationMessages)
    }
    fun onShortBreakStatusChanged(status: ShortBreakStatus?, hasLocation: Boolean) {
        if (status == null) return
        reduceButtons(shortBreakStatus = status, hasLocation = hasLocation)
    }
    fun onLocationTypeChanged(locationType: String?) {
        reduceButtons(locationType = locationType)
    }
    fun onBravoVoiceChanged(voiceValue: Boolean) {
        _uiState.update { state ->
            state.copy(
                buttons = state.buttons.copy(
                    voiceCall = state.buttons.voiceCall.copy(
                        background = if (voiceValue) ContactCentralButtonBackground.DEFAULT else ContactCentralButtonBackground.RED,
                        text = if (voiceValue) "VOICE REQUEST" else "CANCEL VOICE REQUEST",
                        enabled = true
                    )
                )
            )
        }
    }
    fun onHasPredefinedMessagesChanged(hasMessages: Boolean) {
        hasPredefinedMessages = hasMessages
        reduceButtons()
    }
    fun onHasShortBreakChanged(hasShortBreakValue: Boolean) {
        hasShortBreak = hasShortBreakValue
        reduceButtons()
    }
    fun onIsITopTaximeterChanged(isITop: Boolean) {
        isITopTaximeter = isITop
        reduceButtons()
    }
    fun reduceButtons(
        shortBreakStatus: ShortBreakStatus? = null,
        hasLocation: Boolean = true,
        locationType: String? = null
    ) {
        val current = _uiState.value.buttons
        val shortBreakButton = when {
            locationType != null && locationType != "Z" -> current.shortBreak.copy(
                enabled = false,
                loading = false,
                visible = true,
                background = ContactCentralButtonBackground.GREEN,
                text = "SHORT BREAK"
            )
            shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK ||
                shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED -> {
                current.shortBreak.copy(
                    enabled = true,
                    loading = false,
                    visible = true,
                    background = ContactCentralButtonBackground.RED,
                    text = "END SHORT BREAK"
                )
            }
            shortBreakStatus == ShortBreakStatus.CAN_START_SHORT_BREAK -> {
                current.shortBreak.copy(
                    enabled = true,
                    loading = false,
                    visible = true,
                    background = ContactCentralButtonBackground.GREEN,
                    text = "SHORT BREAK"
                )
            }
            shortBreakStatus == ShortBreakStatus.SHORT_BREAK_DISABLED -> {
                current.shortBreak.copy(
                    enabled = false,
                    loading = false,
                    visible = true,
                    background = ContactCentralButtonBackground.GREEN,
                    text = "SHORT BREAK"
                )
            }
            !hasLocation && shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK &&
                shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK_FORCED -> {
                current.shortBreak.copy(
                    enabled = false,
                    loading = false,
                    visible = true,
                    background = ContactCentralButtonBackground.GREEN,
                    text = "SHORT BREAK"
                )
            }
            else -> current.shortBreak
        }
        val cancelVisible = !(hasShortBreak && shortBreakStatus != null &&
            shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK &&
            shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK_FORCED)
        val messagesEnabled = hasPredefinedMessages
        val voiceEnabled = !isITopTaximeter
        _uiState.update {
            it.copy(
                buttons = current.copy(
                    cancel = current.cancel.copy(
                        visible = cancelVisible,
                        enabled = true,
                        background = ContactCentralButtonBackground.DEFAULT,
                        text = "CANCEL"
                    ),
                    shortBreak = shortBreakButton,
                    voiceCall = current.voiceCall.copy(
                        enabled = voiceEnabled,
                        background = current.voiceCall.background,
                    ),
                    messages = current.messages.copy(
                        enabled = messagesEnabled,
                        background = ContactCentralButtonBackground.DEFAULT,
                    ),
                    information = current.information.copy(
                        enabled = true,
                        background = ContactCentralButtonBackground.DEFAULT,
                    )
                )
            )
        }
    }
    fun emitEffect(effect: ContactCentralUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
