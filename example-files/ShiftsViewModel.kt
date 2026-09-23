package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.ShiftExportUseCase
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.ShiftEntity
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import ifac.td.taxi.viewmodel.model.SortState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShiftsViewModel(
    context: Application,
    private val shiftUseCase: ShiftUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftExportUseCase: ShiftExportUseCase,
) :
    BaseViewModel(context) {

    val _shiftFlow = MutableStateFlow<List<ShiftEntity>>(emptyList())
    val shiftFlow = _shiftFlow.asStateFlow()

    private val _loadingFlow = MutableSharedFlow<Boolean>()
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _hasShiftsFlow = MutableSharedFlow<Boolean>()
    val hasShiftsFlow = _hasShiftsFlow.asSharedFlow()

    private val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow = _intentFlow.asSharedFlow()

    private var isAscending = false
    private val pageSize = 25

    private val TAG = "ShiftsViewModel"
    var currentShifts = _shiftFlow.value.toMutableList()

    fun addShifts(page: Int) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            val shifts = shiftUseCase.getShiftsPaged(pageSize, pageSize * page) ?: emptyList()
            currentShifts.addAll(shifts)
            if (currentSortState.option != ShiftOrderOptions.NONE && currentSortState.isAscending != null) {
                sortShiftsBy(currentShifts, currentSortState.option, currentSortState.isAscending!!)
            } else {
                _shiftFlow.emit(currentShifts.toList())
            }
            _loadingFlow.emit(false)
        }
    }

    var currentSortState = SortState(ShiftOrderOptions.NONE)

    fun sortShiftsBy(
        shifts: List<ShiftEntity>?,
        orderOptions: ShiftOrderOptions,
        isAscending: Boolean
    ) {
        viewModelScope.launch {
            if (shifts == null) return@launch

            currentShifts = when (orderOptions) {
                ShiftOrderOptions.ID -> {
                    if (isAscending) {
                        ArrayList(shifts.sortedBy { it.id })
                    } else {
                        ArrayList(shifts.sortedByDescending { it.id })
                    }
                }

                ShiftOrderOptions.AMOUNT -> {
                    if (isAscending) {
                        ArrayList(shifts.sortedBy { it.amount })
                    } else {
                        ArrayList(shifts.sortedByDescending { it.amount })
                    }
                }

                else -> ArrayList(shifts)
            }

            _shiftFlow.emit(currentShifts)
        }
    }

    fun deleteShift(shiftEntity: ShiftEntity) {
        viewModelScope.launch {
            currentShifts.remove(shiftEntity)
            shiftUseCase.deleteShift(shiftEntity)
        }
    }

    fun exportShifts(shiftList: Set<ShiftEntity>) {
        viewModelScope.launch {
            Logs.d(TAG, "exportShifts: $shiftList")
            val shiftIds = shiftList.map { it.id }
            shiftExportUseCase.exportMultipleShifts(shiftIds)?.let { _intentFlow.emit(it) }
        }
    }

    fun checkShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getShiftCount()
            Logs.d("ShiftsViewModel", "shifts: $shifts")
            _hasShiftsFlow.emit((shifts != null) && (shifts > 0L))
        }
    }
}