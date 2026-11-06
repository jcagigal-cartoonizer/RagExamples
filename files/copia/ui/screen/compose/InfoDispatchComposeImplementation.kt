package ifac.td.taxi.ui.screen.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =======================================================
// SAFE-ARGS / NAV DIRECTIONS ADAPTER
// =======================================================

private fun NavController.navigate(directions: NavDirections) {
    navigate(directions)
}

// =======================================================
// UI STATE / EFFECTS
// =======================================================

data class InfoDispatchUiState(
    val dispatch: InfoDispatchModel? = null,
    val buttons: InfoDispatchButtonsState = InfoDispatchButtonsState(),
    val multiDispatch: InfoDispatchMultiDispatchState = InfoDispatchMultiDispatchState(),
    val dispatchNumberText: String = "",
    val zoneText: String = "",
    val zoneVisible: Boolean = false,
    val pickupTimeText: String = "",
    val pickupTimeVisible: Boolean = false,
    val pickupAddressText: String = "",
    val pickupAddressVisible: Boolean = false,
    val destinations: List<String> = emptyList(),
    val destinationsVisible: Boolean = false,
    val observations: List<String> = emptyList(),
    val observationsVisible: Boolean = false,
    val flightCodeText: String = "",
    val flightCodeVisible: Boolean = false,
    val requirementsText: String = "",
    val requirementsVisible: Boolean = false,
    val amountText: String = "",
    val amountVisible: Boolean = false,
    val tollsText: String = "",
    val tollsVisible: Boolean = false,
    val totalText: String = "",
    val totalVisible: Boolean = false,
    val subscriberTitle: String = "",
    val subscriberUserName: String = "",
    val subscriberVisible: Boolean = false,
    val subscriberAuthorization: String = "",
    val subscriberAuthVisible: Boolean = false,
    val serviceTypeText: String = "",
    val serviceTypeVisible: Boolean = false,
    val concertedAmountText: String = "",
    val concertedAmountVisible: Boolean = false
)

data class InfoDispatchMultiDispatchState(
    val visible: Boolean = false,
    val selectedIndex: Int = 0,
    val tabs: List<InfoDispatchMultiDispatchTabState> = emptyList()
)

data class InfoDispatchMultiDispatchTabState(
    val name: String,
    val iconLabel: String,
    val dispatchNumber: String,
    val routeId: String
)

data class InfoDispatchButtonsState(
    val notifications: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "Notifications",
        visible = true,
        enabled = false,
        backgroundColor = Color(0xFF9E9E9E)
    ),
    val navigate: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "Navigate",
        visible = true,
        enabled = true,
        backgroundColor = Color(0xFF1976D2)
    ),
    val voiceCall: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "Call",
        visible = true,
        enabled = false,
        backgroundColor = Color(0xFF4CAF50)
    ),
    val print: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "Print",
        visible = true,
        enabled = true,
        backgroundColor = Color(0xFF607D8B)
    ),
    val noClient: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "No client",
        visible = true,
        enabled = false,
        backgroundColor = Color(0xFFF44336)
    ),
    val returnDispatch: InfoDispatchButtonState = InfoDispatchButtonState(
        text = "Return",
        visible = true,
        enabled = false,
        backgroundColor = Color(0xFFF44336)
    )
)

data class InfoDispatchButtonState(
    val text: String,
    val visible: Boolean,
    val enabled: Boolean,
    val backgroundColor: Color
)

data class InfoDispatchDialogState(
    val title: String,
    val description: String,
    val buttons: List<InfoDispatchDialogButton>
)

data class InfoDispatchDialogButton(
    val type: ButtonType,
    val label: String
)

