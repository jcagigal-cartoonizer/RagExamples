package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.screen.compose.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class HomeComposeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState(buttons = HomeButtonsStateFactory.defaultState()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<HomeUiEffect>()
    val uiEffects: SharedFlow<HomeUiEffect> = _uiEffects.asSharedFlow()
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.ScreenResumed -> refreshState()
            HomeUiEvent.ZoningClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_macroZoning))
            HomeUiEvent.PendingClicked -> handlePendingClick()
            HomeUiEvent.LocationClicked -> handleLocationClick()
            HomeUiEvent.ReceiptsClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_receiptHistoryFragment))
            HomeUiEvent.MessagesClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_messageFragment))
            HomeUiEvent.CentralClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_contactCentralFragment))
            HomeUiEvent.DashboardClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_dashboardFragment))
            HomeUiEvent.FixedPriceClicked -> emitNavigation(HomeNavigation.Id(R.id.action_homeFragment_to_FixedPriceMapFragment))
            HomeUiEvent.RoofLightClicked -> handleRoofLightClick()
            HomeUiEvent.LocateStandClicked -> handleLocateStandClick()
            HomeUiEvent.DialogDismissed,
            HomeUiEvent.DialogCancelled -> clearDialog()
            HomeUiEvent.DialogConfirmed -> confirmDialog()
        }
    }
    fun refreshState() {
        val buttons = _uiState.value.buttons.copy(
            dashboard = _uiState.value.buttons.dashboard.copy(visible = true),
            fixedPrice = _uiState.value.buttons.fixedPrice.copy(visible = false)
        )
        _uiState.value = _uiState.value.copy(buttons = buttons)
    }
    fun handlePendingClick() {
        viewModelScope.launch {
            val canOpen = W2CLocation.isLocationAllowedByCentral()
            if (!canOpen) {
                _uiEffects.emit(HomeUiEffect.ShowToast(R.string.do_not_locate_on_sanction))
                return@launch
            }
            _uiEffects.emit(
                HomeUiEffect.ShowDialog(
                    HomeDialogState(
                        titleRes = R.string.toast_no_hay_pendientes
                    )
                )
            )
        }
    }
    fun handleLocationClick() {
        viewModelScope.launch {
            _uiEffects.emit(
                HomeUiEffect.ShowDialog(
                    HomeDialogState(
                        titleRes = R.string.confirm_activate_location
                    )
                )
            )
        }
    }
    fun handleRoofLightClick() {
        viewModelScope.launch {
            _uiEffects.emit(HomeUiEffect.ShowToast(R.string.btn_roof_light))
        }
    }
    fun handleLocateStandClick() {
        viewModelScope.launch {
            _uiEffects.emit(HomeUiEffect.ShowToast(R.string.btn_locate_stop))
        }
    }
    fun confirmDialog() {
        viewModelScope.launch {
            _uiEffects.emit(HomeUiEffect.HideDialog)
        }
    }
    fun clearDialog() {
        _uiState.value = _uiState.value.copy(dialog = null)
        viewModelScope.launch { _uiEffects.emit(HomeUiEffect.HideDialog) }
    }
    fun emitNavigation(nav: HomeNavigation) {
        viewModelScope.launch {
            _uiEffects.emit(HomeUiEffect.Navigate(nav))
        }
    }
    fun updateButtonsFromCentralState(
        locationAllowed: Boolean,
        hasMessages: Boolean,
        hasNewMessages: Boolean,
        roofLightOn: Boolean?,
        roofLightAvailable: Boolean,
        showDashboard: Boolean,
        showFixedPrice: Boolean
    ) {
        val buttons = _uiState.value.buttons.copy(
            location = _uiState.value.buttons.location.copy(
                enabled = locationAllowed,
                background = if (locationAllowed) HomeButtonBackground.Green else HomeButtonBackground.Red
            ),
            messages = _uiState.value.buttons.messages.copy(
                enabled = hasMessages,
                background = when {
                    hasNewMessages -> HomeButtonBackground.Orange
                    hasMessages -> HomeButtonBackground.Blue
                    else -> HomeButtonBackground.Gray
                }
            ),
            roofLight = _uiState.value.buttons.roofLight.copy(
                visible = roofLightAvailable,
                enabled = roofLightAvailable,
                background = when (roofLightOn) {
                    true -> HomeButtonBackground.Green
                    false -> HomeButtonBackground.Red
                    null -> HomeButtonBackground.Gray
                }
            ),
            dashboard = _uiState.value.buttons.dashboard.copy(visible = showDashboard),
            fixedPrice = _uiState.value.buttons.fixedPrice.copy(visible = showFixedPrice)
        )
        _uiState.value = _uiState.value.copy(buttons = buttons)
    }
}
