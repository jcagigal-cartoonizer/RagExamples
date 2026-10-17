package ifac.td.taxi.ui.screen.compose
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import android.content.DialogInterface
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.ArrowDropUp
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.model.UserPreferences
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.unit.dp
import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import androidx.compose.material3.Icon
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import ifac.td.taxi.ui.model.ColorPickerEnum
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.rememberScrollState
import android.widget.AdapterView
import androidx.compose.material3.SelectableChipColors
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.background
import ifac.td.taxi.domain.usecase.ShiftUseCase
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.navigationBars
import android.media.RingtoneManager
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import ifac.td.taxi.framework.sdk.usecase.SendLogsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import android.app.DatePickerDialog
import kotlinx.coroutines.flow.asSharedFlow
import ifac.td.taxi.framework.util.Logs
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Box
import ifac.td.taxi.domain.usecase.IngenicoUseCase
import androidx.compose.foundation.layout.safeDrawing
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.navigation.NavController
import android.view.View
import androidx.compose.foundation.layout.WindowInsetsSides
import ifac.td.taxi.framework.PermissionRequest
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.setValue
import android.content.Intent
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.ArrowDropDown
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import kotlinx.coroutines.delay
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import androidx.compose.foundation.verticalScroll
import android.provider.Settings
import androidx.compose.ui.Alignment
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.FlowRow
import android.app.Application
import ifac.td.taxi.BuildConfig
import androidx.compose.runtime.collectAsState as collectAsStateDeprecated
import android.content.res.Resources
import androidx.navigation.NavDeepLinkRequest
import androidx.core.net.toUri
import android.app.Activity
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


data class UserPreferencesUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val buttonsState: UserPreferencesButtonsState = UserPreferencesButtonsState(),
    val isLoggedIn: Boolean? = null,
    val showTimeControl: Boolean = false,
    val showLightOffOnDispatched: Boolean = false,
    val showFiscalPortugalButton: Boolean = false,
    val showPinPadPrinter: Boolean = false,
    val showChangePinButton: Boolean = true,
    val showUploadDriversPhotoButton: Boolean = true,
    val showCustomPaymentTimer: Boolean = false,
    val isRedSysConfigured: Boolean = false,
    val isSoundExpanded: Boolean = false,
    val isInvoicesExpanded: Boolean = false,
    val isPinPadExpanded: Boolean = false,
    val isShiftsExpanded: Boolean = false,
    val isSystemExpanded: Boolean = false,
    val isLocationExpanded: Boolean = false,
    val pendingDialog: UserPreferencesDialogState? = null,
)

data class UserPreferencesButtonsState(
    val cancel: UserPreferencesButtonVisualState = UserPreferencesButtonVisualState(
        type = ButtonType.CANCEL,
        enabled = true,
        visible = true,
        backgroundColor = UserPreferencesButtonBackground.RED,
        text = "CANCEL",
    ),
    val accept: UserPreferencesButtonVisualState = UserPreferencesButtonVisualState(
        type = ButtonType.ACCEPT,
        enabled = true,
        visible = true,
        backgroundColor = UserPreferencesButtonBackground.GREEN,
        text = "ACCEPT",
    ),
)

data class UserPreferencesButtonVisualState(
    val type: ButtonType,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val backgroundColor: UserPreferencesButtonBackground = UserPreferencesButtonBackground.DEFAULT,
    val text: String? = null,
    val iconRes: Int? = null,
)

enum class UserPreferencesButtonBackground {
    GREEN, RED, BLUE, YELLOW, ORANGE, DEFAULT, BLACK, TRANSPARENT
}

data class UserPreferencesDialogState(
    val title: String,
    val description: String? = null,
    val hint: String? = null,
    val isPinInput: Boolean = false,
    val buttons: List<ButtonType> = emptyList(),
    val editText: String = "",
)