sealed interface InfoDispatchUiEvent {
    data object OnNotificationsClicked : InfoDispatchUiEvent
    data object OnNavigateClicked : InfoDispatchUiEvent
    data object OnVoiceCallClicked : InfoDispatchUiEvent
    data object OnPrintClicked : InfoDispatchUiEvent
    data object OnNoClientClicked : InfoDispatchUiEvent
    data object OnReturnClicked : InfoDispatchUiEvent
    data object OnDispatchNumberClicked : InfoDispatchUiEvent
    data class OnMultiDispatchTabSelected(val index: Int) : InfoDispatchUiEvent
    data class OnDialogButtonClicked(val button: InfoDispatchDialogButton) : InfoDispatchUiEvent
}

sealed interface InfoDispatchUiEffect {
    data object NavigateToDirections : InfoDispatchUiEffect
    data object NavigateToHome : InfoDispatchUiEffect
    data class NavigateToMeetingSign(val textColor: Int, val backgroundColor: Int) : InfoDispatchUiEffect
    data object RequestPhonePermission : InfoDispatchUiEffect
    data class ShowToast(val messageRes: Int) : InfoDispatchUiEffect
    data class OpenExternalPhoneCall(val phoneNumber: String) : InfoDispatchUiEffect
    data class OpenDialog(val dialog: InfoDispatchDialogState) : InfoDispatchUiEffect
    data object CloseDialog : InfoDispatchUiEffect
}

// =======================================================
// SHARED MAIN-ACTIVITY VM ADAPTER
// =======================================================

data class InfoDispatchSharedUiState(
    val dispatch: InfoDispatchModel? = null,
    val multiDispatch: List<InfoDispatchModel> = emptyList(),
    val hasTaximeterConnection: Boolean = false,
    val shiftIsManual: Boolean = false,
    val currentShiftStatus: Int? = null,
    val customerCallAvailable: Boolean = false,
    val customerCallButtonAvailable: Boolean = false,
    val canMakeCalls: Boolean = false,
    val bridgeCallState: Int = 0,
    val noClientButtonAllowed: Boolean? = null,
    val noClientButtonFlowValue: Boolean? = null,
    val bravoState: InfoDispatchBravoState = InfoDispatchBravoState()
)

data class InfoDispatchBravoState(
    val showReturnDispatchButton: Boolean = false,
    val clientButton: Boolean = true,
    val noClientButtonEnabled: Boolean = false
)

sealed interface InfoDispatchSharedEffect {
    data object ShowHeader : InfoDispatchSharedEffect
    data object ShowBottomBar : InfoDispatchSharedEffect
    data class ShowToast(val resId: Int) : InfoDispatchSharedEffect
    data class OpenDialog(val dialog: InfoDispatchDialogState) : InfoDispatchSharedEffect
    data object CloseDialog : InfoDispatchSharedEffect
    data object NavigateToDirections : InfoDispatchSharedEffect
    data object NavigateToHome : InfoDispatchSharedEffect
}

