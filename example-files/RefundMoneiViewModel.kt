package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.monei.InfoPayment
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

class RefundMoneiViewModel(
    private val moneiUseCase: MoneiUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = this.javaClass.simpleName

    private val _actualStatusFlow = MutableStateFlow(PaymentMoneiViewModel.StatusPayments.SUCCEEDED)
    val actualStatusFlow = _actualStatusFlow.asStateFlow()

    private val _infoPaymentFlow = MutableStateFlow<InfoPayment?>(null)
    val infoPaymentFlow = _infoPaymentFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<SmartTDUIException>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _tripFlow = MutableStateFlow<Trip?>(null)
    val tripFlow = _tripFlow.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    fun updateActualStatus(status: String) {
        viewModelScope.launch {
            Logs.d(
                TAG,
                "updateActualStatus: ${PaymentMoneiViewModel.StatusPayments.valueOf(status)}"
            )
            _actualStatusFlow.emit(PaymentMoneiViewModel.StatusPayments.valueOf(status))
        }
    }

    fun startRunnable(tripId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = tripUseCase.getTripById(tripId)
            trip?.moneiPaymentId?.let {
                _tripFlow.emit(trip)
                runnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch(Dispatchers.IO) {
                            when (val response = moneiUseCase.getInfoPayment(it)) {
                                is Resource.Success -> {
                                    response.data?.let {
                                        _infoPaymentFlow.emit(it)
                                        if (it.status == PaymentMoneiViewModel.StatusPayments.REFUNDED.name) {
                                            handler.removeCallbacks(runnable)
                                        }
                                    }
                                }

                                is Resource.Error -> {
                                    response.exception?.let {
                                        _errorFlow.emit(SmartTDUIException.fromException(it))
                                    }
                                }
                            }
                        }


                        handler.postDelayed(this, 3000)
                    }
                }
                handler.post(runnable)
            }
        }

    }

    fun refund() {
        viewModelScope.launch(Dispatchers.IO) {
            tripFlow.value?.let { trip ->
                trip.moneiPaymentId?.let { paymentId ->
                    when (val response = moneiUseCase.refundPayment(paymentId, trip.totalAmount)) {
                        is Resource.Success -> {
                            response.data?.let {
                                handler.removeCallbacks(runnable)
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
        }
    }


}
