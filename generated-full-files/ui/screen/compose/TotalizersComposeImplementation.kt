package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import com.interfacom.sdk.taximeter.taximeter.events.TotalizersRetrievedEvent
import androidx.compose.foundation.layout.navigationBarsPadding
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.layout.safeDrawing
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Immutable
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.material3.AlertDialog
import ifac.td.taxi.viewmodel.BaseViewModel
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.setValue
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.lifecycle.Lifecycle
import android.app.Application
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


/* -----------------------------------------------------------
 * UI STATE / EVENT / EFFECT
 * ----------------------------------------------------------- */

@Immutable
data class TotalizersUiState(
    val ticketContent: String? = null
)

sealed interface TotalizersUiEvent {
    data class LoadTotalizers(val taximeterTotalizersFlow: TotalizersRetrievedEvent?) : TotalizersUiEvent
    data object CancelClicked : TotalizersUiEvent
    data object PrintClicked : TotalizersUiEvent
    data object DialogConfirmed : TotalizersUiEvent
    data object DialogDismissed : TotalizersUiEvent
}

sealed interface TotalizersUiEffect {
    data object NavigateBack : TotalizersUiEffect
    data class ShowToast(val messageRes: Int) : TotalizersUiEffect
    data class PrintTicket(val ticketContent: String) : TotalizersUiEffect
}

sealed interface TotalizersDialogState {
    data object Hidden : TotalizersDialogState
    data class Visible(
        val title: String,
        val message: String
    ) : TotalizersDialogState
}

/* -----------------------------------------------------------
 * BUTTON STATE
 * ----------------------------------------------------------- */

@Immutable
data class TotalizersButtonVisualState(
    val text: String,
    val iconRes: Int,
    val background: TotalizersButtonBackground,
    val enabled: Boolean,
    val visible: Boolean
)

enum class TotalizersButtonBackground {
    GREEN,
    RED,
    BLUE,
    YELLOW,
    ORANGE,
    DEFAULT,
    BLACK,
    TRANSPARENT
}

@Immutable
data class TotalizersButtonsState(
    val cancel: TotalizersButtonVisualState = TotalizersButtonVisualState(
        text = "VOLVER",
        iconRes = R.drawable.back,
        background = TotalizersButtonBackground.BLUE,
        enabled = true,
        visible = true
    ),
    val print: TotalizersButtonVisualState = TotalizersButtonVisualState(
        text = "IMPRIMIR",
        iconRes = R.drawable.imprimir,
        background = TotalizersButtonBackground.BLUE,
        enabled = true,
        visible = true
    )
)

enum class TotalizersButtonsBranch {
    NoData,
    DataLoaded
}

object TotalizersButtonsStateReducer {
    fun reduce(
        current: TotalizersButtonsState,
        branch: TotalizersButtonsBranch
    ): TotalizersButtonsState {
        return when (branch) {
            TotalizersButtonsBranch.NoData -> current.copy(
                cancel = current.cancel.copy(
                    text = "VOLVER",
                    iconRes = R.drawable.back,
                    background = TotalizersButtonBackground.BLUE,
                    enabled = true,
                    visible = true
                ),
                print = current.print.copy(
                    text = "IMPRIMIR",
                    iconRes = R.drawable.imprimir,
                    background = TotalizersButtonBackground.BLUE,
                    enabled = true,
                    visible = true
                )
            )

            TotalizersButtonsBranch.DataLoaded -> current.copy(
                cancel = current.cancel.copy(
                    text = "VOLVER",
                    iconRes = R.drawable.back,
                    background = TotalizersButtonBackground.BLUE,
                    enabled = true,
                    visible = true
                ),
                print = current.print.copy(
                    text = "IMPRIMIR",
                    iconRes = R.drawable.imprimir,
                    background = TotalizersButtonBackground.BLUE,
                    enabled = true,
                    visible = true
                )
            )
        }
    }
}

/* -----------------------------------------------------------
 * COMPOSE VIEWMODEL
 * ----------------------------------------------------------- */