class InfoDispatchMainActivityViewModelAdapter(
    private val mainActivityViewModel: MainActivityViewModel
) : ViewModel() {

    private val _sharedUiState = MutableStateFlow(InfoDispatchSharedUiState())
    val sharedUiState: StateFlow<InfoDispatchSharedUiState> = _sharedUiState.asStateFlow()

    private val _sharedEffect = MutableSharedFlow<InfoDispatchSharedEffect>()
    val sharedEffect: SharedFlow<InfoDispatchSharedEffect> = _sharedEffect.asSharedFlow()

    fun onDispatchChanged(dispatch: InfoDispatchModel?) {
        _sharedUiState.update { it.copy(dispatch = dispatch) }
    }

    fun onMultiDispatchChanged(list: List<InfoDispatchModel>) {
        _sharedUiState.update { it.copy(multiDispatch = list) }
    }

    fun onHasTaximeterConnectionChanged(value: Boolean) {
        _sharedUiState.update { it.copy(hasTaximeterConnection = value) }
    }

    fun onShiftManualChanged(value: Boolean) {
        _sharedUiState.update { it.copy(shiftIsManual = value) }
    }

    fun onShiftStatusChanged(value: Int?) {
        _sharedUiState.update { it.copy(currentShiftStatus = value) }
    }

    fun onCustomerCallAvailableChanged(value: Boolean) {
        _sharedUiState.update { it.copy(customerCallAvailable = value) }
    }

    fun onCustomerCallButtonAvailableChanged(value: Boolean) {
        _sharedUiState.update { it.copy(customerCallButtonAvailable = value) }
    }

    fun onCanMakeCallsChanged(value: Boolean) {
        _sharedUiState.update { it.copy(canMakeCalls = value) }
    }

    fun onBridgeCallStateChanged(value: Int) {
        _sharedUiState.update { it.copy(bridgeCallState = value) }
    }

    fun onBravoStateChanged(value: InfoDispatchBravoState) {
        _sharedUiState.update { it.copy(bravoState = value) }
    }

    fun onNoClientButtonAllowedChanged(value: Boolean?) {
        _sharedUiState.update { it.copy(noClientButtonAllowed = value) }
    }

    fun onNoClientButtonFlowChanged(value: Boolean?) {
        _sharedUiState.update { it.copy(noClientButtonFlowValue = value) }
    }

    fun showHeader(show: Boolean) {
        if (show) mainActivityViewModel.showHeader(true)
    }

    fun showBottomBar(show: Boolean) {
        if (show) mainActivityViewModel.showBottomBar(true)
    }

    fun showToast(resId: Int) {
        mainActivityViewModel.showToast(resId)
    }

    fun openDialog(dialog: InfoDispatchDialogState) {
        viewModelScope.launch {
            _sharedEffect.emit(InfoDispatchSharedEffect.OpenDialog(dialog))
        }
    }

    fun closeDialog() {
        viewModelScope.launch {
            _sharedEffect.emit(InfoDispatchSharedEffect.CloseDialog)
        }
    }
}

// =======================================================
// SCREEN VM
// =======================================================

class InfoDispatchComposeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InfoDispatchUiState())
    val uiState: StateFlow<InfoDispatchUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<InfoDispatchUiEffect>()
    val uiEffect: SharedFlow<InfoDispatchUiEffect> = _uiEffect.asSharedFlow()

    fun bindDispatch(
        dispatch: InfoDispatchModel?,
        multiDispatch: List<InfoDispatchModel> = emptyList(),
        selectedIndex: Int = 0
    ) {
        _uiState.update { current ->
            current.copy(
                dispatch = dispatch,
                dispatchNumberText = dispatch?.let { buildDispatchIdentification(it) }.orEmpty(),
                zoneVisible = !dispatch?.pickUpZone.isNullOrBlank(),
                zoneText = dispatch?.pickUpZone.orEmpty(),
                pickupTimeVisible = !dispatch?.pickUpTime.isNullOrBlank(),
                pickupTimeText = dispatch?.pickUpTime.orEmpty(),
                pickupAddressVisible = !dispatch?.pickUpAdress.isNullOrBlank(),
                pickupAddressText = dispatch?.pickUpAdress.orEmpty(),
                destinationsVisible = !dispatch?.destinyAdress.isNullOrEmpty(),
                destinations = dispatch?.destinyAdress.orEmpty(),
                observationsVisible = !dispatch?.observations.isNullOrEmpty(),
                observations = dispatch?.observations.orEmpty().filter { it.isNotBlank() },
                flightCodeVisible = !dispatch?.flightCode.isNullOrBlank(),
                flightCodeText = dispatch?.flightCode.orEmpty(),
                requirementsVisible = !dispatch?.observations.isNullOrEmpty(),
                requirementsText = dispatch?.observations.orEmpty().joinToString("\n"),
                amountVisible = (dispatch?.amount ?: 0) != 0,
                amountText = dispatch?.amount?.toString().orEmpty(),
                subscriberVisible = (dispatch?.clientType ?: 0) > 0,
                subscriberTitle = if (dispatch?.dispatchName.isNullOrBlank()) dispatch?.idDispatch.orEmpty()
                else listOfNotNull(dispatch?.subscriber, dispatch?.dispatchName).joinToString(" "),
                subscriberUserName = dispatch?.subscriberUserName.orEmpty(),
                subscriberAuthVisible = !dispatch?.autorizacion.isNullOrBlank() && (dispatch?.clientType ?: 0) > 0,
                subscriberAuthorization = dispatch?.autorizacion.orEmpty(),
                serviceTypeVisible = dispatch?.isConcertedPrice == true || dispatch?.isPaymentByApp == true,
                serviceTypeText = when {
                    dispatch?.isConcertedPrice == true -> "Concerted trip with fixed price"
                    dispatch?.isPaymentByApp == true -> "Payment by app"
                    else -> ""
                },
                concertedAmountVisible = dispatch?.isConcertedPrice == true,
                concertedAmountText = "",
                multiDispatch = InfoDispatchMultiDispatchState(
                    visible = multiDispatch.size > 1,
                    selectedIndex = selectedIndex,
                    tabs = multiDispatch.map { item ->
                        InfoDispatchMultiDispatchTabState(
                            name = item.dispatchName.orEmpty(),
                            iconLabel = when {
                                item.isCancelled -> "Cancelled"
                                item.isEnded -> "Ended"
                                item.isRiderInCabNotificationSent == true -> "Rider"
                                item.isAtDoorNotificationSent == true -> "Door"
                                else -> "People"
                            },
                            dispatchNumber = item.longDispatchNumber.orEmpty(),
                            routeId = item.routeId.orEmpty()
                        )
                    }
                )
            )
        }
    }

    fun onEvent(event: InfoDispatchUiEvent) {
        when (event) {
            InfoDispatchUiEvent.OnNotificationsClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        InfoDispatchUiEffect.OpenDialog(
                            InfoDispatchDialogState(
                                title = "Select notification",
                                description = "",
                                buttons = listOf(
                                    InfoDispatchDialogButton(ButtonType.AT_DOOR, "At door"),
                                    InfoDispatchDialogButton(ButtonType.RIDER_IN_CAB, "Rider in cab")
                                )
                            )
                        )
                    )
                }
            }

            InfoDispatchUiEvent.OnNavigateClicked -> {
                viewModelScope.launch { _uiEffect.emit(InfoDispatchUiEffect.NavigateToDirections) }
            }

            InfoDispatchUiEvent.OnVoiceCallClicked -> {
                viewModelScope.launch { _uiEffect.emit(InfoDispatchUiEffect.RequestPhonePermission) }
            }

            InfoDispatchUiEvent.OnPrintClicked -> Unit

            InfoDispatchUiEvent.OnNoClientClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        InfoDispatchUiEffect.OpenDialog(
                            InfoDispatchDialogState(
                                title = "Confirm no client",
                                description = "",
                                buttons = listOf(
                                    InfoDispatchDialogButton(ButtonType.CANCEL, "Cancel"),
                                    InfoDispatchDialogButton(ButtonType.ACCEPT, "Accept")
                                )
                            )
                        )
                    )
                }
            }

            InfoDispatchUiEvent.OnReturnClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        InfoDispatchUiEffect.OpenDialog(
                            InfoDispatchDialogState(
                                title = "Return dispatch",
                                description = "",
                                buttons = listOf(
                                    InfoDispatchDialogButton(ButtonType.CANCEL, "Cancel"),
                                    InfoDispatchDialogButton(ButtonType.ACCEPT, "Accept")
                                )
                            )
                        )
                    )
                }
            }

            InfoDispatchUiEvent.OnDispatchNumberClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(InfoDispatchUiEffect.NavigateToMeetingSign(0, 0))
                }
            }

            is InfoDispatchUiEvent.OnMultiDispatchTabSelected -> {
                _uiState.update { it.copy(multiDispatch = it.multiDispatch.copy(selectedIndex = event.index)) }
            }

            is InfoDispatchUiEvent.OnDialogButtonClicked -> {
                when (event.button.type) {
                    ButtonType.CANCEL -> viewModelScope.launch { _uiEffect.emit(InfoDispatchUiEffect.CloseDialog) }
                    ButtonType.ACCEPT -> Unit
                    ButtonType.AT_DOOR -> Unit
                    ButtonType.RIDER_IN_CAB -> Unit
                }
            }
        }
    }

    private fun buildDispatchIdentification(dispatch: InfoDispatchModel): String {
        val name = dispatch.dispatchName.orEmpty()
        return if (name.isNotBlank()) {
            "$name - ${dispatch.longDispatchNumber.orEmpty()}"
        } else {
            dispatch.idDispatch.orEmpty()
        }
    }
}

