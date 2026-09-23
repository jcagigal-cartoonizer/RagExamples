package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.GPSTestUiState
import ifac.td.taxi.ui.screen.components.GPSTestUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 118-4: import android.app.Application
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
