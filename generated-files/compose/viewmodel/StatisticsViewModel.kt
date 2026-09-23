package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.StatisticsUiEffect
import ifac.td.taxi.ui.screen.components.StatisticsUiState
import ifac.td.taxi.ui.screen.components.StatisticsButtonsState
import ifac.td.taxi.ui.screen.components.StatisticsUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 150-4: import android.app.Application
class StatisticsComposeViewModel(
    private val tripUseCase: TripUseCase,
    private val shiftUseCase: ShiftUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<StatisticsUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()
    private val _buttonsState = MutableStateFlow(StatisticsButtonsState())
    val buttonsState = _buttonsState.asStateFlow()
    init {
        updateButtonsForTab(StatisticsTab.Week)
        loadForSelectedTab(StatisticsTab.Week)
    }
    fun onEvent(event: StatisticsUiEvent) {
        when (event) {
            is StatisticsUiEvent.TabSelected -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
                updateButtonsForTab(event.tab)
                loadForSelectedTab(event.tab)
            }
            StatisticsUiEvent.BillingClicked -> {
                _uiEffect.tryEmit(
                    StatisticsUiEffect.OpenDialog(
                        StatisticsDialogState(
                            title = "Billing",
                            message = "Show billing statistics?"
                        )
                    )
                )
            }
            StatisticsUiEvent.TimeClicked -> {
                _uiEffect.tryEmit(
                    StatisticsUiEffect.OpenDialog(
                        StatisticsDialogState(
                            title = "Time",
                            message = "Show time statistics?"
                        )
                    )
                )
            }
            StatisticsUiEvent.DialogConfirmed -> {
                _uiEffect.tryEmit(StatisticsUiEffect.CloseDialog)
            }
            StatisticsUiEvent.DialogDismissed -> {
                _uiEffect.tryEmit(StatisticsUiEffect.CloseDialog)
            }
            StatisticsUiEvent.BackClicked -> {
                _uiEffect.tryEmit(StatisticsUiEffect.NavigateBack)
            }
            StatisticsUiEvent.HomeClicked -> {
                _uiEffect.tryEmit(StatisticsUiEffect.NavigateToHome)
            }
        }
    }
    fun loadForSelectedTab(tab: StatisticsTab) {
        when (tab) {
            StatisticsTab.Week -> {
                get7LastDays()
                getTimeLast7Days()
            }
            StatisticsTab.Month -> {
                get30LastDays()
                getTimeLast30Days()
            }
            StatisticsTab.Year -> {
                get12LastMonths()
                getTimeLast12Months()
            }
        }
    }
    fun updateButtonsForTab(tab: StatisticsTab) {
        _buttonsState.value = StatisticsButtonsState.forTab(tab)
    }
    fun get7LastDays() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerDayLast7Days()
            _uiState.update { it.copy(billingValues = values, isLoading = false) }
        }
    }
    fun get30LastDays() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerDayLast30Days()
            _uiState.update { it.copy(billingValues = values, isLoading = false) }
        }
    }
    fun get12LastMonths() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerMonthLast12Months()
            _uiState.update { it.copy(billingValues = values, isLoading = false) }
        }
    }
    fun getTimeLast7Days() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast7Days()
            _uiState.update { it.copy(timeValues = values, isLoading = false) }
        }
    }
    fun getTimeLast30Days() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast30Days()
            _uiState.update { it.copy(timeValues = values, isLoading = false) }
        }
    }
    fun getTimeLast12Months() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast12Months()
            _uiState.update { it.copy(timeValues = values, isLoading = false) }
        }
    }
}
