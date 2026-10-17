package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.update
import androidx.lifecycle.Lifecycle
import androidx.compose.material3.OutlinedTextField
import android.app.Application
import androidx.compose.material3.AlertDialog
import ifac.td.taxi.viewmodel.BaseViewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


@Immutable
data class SecurePinUiState(
    val hasSecurePin: Boolean? = null,
    val pin: String = "",
    val pinRepeat: String = "",
    val buttons: SecurePinButtonsState = SecurePinButtonsState(),
    val dialog: SecurePinDialogState = SecurePinDialogState.Hidden,
)

@Immutable
data class SecurePinButtonsState(
    val cancel: SecurePinButtonVisualState = SecurePinButtonVisualState(
        buttonType = ButtonType.CANCEL,
        enabled = true,
        visible = true,
        backgroundColor = SecurePinButtonBackground.RED,
        textColor = Color.White,
        iconVisible = true,
        textVisible = true,
    ),
    val accept: SecurePinButtonVisualState = SecurePinButtonVisualState(
        buttonType = ButtonType.ACCEPT,
        enabled = true,
        visible = true,
        backgroundColor = SecurePinButtonBackground.GREEN,
        textColor = Color.White,
        iconVisible = true,
        textVisible = true,
    ),
)

@Immutable
data class SecurePinButtonVisualState(
    val buttonType: ButtonType,
    val enabled: Boolean,
    val visible: Boolean,
    val backgroundColor: SecurePinButtonBackground,
    val textColor: Color,
    val iconVisible: Boolean,
    val textVisible: Boolean,
)

enum class SecurePinButtonBackground {
    GREEN, RED, BLUE, YELLOW, ORANGE, DEFAULT, BLACK, TRANSPARENT
}

sealed interface SecurePinDialogState {
    data object Hidden : SecurePinDialogState
    data class Visible(
        val titleRes: Int,
        val descriptionRes: Int? = null,
        val buttons: List<ButtonType> = listOf(ButtonType.ACCEPT),
    ) : SecurePinDialogState
}

sealed interface SecurePinUiEvent {
    data class PinChanged(val value: String) : SecurePinUiEvent
    data class PinRepeatChanged(val value: String) : SecurePinUiEvent
    data object AcceptClicked : SecurePinUiEvent
    data object CancelClicked : SecurePinUiEvent
    data object DialogAccepted : SecurePinUiEvent
    data object CheckSecurePin : SecurePinUiEvent
}

sealed interface SecurePinUiEffect {
    data object NavigateBack : SecurePinUiEffect
    data class ShowDialog(val dialog: SecurePinDialogState.Visible) : SecurePinUiEffect
}

class SecurePinViewModel(
    context: Application,
    val userPreferencesUseCase: UserPreferencesUseCase,
) : BaseViewModel(context) {

    val _uiState = MutableStateFlow(SecurePinUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<SecurePinUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<SecurePinUiEffect> = _uiEffect.asSharedFlow()

    fun onEvent(event: SecurePinUiEvent) {
        when (event) {
            SecurePinUiEvent.CheckSecurePin -> checkSecurePin()
            is SecurePinUiEvent.PinChanged -> _uiState.update { it.copy(pin = event.value) }
            is SecurePinUiEvent.PinRepeatChanged -> _uiState.update { it.copy(pinRepeat = event.value) }
            SecurePinUiEvent.AcceptClicked -> clickOnAccept()
            SecurePinUiEvent.CancelClicked -> _uiEffect.tryEmit(SecurePinUiEffect.NavigateBack)
            SecurePinUiEvent.DialogAccepted -> _uiEffect.tryEmit(SecurePinUiEffect.NavigateBack)
        }
    }

    fun checkSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            val hasSecurePin = !currentSecurePin.isNullOrEmpty()
            _uiState.update {
                it.copy(
                    hasSecurePin = hasSecurePin,
                    buttons = it.buttons.reduce(hasSecurePin)
                )
            }
        }
    }

    private fun clickOnAccept() {
        val pin = uiState.value.pin
        val pinRepeat = uiState.value.pinRepeat

        if (pin.isEmpty() && pinRepeat.isEmpty()) {
            savePin("")
            return
        }

        if (isValidPassword(pin) && isValidRepeatPassword(pinRepeat, pin)) {
            savePin(pin)
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesUseCase.insertSecurePin(pin)
            _uiEffect.tryEmit(
                SecurePinUiEffect.ShowDialog(
                    SecurePinDialogState.Visible(
                        titleRes = R.string.dialog_success,
                        descriptionRes = null,
                        buttons = listOf(ButtonType.ACCEPT)
                    )
                )
            )
        }
    }

    private fun isValidPassword(value: String): Boolean {
        return value.isNotBlank() && value.length >= 4
    }

    private fun isValidRepeatPassword(repeat: String, original: String): Boolean {
        return repeat == original && isValidPassword(original)
    }
}

/**
 * Exact reducer mirroring the fragment's collect branch:
 * collect { hasSecurePin -> hasSecurePin?.let { setupMode(it) } }
 *
 * The fragment only changes title strings, not button visuals.
 * Therefore this reducer preserves the exact button colors/visibility from XML.
 */