sealed interface UserPreferencesUiEvent {
    data object OnCancelClicked : UserPreferencesUiEvent
    data object OnAcceptClicked : UserPreferencesUiEvent
    data object OnSoundCardClicked : UserPreferencesUiEvent
    data object OnInvoicesCardClicked : UserPreferencesUiEvent
    data object OnPinPadCardClicked : UserPreferencesUiEvent
    data object OnShiftsCardClicked : UserPreferencesUiEvent
    data object OnSystemCardClicked : UserPreferencesUiEvent
    data object OnLocationCardClicked : UserPreferencesUiEvent
    data object OnFiscalPortugalClicked : UserPreferencesUiEvent
    data object OnSecurePinClicked : UserPreferencesUiEvent
    data object OnSendLogsClicked : UserPreferencesUiEvent
    data object OnInfoInvoicesClicked : UserPreferencesUiEvent
    data object OnSelectDateEraseShiftsClicked : UserPreferencesUiEvent
    data object OnChangePinClicked : UserPreferencesUiEvent
    data object OnUploadDriversPhotoClicked : UserPreferencesUiEvent
    data object OnNotificationSoundClicked : UserPreferencesUiEvent
    data object OnIncomingSoundClicked : UserPreferencesUiEvent
    data class OnDialogButtonClicked(val button: ButtonType, val input: String? = null) : UserPreferencesUiEvent
    data class OnSpinnerChanged(val key: String, val position: Int, val id: Long) : UserPreferencesUiEvent
    data class OnCheckboxChanged(val key: String, val checked: Boolean) : UserPreferencesUiEvent
    data class OnTextChanged(val key: String, val value: String) : UserPreferencesUiEvent
    data class OnRingtonePicked(val type: Int, val uri: Uri?, val title: String?) : UserPreferencesUiEvent
    data class OnPermissionResult(val permission: String, val granted: Boolean) : UserPreferencesUiEvent
}

sealed interface UserPreferencesUiEffect {
    data object NavigateBack : UserPreferencesUiEffect
    data object NavigateToSecurePin : UserPreferencesUiEffect
    data object NavigateToPortugalSettings : UserPreferencesUiEffect
    data object RequestBluetoothPermission : UserPreferencesUiEffect
    data object RequestPhonePermission : UserPreferencesUiEffect
    data object RequestOverlayPermission : UserPreferencesUiEffect
    data object LaunchRingtonePicker : UserPreferencesUiEffect
    data object LaunchNotificationPicker : UserPreferencesUiEffect
    data object RefreshCabId : UserPreferencesUiEffect
    data object KeepScreenActive : UserPreferencesUiEffect
    data class ShowToast(val resId: Int) : UserPreferencesUiEffect
    data class OpenDialog(val dialog: UserPreferencesDialogState) : UserPreferencesUiEffect
    data object CloseDialog : UserPreferencesUiEffect
}

sealed interface UserPreferencesFragmentBranch {
    data object ShiftsEmpty : UserPreferencesFragmentBranch
    data object TaximeterSkyGlassEnabled : UserPreferencesFragmentBranch
    data object TaximeterSkyGlassDisabled : UserPreferencesFragmentBranch
    data object LightOffOnDispatchedEnabled : UserPreferencesFragmentBranch
    data object LightOffOnDispatchedDisabled : UserPreferencesFragmentBranch
    data object UserNotLoggedIn : UserPreferencesFragmentBranch
    data object FiscalLicensingEnabled : UserPreferencesFragmentBranch
    data object FiscalLicensingDisabled : UserPreferencesFragmentBranch
    data object IngenicoInstalled : UserPreferencesFragmentBranch
    data object IngenicoNotInstalled : UserPreferencesFragmentBranch
    data object DriverTurnModeZero : UserPreferencesFragmentBranch
    data object RequestCurrentSecurePin : UserPreferencesFragmentBranch
    data object WrongSecurePin : UserPreferencesFragmentBranch
}

fun UserPreferencesButtonsState.reduceFromFragmentBranch(
    branch: UserPreferencesFragmentBranch,
): UserPreferencesButtonsState {
    return when (branch) {
        UserPreferencesFragmentBranch.ShiftsEmpty -> copy()
        UserPreferencesFragmentBranch.TaximeterSkyGlassEnabled -> copy()
        UserPreferencesFragmentBranch.TaximeterSkyGlassDisabled -> copy()
        UserPreferencesFragmentBranch.LightOffOnDispatchedEnabled -> copy()
        UserPreferencesFragmentBranch.LightOffOnDispatchedDisabled -> copy()
        UserPreferencesFragmentBranch.UserNotLoggedIn -> copy()
        UserPreferencesFragmentBranch.FiscalLicensingEnabled -> copy()
        UserPreferencesFragmentBranch.FiscalLicensingDisabled -> copy()
        UserPreferencesFragmentBranch.IngenicoInstalled -> copy()
        UserPreferencesFragmentBranch.IngenicoNotInstalled -> copy()
        UserPreferencesFragmentBranch.DriverTurnModeZero -> copy()
        UserPreferencesFragmentBranch.RequestCurrentSecurePin -> copy()
        UserPreferencesFragmentBranch.WrongSecurePin -> copy()
    }
}

