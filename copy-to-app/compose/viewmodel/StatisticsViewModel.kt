package ifac.td.taxi.compose.viewmodel
import  android.app.Application
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
