package ifac.td.taxi.ui.screen.compose
import ifac.td.taxi.domain.model.PartialModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import ifac.td.taxi.compose.viewmodel.ComposeSharedViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState as collectAsStateCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavHostController
import ifac.td.taxi.compose.viewmodel.BaseComposeViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.material3.AlertDialog
import ifac.td.taxi.viewmodel.BaseViewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import ifac.td.taxi.viewmodel.DestinationMapViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.domain.usecase.PrinterUseCase
import androidx.compose.foundation.layout.WindowInsetsSides
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import androidx.compose.material3.MaterialTheme
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.collectAsState
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import androidx.compose.runtime.setValue
import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import ifac.td.taxi.domain.usecase.PartialUseCase
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.navigation.NavDirections
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.WindowInsets
import android.app.Application
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


data class OpenPartialUiState(
    val isLoading: Boolean = true,
    val rawPartialContent: String? = null,
    val ticketContent: String = "",
    val buttons: OpenPartialButtonsState = OpenPartialButtonsState(),
    val dialog: OpenPartialDialogState = OpenPartialDialogState.Hidden,
)

data class OpenPartialButtonsState(
    val back: OpenPartialButtonVisualState = OpenPartialButtonVisualState(
        visible = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        enabled = true
    ),
    val print: OpenPartialButtonVisualState = OpenPartialButtonVisualState(
        visible = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        enabled = true
    ),
    val close: OpenPartialButtonVisualState = OpenPartialButtonVisualState(
        visible = false,
        style = CustomButton.StyleButton.DISABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        enabled = false
    ),
    val totalizers: OpenPartialButtonVisualState = OpenPartialButtonVisualState(
        visible = false,
        style = CustomButton.StyleButton.DISABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        enabled = false
    )
)

data class OpenPartialButtonVisualState(
    val visible: Boolean,
    val style: CustomButton.StyleButton,
    val background: CustomButton.BackgroundButtonColor,
    val enabled: Boolean,
)

sealed interface OpenPartialDialogState {
    data object Hidden : OpenPartialDialogState
    data class ConfirmClose(
        val title: String,
        val description: String,
    ) : OpenPartialDialogState
}

sealed interface OpenPartialUiEvent {
    data object OnScreenStarted : OpenPartialUiEvent
    data object OnBackClicked : OpenPartialUiEvent
    data object OnPrintClicked : OpenPartialUiEvent
    data object OnCloseClicked : OpenPartialUiEvent
    data object OnTotalizersClicked : OpenPartialUiEvent
    data object OnDialogAccept : OpenPartialUiEvent
    data object OnDialogCancel : OpenPartialUiEvent
}

sealed interface OpenPartialUiEffect {
    data object NavigateBack : OpenPartialUiEffect
    data class NavigateTo(val destinationId: Int) : OpenPartialUiEffect
    data class ShowToast(@StringRes val resId: Int) : OpenPartialUiEffect
    data class ShowDialog(val dialog: OpenPartialDialogState.ConfirmClose) : OpenPartialUiEffect
    data object DismissDialog : OpenPartialUiEffect
}

data class OpenPartialButtonsInput(
    val canClosePermission: Boolean?,
    val canCloseArg: Boolean,
    val isTaximeterConnected: Boolean,
    val hasTotalizers: Boolean,
    val currentShiftStatus: Int?,
)

