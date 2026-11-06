package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.components.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------------
// DOMAIN / SHARED MODELS
// -----------------------------------------------------------------------------

@Immutable
data class DispatchReceivedUiState(
    val isLoading: Boolean = false,
    val dispatch: DispatchReceivedDispatchUi? = null,
    val buttons: DispatchReceivedButtonsState = DispatchReceivedButtonsState(),
    val showCancelDialog: Boolean = false,
    val cancelDialog: DispatchReceivedDialogState? = null,
)

@Immutable
data class DispatchReceivedDispatchUi(
    val idDispatch: Long = 0L,
    val pickUpTime: String? = null,
    val pickUpZone: String? = null,
    val smallPickUpAddress: String? = null,
    val city: String? = null,
    val passengerName: String? = null,
    val requireSignature: Boolean = false,
    val requireVoucher: Boolean = false,
    val requireQr: Boolean = false,
    val amount: Int? = null,
    val clientType: Int? = null,
    val isConcertedPrice: Boolean? = null,
    val isConcertedMaximumPrice: Boolean? = null,
    val attribute1Title: String? = null,
    val attribute2Title: String? = null,
    val attribute3Title: String? = null,
    val attribute4Title: String? = null,
)

@Immutable
data class DispatchReceivedButtonsState(
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptBackgroundColor: Long = 0xFF2E7D32,
    val acceptContentColor: Long = 0xFFFFFFFF,
    val cancelBackgroundColor: Long = 0xFFC62828,
    val cancelContentColor: Long = 0xFFFFFFFF,
    val containerVisible: Boolean = true,
    val loadingVisible: Boolean = false,
    val isLandscape: Boolean = false,
    val attribute1Visible: Boolean = true,
    val attribute2Visible: Boolean = true,
    val attribute3Visible: Boolean = true,
    val attribute4Visible: Boolean = true,
    val attribute1Title: String? = null,
    val attribute2Title: String? = null,
    val attribute3Title: String? = null,
    val attribute4Title: String? = null,
    val flowMenuVisible: Boolean = false,
    val extraButtonsVisible: Boolean = false,
    val subscriberButtonVisible: Boolean = false,
    val appPaymentButtonVisible: Boolean = false,
    val othersPaymentButtonVisible: Boolean = false,
    val cardButtonVisible: Boolean = false,
    val cashButtonVisible: Boolean = false,
    val cancelPaymentButtonVisible: Boolean = false,
    val addButtonClickable: Boolean = true,
    val amountVisible: Boolean = false,
    val amountText: String? = null,
    val isWaitingAppPaymentResponse: Boolean = false,
    val showAppPaymentDialog: Boolean = false,
    val appPaymentDialog: DispatchReceivedDialogState? = null,
)

@Immutable
data class DispatchReceivedDialogState(
    val title: String,
    val description: String,
    val acceptText: String = "Aceptar",
    val cancelText: String = "Cancelar",
    val centerText: Boolean = false,
    val cancellable: Boolean = false,
)

@Immutable
data class DispatchReceivedSharedState(
    val dispatch: DispatchReceivedDispatchUi? = null,
    val tripId: Long? = null,
    val inAppPaymentResponse: Pair<Boolean, String?>? = null,
)

sealed interface DispatchReceivedUiEvent {
    data object OnScreenStarted : DispatchReceivedUiEvent
    data object OnAcceptClicked : DispatchReceivedUiEvent
    data object OnCancelClicked : DispatchReceivedUiEvent
    data object OnCancelDialogAccepted : DispatchReceivedUiEvent
    data object OnCancelDialogDismissed : DispatchReceivedUiEvent
    data object OnAppPaymentDialogAccepted : DispatchReceivedUiEvent
    data object OnAppPaymentDialogDismissed : DispatchReceivedUiEvent
}

sealed interface DispatchReceivedUiEffect {
    data class NavigateToInfoDispatch(val directions: NavDirections) : DispatchReceivedUiEffect
    data object NavigateBack : DispatchReceivedUiEffect
    data object RequestPhonePermission : DispatchReceivedUiEffect
    data object OpenCancelDispatchDialog : DispatchReceivedUiEffect
    data class OpenAppPaymentDialog(val dialog: DispatchReceivedDialogState) : DispatchReceivedUiEffect
    data object CloseAppPaymentDialog : DispatchReceivedUiEffect
}

// -----------------------------------------------------------------------------
// SHARED VM ADAPTER FOR YOUR REAL MainActivityViewModel
// -----------------------------------------------------------------------------

