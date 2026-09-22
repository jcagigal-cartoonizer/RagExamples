package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.models.GPSData
import com.interfacom.sdk.taximeter.log.Log as SdkLog
import com.interfacom.sdk.taximeter.taximeter.BluetoothConstants
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
class GPSTestComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    private val sessionUseCase: SessionUseCase,
) : AndroidViewModel(application) {
    private val TAG = "GPSTestViewModel"
    private val _uiState = MutableStateFlow(GPSTestUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<GPSTestUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    var bluetoothInfo: BluetoothInfo? = null
        private set
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            viewModelScope.launch {
                val gpsData: GPSData? = W2CLocation.getGPSData()
                updateGpsData(gpsData)
                if (Taximeter.isPanicButtonReceived() && Taximeter.isPanicButtonDetected()) {
                    _uiState.update { it.copy(alarmActive = Taximeter.isPanicButtonOn()) }
                }
                if (Taximeter.isContactKeyReceived() && Taximeter.isContactKeyDetected()) {
                    _uiState.update { it.copy(keyActive = Taximeter.isContactKeyOn()) }
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
    fun onAcceptClicked() {
        viewModelScope.launch {
            _uiEffect.emit(GPSTestUiEffect.NavigateBack)
        }
    }
    fun onGpsClicked() {
        viewModelScope.launch {
            navigatorUseCase.openNavigatorApp()?.let { intent ->
                _uiEffect.emit(GPSTestUiEffect.LaunchIntent(intent))
            }
        }
    }
    fun checkExternalGPS() {
        viewModelScope.launch {
            val useExternalGPS = sessionUseCase.getUseExternalGPS()
            Logs.d(TAG, "checkExternalGPS: Use external GPS: $useExternalGPS")
            _uiState.update {
                it.copy(useExternalGps = useExternalGPS == true)
            }
        }
    }
    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }
    fun updateGpsData(gpsData: GPSData?) {
        if (gpsData == null) return
        val gpsType = getApplication<Application>().getString(ifac.td.taxi.R.string.strGPSAndroid)
        val useExternalGps = _uiState.value.useExternalGps == true
        val replacingGps = W2CLocation.isReplacingGPS()
        val bluetoothName = bluetoothInfo?.name
        val showExternalBlocks = useExternalGps
        val showSpeedAndAngle = useExternalGps && _uiState.value.isDebug
        val gpsTypeText = when {
            !useExternalGps -> gpsType
            replacingGps -> gpsType
            !bluetoothName.isNullOrBlank() -> bluetoothName
            else -> gpsType
        }
        _uiState.update {
            it.copy(
                gpsType = gpsTypeText,
                utcTime = gpsData.utcTime,
                satellites = gpsData.satellites.toString(),
                power = if (W2CLocation.get_tipoGps() == "S") {
                    "dB [max ${W2CLocation.getMaxMediaSNR()}] ${W2CLocation.getMediaSNR()}"
                } else {
                    ""
                },
                hdop = String.format(Locale.US, "%d", gpsData.hdop.toInt()),
                latitude = gpsData.latitude,
                longitude = gpsData.longitude,
                speed = gpsData.speed,
                heading = "${gpsData.heading}°",
                showSpeedAndAngle = showSpeedAndAngle,
                showPower = W2CLocation.get_tipoGps() == "S",
                showExternalGpsBlocks = showExternalBlocks,
                isReplacingGps = replacingGps,
                isDebug = SdkLog.is_debug(),
                bluetoothName = bluetoothName
            )
        }
    }
    fun checkContactKey() {
        viewModelScope.launch {
            if (Taximeter.getInstance().bluetoothState == BluetoothConstants.BT_CONNECTED) {
                Logs.d(TAG, "Taximeter Bluetooth Connected")
                if (Taximeter.isContactKeyReceived() && Taximeter.isContactKeyDetected()) {
                    val isKeyOn = Taximeter.isContactKeyOn()
                    _uiState.update { it.copy(keyActive = isKeyOn) }
                }
            }
        }
    }
    fun checkPanicButton() {
        viewModelScope.launch {
            if (Taximeter.getInstance().bluetoothState == BluetoothConstants.BT_CONNECTED) {
                Logs.d(TAG, "Taximeter Bluetooth Connected")
                if (Taximeter.isPanicButtonReceived() && Taximeter.isPanicButtonDetected()) {
                    _uiState.update { it.copy(alarmActive = Taximeter.isPanicButtonOn()) }
                }
            }
        }
    }
}