// =======================================================
// COMPOSE SCREEN
// =======================================================

@Composable
fun InfoDispatchScreen(
    navController: NavController,
    viewModel: InfoDispatchComposeViewModel,
    sharedViewModel: InfoDispatchMainActivityViewModelAdapter
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedUiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<InfoDispatchDialogState?>(null) }

    LaunchedEffect(Unit) {
        sharedViewModel.showHeader(true)
        sharedViewModel.showBottomBar(true)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                InfoDispatchUiEffect.NavigateToDirections -> {
                    navController.navigate(InfoDispatchFragmentDirections.actionInfoDispatchFragmentToDirectionsFragment())
                }

                InfoDispatchUiEffect.NavigateToHome -> {
                    navController.navigate(HomeDirections.goToOnTripFragment())
                }

                is InfoDispatchUiEffect.NavigateToMeetingSign -> {
                    navController.navigate(
                        InfoDispatchFragmentDirections.actionInfoDispatchFragmentToMeetingSignFragment(
                            effect.textColor,
                            effect.backgroundColor
                        )
                    )
                }

                InfoDispatchUiEffect.RequestPhonePermission -> {
                    // launch permission flow from host if desired
                }

                is InfoDispatchUiEffect.ShowToast -> {
                    // host-side toast/snackbar handling
                }

                is InfoDispatchUiEffect.OpenExternalPhoneCall -> {
                    // use host / activity to start phone intent
                }

                is InfoDispatchUiEffect.OpenDialog -> dialogState = effect.dialog
                InfoDispatchUiEffect.CloseDialog -> dialogState = null
            }
        }
    }

    LaunchedEffect(sharedViewModel) {
        sharedViewModel.sharedEffect.collectLatest { effect ->
            when (effect) {
                InfoDispatchSharedEffect.ShowHeader -> Unit
                InfoDispatchSharedEffect.ShowBottomBar -> Unit
                is InfoDispatchSharedEffect.ShowToast -> Unit
                is InfoDispatchSharedEffect.OpenDialog -> dialogState = effect.dialog
                InfoDispatchSharedEffect.CloseDialog -> dialogState = null
                InfoDispatchSharedEffect.NavigateToDirections -> {
                    navController.navigate(InfoDispatchFragmentDirections.actionInfoDispatchFragmentToDirectionsFragment())
                }
                InfoDispatchSharedEffect.NavigateToHome -> {
                    navController.navigate(HomeDirections.goToOnTripFragment())
                }
            }
        }
    }

    Scaffold(
        topBar = { },
        bottomBar = { }
    ) { padding ->
        InfoDispatchContent(
            modifier = Modifier.padding(padding),
            state = state,
            sharedState = sharedState,
            onEvent = viewModel::onEvent
        )
    }

    dialogState?.let { dialog ->
        InfoDispatchCustomDialog(
            state = dialog,
            onDismiss = { dialogState = null },
            onButtonClick = { button ->
                viewModel.onEvent(InfoDispatchUiEvent.OnDialogButtonClicked(button))
                dialogState = null
            }
        )
    }
}

// =======================================================
// CONTENT
// =======================================================