class TotalizersViewModel(
    context: Application,
    val ticketUseCase: TicketUseCase
) : BaseViewModel(context) {

    val _uiState = MutableStateFlow(TotalizersUiState())
    val uiState: StateFlow<TotalizersUiState> = _uiState.asStateFlow()

    val _buttonsState = MutableStateFlow(TotalizersButtonsState())
    val buttonsState: StateFlow<TotalizersButtonsState> = _buttonsState.asStateFlow()

    val _dialogState = MutableStateFlow<TotalizersDialogState>(TotalizersDialogState.Hidden)
    val dialogState: StateFlow<TotalizersDialogState> = _dialogState.asStateFlow()

    val _uiEffect = MutableSharedFlow<TotalizersUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<TotalizersUiEffect> = _uiEffect

    private var rawTotalizersContent: String? = null

    fun onEvent(event: TotalizersUiEvent) {
        when (event) {
            is TotalizersUiEvent.LoadTotalizers -> loadTotalizersTicket(event.taximeterTotalizersFlow)

            TotalizersUiEvent.CancelClicked -> {
                _uiEffect.tryEmit(TotalizersUiEffect.NavigateBack)
            }

            TotalizersUiEvent.PrintClicked -> {
                val content = rawTotalizersContent
                if (!content.isNullOrEmpty()) {
                    _uiEffect.tryEmit(TotalizersUiEffect.PrintTicket(content.addTicketLines()))
                } else {
                    _uiEffect.tryEmit(TotalizersUiEffect.ShowToast(R.string.no_totalizers_to_print))
                }
            }

            TotalizersUiEvent.DialogConfirmed -> {
                _dialogState.value = TotalizersDialogState.Hidden
            }

            TotalizersUiEvent.DialogDismissed -> {
                _dialogState.value = TotalizersDialogState.Hidden
            }
        }
    }

    fun loadTotalizersTicket(taximeterTotalizersFlow: TotalizersRetrievedEvent?) {
        viewModelScope.launch {
            if (taximeterTotalizersFlow == null) {
                rawTotalizersContent = null
                _uiState.value = TotalizersUiState(
                    ticketContent = context.getString(R.string.no_active_totalizers_with_data)
                )
                _buttonsState.value = TotalizersButtonsStateReducer.reduce(
                    current = _buttonsState.value,
                    branch = TotalizersButtonsBranch.NoData
                )
            } else {
                val ticket = ticketUseCase.prepareTotalizersTicket(taximeterTotalizersFlow)
                rawTotalizersContent = ticket
                _uiState.value = TotalizersUiState(
                    ticketContent = ticket.takeIf { !it.isNullOrEmpty() }
                        ?: context.getString(R.string.no_active_totalizers_with_data)
                )
                _buttonsState.value = TotalizersButtonsStateReducer.reduce(
                    current = _buttonsState.value,
                    branch = TotalizersButtonsBranch.DataLoaded
                )
            }
        }
    }
}

/* -----------------------------------------------------------
 * MAIN ACTIVITY BRIDGE
 * ----------------------------------------------------------- */

class TotalizersMainActivityViewModel(
    val delegate: MainActivityViewModel
) {
    val taximeterTotalizersFlow: StateFlow<TotalizersRetrievedEvent?> = delegate.taximeterTotalizersFlow

    fun navigateBack() = delegate.navigateBack()
    fun showToast(messageRes: Int) = delegate.showToast(messageRes)
    fun printTicket(content: String) = delegate.printTicket(content)
}

/* -----------------------------------------------------------
 * COMPOSE SCREEN
 * ----------------------------------------------------------- */

@Composable
fun TotalizersScreen(
    viewModel: TotalizersViewModel,
    sharedViewModel: TotalizersMainActivityViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collectLatest { effect ->
                    when (effect) {
                        TotalizersUiEffect.NavigateBack -> onNavigateBack()
                        is TotalizersUiEffect.ShowToast -> onShowToast(effect.messageRes)
                        is TotalizersUiEffect.PrintTicket -> sharedViewModel.printTicket(effect.ticketContent)
                    }
                }
            }
        }
    }

    LaunchedEffect(sharedViewModel.taximeterTotalizersFlow.value) {
        viewModel.onEvent(
            TotalizersUiEvent.LoadTotalizers(sharedViewModel.taximeterTotalizersFlow.value)
        )
    }

    TotalizersContent(
        uiState = uiState,
        buttonsState = buttonsState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun TotalizersContent(
    uiState: TotalizersUiState,
    buttonsState: TotalizersButtonsState,
    dialogState: TotalizersDialogState,
    onEvent: (TotalizersUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 15.dp, bottom = 15.dp)
            ) {
                Text(
                    text = uiState.ticketContent ?: stringResource(R.string.no_active_totalizers_with_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TotalizersCustomButton(
                    state = buttonsState.cancel,
                    onClick = { onEvent(TotalizersUiEvent.CancelClicked) },
                    modifier = Modifier.weight(1f)
                )
                TotalizersCustomButton(
                    state = buttonsState.print,
                    onClick = { onEvent(TotalizersUiEvent.PrintClicked) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (dialogState is TotalizersDialogState.Visible) {
            AlertDialog(
                onDismissRequest = { onEvent(TotalizersUiEvent.DialogDismissed) },
                title = { Text(text = dialogState.title) },
                text = { Text(text = dialogState.message) },
                confirmButton = {
                    TextButton(onClick = { onEvent(TotalizersUiEvent.DialogConfirmed) }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
fun TotalizersCustomButton(
    state: TotalizersButtonVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return

    val bgColor = when (state.background) {
        TotalizersButtonBackground.GREEN -> Color(0xFF2E7D32)
        TotalizersButtonBackground.RED -> Color(0xFFC62828)
        TotalizersButtonBackground.BLUE -> Color(0xFF1565C0)
        TotalizersButtonBackground.YELLOW -> Color(0xFFF9A825)
        TotalizersButtonBackground.ORANGE -> Color(0xFFEF6C00)
        TotalizersButtonBackground.DEFAULT -> Color(0xFF1565C0)
        TotalizersButtonBackground.BLACK -> Color.Black
        TotalizersButtonBackground.TRANSPARENT -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .clickable(enabled = state.enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = state.iconRes),
            contentDescription = state.text,
            tint = Color.White
        )
        Text(
            text = state.text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 28.dp)
        )
    }
}

If you want, I can do a second pass and make this **strictly production-ready** by:
- removing the duplicate `TotalizersViewModel` name conflict with your existing XML ViewModel,
- wiring it to Koin,
- and providing a `Fragment -> ComposeView` migration wrapper that preserves your current navigation graph exactly.
