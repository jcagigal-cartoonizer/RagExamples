package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.BuildConfig
import ifac.td.taxi.domain.model.PortugalModel
import ifac.td.taxi.domain.usecase.PortugalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PortugalSettingsViewModel(
    context: Application,
    private val portugalUseCase: PortugalUseCase
) : BaseViewModel(context) {

    val TAG = this.javaClass.simpleName

    private val _portugalDataFlow = MutableStateFlow<PortugalModel?>(null)
    val portugalDataFlow = _portugalDataFlow.asStateFlow()

    private val _backDataFlow = MutableSharedFlow<Boolean>()
    val backDataFlow = _backDataFlow.asSharedFlow()

    private val _pinCallbackFlow = MutableSharedFlow<Boolean>()
    val pinCallbackFlow = _pinCallbackFlow.asSharedFlow()

    fun getPortugalData() {
        viewModelScope.launch(Dispatchers.IO) {
            _portugalDataFlow.emit(portugalUseCase.getPortugalData())
        }
    }

    fun updatePortugalData(
        atcud: String,
        sequenceNumber: Int,
        document: Int,
        portugalPassword: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val portugalData = portugalUseCase.getPortugalData()

            val portugalModel = PortugalModel(
                atcud = atcud,
                sequenceNumber = sequenceNumber,
                numTicketPrint = document,
                portugalPassword = portugalData?.portugalPassword ?: portugalPassword,
            )

            portugalUseCase.updatePortugalData(portugalModel)
            _backDataFlow.emit(true)
        }
    }

    fun updatePortugalPin(portugalPassword: String) {
        viewModelScope.launch {
            val portugalData = portugalUseCase.getPortugalData()
            portugalData?.let {
                portugalData.portugalPassword = portugalData.encryptPortugalPassword(portugalPassword)
                portugalUseCase.updatePortugalData(portugalData)
            }
        }
    }

    fun checkPinPortugal(editTextString: String?) {
        viewModelScope.launch {
            try {
                val pin = editTextString?.toInt()
                val portugalCode = BuildConfig.portugal_p.toInt()
                val portugalPassword = portugalUseCase.getPortugalData()
                if (portugalPassword != null && portugalPassword.portugalPassword.isNotBlank()) {
                    _pinCallbackFlow.emit(portugalPassword.encryptPortugalPassword(pin.toString()) == portugalPassword.portugalPassword)
                } else {
                    _pinCallbackFlow.emit(pin == portugalCode)
                }
            } catch (e: Exception) {
                Logs.d(TAG, "checkPinPortugal: $e")
            }
        }
    }
}
