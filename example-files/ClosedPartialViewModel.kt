package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.PartialModel
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClosedPartialViewModel(
    context: Application,
    private val printerUseCase: PrinterUseCase,
    private val partialUseCase: PartialUseCase,
    private val taximeterUseCase: TaximeterConnectUseCase,
) : BaseViewModel(context) {

    private val TAG = "ClosedPartialViewModel"

    private val _partialFlow = MutableStateFlow<PartialModel?>(null)
    val partialFlow = _partialFlow.asStateFlow()

    private val _isTaximeterConnectedFlow = MutableStateFlow<Boolean>(false)
    val isTaximeterConnectedFlow = _isTaximeterConnectedFlow.asStateFlow()

    fun getClosedPartial() {
        Logs.d(TAG, "getClosedPartial: Fetching closed partial from useCase")
        viewModelScope.launch {
            val result = partialUseCase.getClosedPartial()
            Logs.d(TAG, "getClosedPartial: Result obtained. emitting...")
            _partialFlow.emit(result)
        }
    }

    fun manageBack(justClosed: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "manageBack: justClosed=$justClosed")
            if (justClosed) {
                Logs.d(TAG, "manageBack: Navigating back twice (closing flow)")
                navigateBack()
                navigateBack()
            } else {
                Logs.d(TAG, "manageBack: Navigating back once")
                navigateBack()
            }
        }
    }

    fun printPartial() {
        viewModelScope.launch {
            val partial = partialFlow.value
            if (partial != null) {
                Logs.d(TAG, "printPartial: Printing closed partial")
                printerUseCase.printMessage(header = null, message = partial.bufLastTicketCierre.addTicketLines(), paperFeed = false )
            } else {
                Logs.d(TAG, "printPartial: Failed - No partial data available to print")
            }
        }
    }

    fun checkTaximeterStatus() {
        viewModelScope.launch {
            Logs.d(TAG, "checkTaximeterStatus: Checking taximeter status")
            val isTaximeterConnected = taximeterUseCase.isTaximeterConnected()
            Logs.d(TAG, "checkTaximeterStatus: isTaximeterConnected=$isTaximeterConnected")
            _isTaximeterConnectedFlow.emit(isTaximeterConnected)
        }
    }
}