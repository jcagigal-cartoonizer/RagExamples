package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.util.ApkUtils
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class AboutComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase
) : AndroidViewModel(application) {
    private val TAG = "AboutViewModel"
    private val context get() = getApplication<Application>()
    private val _uiState = MutableStateFlow(
        AboutUiState(
            appInfo = buildAppInfo(),
            privacyPolicyText = "Privacy Policy",
            bluetoothInfoText = "",
            showBluetoothInfo = false,
            buttonsState = AboutButtonsState.default()
        )
    )
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<AboutUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AboutUiEffect> = _effects.asSharedFlow()
    private var logoTapCounter = 0
    init {
        checkSavedDevice()
    }
    fun onEvent(event: AboutUiEvent) {
        when (event) {
            AboutUiEvent.ClickPrivacy -> clickPrivacy()
            AboutUiEvent.ClickAccept -> emitEffect(AboutUiEffect.NavigateBack)
            AboutUiEvent.LogoTapped -> onLogoTapped()
            AboutUiEvent.DismissWarningDialog -> { /* handled in UI */ }
        }
    }
    fun onLogoTapped() {
        logoTapCounter++
        Logs.d(TAG, "logoTapped count=$logoTapCounter")
        if (logoTapCounter == 5) {
            logoTapCounter = 0
        }
    }
    fun clickPrivacy() {
        val privacyPolicyUrl = "https://www.taxitronic.com/en/privacy-policy-smart-td/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            viewModelScope.launch {
                _effects.emit(AboutUiEffect.ShowWarningDialog)
            }
            Logs.d(TAG, "clickPrivacy: No browser available")
        }
    }
    fun checkSavedDevice() {
        viewModelScope.launch {
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            val isDeviceAvailable = bluetooth != null
            val deviceInfoText = if (isDeviceAvailable && bluetooth != null) {
                buildBluetoothInfoText(bluetooth)
            } else {
                ""
            }
            _uiState.update {
                it.copy(
                    showBluetoothInfo = isDeviceAvailable && bluetooth != null,
                    bluetoothInfoText = deviceInfoText,
                    buttonsState = it.buttonsState.copy(
                        accept = it.buttonsState.accept.copy(
                            visible = true,
                            enabled = true
                        )
                    )
                )
            }
        }
    }
    fun buildBluetoothInfoText(bluetoothInfo: BluetoothInfo): String {
        return buildString {
            appendLine("${bluetoothInfo.name} [${bluetoothInfo.btPIN}]")
            val firmware = runCatching { com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().taximeterVersionFirmware }.getOrDefault("")
            val hardware = runCatching { com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().taximeterVersionHardware }.getOrDefault("")
            if (firmware.isNotEmpty() && hardware.isNotEmpty()) {
                appendLine("$firmware - $hardware")
            }
            val answerK62 = runCatching { com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().answerK62 }.getOrDefault("")
            if (answerK62.isNotEmpty()) {
                appendLine(answerK62)
            }
        }.trim()
    }
    fun buildAppInfo(): String {
        val name = ApkUtils.getAppNameVersionDate(
            name = true, version = false, date = false, context = context
        )
        val version = ApkUtils.getAppNameVersionDate(
            name = false, version = true, date = false, context = context
        )
        val date = ApkUtils.getAppNameVersionDate(
            name = false, version = false, date = true, context = context
        )
        return "App: $name | $version | $date"
    }
    fun emitEffect(effect: AboutUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
}