class UserPreferencesComposeViewModel(
    val externalBridgeInterface: ExternalBridgeInterface,
    val shiftUseCase: ShiftUseCase,
    val sessionUseCase: SessionUseCase,
    val userPreferencesUseCase: UserPreferencesUseCase,
    val bluetoothLocalUseCase: BluetoothLocalUseCase,
    val sendLogsUseCase: SendLogsUseCase,
    val licensingUseCase: LicensingUseCase,
    val portugalUseCase: PortugalUseCase,
    val ingenicoUseCase: IngenicoUseCase,
    val app: Application,
) : ViewModel() {

    val _uiState = MutableStateFlow(UserPreferencesUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<UserPreferencesUiEffect>()
    val uiEffect: SharedFlow<UserPreferencesUiEffect> = _uiEffect.asSharedFlow()

    init {
        loadInitialData()
    }

    fun onEvent(event: UserPreferencesUiEvent) {
        when (event) {
            UserPreferencesUiEvent.OnCancelClicked -> emitEffect(UserPreferencesUiEffect.NavigateBack)
            UserPreferencesUiEvent.OnAcceptClicked -> savePreferencesAndExit()
            UserPreferencesUiEvent.OnSoundCardClicked -> _uiState.value = _uiState.value.copy(isSoundExpanded = !_uiState.value.isSoundExpanded)
            UserPreferencesUiEvent.OnInvoicesCardClicked -> _uiState.value = _uiState.value.copy(isInvoicesExpanded = !_uiState.value.isInvoicesExpanded)
            UserPreferencesUiEvent.OnPinPadCardClicked -> _uiState.value = _uiState.value.copy(isPinPadExpanded = !_uiState.value.isPinPadExpanded)
            UserPreferencesUiEvent.OnShiftsCardClicked -> _uiState.value = _uiState.value.copy(isShiftsExpanded = !_uiState.value.isShiftsExpanded)
            UserPreferencesUiEvent.OnSystemCardClicked -> _uiState.value = _uiState.value.copy(isSystemExpanded = !_uiState.value.isSystemExpanded)
            UserPreferencesUiEvent.OnLocationCardClicked -> _uiState.value = _uiState.value.copy(isLocationExpanded = !_uiState.value.isLocationExpanded)
            UserPreferencesUiEvent.OnFiscalPortugalClicked -> requestFiscalPortugalDialog()
            UserPreferencesUiEvent.OnSecurePinClicked -> navigateToSecurePin()
            UserPreferencesUiEvent.OnSendLogsClicked -> requestSendLogsDialog()
            UserPreferencesUiEvent.OnInfoInvoicesClicked -> emitEffect(
                UserPreferencesUiEffect.OpenDialog(
                    UserPreferencesDialogState(
                        title = app.getString(R.string.dialog_information),
                        description = app.getString(R.string.dialog_invoice_direction),
                        buttons = listOf(ButtonType.ACCEPT)
                    )
                )
            )
            UserPreferencesUiEvent.OnSelectDateEraseShiftsClicked -> checkShifts()
            UserPreferencesUiEvent.OnChangePinClicked -> navigateDriverPin()
            UserPreferencesUiEvent.OnUploadDriversPhotoClicked -> Unit
            UserPreferencesUiEvent.OnNotificationSoundClicked -> emitEffect(UserPreferencesUiEffect.LaunchNotificationPicker)
            UserPreferencesUiEvent.OnIncomingSoundClicked -> emitEffect(UserPreferencesUiEffect.LaunchRingtonePicker)
            is UserPreferencesUiEvent.OnDialogButtonClicked -> handleDialogButton(event)
            is UserPreferencesUiEvent.OnSpinnerChanged -> Unit
            is UserPreferencesUiEvent.OnCheckboxChanged -> Unit
            is UserPreferencesUiEvent.OnTextChanged -> Unit
            is UserPreferencesUiEvent.OnRingtonePicked -> handleRingtonePicked(event)
            is UserPreferencesUiEvent.OnPermissionResult -> Unit
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = userPreferencesUseCase.getUserPreferences() ?: UserPreferences()
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.value = _uiState.value.copy(
                userPreferences = prefs,
                isLoggedIn = loggedIn,
                showTimeControl = prefs.useTimeControl,
                showCustomPaymentTimer = prefs.useExternalApp,
                isRedSysConfigured = prefs.pinPadSerialNumber.isNotBlank() || prefs.pinPadTypeId == 2,
            )
            checkShifts()
            checkTaximeter()
            checkLightOffOnDispatched()
            checkFiscalLicensing()
            hasIngenicoInstalled()
            checkDriverTrunMode()
        }
    }

    private fun savePreferencesAndExit() {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesUseCase.insertUserPreferences(_uiState.value.userPreferences)
            emitEffect(UserPreferencesUiEffect.KeepScreenActive)
            emitEffect(UserPreferencesUiEffect.NavigateBack)
        }
    }

    private fun checkShifts() {
        viewModelScope.launch {
            val allShifts = shiftUseCase.getAll()
            if (allShifts.isNullOrEmpty()) emitEffect(UserPreferencesUiEffect.ShowToast(R.string.toast_sin_turnos_guardados_para_eliminar))
        }
    }

    private fun checkTaximeter() {
        viewModelScope.launch {
            val taximeterInstance = com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance().isSkyGlass
            val isTimeControlAvailable = externalBridgeInterface.isSkyGlassWithTimeControlAvailable
            _uiState.value = _uiState.value.copy(showTimeControl = taximeterInstance && isTimeControlAvailable)
        }
    }

    private fun checkLightOffOnDispatched() {
        viewModelScope.launch {
            val txmInstance = com.interfacom.sdk.taximeter.taximeter.Taximeter.getInstance()
            var show = false
            if (txmInstance.isConnected && txmInstance.isRoofLightAvailable) {
                show = licensingUseCase.getLicensingParameters()?.isDisableLuminous ?: false
            }
            _uiState.value = _uiState.value.copy(showLightOffOnDispatched = show)
        }
    }

    private fun checkFiscalLicensing() {
        viewModelScope.launch {
            val fiscal = licensingUseCase.getLicensingParameters()?.isFiscalService ?: false
            _uiState.value = _uiState.value.copy(showFiscalPortugalButton = fiscal)
        }
    }

    private fun hasIngenicoInstalled() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasIngenico = ingenicoUseCase.hasIngenicoInstalled()
            _uiState.value = _uiState.value.copy(showPinPadPrinter = hasIngenico)
        }
    }

    private fun checkDriverTrunMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val driverTrunMode = licensingUseCase.getStartTurnMode()
            if (driverTrunMode == 0) {
                _uiState.value = _uiState.value.copy(showChangePinButton = false, showUploadDriversPhotoButton = false)
            }
        }
    }

    private fun navigateToSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (actualPin.isNullOrEmpty()) {
                emitEffect(UserPreferencesUiEffect.NavigateToSecurePin)
            } else {
                emitEffect(
                    UserPreferencesUiEffect.OpenDialog(
                        UserPreferencesDialogState(
                            title = app.getString(R.string.user_preferences_btn_secure_pin),
                            hint = app.getString(R.string.pin_actual_hint),
                            isPinInput = true,
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                        )
                    )
                )
            }
        }
    }

    private fun handleDialogButton(event: UserPreferencesUiEvent.OnDialogButtonClicked) {
        when (event.button) {
            ButtonType.ACCEPT -> {
                val dialog = _uiState.value.pendingDialog
                when (dialog?.title) {
                    app.getString(R.string.user_preferences_btn_secure_pin) -> verifyCurrentSecurePin(event.input.orEmpty())
                    app.getString(R.string.btn_portugal_password) -> if (!event.input.isNullOrEmpty()) checkPinPortugal(event.input)
                    app.getString(R.string.dialog_add_comment) -> sendLogs(event.input)
                }
            }
            else -> emitEffect(UserPreferencesUiEffect.CloseDialog)
        }
    }

    fun checkPinPortugal(editTextString: String) {
        viewModelScope.launch {
            val portugalCode = BuildConfig.portugal_p
            val portugalPassword = portugalUseCase.getPortugalData()
            if (
                editTextString == portugalCode ||
                (portugalPassword != null && portugalPassword.portugalPassword == portugalPassword.encryptPortugalPassword(editTextString))
            ) {
                emitEffect(UserPreferencesUiEffect.NavigateToPortugalSettings)
            }
        }
    }

    fun verifyCurrentSecurePin(currentPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) {
                emitEffect(UserPreferencesUiEffect.NavigateToSecurePin)
            } else {
                emitEffect(UserPreferencesUiEffect.ShowToast(R.string.pin_incorrecto))
            }
        }
    }

    fun sendLogs(description: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            sendLogsUseCase.prepareLogSending(description)
        }
    }

    private fun requestFiscalPortugalDialog() {
        val dialog = UserPreferencesDialogState(
            title = app.getString(R.string.btn_portugal_password),
            hint = "PIN Portugal",
            isPinInput = true,
            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT)
        )
        _uiState.value = _uiState.value.copy(pendingDialog = dialog)
        emitEffect(UserPreferencesUiEffect.OpenDialog(dialog))
    }

    private fun requestSendLogsDialog() {
        val dialog = UserPreferencesDialogState(
            title = app.getString(R.string.dialog_add_comment),
            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT)
        )
        _uiState.value = _uiState.value.copy(pendingDialog = dialog)
        emitEffect(UserPreferencesUiEffect.OpenDialog(dialog))
    }

    private fun navigateDriverPin() {
        emitEffect(UserPreferencesUiEffect.NavigateToSecurePin)
    }

    private fun handleRingtonePicked(event: UserPreferencesUiEvent.OnRingtonePicked) {
        val prefs = _uiState.value.userPreferences
        when (event.type) {
            RingtoneManager.TYPE_RINGTONE -> _uiState.value = _uiState.value.copy(
                userPreferences = prefs.copy(ringtoneUri = event.uri, ringtoneTitle = event.title.orEmpty())
            )
            RingtoneManager.TYPE_NOTIFICATION -> _uiState.value = _uiState.value.copy(
                userPreferences = prefs.copy(notificationUri = event.uri, notificationTitle = event.title.orEmpty())
            )
        }
    }

    private fun emitEffect(effect: UserPreferencesUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}