fun OpenPartialButtonsState.reduce(input: OpenPartialButtonsInput): OpenPartialButtonsState {
    val canClose = input.canClosePermission == true && input.canCloseArg

    val closeState = if (canClose) {
        OpenPartialButtonVisualState(
            visible = true,
            style = CustomButton.StyleButton.ENABLE,
            background = CustomButton.BackgroundButtonColor.BLUE,
            enabled = true
        )
    } else {
        OpenPartialButtonVisualState(
            visible = false,
            style = CustomButton.StyleButton.DISABLE,
            background = CustomButton.BackgroundButtonColor.BLUE,
            enabled = false
        )
    }

    val totalizersVisible =
        !canClose &&
            input.currentShiftStatus != com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISCONNECTED &&
            input.isTaximeterConnected

    val totalizersStyle =
        if (input.isTaximeterConnected && input.hasTotalizers) {
            CustomButton.StyleButton.ENABLE
        } else {
            CustomButton.StyleButton.DISABLE
        }

    val totalizersState = if (totalizersVisible) {
        OpenPartialButtonVisualState(
            visible = true,
            style = totalizersStyle,
            background = CustomButton.BackgroundButtonColor.BLUE,
            enabled = totalizersStyle == CustomButton.StyleButton.ENABLE
        )
    } else {
        OpenPartialButtonVisualState(
            visible = false,
            style = totalizersStyle,
            background = CustomButton.BackgroundButtonColor.BLUE,
            enabled = false
        )
    }

    return copy(
        close = closeState,
        totalizers = totalizersState
    )
}

class OpenPartialViewModel(
    context: Application,
    val partialUseCase: PartialUseCase,
    val printerUseCase: PrinterUseCase,
    val licensingUseCase: LicensingUseCase,
    val taximeterUseCase: TaximeterConnectUseCase,
    val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {

    val TAG = "OpenPartialViewModel"

    val _uiState = MutableStateFlow(OpenPartialUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffects = MutableSharedFlow<OpenPartialUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    val _partialPermission = MutableStateFlow<Boolean?>(null)
    val _isTaximeterConnected = MutableStateFlow(false)

    val loadedTicketFiles: () -> Unit = {
        viewModelScope.launch {
            partialUseCase.doPartials { partial ->
                Logs.d(TAG, "getPartial: doPartials callback received result = $partial")
                handlePartial(partial)
            }
        }
    }

    fun onEvent(event: OpenPartialUiEvent) {
        when (event) {
            OpenPartialUiEvent.OnScreenStarted -> {
                checkClosuresPermission()
                getPartial()
            }

            OpenPartialUiEvent.OnBackClicked -> {
                viewModelScope.launch { _uiEffects.emit(OpenPartialUiEffect.NavigateBack) }
            }

            OpenPartialUiEvent.OnPrintClicked -> {
                val raw = _uiState.value.rawPartialContent
                if (!raw.isNullOrEmpty()) {
                    printPartial(raw)
                } else {
                    viewModelScope.launch { _uiEffects.emit(OpenPartialUiEffect.ShowToast(R.string.no_partials_to_print)) }
                }
            }

            OpenPartialUiEvent.OnCloseClicked -> {
                viewModelScope.launch {
                    _uiEffects.emit(
                        OpenPartialUiEffect.ShowDialog(
                            OpenPartialDialogState.ConfirmClose(
                                title = getApplication<Application>().getString(R.string.alert),
                                description = getApplication<Application>().getString(R.string.alert_close_partial_dialog),
                            )
                        )
                    )
                }
            }

            OpenPartialUiEvent.OnTotalizersClicked -> {
                viewModelScope.launch {
                    _uiEffects.emit(
                        OpenPartialUiEffect.NavigateTo(R.id.action_openPartialFragment_to_totalizersFragment)
                    )
                }
            }

            OpenPartialUiEvent.OnDialogAccept -> {
                closePartials()
                viewModelScope.launch { _uiEffects.emit(OpenPartialUiEffect.DismissDialog) }
            }

            OpenPartialUiEvent.OnDialogCancel -> {
                viewModelScope.launch { _uiEffects.emit(OpenPartialUiEffect.DismissDialog) }
            }
        }
    }

    private fun handlePartial(partial: PartialModel?) {
        if (partial != null) {
            val raw = partial.bufLastTicketParciales
            val content = raw.takeIf { !it.isNullOrEmpty() }
                ?: getApplication<Application>().getString(R.string.no_active_partials_with_data)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    rawPartialContent = raw,
                    ticketContent = content
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    ticketContent = getApplication<Application>().getString(R.string.no_active_partials)
                )
            }
        }
    }

    fun getPartial() {
        Logs.d(TAG, "getPartial: Requesting partials data")
        viewModelScope.launch {
            ticketUseCase.loadCampos()
            ticketUseCase.loadTickets(loadedTicketFiles)
        }
    }

    fun closePartials() {
        viewModelScope.launch {
            Logs.d(TAG, "closePartials: executing closePartial useCase")
            partialUseCase.closePartial()
            _uiEffects.emit(
                OpenPartialUiEffect.NavigateTo(R.id.action_openPartialsFragment_to_closedPartialFragment)
            )
        }
    }

    fun printPartial(ticket: String) {
        viewModelScope.launch {
            Logs.d(TAG, "printPartial: Attempting to print ticket (length: ${ticket.length})")
            printerUseCase.printTicket(buf = ticket, paperFeed = true)
        }
    }

    fun checkClosuresPermission() {
        viewModelScope.launch {
            Logs.d(TAG, "checkClosuresPermission: Checking parameters")
            val parameters = licensingUseCase.getLicensingParameters()
            _partialPermission.value = parameters?.isClosuresButton
            _isTaximeterConnected.value = taximeterUseCase.isTaximeterConnected()
            reduceButtons(canCloseArg = true, hasTotalizers = false, currentShiftStatus = null)
        }
    }

    fun updateButtonsFromBridge(
        canCloseArg: Boolean,
        hasTotalizers: Boolean,
        currentShiftStatus: Int?
    ) {
        reduceButtons(
            canCloseArg = canCloseArg,
            hasTotalizers = hasTotalizers,
            currentShiftStatus = currentShiftStatus
        )
    }

    private fun reduceButtons(
        canCloseArg: Boolean,
        hasTotalizers: Boolean,
        currentShiftStatus: Int?
    ) {
        val updated = _uiState.value.buttons.reduce(
            OpenPartialButtonsInput(
                canClosePermission = _partialPermission.value,
                canCloseArg = canCloseArg,
                isTaximeterConnected = _isTaximeterConnected.value,
                hasTotalizers = hasTotalizers,
                currentShiftStatus = currentShiftStatus
            )
        )
        _uiState.update { it.copy(buttons = updated) }
    }
}