class DispatchReceivedSharedVmAdapter(
    private val mainActivityViewModel: MainActivityViewModel
) {
    val sharedState: MutableStateFlow<DispatchReceivedSharedState> = MutableStateFlow(
        DispatchReceivedSharedState()
    )

    fun bindDispatch(dispatch: DispatchReceivedDispatchUi?) {
        sharedState.update { it.copy(dispatch = dispatch) }
    }

    fun bindTripId(tripId: Long?) {
        sharedState.update { it.copy(tripId = tripId) }
    }

    fun bindInAppPaymentResponse(response: Pair<Boolean, String?>?) {
        sharedState.update { it.copy(inAppPaymentResponse = response) }
    }

    fun updateDispatchFlow(dispatch: DispatchReceivedDispatchUi?) {
        bindDispatch(dispatch)
        mainActivityViewModel.updateDispatchFlow(dispatch)
    }

    fun updateTripFlow(tripId: Long?) {
        bindTripId(tripId)
        mainActivityViewModel.updateTripFlow(tripId)
    }

    fun updateInAppPaymentResponse(response: Pair<Boolean, String?>?) {
        bindInAppPaymentResponse(response)
        mainActivityViewModel.updateInAppPaymentResponse(response)
    }
}

// -----------------------------------------------------------------------------
// SCREEN VIEWMODEL
// -----------------------------------------------------------------------------

class DispatchReceivedComposeViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val licensingUsecase: LicensingUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    application: Application,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DispatchReceivedUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DispatchReceivedUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var currentDispatchId: Long? = null
    private var showCancelDialogPermission: Boolean = false

    fun initialize(idDispatch: Long) {
        getDispatch(idDispatch)
        checkCancelDialogPermission()
    }

    fun onEvent(event: DispatchReceivedUiEvent) {
        when (event) {
            DispatchReceivedUiEvent.OnScreenStarted -> Unit
            DispatchReceivedUiEvent.OnAcceptClicked -> acceptDispatch()
            DispatchReceivedUiEvent.OnCancelClicked -> {
                if (showCancelDialogPermission) {
                    _uiState.update {
                        it.copy(
                            showCancelDialog = true,
                            cancelDialog = DispatchReceivedDialogState(
                                title = "Advertencia",
                                description = "¿Quieres rechazar este despacho?"
                            )
                        )
                    }
                    viewModelScope.launch { _uiEffect.emit(DispatchReceivedUiEffect.OpenCancelDispatchDialog) }
                } else {
                    rejectManualDispatch()
                }
            }
            DispatchReceivedUiEvent.OnCancelDialogAccepted -> {
                _uiState.update { it.copy(showCancelDialog = false, cancelDialog = null) }
                rejectManualDispatch()
            }
            DispatchReceivedUiEvent.OnCancelDialogDismissed -> {
                _uiState.update { it.copy(showCancelDialog = false, cancelDialog = null) }
            }
            DispatchReceivedUiEvent.OnAppPaymentDialogAccepted -> {
                _uiState.update { it.copy(buttons = it.buttons.copy(showAppPaymentDialog = false, appPaymentDialog = null)) }
            }
            DispatchReceivedUiEvent.OnAppPaymentDialogDismissed -> {
                _uiState.update { it.copy(buttons = it.buttons.copy(showAppPaymentDialog = false, appPaymentDialog = null)) }
            }
        }
    }

    fun getDispatch(id: Long) {
        viewModelScope.launch {
            currentDispatchId = id
            val dispatch = dispatchUseCase.getDispatchById(id)
            if (dispatch != null) {
                _uiState.update {
                    it.copy(
                        dispatch = DispatchReceivedDispatchUi(
                            idDispatch = id,
                            pickUpTime = dispatch.pickUpTime,
                            pickUpZone = dispatch.pickUpZone,
                            smallPickUpAddress = dispatch.smallPickUpAdress,
                            city = dispatch.city,
                            passengerName = dispatch.passengerName,
                            requireSignature = dispatch.requireSignature,
                            requireVoucher = dispatch.requireVoucher,
                            requireQr = dispatch.requireQr,
                            amount = dispatch.amount,
                            clientType = dispatch.clientType,
                            isConcertedPrice = dispatch.isConcertedPrice,
                            isConcertedMaximumPrice = dispatch.isConcertedMaximumPrice,
                            attribute1Title = dispatch.attribute1Title,
                            attribute2Title = dispatch.attribute2Title,
                            attribute3Title = dispatch.attribute3Title,
                            attribute4Title = dispatch.attribute4Title,
                        )
                    )
                }
                handleDispatchRequirements(dispatch)
            }
        }
    }

    fun checkCancelDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            showCancelDialogPermission = preferences?.showConfRejectDispatch == true
        }
    }

    fun checkIsDirRecogidaAceptaDespacho() {
        viewModelScope.launch {
            val preferences = licensingUsecase.getLicensingParameters()
            val addressAccepted = preferences?.isAddressTakenAcceptsDispatch == true
            _uiState.update {
                it.copy(buttons = it.buttons.copy(acceptVisible = addressAccepted))
            }
        }
    }

    fun stopCallTimer() = Unit

    fun rejectManualDispatch() {
        viewModelScope.launch { _uiEffect.emit(DispatchReceivedUiEffect.NavigateBack) }
    }

    fun acceptDispatch() {
        val id = currentDispatchId ?: return
        viewModelScope.launch {
            _uiEffect.emit(
                DispatchReceivedUiEffect.NavigateToInfoDispatch(
                    HomeDirections.goToInfoDispatchFragment(id)
                )
            )
        }
    }

    fun isNumberPhoneIn() {
        viewModelScope.launch { _uiEffect.emit(DispatchReceivedUiEffect.RequestPhonePermission) }
    }

    fun dispatchRequireSignature(dispatch: DispatchReceivedDispatchUi) = Unit
    fun dispatchRequireVoucher(dispatch: DispatchReceivedDispatchUi) = Unit
    fun dispatchRequireQR(dispatch: DispatchReceivedDispatchUi) = Unit
    fun validCredit(dispatch: DispatchReceivedDispatchUi) = Unit

    private fun handleDispatchRequirements(dispatch: DispatchReceivedDispatchUi) {
        if (dispatch.requireSignature) {
            dispatchRequireSignature(dispatch)
        } else if (dispatch.requireVoucher) {
            dispatchRequireVoucher(dispatch)
        } else if (dispatch.requireQr) {
            dispatchRequireQR(dispatch)
        } else {
            validCredit(dispatch)
        }
    }

    fun updateTripAmount(totalAmount: Int) {
        _uiState.update {
            it.copy(buttons = it.buttons.copy(amountVisible = true, amountText = totalAmount.toString()))
        }
    }
}

