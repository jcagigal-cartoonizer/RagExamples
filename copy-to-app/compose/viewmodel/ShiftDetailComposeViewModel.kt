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
    fun showDialog(dialog: ShiftDetailComposeFragmentCustomDialogState) {
        _uiEffect.tryEmit(ShiftDetailUiEffect.ShowDialog(dialog))
    }
    fun showToast(messageRes: Int) {
        _uiEffect.tryEmit(ShiftDetailUiEffect.ShowToast(messageRes))
    }
}
