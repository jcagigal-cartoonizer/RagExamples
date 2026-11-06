package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.NavGraphDirections
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.monei.GeneratePaymentRequest
import ifac.td.taxi.domain.model.monei.InfoPayment
import ifac.td.taxi.domain.model.monei.MoneiPaymentInfo
import ifac.td.taxi.domain.usecase.MoneiUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.repository.connections.rest.invoice.Resource
import ifac.td.taxi.ui.model.SmartTDUIException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentMoneiViewModel(
    private val moneiUseCase: MoneiUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = this.javaClass.simpleName

    private val _actualStatusFlow = MutableStateFlow(StatusPayments.PENDING)
    val actualStatusFlow = _actualStatusFlow.asStateFlow()

    private val _generatePaymentFlow = MutableStateFlow<MoneiPaymentInfo?>(null)
    val generatePaymentFlow = _generatePaymentFlow.asStateFlow()

    private val _infoPaymentFlow = MutableStateFlow<InfoPayment?>(null)
    val infoPaymentFlow = _infoPaymentFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<SmartTDUIException>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    fun finishPayment(trip: Trip) {
        viewModelScope.launch {
            trip.moneiPaymentId = generatePaymentFlow.value?.id
            tripUseCase.endTrip(trip, PaymentMethod.MONEI)
            val status =
                shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
            status?.let { statusWithSubStatus ->
                shiftStatusUseCase.setStatus(statusWithSubStatus, true)
            }
            navigateTo(NavGraphDirections.goToHomeFragment())
        }
    }

    fun updateActualStatus(status: String){
        viewModelScope.launch {
            Logs.d(TAG, "updateActualStatus: ${StatusPayments.valueOf(status)}")
            _actualStatusFlow.emit(StatusPayments.valueOf(status))
        }
    }

    init {

    }

    fun initVM(totalAmount: Int, isoMoneda: String, orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = moneiUseCase.generatePayment(GeneratePaymentRequest(totalAmount, isoMoneda, orderId))) {
                is Resource.Success -> {
                    response.data?.let {
                        _generatePaymentFlow.emit(it)
                    }
                }
                is Resource.Error -> {
                    response.exception?.let {
                        _errorFlow.emit(SmartTDUIException.fromException(it))
                    }
                }
            }
        }
        runnable = object : Runnable {
            override fun run() {
                viewModelScope.launch(Dispatchers.IO) {
                    generatePaymentFlow.value?.id?.let { moneiPaymentInfoId ->
                        when(val response = moneiUseCase.getInfoPayment(moneiPaymentInfoId)) {
                            is Resource.Success -> {
                                response.data?.let {
                                    _infoPaymentFlow.emit(it)
                                }
                            }
                            is Resource.Error -> {
                                response.exception?.let {
                                    _errorFlow.emit(SmartTDUIException.fromException(it))
                                }
                            }
                        }
                    }

                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    enum class StatusPayments {
        PENDING,
        SUCCEEDED,
        FAILED,
        CANCELED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        AUTHORIZED,
        EXPIRED
    }

}