@Composable
fun InfoDispatchContent(
    modifier: Modifier = Modifier,
    state: InfoDispatchUiState,
    sharedState: InfoDispatchSharedUiState,
    onEvent: (InfoDispatchUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = state.dispatchNumberText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable { onEvent(InfoDispatchUiEvent.OnDispatchNumberClicked) }
        )

        if (state.zoneVisible) Text(text = state.zoneText)
        if (state.pickupTimeVisible) Text(text = state.pickupTimeText)
        if (state.pickupAddressVisible) Text(text = state.pickupAddressText)

        if (state.destinationsVisible) {
            Text(text = "Destinations")
            state.destinations.forEach { Text(text = "• $it") }
        }

        if (state.observationsVisible) {
            Text(text = "Observations")
            state.observations.forEach { Text(text = "• $it") }
        }

        if (state.flightCodeVisible) Text(text = state.flightCodeText)
        if (state.requirementsVisible) Text(text = state.requirementsText)
        if (state.amountVisible) Text(text = state.amountText)
        if (state.subscriberVisible) {
            Text(text = state.subscriberTitle)
            Text(text = state.subscriberUserName)
        }
        if (state.subscriberAuthVisible) Text(text = state.subscriberAuthorization)
        if (state.serviceTypeVisible) Text(text = state.serviceTypeText)
        if (state.concertedAmountVisible) Text(text = state.concertedAmountText)

        if (state.multiDispatch.visible) {
            MultiDispatchTabs(
                tabs = state.multiDispatch.tabs,
                selectedIndex = state.multiDispatch.selectedIndex,
                onTabSelected = { onEvent(InfoDispatchUiEvent.OnMultiDispatchTabSelected(it)) }
            )
        }

        InfoDispatchButtons(
            state = state.buttons,
            onEvent = onEvent
        )
    }
}

@Composable
fun InfoDispatchButtons(
    state: InfoDispatchButtonsState,
    onEvent: (InfoDispatchUiEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DispatchButton(
            state = state.notifications,
            onClick = { onEvent(InfoDispatchUiEvent.OnNotificationsClicked) }
        )
        DispatchButton(
            state = state.navigate,
            onClick = { onEvent(InfoDispatchUiEvent.OnNavigateClicked) }
        )
        DispatchButton(
            state = state.voiceCall,
            onClick = { onEvent(InfoDispatchUiEvent.OnVoiceCallClicked) }
        )
        DispatchButton(
            state = state.print,
            onClick = { onEvent(InfoDispatchUiEvent.OnPrintClicked) }
        )
        DispatchButton(
            state = state.noClient,
            onClick = { onEvent(InfoDispatchUiEvent.OnNoClientClicked) }
        )
        DispatchButton(
            state = state.returnDispatch,
            onClick = { onEvent(InfoDispatchUiEvent.OnReturnClicked) }
        )
    }
}

@Composable
private fun DispatchButton(
    state: InfoDispatchButtonState,
    onClick: () -> Unit
) {
    if (!state.visible) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(state.backgroundColor, RoundedCornerShape(12.dp))
            .clickable(enabled = state.enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.text,
            color = Color.White
        )
    }
}

@Composable
fun MultiDispatchTabs(
    tabs: List<InfoDispatchMultiDispatchTabState>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .background(
                        if (selected) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(12.dp)
                    .width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = tab.name, color = if (selected) Color.White else Color.Black)
                Spacer(Modifier.height(4.dp))
                Text(text = tab.iconLabel, color = if (selected) Color.White else Color.DarkGray)
            }
        }
    }
}

// =======================================================
// DIALOG
// =======================================================

@Composable
fun InfoDispatchCustomDialog(
    state: InfoDispatchDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (InfoDispatchDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = { Text(state.description) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.buttons.forEach { button ->
                    TextButton(onClick = { onButtonClick(button) }) {
                        Text(button.label)
                    }
                }
            }
        }
    )
}