class OpenPartialMainActivityViewModel : ViewModel() {

    val TAG = "OpenPartialMainActivityViewModel"

    val _taximeterTotalizersFlow = MutableStateFlow<Any?>(null)
    val taximeterTotalizersFlow = _taximeterTotalizersFlow.asStateFlow()

    val _shiftStatusFlow = MutableStateFlow<ShiftStatusModel?>(null)
    val shiftStatusFlow = _shiftStatusFlow.asStateFlow()

    fun setTaximeterTotalizers(value: Any?) {
        _taximeterTotalizersFlow.value = value
        Logs.d(TAG, "setTaximeterTotalizers: value=${value != null}")
    }

    fun setShiftStatus(value: ShiftStatusModel?) {
        _shiftStatusFlow.value = value
        Logs.d(TAG, "setShiftStatus: currentStatus=${value?.currentStatus}")
    }

    fun printTicket(ticket: String) {
        Logs.d(TAG, "printTicket: length=${ticket.length}")
    }

    fun navigateBack() {
        Logs.d(TAG, "navigateBack")
    }

    fun navigateTo(destinationId: Int) {
        Logs.d(TAG, "navigateTo: destinationId=$destinationId")
    }

    fun showToast(messageRes: Int) {
        Logs.d(TAG, "showToast: resId=$messageRes")
    }

    data class ShiftStatusModel(
        val currentStatus: Int? = null
    )
}