// -----------------------------------------------------------------------------
// REAL SHARED VM ADAPTER FOR YOUR EXISTING MainActivityViewModel
// -----------------------------------------------------------------------------

class MainActivityViewModel : ViewModel() {
    private val _dispatchFlow = MutableStateFlow<DispatchReceivedDispatchUi?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    private val _tripFlow = MutableStateFlow<Long?>(null)
    val tripFlow = _tripFlow.asStateFlow()

    private val _inAppPaymentResponseFlow = MutableSharedFlow<Pair<Boolean, String?>>()
    val inAppPaymentResponseFlow = _inAppPaymentResponseFlow.asSharedFlow()

    fun updateDispatchFlow(dispatch: DispatchReceivedDispatchUi?) {
        _dispatchFlow.value = dispatch
    }

    fun updateTripFlow(tripId: Long?) {
        _tripFlow.value = tripId
    }

    fun updateInAppPaymentResponse(response: Pair<Boolean, String?>?) {
        if (response != null) {
            viewModelScope.launch { _inAppPaymentResponseFlow.emit(response) }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPOSE SCREEN
// -----------------------------------------------------------------------------

@Composable
fun DispatchReceivedScreen(
    navController: NavController,
    viewModel: DispatchReceivedComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    idDispatch: Long,
    onShowHeader: (Boolean) -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onCloseDialog: () -> Unit,
    onRequestPhonePermission: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.dispatchFlow.collectAsStateWithLifecycle()

    var cancelDialogState by remember { mutableStateOf<DispatchReceivedDialogState?>(null) }
    var appPaymentDialogState by remember { mutableStateOf<DispatchReceivedDialogState?>(null) }

    LaunchedEffect(Unit) {
        onShowHeader(false)
        onShowBottomBar(false)
        onCloseDialog()
        viewModel.initialize(idDispatch)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DispatchReceivedUiEffect.NavigateToInfoDispatch -> {
                    navController.navigate(effect.directions)
                }
                DispatchReceivedUiEffect.NavigateBack -> navController.popBackStack()
                DispatchReceivedUiEffect.RequestPhonePermission -> onRequestPhonePermission()
                DispatchReceivedUiEffect.OpenCancelDispatchDialog -> {
                    cancelDialogState = state.cancelDialog
                }
                is DispatchReceivedUiEffect.OpenAppPaymentDialog -> {
                    appPaymentDialogState = effect.dialog
                }
                DispatchReceivedUiEffect.CloseAppPaymentDialog -> {
                    appPaymentDialogState = null
                }
            }
        }
    }

    LaunchedEffect(sharedState) {
        sharedState?.let { dispatch ->
            if (dispatch.requireSignature) {
                viewModel.dispatchRequireSignature(dispatch)
            } else if (dispatch.requireVoucher) {
                viewModel.dispatchRequireVoucher(dispatch)
            } else if (dispatch.requireQr) {
                viewModel.dispatchRequireQR(dispatch)
            } else {
                viewModel.validCredit(dispatch)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                state.dispatch?.let { dispatch ->
                    Text(text = dispatch.pickUpTime.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(text = dispatch.pickUpZone.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    Text(text = dispatch.smallPickUpAddress.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    Text(text = dispatch.city.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    Text(text = dispatch.passengerName.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))

                DispatchReceivedButtons(
                    state = state.buttons,
                    onAccept = {
                        viewModel.acceptDispatch()
                        viewModel.stopCallTimer()
                    },
                    onCancel = {
                        viewModel.onEvent(DispatchReceivedUiEvent.OnCancelClicked)
                        viewModel.stopCallTimer()
                    }
                )
            }
        }
    }

    cancelDialogState?.let { dialog ->
        AlertDialog(
            onDismissRequest = {
                cancelDialogState = null
                viewModel.onEvent(DispatchReceivedUiEvent.OnCancelDialogDismissed)
            },
            title = { Text(dialog.title) },
            text = { Text(dialog.description) },
            confirmButton = {
                TextButton(onClick = {
                    cancelDialogState = null
                    viewModel.onEvent(DispatchReceivedUiEvent.OnCancelDialogAccepted)
                }) { Text(dialog.acceptText) }
            },
            dismissButton = {
                TextButton(onClick = {
                    cancelDialogState = null
                    viewModel.onEvent(DispatchReceivedUiEvent.OnCancelDialogDismissed)
                }) { Text(dialog.cancelText) }
            }
        )
    }

    appPaymentDialogState?.let { dialog ->
        AlertDialog(
            onDismissRequest = { appPaymentDialogState = null },
            title = { Text(dialog.title) },
            text = { Text(dialog.description) },
            confirmButton = {
                TextButton(onClick = { appPaymentDialogState = null }) {
                    Text(dialog.acceptText)
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// CUSTOM BUTTON API MATCHING YOUR COMPOSE STYLE
// -----------------------------------------------------------------------------

@Composable
fun DispatchReceivedButtons(
    state: DispatchReceivedButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            CustomButton(
                style = CustomButtonStyle(
                    visible = true,
                    enabled = state.cancelEnabled,
                    backgroundColor = Color(state.cancelBackgroundColor),
                    contentColor = Color(state.cancelContentColor),
                    text = "Rechazar"
                ),
                modifier = Modifier.weight(1f),
                onClick = onCancel
            )
        }

        if (state.acceptVisible) {
            CustomButton(
                style = CustomButtonStyle(
                    visible = true,
                    enabled = state.acceptEnabled,
                    backgroundColor = Color(state.acceptBackgroundColor),
                    contentColor = Color(state.acceptContentColor),
                    text = "Aceptar"
                ),
                modifier = Modifier.weight(1f),
                onClick = onAccept
            )
        }
    }
}

@Immutable
data class CustomButtonStyle(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val backgroundColor: Color,
    val contentColor: Color,
    val text: String,
)

@Composable
fun CustomButton(
    style: CustomButtonStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (!style.visible) return

    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = style.enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.backgroundColor.copy(alpha = 0.4f),
            disabledContentColor = style.contentColor.copy(alpha = 0.4f)
        )
    ) {
        Text(text = style.text)
    }
}

// -----------------------------------------------------------------------------
// PLACEHOLDER USE CASES / DOMAIN CONTRACTS
// Replace with your real project types.
// -----------------------------------------------------------------------------

interface DispatchUseCase {
    suspend fun getDispatchById(id: Long): DispatchReceivedDispatchUi?
}

interface UserPreferencesUseCase {
    suspend fun getUserPreferences(): UserPreferencesLike?
}

interface LicensingUseCase {
    suspend fun getLicensingParameters(): LicensingParametersLike?
}

interface BravoConfigurationVariableDao {
    suspend fun getBravoConfigurationVariable(): Any?
}

interface UserPreferencesLike {
    val showConfRejectDispatch: Boolean?
}

interface LicensingParametersLike {
    val isAddressTakenAcceptsDispatch: Boolean?
}


