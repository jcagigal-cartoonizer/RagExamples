package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.models.GPSData
import ifac.td.taxi.framework.util.Logs
import com.interfacom.sdk.taximeter.taximeter.BluetoothConstants
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GPSTestViewModel(
    context: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    private val sessionUseCase: SessionUseCase,
) : BaseViewModel(context) {

    private val TAG = "GPSTestViewModel"

    private val _gpsDataFlow = MutableStateFlow<GPSData?>(null)
    val gpsDataFlow = _gpsDataFlow.asStateFlow()

    private val _keyFlow = MutableSharedFlow<Boolean>()
    val keyFlow = _keyFlow.asSharedFlow()

    private val _alarmFlow = MutableSharedFlow<Boolean>()
    val alarmFlow = _alarmFlow.asSharedFlow()

    private val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    private val _externalGPSFlow = MutableStateFlow<Boolean?>(null)
    val externalGPSFlow = _externalGPSFlow.asStateFlow()

    var bluetoothInfo: BluetoothInfo? = null

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            viewModelScope.launch {
                _gpsDataFlow.emit(W2CLocation.getGPSData())
                if (Taximeter.isPanicButtonReceived() && Taximeter.isPanicButtonDetected()) {
                    _alarmFlow.emit(Taximeter.isPanicButtonOn())
                }
                if (Taximeter.isContactKeyReceived() && Taximeter.isContactKeyDetected()) {
                    _keyFlow.emit(Taximeter.isContactKeyOn())
                }
                checkContactKey()
                checkPanicButton()
            }
            handler.postDelayed(this, 1000)
        }
    }

    init {
        viewModelScope.launch {
            bluetoothLocalUseCase.getLocalBluetooth().let {
                bluetoothInfo = it
            }
        }
    }

    fun initViewModel() {
        handler.post(runnable)
    }

    fun openNavigatorApp() {
        viewModelScope.launch {
            navigatorUseCase.openNavigatorApp()?.let {
                _navigatorFlow.emit(it)
            }
        }
    }

    fun checkContactKey() {
        viewModelScope.launch {
            if (Taximeter.getInstance().bluetoothState == BluetoothConstants.BT_CONNECTED) {
                Logs.d(TAG, "Taximeter Bluetooth Connected")
                if (Taximeter.isContactKeyReceived() && Taximeter.isContactKeyDetected()) {
                    val isKeyOn = Taximeter.isContactKeyOn()
                    Logs.d(TAG, "Contact key is on: $isKeyOn")
                    _keyFlow.emit(isKeyOn)
                } else {
                    Logs.d(TAG, "Contact key conditions not met")
                }
            } else {
                Logs.d(TAG, "Taximeter Bluetooth Disconnected")
            }
        }
    }

    fun checkPanicButton() {
        viewModelScope.launch {
            if (Taximeter.getInstance().bluetoothState == BluetoothConstants.BT_CONNECTED) {
                Logs.d(TAG, "Taximeter Bluetooth Connected")
                val isPanicButtonOn = Taximeter.isPanicButtonOn() // Capture state for logging
                if (Taximeter.isPanicButtonReceived() && Taximeter.isPanicButtonDetected()) {
                    Logs.d(TAG, "Panic button status: $isPanicButtonOn")
                    _alarmFlow.emit(isPanicButtonOn)
                } else {
                    Logs.d(TAG, "Panic button conditions not met.")
                }
            } else {
                Logs.d(TAG, "Taximeter Bluetooth Disconnected")
            }
        }
    }

    fun checkExternalGPS() {
        viewModelScope.launch {
            val useExternalGPS = sessionUseCase.getUseExternalGPS()
            Logs.d(TAG, "checkExternalGPS: Use external GPS: $useExternalGPS")
            _externalGPSFlow.emit(useExternalGPS == true)
        }
    }

    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }
}