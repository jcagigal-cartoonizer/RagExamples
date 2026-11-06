package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.repository.room.dao.ShiftDao.ShiftStatistics
import ifac.td.taxi.repository.room.dao.TripDao.TotalAmountByDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val tripUseCase: TripUseCase,
    private val shiftUseCase: ShiftUseCase,
    context: Application,
) :
    BaseViewModel(context) {

    private val _datesFlow = MutableStateFlow<List<String>?>(null)
    val datesFlow = _datesFlow.asStateFlow()

    private val _totalFlow = MutableStateFlow<List<Double>?>(null)
    val totalFlow = _totalFlow.asStateFlow()

    private val _billingValuesFlow = MutableStateFlow<List<TotalAmountByDate>?>(null)
    val billingValuesFlow = _billingValuesFlow.asStateFlow()

    private val _timeValuesFlow = MutableStateFlow<List<ShiftStatistics>?>(null)
    val timeValuesFlow = _timeValuesFlow.asStateFlow()

    fun get7LastDays() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerDayLast7Days()
            _billingValuesFlow.emit(values)
        }
    }

    fun get30LastDays() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerDayLast30Days()
            _billingValuesFlow.emit(values)
        }
    }

    fun get12LastMonths() {
        viewModelScope.launch {
            val values = tripUseCase.getTotalAmountPerMonthLast12Months()
            _billingValuesFlow.emit(values)
        }
    }

    fun getTimeLast7Days() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast7Days()
            _timeValuesFlow.emit(values)
        }
    }

    fun getTimeLast30Days() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast30Days()
            _timeValuesFlow.emit(values)
        }
    }

    fun getTimeLast12Months() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = shiftUseCase.getTimePerMonthLast12Months()
            _timeValuesFlow.emit(values)
        }
    }
}