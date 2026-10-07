package ifac.td.taxi.compose.viewmodel
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.GPSTestViewModel
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.GPSTestScreen
// # Block 79-2: import android.app.Application
class GPSTestComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    private val sessionUseCase: SessionUseCase,
) : AndroidViewModel(application) {
    private val handler = Handler(Looper.getMainLooper())
    lateinit var buttonsState : GPSTestButtonsState
    lateinit var dialogState : GPSTestDialogState
    private val _uiState = MutableStateFlow(GPSTestUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<GPSTestUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    var bluetoothInfo: BluetoothInfo? = null
        private set
    private val runnable = object : Runnable {
        override fun run() {
            viewModelScope.launch {
                val gpsData: GPSData? = W2CLocation.getGPSData()
                val useExternalGPS = sessionUseCase.getUseExternalGPS() == true
                val isReplacing = W2CLocation.isReplacingGPS()
                val gpsType = context.getString(
                    ifac.td.taxi.R.string.strGPSAndroid
                )
                bluetoothInfo?.let { info ->
                    _uiState.update {
                        it.copy(
                            bluetoothName = info.name
                        )
                    }
                }
                val updated = _uiState.value.copy(
                    isExternalGPS = useExternalGPS,
                    isReplacingGPS = isReplacing,
                    gpsType = if (useExternalGPS) {
                        if (isReplacing) gpsType else (bluetoothInfo?.name ?: gpsType)
                    } else {
                        gpsType
                    }
                )
                _uiState.value = updated.copy(
                    buttons = buildButtonsState(updated)
                )
                gpsData?.let {
                    _uiState.update { state ->
                        state.copy(
                            utcTime = it.utcTime,
                            satellites = it.satellites.toString(),
                            power = if (W2CLocation.get_tipoGps() == "S") {
                                "dB [max ${W2CLocation.getMaxMediaSNR()}] ${W2CLocation.getMediaSNR()}"
                            } else {
                                ""
                            },
                            hdop = String.format(Locale.US, "%d", it.hdop.toInt()),
                            latitude = it.latitude,
                            longitude = it.longitude,
                            speed = if (state.isDebug) it.speed else "",
                            heading = if (state.isDebug) "${it.heading}°" else "",
                            buttons = buildButtonsState(state.copy(
                                utcTime = it.utcTime,
                                satellites = it.satellites.toString()
                            ))
                        )
                    }
                }
                updateKeysAndAlarm()
            }
            handler.postDelayed(this, 1000)
        }
    }
    init {
        dialogState = _uiState.value.dialog
        viewModelScope.launch {
            bluetoothLocalUseCase.getLocalBluetooth().let {
                bluetoothInfo = it
                _uiState.update { s ->
                    s.copy(
                        bluetoothName = it?.name,
                        buttons = buildButtonsState(s.copy(bluetoothName = it?.name))
                    )
                }
            }
        }
    }
    fun initViewModel() {
        handler.post(runnable)
    }
    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }
    fun checkExternalGPS() {
        viewModelScope.launch {
            val useExternalGPS = sessionUseCase.getUseExternalGPS() == true
            _uiState.update {
                it.copy(
                    isExternalGPS = useExternalGPS,
                    buttons = buildButtonsState(it.copy(isExternalGPS = useExternalGPS))
                )
            }
        }
    }
    fun onAcceptClicked() {
        viewModelScope.launch {
            _effects.emit(GPSTestUiEffect.NavigateBack)
        }
    }
    fun onGpsClicked() {
        viewModelScope.launch {
            navigatorUseCase.openNavigatorApp()?.let { intent ->
                _effects.emit(GPSTestUiEffect.LaunchIntent(intent))
            }
        }
    }
    fun onDialogDismiss() {
        viewModelScope.launch {
            _uiState.update { it.copy(dialog = GPSTestDialogState.Hidden) }
            _effects.emit(GPSTestUiEffect.DismissDialog)
        }
    }
    fun onRequestExitDialog() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(dialog = GPSTestDialogState.ConfirmExit(
                    title = "Confirm",
                    message = "Do you want to exit?"
                ))
            }
            _effects.emit(
                GPSTestUiEffect.ShowDialog(
                    GPSTestDialogState.ConfirmExit("Confirm", "Do you want to exit?")
                )
            )
        }
    }
    fun updateKeysAndAlarm() {
        viewModelScope.launch {
            val alarm = Taximeter.isPanicButtonReceived() &&
                Taximeter.isPanicButtonDetected() &&
                Taximeter.isPanicButtonOn()
            val key = Taximeter.isContactKeyReceived() &&
                Taximeter.isContactKeyDetected() &&
                Taximeter.isContactKeyOn()
            _uiState.update { s ->
                s.copy(
                    alarmActive = alarm,
                    keyActive = key,
                    buttons = buildButtonsState(s.copy(alarmActive = alarm, keyActive = key))
                )
            }
        }
    }
    fun buildButtonsState(state: GPSTestUiState): GPSTestButtonsState {
        return GPSTestButtonsState(
            accept = GPSTestCustomDialogButtonVisualState(
                visible = true,
                enabled = true,
                backgroundColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                contentColor = androidx.compose.ui.graphics.Color.White,
                text = "Accept"
            ),
            gps = GPSTestCustomDialogButtonVisualState(
                visible = true,
                enabled = true,
                backgroundColor = androidx.compose.ui.graphics.Color(0xFF1565C0),
                contentColor = androidx.compose.ui.graphics.Color.White,
                text = "GPS"
            )
        )
    }
}
