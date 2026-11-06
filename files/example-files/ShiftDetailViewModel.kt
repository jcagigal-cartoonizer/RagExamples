package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.ShiftExportUseCase
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import ifac.td.taxi.viewmodel.model.SortState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShiftDetailViewModel(
    context: Application,
    private val tripUseCase: TripUseCase,
    private val shiftExportUseCase: ShiftExportUseCase,
    private val shiftUseCase: ShiftUseCase
) :
    BaseViewModel(context) {

    private val TAG = "ShiftDetailViewModel"

    private val _tripsFlow = MutableStateFlow<List<Trip>?>(null)
    val tripFlow = _tripsFlow.asStateFlow()

    private val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow = _intentFlow.asSharedFlow()


    fun getTripsFromShiftId(shiftId: Long) {
        viewModelScope.launch {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            _tripsFlow.emit(trips)
        }
    }

    var currentSortState = SortState(ShiftOrderOptions.NONE)

    fun sortShiftTripsBy(shiftId: Long, orderOptions: ShiftOrderOptions, isAscending: Boolean) {
        viewModelScope.launch {
            val shiftTrips = when (orderOptions) {
                ShiftOrderOptions.ID -> {
                    if (isAscending) {
                        tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.id }
                    } else {
                        tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.id }
                    }
                }
                ShiftOrderOptions.AMOUNT -> {
                    if (isAscending) {
                        tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.totalAmount }
                    } else {
                        tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.totalAmount }
                    }
                }
                ShiftOrderOptions.START_DATE -> {
                    if (isAscending) {
                        tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.initDate }
                    } else {
                        tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.initDate }
                    }
                }
                ShiftOrderOptions.DISTANCE -> {
                    if (isAscending) {
                        tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.distance }
                    } else {
                        tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.distance }
                    }
                }
                ShiftOrderOptions.NONE -> {
                    tripUseCase.getTripsByShiftId(shiftId)
                }
            }
            _tripsFlow.emit(shiftTrips)
        }
    }

    fun exportFile(shiftId: Long, action: Int) {
        viewModelScope.launch {
            Logs.d(TAG, "exportFile() called with: shiftId = $shiftId, action = $action")
            shiftExportUseCase.exportShift(shiftId, action)?.let { _intentFlow.emit(it) }
        }
    }

    fun printShift(shiftId: Long) {
        viewModelScope.launch (Dispatchers.IO) {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            shiftUseCase.printShift(trips)
        }
    }
}