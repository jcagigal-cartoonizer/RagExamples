package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ShiftDetailUiEffect
import ifac.td.taxi.ui.screen.components.ShiftDetailUiState
import ifac.td.taxi.ui.screen.components.ShiftDetailCustomDialogState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 116-3: import android.app.Application
class ShiftDetailComposeViewModel(
    application: Application,
    private val tripUseCase: TripUseCase,
    private val shiftExportUseCase: ShiftExportUseCase,
    private val shiftUseCase: ShiftUseCase
) : BaseViewModel(application) {
    private val _uiState = MutableStateFlow(ShiftDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ShiftDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    fun onScreenStarted(shiftId: Long) {
        _uiState.update { it.copy(shiftId = shiftId) }
        loadTrips(shiftId)
    }
    fun loadTrips(shiftId: Long) {
        viewModelScope.launch {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            _uiState.update { it.copy(trips = trips) }
        }
    }
    fun onExportClicked() {
        val shiftId = uiState.value.shiftId
        viewModelScope.launch {
            shiftExportUseCase.exportShift(shiftId, ShiftExportUseCaseImpl.ACTION_EXPORT_FILE)
                ?.let { _uiEffect.emit(ShiftDetailUiEffect.OpenIntent(it)) }
        }
    }
    fun onEmailClicked() {
        val shiftId = uiState.value.shiftId
        viewModelScope.launch {
            shiftExportUseCase.exportShift(shiftId, ShiftExportUseCaseImpl.ACTION_SEND_FILE)
                ?.let { _uiEffect.emit(ShiftDetailUiEffect.OpenIntent(it)) }
        }
    }
    fun onPrintClicked() {
        val shiftId = uiState.value.shiftId
        viewModelScope.launch(Dispatchers.IO) {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            shiftUseCase.printShift(trips)
        }
    }
    fun onSortClicked(option: ShiftOrderOptions) {
        val current = uiState.value.sortState
        val next = when {
            current.option != option -> SortStateUi(option, true)
            current.isAscending == true -> SortStateUi(option, false)
            current.isAscending == false -> SortStateUi(ShiftOrderOptions.NONE, null)
            else -> SortStateUi(option, true)
        }
        _uiState.update { state ->
            state.copy(
                sortState = next,
                buttons = state.buttons.applySort(next.option, next.isAscending)
            )
        }
        val shiftId = uiState.value.shiftId
        sortShiftTripsBy(shiftId, next.option, next.isAscending ?: true)
    }
    fun sortShiftTripsBy(
        shiftId: Long,
        orderOptions: ShiftOrderOptions,
        isAscending: Boolean
    ) {
        viewModelScope.launch {
            val shiftTrips = when (orderOptions) {
                ShiftOrderOptions.ID ->
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.id }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.id }
                ShiftOrderOptions.AMOUNT ->
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.totalAmount }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.totalAmount }
                ShiftOrderOptions.START_DATE ->
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.initDate }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.initDate }
                ShiftOrderOptions.DISTANCE ->
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.distance }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.distance }
                ShiftOrderOptions.NONE ->
                    tripUseCase.getTripsByShiftId(shiftId)
            }
            _uiState.update { it.copy(trips = shiftTrips) }
        }
    }
    fun showDialog(dialog: ShiftDetailCustomDialogState) {
        _uiEffect.tryEmit(ShiftDetailUiEffect.ShowDialog(dialog))
    }
    fun showToast(messageRes: Int) {
        _uiEffect.tryEmit(ShiftDetailUiEffect.ShowToast(messageRes))
    }
}