class UserPreferencesMainActivityViewModel(
    val activity: Activity,
) {
    fun navigateBack() {
        activity.onBackPressedDispatcher.onBackPressed()
    }

    fun keepScreenActive() {
        activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun refreshCabId() {
    }

    fun showToast(resId: Int) {
        Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_SHORT).show()
    }

    fun requestBluetoothPermission() {}
    fun requestPhonePermission() {}
    fun requestOverlayPermission() {}
    fun launchRingtonePicker() {}
    fun launchNotificationPicker() {}

    fun navigateToSecurePin(navController: NavController) {
        val navDeepLink = NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/changeDriverPinFragment/".toUri()).build()
        navController.navigate(navDeepLink)
    }

    fun navigateToPortugalSettings(navController: NavController) {
        val navDeepLink = NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/portugalSettingsFragment/".toUri()).build()
        navController.navigate(navDeepLink)
    }
}

class ToolsViewModel(
    val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "ToolsViewModel"

    val _meetingSignColors = MutableStateFlow(Pair(0, 0))
    val meetingSignColors = _meetingSignColors.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                _meetingSignColors.emit(Pair(it.meetingSignTextColor, it.meetingSignBackgroundColor))
            }
        }
    }

    fun clickRequirements() {
        viewModelScope.launch {
            navigateTo(R.id.action_toolsFragment_to_requirementsFragment)
        }
    }
}

