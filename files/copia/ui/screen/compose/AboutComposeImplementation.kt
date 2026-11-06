package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.location.AlfaLocation
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.util.ApkUtils
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// Shared state holder for the activity-viewmodel part
// ------------------------------------------------------------

data class SharedAboutState(
    val headerVisible: Boolean = false
)

// ------------------------------------------------------------
// UI state / events / effects
// ------------------------------------------------------------

data class AboutUiState(
    val appInfo: String = "",
    val privacyText: String = "",
    val buttons: AboutButtonsState = AboutButtonsState(),
)

data class AboutButtonsState(
    val accept: AboutButtonState = AboutButtonState(),
    val logo: AboutLogoState = AboutLogoState(),
    val bluetooth: AboutBluetoothState = AboutBluetoothState(),
    val warning: AboutWarningState = AboutWarningState(),
)

data class AboutButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val text: String = "Accept",
    val containerColor: Color = Color(0xFF1976D2),
    val contentColor: Color = Color.White,
)

data class AboutLogoState(
    val visible: Boolean = true,
    val tapCount: Int = 0,
)

data class AboutBluetoothState(
    val visible: Boolean = false,
    val bluetoothInfo: BluetoothInfo? = null,
    val text: String = "",
)

data class AboutWarningState(
    val shouldShow: Boolean = false,
    val messageRes: Int? = null,
)

sealed interface AboutUiEvent {
    data object OnScreenStarted : AboutUiEvent
    data object OnBackClicked : AboutUiEvent
    data object OnPrivacyClicked : AboutUiEvent
    data object OnLogoClicked : AboutUiEvent
    data object OnWarningConsumed : AboutUiEvent
}

sealed interface AboutUiEffect {
    data object NavigateBack : AboutUiEffect
    data object TriggerAlarmTracking : AboutUiEffect
    data class ShowToast(val messageRes: Int) : AboutUiEffect
    data class OpenUrl(val url: String) : AboutUiEffect
}

// ------------------------------------------------------------
// Reducer
// ------------------------------------------------------------

sealed interface AboutButtonsAction {
    data object OnAcceptClicked : AboutButtonsAction
    data object OnLogoClicked : AboutButtonsAction
    data class OnDeviceFlowChanged(
        val isDeviceAvailable: Boolean,
        val bluetoothInfo: BluetoothInfo?
    ) : AboutButtonsAction

    data object OnWarningFlowChanged : AboutButtonsAction
    data object OnWarningConsumed : AboutButtonsAction
}

fun AboutButtonsState.reduce(action: AboutButtonsAction): AboutButtonsState {
    return when (action) {
        AboutButtonsAction.OnAcceptClicked -> this

        AboutButtonsAction.OnLogoClicked -> {
            val nextCount = logo.tapCount + 1
            copy(
                logo = logo.copy(
                    tapCount = if (nextCount >= 5) 0 else nextCount
                )
            )
        }

        is AboutButtonsAction.OnDeviceFlowChanged -> {
            if (action.isDeviceAvailable && action.bluetoothInfo != null) {
                val taximeter = Taximeter.getInstance()
                val deviceInfo = buildString {
                    appendLine("${action.bluetoothInfo.name} [${action.bluetoothInfo.btPIN}]")
                    val firmware = taximeter.taximeterVersionFirmware
                    val hardware = taximeter.taximeterVersionHardware
                    if (firmware.isNotEmpty() && hardware.isNotEmpty()) {
                        appendLine("$firmware - $hardware")
                    }
                    val answerK62 = taximeter.answerK62
                    if (answerK62.isNotEmpty()) {
                        appendLine(answerK62)
                    }
                }

                copy(
                    bluetooth = bluetooth.copy(
                        visible = true,
                        bluetoothInfo = action.bluetoothInfo,
                        text = deviceInfo
                    )
                )
            } else {
                copy(
                    bluetooth = bluetooth.copy(
                        visible = false,
                        bluetoothInfo = null,
                        text = ""
                    )
                )
            }
        }

        AboutButtonsAction.OnWarningFlowChanged -> {
            copy(
                warning = warning.copy(
                    shouldShow = true,
                    messageRes = R.string.warning_not_browser
                )
            )
        }

        AboutButtonsAction.OnWarningConsumed -> {
            copy(
                warning = warning.copy(
                    shouldShow = false,
                    messageRes = null
                )
            )
        }
    }
}

// ------------------------------------------------------------
// Screen ViewModel
// ------------------------------------------------------------

class AboutComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
) : AndroidViewModel(application) {

    private val privacyPolicyUrl =
        "https://www.taxitronic.com/en/privacy-policy-smart-td/"

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AboutUiEffect>()
    val uiEffect: SharedFlow<AboutUiEffect> = _uiEffect.asSharedFlow()

    fun initialize() {
        loadAppInfo()
        checkSavedDevice()
    }

    fun onEvent(event: AboutUiEvent) {
        when (event) {
            AboutUiEvent.OnScreenStarted -> initialize()
            AboutUiEvent.OnBackClicked -> emitEffect(AboutUiEffect.NavigateBack)
            AboutUiEvent.OnPrivacyClicked -> clickPrivacy()
            AboutUiEvent.OnLogoClicked -> onLogoClicked()
            AboutUiEvent.OnWarningConsumed -> {
                _uiState.update { current ->
                    current.copy(buttons = current.buttons.reduce(AboutButtonsAction.OnWarningConsumed))
                }
            }
        }
    }

    private fun loadAppInfo() {
        val context = getApplication<Application>()
        val name = ApkUtils.getAppNameVersionDate(
            name = true,
            version = false,
            date = false,
            context = context
        )
        val version = ApkUtils.getAppNameVersionDate(
            name = false,
            version = true,
            date = false,
            context = context
        )
        val date = ApkUtils.getAppNameVersionDate(
            name = false,
            version = false,
            date = true,
            context = context
        )

        val appInfo = context.getString(R.string.app_info_format, name, version, date)
        _uiState.update {
            it.copy(
                appInfo = appInfo,
                privacyText = context.getString(R.string.privacy_policy)
            )
        }
    }

    private fun checkSavedDevice() {
        viewModelScope.launch {
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            val isDeviceAvailable = bluetooth != null
            _uiState.update { current ->
                current.copy(
                    buttons = current.buttons.reduce(
                        AboutButtonsAction.OnDeviceFlowChanged(
                            isDeviceAvailable = isDeviceAvailable,
                            bluetoothInfo = bluetooth
                        )
                    )
                )
            }
        }
    }

    private fun clickPrivacy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            getApplication<Application>().startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            _uiState.update { current ->
                current.copy(
                    buttons = current.buttons.reduce(AboutButtonsAction.OnWarningFlowChanged)
                )
            }
            emitEffect(AboutUiEffect.ShowToast(R.string.warning_not_browser))
        }
    }

    private fun onLogoClicked() {
        val nextState = _uiState.value.buttons.reduce(AboutButtonsAction.OnLogoClicked)
        _uiState.update { current -> current.copy(buttons = nextState) }

        if (nextState.logo.tapCount == 0) {
            emitEffect(AboutUiEffect.TriggerAlarmTracking)
        }
    }

    private fun emitEffect(effect: AboutUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}

// ------------------------------------------------------------
// Shared VM adapter for your existing MainActivityViewModel
// ------------------------------------------------------------

data class AboutSharedUiState(
    val headerVisible: Boolean = false
)

/**
 * Adapter around your real MainActivityViewModel.
 * Replace the property accessors below with your exact existing API names if needed.
 */
class AboutMainActivityViewModelAdapter(
    private val mainActivityViewModel: MainActivityViewModel
) {
    fun setHeaderVisible(visible: Boolean) {
        // adapt to your real MainActivityViewModel API
        mainActivityViewModel.showHeader(visible)
    }

    fun navigateBack(navController: NavController) {
        navController.popBackStack()
    }

    fun navigate(navController: NavController, directions: NavDirections) {
        navController.navigate(directions)
    }
}

// ------------------------------------------------------------
// Compose screen
// ------------------------------------------------------------

@Composable
fun AboutRoute(
    navController: NavController,
    viewModel: AboutComposeViewModel,
    mainActivityViewModel: MainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sharedAdapter = remember(mainActivityViewModel) {
        AboutMainActivityViewModelAdapter(mainActivityViewModel)
    }

    LaunchedEffect(Unit) {
        onShowHeader(false)
        sharedAdapter.setHeaderVisible(false)
        viewModel.initialize()
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                AboutUiEffect.NavigateBack -> {
                    val directions = AboutFragmentDirections.actionAboutFragmentToHomeFragment()
                    navController.navigate(directions)
                }

                is AboutUiEffect.ShowToast -> onShowToast(effect.messageRes)

                is AboutUiEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        onShowToast(R.string.warning_not_browser)
                    }
                }

                AboutUiEffect.TriggerAlarmTracking -> {
                    W2CLocation.setTrackingState(
                        AlfaLocation.ESTADO_SEGUIMIENTO_ALARMA,
                        "1"
                    )
                }
            }
        }
    }

    AboutScreen(
        state = uiState,
        onAcceptClick = { viewModel.onEvent(AboutUiEvent.OnBackClicked) },
        onPrivacyClick = { viewModel.onEvent(AboutUiEvent.OnPrivacyClicked) },
        onLogoClick = { viewModel.onEvent(AboutUiEvent.OnLogoClicked) },
    )
}

@Composable
fun AboutScreen(
    state: AboutUiState,
    onAcceptClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoClick: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.appInfo,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline)
                    ) {
                        append(state.privacyText.ifBlank { "Privacy Policy" })
                    }
                },
                modifier = Modifier.clickable { onPrivacyClick() }
            )

            if (state.buttons.bluetooth.visible) {
                Text(text = state.buttons.bluetooth.text)
            }

            if (state.buttons.accept.visible) {
                ContactAboutButton(
                    text = state.buttons.accept.text,
                    enabled = state.buttons.accept.enabled,
                    containerColor = state.buttons.accept.containerColor,
                    contentColor = state.buttons.accept.contentColor,
                    onClick = onAcceptClick
                )
            }

            if (state.buttons.logo.visible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { onLogoClick() }
                )
            }
        }
    }
}

// ------------------------------------------------------------
// Custom button matching your component style
// ------------------------------------------------------------

@Composable
fun ContactAboutButton(
    text: String,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}


