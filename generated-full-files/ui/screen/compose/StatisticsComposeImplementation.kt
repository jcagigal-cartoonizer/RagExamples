package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.material3.Surface
import androidx.lifecycle.AndroidViewModel
import ifac.td.taxi.domain.usecase.ShiftUseCase
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.rememberPagerState
import ifac.td.taxi.repository.room.dao.ShiftDao.ShiftStatistics
import kotlinx.coroutines.flow.asStateFlow
import android.app.Application
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.repository.room.dao.TripDao.TotalAmountByDate
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


// ----------------------------
// UI STATE / EFFECTS / EVENTS
// ----------------------------

data class StatisticsUiState(
    val selectedTabIndex: Int = 0,
    val billingValues: List<TotalAmountByDate> = emptyList(),
    val timeValues: List<ShiftStatistics> = emptyList(),
    val isLoading: Boolean = false,
    val buttonsState: StatisticsButtonsState = StatisticsButtonsState(),
)

sealed interface StatisticsUiEvent {
    data class TabSelected(val index: Int) : StatisticsUiEvent
    data object LoadWeek : StatisticsUiEvent
    data object LoadMonth : StatisticsUiEvent
    data object LoadYear : StatisticsUiEvent
}

sealed interface StatisticsUiEffect {
    data object NavigateBack : StatisticsUiEffect
    data class NavigateTo(val route: String) : StatisticsUiEffect
    data class ShowToast(val message: String) : StatisticsUiEffect
}

// ----------------------------
// BUTTON VISUAL STATE / REDUCER
// ----------------------------

data class StatisticsButtonVisualState(
    val text: String = "",
    val iconRes: Int? = null,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val backgroundColor: Color = Color(0xFF1976D2),
    val textColor: Color = Color.White,
    val iconVisible: Boolean = true,
    val textVisible: Boolean = true,
    val onlyTextVisible: Boolean = false,
    val onlyTextValue: String = "",
)

data class StatisticsDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
)

data class StatisticsButtonsState(
    val week: StatisticsButtonVisualState = StatisticsButtonVisualState(),
    val month: StatisticsButtonVisualState = StatisticsButtonVisualState(),
    val year: StatisticsButtonVisualState = StatisticsButtonVisualState(),
    val dialogState: StatisticsDialogState = StatisticsDialogState(),
)

sealed interface StatisticsButtonsEffect {
    data object ShowWeekEnabled : StatisticsButtonsEffect
    data object ShowWeekDisabled : StatisticsButtonsEffect
    data object ShowMonthEnabled : StatisticsButtonsEffect
    data object ShowMonthDisabled : StatisticsButtonsEffect
    data object ShowYearEnabled : StatisticsButtonsEffect
    data object ShowYearDisabled : StatisticsButtonsEffect
    data class ShowDialog(val title: String, val message: String) : StatisticsButtonsEffect
    data object HideDialog : StatisticsButtonsEffect
}