class LightsTestViewModel(
    private var lightSkyGlassUseCase: LightSkyGlassUseCase,
    private var userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    val _uvLightFlow = MutableSharedFlow<Boolean>()
    val uvLightFlow = _uvLightFlow.asSharedFlow()

    val _beeperLightFlow = MutableStateFlow<Int?>(null)
    val beeperLightFlow = _beeperLightFlow.asStateFlow()

    fun turnUVLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnUVLight()
            delay(8000L)
            _uvLightFlow.emit(false)
        }
    }

    fun turnCourtesyLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnCourtesyLight()
        }
    }

    fun setBeeperVolume(volume: Int?) {
        viewModelScope.launch {
            lightSkyGlassUseCase.setBeeperVolume(volume ?: 50)
            volume?.let { value ->
                userPreferencesUseCase.insertBeeperVolumeValue(value)
            }
        }
    }

    fun getBeeperVolume() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            if (preferences != null) {
                _beeperLightFlow.emit(preferences.beeperVolume)
            } else {
                userPreferencesUseCase.insertUserPreferences(UserPreferences())
            }
        }
    }
}

@Composable
fun UserPreferencesScreen(
    navController: NavController,
    viewModel: UserPreferencesComposeViewModel,
    mainActivityBridge: UserPreferencesMainActivityViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<UserPreferencesDialogState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                UserPreferencesUiEffect.NavigateBack -> mainActivityBridge.navigateBack()
                UserPreferencesUiEffect.NavigateToSecurePin -> mainActivityBridge.navigateToSecurePin(navController)
                UserPreferencesUiEffect.NavigateToPortugalSettings -> mainActivityBridge.navigateToPortugalSettings(navController)
                UserPreferencesUiEffect.RequestBluetoothPermission -> mainActivityBridge.requestBluetoothPermission()
                UserPreferencesUiEffect.RequestPhonePermission -> mainActivityBridge.requestPhonePermission()
                UserPreferencesUiEffect.RequestOverlayPermission -> mainActivityBridge.requestOverlayPermission()
                UserPreferencesUiEffect.LaunchRingtonePicker -> mainActivityBridge.launchRingtonePicker()
                UserPreferencesUiEffect.LaunchNotificationPicker -> mainActivityBridge.launchNotificationPicker()
                UserPreferencesUiEffect.RefreshCabId -> mainActivityBridge.refreshCabId()
                UserPreferencesUiEffect.KeepScreenActive -> mainActivityBridge.keepScreenActive()
                is UserPreferencesUiEffect.ShowToast -> mainActivityBridge.showToast(effect.resId)
                is UserPreferencesUiEffect.OpenDialog -> dialogState = effect.dialog
                UserPreferencesUiEffect.CloseDialog -> dialogState = null
            }
        }
    }

    BackHandler {
        viewModel.onEvent(UserPreferencesUiEvent.OnCancelClicked)
    }

    Scaffold(
        bottomBar = {
            UserPreferencesBottomBar(
                buttonsState = state.buttonsState,
                onCancel = { viewModel.onEvent(UserPreferencesUiEvent.OnCancelClicked) },
                onAccept = { viewModel.onEvent(UserPreferencesUiEvent.OnAcceptClicked) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            UserPreferencesSectionCard(
                title = "Sounds",
                expanded = state.isSoundExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnSoundCardClicked) }
            ) {
                UserPreferencesCheckboxRow(
                    text = "Sound ubication disabled on TX",
                    checked = state.userPreferences.beepNoBt,
                    onCheckedChange = { }
                )
                UserPreferencesCheckboxRow(
                    text = "Vibration and sound",
                    checked = state.userPreferences.vibrateNoBT,
                    onCheckedChange = { }
                )
                UserPreferencesCheckboxRow(
                    text = "TTS for messages",
                    checked = state.userPreferences.ttsMsg,
                    onCheckedChange = { }
                )
                UserPreferencesCheckboxRow(
                    text = "TTS for dialogs",
                    checked = state.userPreferences.ttsDialog,
                    onCheckedChange = { }
                )
                UserPreferencesCheckboxRow(
                    text = "TTS dispatch",
                    checked = state.userPreferences.ttsDispatch,
                    onCheckedChange = { }
                )
                UserPreferencesCheckboxRow(
                    text = "TTS location",
                    checked = state.userPreferences.ttsLocation,
                    onCheckedChange = { }
                )
                UserPreferencesSpinnerField("Locution blinds", listOf("1", "2", "3"), state.userPreferences.blindLocutionPosition)
            }

            UserPreferencesSectionCard(
                title = "Invoices",
                expanded = state.isInvoicesExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnInvoicesCardClicked) }
            ) {
                UserPreferencesTextField("Issuer direction", state.userPreferences.invoiceIssuerAddress)
                UserPreferencesTextField("Postal code", state.userPreferences.invoiceIssuerZipCode)
                UserPreferencesTextField("City issuer", state.userPreferences.invoiceIssuerCity)
                UserPreferencesTextField("Invoice series", state.userPreferences.invoiceSerialNumber)
                UserPreferencesTextField("Invoice number", state.userPreferences.invoiceNumber.toString())
                UserPreferencesCheckboxRow("Use external app", state.userPreferences.useExternalApp, {})
                if (state.userPreferences.useExternalApp) {
                    UserPreferencesCheckboxRow("Use custom payment timer", state.userPreferences.useCustomPaymentTimerWithExternalApp, {})
                }
                if (state.showFiscalPortugalButton) {
                    Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnFiscalPortugalClicked) }) {
                        Text("Portugal password")
                    }
                }
            }

            UserPreferencesSectionCard(
                title = "PinPad",
                expanded = state.isPinPadExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnPinPadCardClicked) }
            ) {
                UserPreferencesSpinnerField("Type PinPad", listOf("1", "2", "3"), state.userPreferences.pinPadTypePosition)
                UserPreferencesTextField("PinPad serial", state.userPreferences.pinPadSerialNumber)
                UserPreferencesTextField("SumUp merchant", state.userPreferences.sumUpMerchantCode)
            }

            UserPreferencesSectionCard(
                title = "Shifts",
                expanded = state.isShiftsExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnShiftsCardClicked) }
            ) {
                UserPreferencesSpinnerField("Minutes shift", listOf("1", "2", "3"), state.userPreferences.minutesBeforeWarningPosition)
                Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnSelectDateEraseShiftsClicked) }) {
                    Text("Delete shifts by date")
                }
                if (state.showTimeControl) {
                    UserPreferencesCheckboxRow("Time control", state.userPreferences.useTimeControl, {})
                }
                if (state.showChangePinButton) {
                    Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnChangePinClicked) }) { Text("Change PIN") }
                }
                if (state.showPinPadPrinter) {
                    UserPreferencesSpinnerField("PinPad printer", listOf("1", "2", "3"), state.userPreferences.pinPadPrinterPosition)
                    UserPreferencesSpinnerField("Export format", listOf("1", "2", "3"), state.userPreferences.fileFormatPosition)
                }
                if (state.showUploadDriversPhotoButton) {
                    Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnUploadDriversPhotoClicked) }) { Text("Upload drivers photo") }
                }
            }

            UserPreferencesSectionCard(
                title = "System",
                expanded = state.isSystemExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnSystemCardClicked) }
            ) {
                UserPreferencesCheckboxRow("Show conf accept", state.userPreferences.showConfAcceptDispatch, {})
                UserPreferencesCheckboxRow("Show conf reject", state.userPreferences.showConfRejectDispatch, {})
                UserPreferencesCheckboxRow("Print driver subscriber", state.userPreferences.printDriverSubscriber, {})
                if (state.userPreferences.printRedSysCommerceTicketAlways || state.isRedSysConfigured) {
                    UserPreferencesCheckboxRow("Always print RedSys ticket", state.userPreferences.printRedSysCommerceTicketAlways, {})
                }
                if (state.showLightOffOnDispatched) {
                    UserPreferencesCheckboxRow("Light off on dispatched", state.userPreferences.lightOffOnDispatched, {})
                }
                UserPreferencesCheckboxRow("Floating window on background", state.userPreferences.useFloatingWindow, {})
                UserPreferencesSpinnerField("Screen active", listOf("1", "2", "3"), state.userPreferences.turnOffScreenPosition)
                UserPreferencesSpinnerField("Close message", listOf("1", "2", "3"), state.userPreferences.secondsToCloseMsgPosition)
                UserPreferencesTextField("Notice phone", state.userPreferences.phoneCall)
                UserPreferencesSpinnerField("Call delay", listOf("1", "2", "3"), state.userPreferences.phoneCallTimerPosition)
                Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnSendLogsClicked) }) { Text("Send logs") }
                UserPreferencesSpinnerField("Meeting sign text color", ColorPickerEnum.values().map { it.name }, 0)
                UserPreferencesSpinnerField("Meeting sign background color", ColorPickerEnum.values().map { it.name }, 0)
                Button(onClick = { viewModel.onEvent(UserPreferencesUiEvent.OnSecurePinClicked) }) { Text("Secure PIN") }
            }

            UserPreferencesSectionCard(
                title = "Location",
                expanded = state.isLocationExpanded,
                onHeaderClick = { viewModel.onEvent(UserPreferencesUiEvent.OnLocationCardClicked) }
            ) {
                UserPreferencesCheckboxRow("Close pending trips if empty", state.userPreferences.closePendingTripsIfEmpty, {})
                UserPreferencesCheckboxRow("Macrozone query", state.userPreferences.macroZoneQuery, {})
                UserPreferencesCheckboxRow("Sort favorites by proximity", state.userPreferences.orderFavoritesByProximity, {})
                UserPreferencesCheckboxRow("Disable Glonass", state.userPreferences.disableGlonass, {})
                UserPreferencesCheckboxRow("Numeric input filter", state.userPreferences.numericInputFilter, {})
                UserPreferencesSpinnerField("GPS app", listOf("1", "2", "3"), state.userPreferences.navigatorTypePosition)
                UserPreferencesSpinnerField("Order style", listOf("1", "2", "3"), state.userPreferences.orderStylePosition)
            }
        }
    }

    dialogState?.let { dialog ->
        UserPreferencesCustomDialog(
            state = dialog,
            onDismiss = { dialogState = null },
            onButtonClick = { button, input ->
                viewModel.onEvent(UserPreferencesUiEvent.OnDialogButtonClicked(button, input))
                dialogState = null
            }
        )
    }
}