@Composable
fun OpenPartialScreen(
    viewModel: OpenPartialViewModel,
    sharedViewModel: OpenPartialMainActivityViewModel,
    canCloseArg: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateTo: (Int) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var dialogState by remember { mutableStateOf<OpenPartialDialogState>(OpenPartialDialogState.Hidden) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(OpenPartialUiEvent.OnScreenStarted)
    }

    LaunchedEffect(sharedViewModel.taximeterTotalizersFlow.value, sharedViewModel.shiftStatusFlow.value, canCloseArg) {
        viewModel.updateButtonsFromBridge(
            canCloseArg = canCloseArg,
            hasTotalizers = sharedViewModel.taximeterTotalizersFlow.value != null,
            currentShiftStatus = sharedViewModel.shiftStatusFlow.value?.currentStatus
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                OpenPartialUiEffect.NavigateBack -> onNavigateBack()
                is OpenPartialUiEffect.NavigateTo -> onNavigateTo(effect.destinationId)
                is OpenPartialUiEffect.ShowToast -> onShowToast(effect.resId)
                is OpenPartialUiEffect.ShowDialog -> dialogState = effect.dialog
                OpenPartialUiEffect.DismissDialog -> dialogState = OpenPartialDialogState.Hidden
            }
        }
    }

    BackHandler {
        viewModel.onEvent(OpenPartialUiEvent.OnBackClicked)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(96.dp))
                }
            } else {
                TextTicketViewer(text = uiState.ticketContent)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color.Black)
                .padding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OpenPartialButton(
                type = ButtonType.VOLVER,
                visualState = uiState.buttons.back,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onEvent(OpenPartialUiEvent.OnBackClicked) }
            )

            OpenPartialButton(
                type = ButtonType.IMPRIMIR,
                visualState = uiState.buttons.print,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onEvent(OpenPartialUiEvent.OnPrintClicked) }
            )

            when {
                uiState.buttons.close.visible -> {
                    OpenPartialButton(
                        type = ButtonType.CERRAR,
                        visualState = uiState.buttons.close,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onEvent(OpenPartialUiEvent.OnCloseClicked) }
                    )
                }

                uiState.buttons.totalizers.visible -> {
                    OpenPartialButton(
                        type = ButtonType.TOTALIZERS,
                        visualState = uiState.buttons.totalizers,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onEvent(OpenPartialUiEvent.OnTotalizersClicked) }
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    when (val dialog = dialogState) {
        OpenPartialDialogState.Hidden -> Unit
        is OpenPartialDialogState.ConfirmClose -> {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(OpenPartialUiEvent.OnDialogCancel) },
                title = { Text(dialog.title) },
                text = { Text(dialog.description) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(OpenPartialUiEvent.OnDialogAccept) }) {
                        Text("ACCEPT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(OpenPartialUiEvent.OnDialogCancel) }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

@Composable
private fun OpenPartialButton(
    type: ButtonType,
    visualState: OpenPartialButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (!visualState.visible) return

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        enabled = visualState.enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (visualState.style) {
                CustomButton.StyleButton.ENABLE -> when (visualState.background) {
                    CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2E7D32)
                    CustomButton.BackgroundButtonColor.RED -> Color(0xFFC62828)
                    CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
                    CustomButton.BackgroundButtonColor.YELLOW -> Color(0xFFF9A825)
                    CustomButton.BackgroundButtonColor.ORANGE -> Color(0xFFEF6C00)
                    CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
                    CustomButton.BackgroundButtonColor.BLACK -> Color.Black
                    CustomButton.BackgroundButtonColor.TRANSPARENT -> Color.Transparent
                }

                CustomButton.StyleButton.DISABLE -> Color(0xFF9E9E9E)
                CustomButton.StyleButton.LOADING -> Color(0xFF9E9E9E)
            }
        )
    ) {
        Text(text = type.name)
    }
}

@Composable
private fun TextTicketViewer(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}