fun StatisticsButtonsState.reduce(effect: StatisticsButtonsEffect): StatisticsButtonsState {
    return when (effect) {
        StatisticsButtonsEffect.ShowWeekEnabled -> copy(
            week = week.copy(
                enabled = true,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFF1976D2),
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        StatisticsButtonsEffect.ShowWeekDisabled -> copy(
            week = week.copy(
                enabled = false,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFFBDBDBD),
                textColor = Color(0xFF757575),
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        StatisticsButtonsEffect.ShowMonthEnabled -> copy(
            month = month.copy(
                enabled = true,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFF1976D2),
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        StatisticsButtonsEffect.ShowMonthDisabled -> copy(
            month = month.copy(
                enabled = false,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFFBDBDBD),
                textColor = Color(0xFF757575),
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        StatisticsButtonsEffect.ShowYearEnabled -> copy(
            year = year.copy(
                enabled = true,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFF1976D2),
                textColor = Color.White,
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        StatisticsButtonsEffect.ShowYearDisabled -> copy(
            year = year.copy(
                enabled = false,
                loading = false,
                visible = true,
                backgroundColor = Color(0xFFBDBDBD),
                textColor = Color(0xFF757575),
                iconVisible = true,
                textVisible = true,
                onlyTextVisible = false
            )
        )

        is StatisticsButtonsEffect.ShowDialog -> copy(
            dialogState = StatisticsDialogState(
                visible = true,
                title = effect.title,
                message = effect.message
            )
        )

        StatisticsButtonsEffect.HideDialog -> copy(
            dialogState = dialogState.copy(visible = false)
        )
    }
}

// ----------------------------
// COMPOSE VIEWMODELS
// ----------------------------

class StatisticsViewModel(
    val tripUseCase: TripUseCase,
    val shiftUseCase: ShiftUseCase,
    context: Application,
) : BaseViewModel(context) {

    val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()

    val _effects = MutableSharedFlow<StatisticsUiEffect>()
    val effects: SharedFlow<StatisticsUiEffect> = _effects.asSharedFlow()

    val _buttonsState = MutableStateFlow(StatisticsButtonsState())
    val buttonsState = _buttonsState.asStateFlow()

    val _datesFlow = MutableStateFlow<List<String>?>(null)
    val datesFlow = _datesFlow.asStateFlow()

    val _totalFlow = MutableStateFlow<List<Double>?>(null)
    val totalFlow = _totalFlow.asStateFlow()

    val _billingValuesFlow = MutableStateFlow<List<TotalAmountByDate>?>(null)
    val billingValuesFlow = _billingValuesFlow.asStateFlow()

    val _timeValuesFlow = MutableStateFlow<List<ShiftStatistics>?>(null)
    val timeValuesFlow = _timeValuesFlow.asStateFlow()

    fun onEvent(event: StatisticsUiEvent) {
        when (event) {
            is StatisticsUiEvent.TabSelected -> onTabSelected(event.index)
            StatisticsUiEvent.LoadWeek -> loadWeek()
            StatisticsUiEvent.LoadMonth -> loadMonth()
            StatisticsUiEvent.LoadYear -> loadYear()
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
        when (index) {
            0 -> loadWeek()
            1 -> loadMonth()
            2 -> loadYear()
        }
    }

    private fun loadWeek() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val billing = tripUseCase.getTotalAmountPerDayLast7Days()
            _billingValuesFlow.emit(billing)
            val time = shiftUseCase.getTimePerMonthLast7Days()
            _timeValuesFlow.emit(time)
            _uiState.value = _uiState.value.copy(
                billingValues = billing,
                timeValues = time,
                isLoading = false
            )
        }
    }

    private fun loadMonth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val billing = tripUseCase.getTotalAmountPerDayLast30Days()
            _billingValuesFlow.emit(billing)
            val time = shiftUseCase.getTimePerMonthLast30Days()
            _timeValuesFlow.emit(time)
            _uiState.value = _uiState.value.copy(
                billingValues = billing,
                timeValues = time,
                isLoading = false
            )
        }
    }

    private fun loadYear() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val billing = tripUseCase.getTotalAmountPerMonthLast12Months()
            _billingValuesFlow.emit(billing)
            val time = shiftUseCase.getTimePerMonthLast12Months()
            _timeValuesFlow.emit(time)
            _uiState.value = _uiState.value.copy(
                billingValues = billing,
                timeValues = time,
                isLoading = false
            )
        }
    }

    fun updateButtonsState(state: StatisticsButtonsState) {
        _buttonsState.value = state
        _uiState.value = _uiState.value.copy(buttonsState = state)
    }

    fun emitEffect(effect: StatisticsUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
}

class StatisticsMainActivityViewModel : ViewModel() {
    val _uiState = MutableStateFlow(StatisticsMainActivityUiState())
    val uiState = _uiState.asStateFlow()

    fun showHeader(show: Boolean) {
        _uiState.value = _uiState.value.copy(showHeader = show)
    }
}

data class StatisticsMainActivityUiState(
    val showHeader: Boolean = true
)

// ----------------------------
// COMPOSE SCREEN
// ----------------------------

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel,
    mainActivityViewModel: StatisticsMainActivityViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val mainUiState by mainActivityViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainActivityViewModel.showHeader(true)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                StatisticsUiEffect.NavigateBack -> navController.popBackStack()
                is StatisticsUiEffect.NavigateTo -> navController.navigate(effect.route)
                is StatisticsUiEffect.ShowToast -> Unit
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        StatisticsContent(
            uiState = uiState,
            buttonsState = buttonsState,
            onTabSelected = { viewModel.onEvent(StatisticsUiEvent.TabSelected(it)) }
        )
    }
}

@Composable
private fun StatisticsContent(
    uiState: StatisticsUiState,
    buttonsState: StatisticsButtonsState,
    onTabSelected: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { 3 }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        if (uiState.selectedTabIndex != pagerState.currentPage) {
            onTabSelected(pagerState.currentPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTabIndex) {
            Tab(
                selected = uiState.selectedTabIndex == 0,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(0) }
                    onTabSelected(0)
                },
                text = { Text(text = "Week") }
            )
            Tab(
                selected = uiState.selectedTabIndex == 1,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(1) }
                    onTabSelected(1)
                },
                text = { Text(text = "Month") }
            )
            Tab(
                selected = uiState.selectedTabIndex == 2,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(2) }
                    onTabSelected(2)
                },
                text = { Text(text = "Year") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StatisticsWeekPage(buttonsState)
                1 -> StatisticsMonthPage(buttonsState)
                2 -> StatisticsYearPage(buttonsState)
            }
        }
    }
}

@Composable
private fun StatisticsWeekPage(buttonsState: StatisticsButtonsState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Week statistics")
        Text(text = "Button state: ${buttonsState.week.text}")
    }
}

@Composable
private fun StatisticsMonthPage(buttonsState: StatisticsButtonsState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Month statistics")
        Text(text = "Button state: ${buttonsState.month.text}")
    }
}

@Composable
private fun StatisticsYearPage(buttonsState: StatisticsButtonsState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Year statistics")
        Text(text = "Button state: ${buttonsState.year.text}")
    }
}

@Composable
fun StatisticsDialogHost(
    dialogState: StatisticsDialogState,
    onDismiss: () -> Unit
) {
    if (dialogState.visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(dialogState.title) },
            text = { Text(dialogState.message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}