@Composable
fun UserPreferencesBottomBar(
    buttonsState: UserPreferencesButtonsState,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.Black)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        UserPreferencesCustomButton(
            modifier = Modifier.weight(1f),
            state = buttonsState.cancel,
            onClick = onCancel
        )
        UserPreferencesCustomButton(
            modifier = Modifier.weight(1f),
            state = buttonsState.accept,
            onClick = onAccept
        )
    }
}

@Composable
fun UserPreferencesCustomButton(
    modifier: Modifier = Modifier,
    state: UserPreferencesButtonVisualState,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    val containerColor = when (state.backgroundColor) {
        UserPreferencesButtonBackground.GREEN -> Color(0xFF2E7D32)
        UserPreferencesButtonBackground.RED -> Color(0xFFC62828)
        UserPreferencesButtonBackground.BLUE -> Color(0xFF1565C0)
        UserPreferencesButtonBackground.YELLOW -> Color(0xFFF9A825)
        UserPreferencesButtonBackground.ORANGE -> Color(0xFFEF6C00)
        UserPreferencesButtonBackground.DEFAULT -> Color(0xFF1565C0)
        UserPreferencesButtonBackground.BLACK -> Color(0xFF000000)
        UserPreferencesButtonBackground.TRANSPARENT -> Color.Transparent
    }

    Button(
        modifier = modifier.height(56.dp),
        onClick = onClick,
        enabled = state.enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = Color(0xFF9E9E9E)
        )
    ) {
        Text(text = state.text ?: state.type.name)
    }
}

@Composable
fun UserPreferencesSectionCard(
    title: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                Column(modifier = Modifier.padding(12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun UserPreferencesCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun UserPreferencesTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    password: Boolean = false,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun UserPreferencesSpinnerField(
    label: String,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = items.getOrNull(selectedIndex) ?: items.firstOrNull().orEmpty()

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        expanded = false
                        onSelectedIndexChange(index)
                    }
                )
            }
        }
    }
}

@Composable
fun UserPreferencesCustomDialog(
    state: UserPreferencesDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (ButtonType, String?) -> Unit,
) {
    var input by remember(state.title) { mutableStateOf(state.editText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column {
                state.description?.let { Text(it) }
                if (state.isPinInput) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(state.hint ?: "") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else if (state.hint != null) {
                    Text(state.hint)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onButtonClick(ButtonType.ACCEPT, input) }) { Text("ACCEPT") }
        },
        dismissButton = {
            TextButton(onClick = { onButtonClick(ButtonType.CANCEL, input) }) { Text("CANCEL") }
        }
    )
}
