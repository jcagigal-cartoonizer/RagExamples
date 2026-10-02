package ifac.td.taxi.compose.viewmodel
import  android.app.Application
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
// # Block 57-2: import android.app.Application
class AboutComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
) : AndroidViewModel(application) {
    private val TAG = "AboutViewModel"
    private val context get() = getApplication<Application>()
    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<AboutUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AboutUiEffect> = _effects.asSharedFlow()
    fun init() {
        val name = ApkUtils.getAppNameVersionDate(
            name = true, version = false, date = false, context = context
        )
        val version = ApkUtils.getAppNameVersionDate(
            name = false, version = true, date = false, context = context
        )
        val date = ApkUtils.getAppNameVersionDate(
            name = false, version = false, date = true, context = context
        )
        _uiState.update {
            it.copy(
                appInfo = context.getString(
                    ifac.td.taxi.R.string.app_info_format,
                    name, version, date
                ),
                buttons = AboutButtonsState(),
                privacyPolicyText = context.getString(ifac.td.taxi.R.string.privacy_policy)
            )
        }
        checkSavedDevice()
    }
    fun onAcceptClicked() {
        viewModelScope.launch {
            _effects.emit(AboutUiEffect.NavigateBack)
        }
    }
    fun onPrivacyClicked() {
        val privacyPolicyUrl = "https://www.taxitronic.com/en/privacy-policy-smart-td/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(dialog = AboutDialogState.Warning("No browser available to open this link"))
                }
                _effects.emit(AboutUiEffect.ShowWarningDialog)
            }
            Logs.d(TAG, "onPrivacyClicked: No browser available")
        }
    }
    fun dismissDialog() {
        _uiState.update { it.copy(dialog = AboutDialogState.Hidden) }
    }
    fun onLogoClicked() {
        val current = _uiState.value.logoTapCount + 1
        if (current >= 5) {
            _uiState.update { it.copy(logoTapCount = 0) }
            viewModelScope.launch {
                _effects.emit(AboutUiEffect.TriggerSecretTracking)
            }
        } else {
            _uiState.update { it.copy(logoTapCount = current) }
        }
    }
    fun checkSavedDevice() {
        viewModelScope.launch {
            val bluetooth: BluetoothInfo? = bluetoothLocalUseCase.getLocalBluetooth()
            if (bluetooth != null) {
                val deviceInfo = buildString {
                    appendLine("${bluetooth.name} [${bluetooth.btPIN}]")
                    val firmware = com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().taximeterVersionFirmware
                    val hardware = com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().taximeterVersionHardware
                    if (firmware.isNotEmpty() && hardware.isNotEmpty()) {
                        appendLine("$firmware - $hardware")
                    }
                    val answerK62 = com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().answerK62
                    if (answerK62.isNotEmpty()) appendLine(answerK62)
                }
                _uiState.update {
                    it.copy(
                        isBluetoothInfoVisible = true,
                        bluetoothInfoText = deviceInfo
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isBluetoothInfoVisible = false,
                        bluetoothInfoText = ""
                    )
                }
            }
        }
    }
}