fun SecurePinButtonsState.reduce(hasSecurePin: Boolean): SecurePinButtonsState {
    return when (hasSecurePin) {
        true -> copy(
            cancel = cancel.copy(
                enabled = true,
                visible = true,
                backgroundColor = SecurePinButtonBackground.RED,
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
            ),
            accept = accept.copy(
                enabled = true,
                visible = true,
                backgroundColor = SecurePinButtonBackground.GREEN,
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
            )
        )
        false -> copy(
            cancel = cancel.copy(
                enabled = true,
                visible = true,
                backgroundColor = SecurePinButtonBackground.RED,
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
            ),
            accept = accept.copy(
                enabled = true,
                visible = true,
                backgroundColor = SecurePinButtonBackground.GREEN,
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
            )
        )
    }
}

class SecurePinMainActivityViewModel(
    application: Application
) : androidx.lifecycle.ViewModel() {

    val _navigationEffect = MutableSharedFlow<SecurePinMainActivityEffect>(extraBufferCapacity = 1)
    val navigationEffect: SharedFlow<SecurePinMainActivityEffect> = _navigationEffect.asSharedFlow()

    fun showBottomBar(visible: Boolean) {
        _navigationEffect.tryEmit(SecurePinMainActivityEffect.ShowBottomBar(visible))
    }

    fun showHeader(visible: Boolean) {
        _navigationEffect.tryEmit(SecurePinMainActivityEffect.ShowHeader(visible))
    }

    fun navigateBack() {
        _navigationEffect.tryEmit(SecurePinMainActivityEffect.NavigateBack)
    }
}

sealed interface SecurePinMainActivityEffect {
    data class ShowBottomBar(val visible: Boolean) : SecurePinMainActivityEffect
    data class ShowHeader(val visible: Boolean) : SecurePinMainActivityEffect
    data object NavigateBack : SecurePinMainActivityEffect
}

@Composable
fun SecurePinScreen(
    viewModel: SecurePinViewModel,
    onNavigateBack: () -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onShowHeader: (Boolean) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowBottomBar(true)
        onShowHeader(true)
        viewModel.onEvent(SecurePinUiEvent.CheckSecurePin)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        SecurePinUiEffect.NavigateBack -> onNavigateBack()
                        is SecurePinUiEffect.ShowDialog -> Unit
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(
                    if (uiState.hasSecurePin == true) {
                        R.string.title_update_pin
                    } else {
                        R.string.title_insert_pin
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.pin,
                onValueChange = { viewModel.onEvent(SecurePinUiEvent.PinChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(
                            if (uiState.hasSecurePin == true) {
                                R.string.title_update_pin
                            } else {
                                R.string.title_insert_pin
                            }
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(
                    if (uiState.hasSecurePin == true) {
                        R.string.title_update_repeat_pin
                    } else {
                        R.string.title_insert_repeat_pin
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.pinRepeat,
                onValueChange = { viewModel.onEvent(SecurePinUiEvent.PinRepeatChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(
                            if (uiState.hasSecurePin == true) {
                                R.string.title_update_repeat_pin
                            } else {
                                R.string.title_insert_repeat_pin
                            }
                        )
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SecurePinCustomButton(
                modifier = Modifier.weight(1f),
                visualState = uiState.buttons.cancel,
                text = stringResource(R.string.btn_cancel).uppercase(),
                onClick = { viewModel.onEvent(SecurePinUiEvent.CancelClicked) }
            )
            SecurePinCustomButton(
                modifier = Modifier.weight(1f),
                visualState = uiState.buttons.accept,
                text = stringResource(R.string.btn_accept).uppercase(),
                onClick = { viewModel.onEvent(SecurePinUiEvent.AcceptClicked) }
            )
        }

        when (val dialog = uiState.dialog) {
            SecurePinDialogState.Hidden -> Unit
            is SecurePinDialogState.Visible -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(text = stringResource(dialog.titleRes)) },
                    text = {
                        dialog.descriptionRes?.let { Text(text = stringResource(it)) }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onEvent(SecurePinUiEvent.DialogAccepted) }) {
                            Text(text = stringResource(R.string.btn_accept))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SecurePinCustomButton(
    modifier: Modifier = Modifier,
    visualState: SecurePinButtonVisualState,
    text: String,
    onClick: () -> Unit,
) {
    if (!visualState.visible) return

    val background = when (visualState.backgroundColor) {
        SecurePinButtonBackground.GREEN -> Color(0xFF2E7D32)
        SecurePinButtonBackground.RED -> Color(0xFFC62828)
        SecurePinButtonBackground.BLUE -> Color(0xFF1565C0)
        SecurePinButtonBackground.YELLOW -> Color(0xFFF9A825)
        SecurePinButtonBackground.ORANGE -> Color(0xFFEF6C00)
        SecurePinButtonBackground.DEFAULT -> Color(0xFF1565C0)
        SecurePinButtonBackground.BLACK -> Color.Black
        SecurePinButtonBackground.TRANSPARENT -> Color.Transparent
    }

    Button(
        onClick = onClick,
        enabled = visualState.enabled,
        modifier = modifier.height(72.dp)
    ) {
        Text(text = text, color = visualState.textColor)
    }
}
