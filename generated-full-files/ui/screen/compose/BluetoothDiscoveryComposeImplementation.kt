package ifac.td.taxi.ui.screen.compose

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.licensing.models.bluetooth.Result
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.model.BluetoothInfo.Companion.checkTaximeterType
import ifac.td.taxi.framework.sdk.usecase.BluetoothUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// STATE / EFFECT / EVENT
// ------------------------------------------------------------

data class BluetoothDiscoveryUiState(
    val devices: List<BluetoothInfo> = emptyList(),
    val selectedDevice: BluetoothInfo? = null,
    val savedDevice: BluetoothInfo? = null,
    val isLoaded: Boolean = false,
    val titleText: String = "Bluetooth",
    val taximeterImageRes: Int = R.drawable.background_dialog_transparent,
    val externalGpsChecked: Boolean = false,
    val showExternalGpsCheckbox: Boolean = false,
    val buttonsState: BluetoothDiscoveryButtonsState = BluetoothDiscoveryButtonsState.initial(),
)

data class BluetoothDiscoveryButtonsState(
    val accept: ButtonVisualState = ButtonVisualState.Enabled,
    val cancel: ButtonVisualState = ButtonVisualState.Enabled,
    val discover: ButtonVisualState = ButtonVisualState.Enabled,
) {
    companion object {
        fun initial() = BluetoothDiscoveryButtonsState()

        fun onBluetoothDiscoveryEvent(current: BluetoothDiscoveryButtonsState) =
            current.copy(discover = ButtonVisualState.Enabled)

        fun onDiscoveryStarted(current: BluetoothDiscoveryButtonsState) =
            current.copy(discover = ButtonVisualState.Loading)

        fun onShiftStatusChanged(
            current: BluetoothDiscoveryButtonsState,
            currentStatus: Int
        ): BluetoothDiscoveryButtonsState {
            val enabled = currentStatus == ifConstants.STATE_DISCONNECTED ||
                currentStatus == ifConstants.STATE_UNKNOWN

            return current.copy(
                accept = if (enabled) ButtonVisualState.Enabled else ButtonVisualState.Disabled,
                discover = if (enabled) ButtonVisualState.Enabled else ButtonVisualState.Disabled
            )
        }
    }
}

sealed class ButtonVisualState {
    data object Enabled : ButtonVisualState()
    data object Disabled : ButtonVisualState()
    data object Loading : ButtonVisualState()
}

data class BluetoothDiscoveryDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonType>,
    val cancellable: Boolean = true,
    val onAccept: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null
)

sealed interface BluetoothDiscoveryUiEvent {
    data object ScreenStarted : BluetoothDiscoveryUiEvent
    data object ScreenPaused : BluetoothDiscoveryUiEvent
    data object DiscoverClicked : BluetoothDiscoveryUiEvent
    data object AcceptClicked : BluetoothDiscoveryUiEvent
    data object CancelClicked : BluetoothDiscoveryUiEvent
    data class DeviceSelected(val device: BluetoothInfo) : BluetoothDiscoveryUiEvent
    data class ExternalGpsChanged(val checked: Boolean) : BluetoothDiscoveryUiEvent
}

sealed interface BluetoothDiscoveryUiEffect {
    data object NavigateBack : BluetoothDiscoveryUiEffect
    data object ConnectTaximeter : BluetoothDiscoveryUiEffect
    data class ShowToast(val messageRes: Int) : BluetoothDiscoveryUiEffect
    data class RequestPermission(val permissionType: PermissionRequest.PermissionTypeList) : BluetoothDiscoveryUiEffect
    data class OpenSystemIntent(val intentType: SystemIntentType) : BluetoothDiscoveryUiEffect
    data class ShowCustomDialog(val dialog: BluetoothDiscoveryDialogState) : BluetoothDiscoveryUiEffect
}

enum class SystemIntentType {
    EnableBluetooth,
    OpenLocationSettings
}

// ------------------------------------------------------------
// SHARED VM ADAPTER FOR EXISTING MainActivityViewModel
// ------------------------------------------------------------

data class BluetoothDiscoverySharedUiState(
    val currentShiftStatus: Int = ifConstants.STATE_UNKNOWN,
    val gpsExternal: Boolean = false,
    val taximeterAvailable: Boolean = false
)

sealed interface BluetoothDiscoverySharedUiEffect {
    data object NavigateBack : BluetoothDiscoverySharedUiEffect
    data object ConnectTaximeter : BluetoothDiscoverySharedUiEffect
    data class UpdateButtonsFromShiftStatus(val currentStatus: Int) : BluetoothDiscoverySharedUiEffect
}

/**
 * Adapter over your real existing MainActivityViewModel.
 * Replace the body calls with your exact MainActivityViewModel API if needed.
 */
class BluetoothDiscoveryMainActivityViewModel(
    private val mainActivityViewModel: MainActivityViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothDiscoverySharedUiState())
    val uiState: StateFlow<BluetoothDiscoverySharedUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<BluetoothDiscoverySharedUiEffect>()
    val uiEffect: SharedFlow<BluetoothDiscoverySharedUiEffect> = _uiEffect.asSharedFlow()

    fun checkTaximeterAvailability() {
        mainActivityViewModel.checkTaximeterAvailability()
    }

    fun updateGpsExternal(isChecked: Boolean) {
        mainActivityViewModel.updateGpsExternal(isChecked)
        _uiState.value = _uiState.value.copy(gpsExternal = isChecked)
    }

    fun onShiftStatusChanged(currentStatus: Int) {
        _uiState.value = _uiState.value.copy(currentShiftStatus = currentStatus)
        viewModelScope.launch {
            _uiEffect.emit(BluetoothDiscoverySharedUiEffect.UpdateButtonsFromShiftStatus(currentStatus))
        }
    }

    fun connectTaximeter() {
        mainActivityViewModel.connectTaximeter()
        viewModelScope.launch {
            _uiEffect.emit(BluetoothDiscoverySharedUiEffect.ConnectTaximeter)
        }
    }

    fun navigateBack() {
        mainActivityViewModel.navigateBack()
        viewModelScope.launch {
            _uiEffect.emit(BluetoothDiscoverySharedUiEffect.NavigateBack)
        }
    }
}

// ------------------------------------------------------------
// SCREEN VIEWMODEL
// ------------------------------------------------------------

class BluetoothDiscoveryComposeViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bluetoothUseCase: BluetoothUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    application: Application,
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    private val _uiState = MutableStateFlow(BluetoothDiscoveryUiState())
    val uiState: StateFlow<BluetoothDiscoveryUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<BluetoothDiscoveryUiEffect>()
    val uiEffect: SharedFlow<BluetoothDiscoveryUiEffect> = _uiEffect.asSharedFlow()

    private val _bluetoothDiscoveryFlow = MutableSharedFlow<BluetoothInfo?>()
    val bluetoothDiscoveryFlow: SharedFlow<BluetoothInfo?> = _bluetoothDiscoveryFlow.asSharedFlow()

    private val _bluetoothLocalFlow = MutableSharedFlow<List<BluetoothInfo?>>()
    val bluetoothLocalFlow: SharedFlow<List<BluetoothInfo?>> = _bluetoothLocalFlow.asSharedFlow()

    private val _finishSaveFlow = MutableSharedFlow<Pair<String, Boolean>?>()
    val finishSaveFlow: SharedFlow<Pair<String, Boolean>?> = _finishSaveFlow.asSharedFlow()

    private val _hasExternalGpsFlow = MutableStateFlow<Boolean?>(null)
    val hasExternalGpsFlow: StateFlow<Boolean?> = _hasExternalGpsFlow.asStateFlow()

    private val _hasExternalGpsPermissionFlow = MutableStateFlow<Boolean?>(null)
    val hasExternalGpsPermissionFlow: StateFlow<Boolean?> = _hasExternalGpsPermissionFlow.asStateFlow()

    private val _hasBluetoothActivatedFlow = MutableSharedFlow<Boolean>()
    val hasBluetoothActivatedFlow: SharedFlow<Boolean> = _hasBluetoothActivatedFlow.asSharedFlow()

    private val _hasGPSActivatedFlow = MutableSharedFlow<Boolean>()
    val hasGPSActivatedFlow: SharedFlow<Boolean> = _hasGPSActivatedFlow.asSharedFlow()

    private var locallySavedDevice: BluetoothInfo? = null

    init {
        viewModelScope.launch {
            licensingUseCase.getLicensingParameters()?.let {
                _hasExternalGpsPermissionFlow.value = it.isAskInternalGPS
                _uiState.value = _uiState.value.copy(showExternalGpsCheckbox = it.isAskInternalGPS)
            }
            sessionUseCase.getSession()?.let { session ->
                session.useExternalGPS?.let { gps ->
                    _hasExternalGpsFlow.value = gps
                    _uiState.value = _uiState.value.copy(externalGpsChecked = gps)
                }
            }
        }
    }

    fun onEvent(event: BluetoothDiscoveryUiEvent) {
        when (event) {
            BluetoothDiscoveryUiEvent.ScreenStarted -> onScreenStarted()
            BluetoothDiscoveryUiEvent.ScreenPaused -> cancelDiscovery()
            BluetoothDiscoveryUiEvent.DiscoverClicked -> getDevices()
            BluetoothDiscoveryUiEvent.AcceptClicked -> clickOnAccept()
            BluetoothDiscoveryUiEvent.CancelClicked -> viewModelScope.launch {
                _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
            }
            is BluetoothDiscoveryUiEvent.DeviceSelected -> handleDeviceSelection(event.device)
            is BluetoothDiscoveryUiEvent.ExternalGpsChanged -> {
                _uiState.value = _uiState.value.copy(externalGpsChecked = event.checked)
            }
        }
    }

    private fun onScreenStarted() {
        viewModelScope.launch {
            val bluetoothPermission = PermissionRequest.checkHavePermission(
                PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH,
                context
            )
            getSavedDevice(bluetoothPermission)
            checkBluetoothPermissions()
            checkLocationPermissions()
        }
    }

    private fun checkBluetoothPermissions() {
        PermissionRequest.needBluetoothPermission(context) { hasPermission ->
            if (!hasPermission) {
                viewModelScope.launch {
                    _uiEffect.emit(
                        BluetoothDiscoveryUiEffect.ShowCustomDialog(
                            BluetoothDiscoveryDialogState(
                                title = context.getString(R.string.bluetooth),
                                description = context.getString(R.string.dialog_no_bt_permission_desc),
                                buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                onAccept = {
                                    viewModelScope.launch {
                                        _uiEffect.emit(
                                            BluetoothDiscoveryUiEffect.RequestPermission(
                                                PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH
                                            )
                                        )
                                    }
                                }
                            )
                        )
                    )
                }
            } else {
                checkBluetoothActivated()
            }
        }
    }

    private fun checkLocationPermissions() {
        PermissionRequest.needLocationPermission(context) { hasPermission ->
            if (!hasPermission) {
                viewModelScope.launch {
                    _uiEffect.emit(
                        BluetoothDiscoveryUiEffect.ShowCustomDialog(
                            BluetoothDiscoveryDialogState(
                                title = context.getString(R.string.strLocalizacion),
                                description = context.getString(R.string.no_location_permission),
                                buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                onAccept = {
                                    viewModelScope.launch {
                                        _uiEffect.emit(
                                            BluetoothDiscoveryUiEffect.RequestPermission(
                                                PermissionRequest.PermissionTypeList.PERMISSION_LOCATION
                                            )
                                        )
                                    }
                                }
                            )
                        )
                    )
                }
            } else {
                checkLocationActivated()
            }
        }
    }

    fun getDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                buttonsState = BluetoothDiscoveryButtonsState.onDiscoveryStarted(_uiState.value.buttonsState)
            )
            if (PermissionRequest.checkHavePermission(
                    PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH,
                    context
                )
            ) {
                bluetoothLocalUseCase.getNearbyDevices(_bluetoothDiscoveryFlow)
            }
        }
    }

    fun cancelDiscovery() {
        viewModelScope.launch {
            bluetoothLocalUseCase.cancelDiscovery()
        }
    }

    fun getSavedDevice(hasPermission: Boolean) {
        viewModelScope.launch {
            val bluetoothPaired = bluetoothLocalUseCase.getLocalBluetooth()
            val newMutableList = mutableListOf<BluetoothInfo?>()

            if (bluetoothPaired != null) newMutableList.add(bluetoothPaired)
            if (hasPermission) newMutableList.addAll(bluetoothLocalUseCase.getBondedDevices())

            _bluetoothLocalFlow.emit(newMutableList)
            applySavedDeviceUi(bluetoothPaired)
        }
    }

    private fun applySavedDeviceUi(device: BluetoothInfo?) {
        if (device != null) {
            _uiState.value = _uiState.value.copy(
                savedDevice = device,
                selectedDevice = device,
                isLoaded = device.name.isNotBlank(),
                titleText = mapTitle(device.name),
                taximeterImageRes = mapImage(device.name)
            )
            locallySavedDevice = device
        } else {
            _uiState.value = _uiState.value.copy(
                savedDevice = null,
                selectedDevice = null,
                isLoaded = false,
                titleText = context.getString(R.string.bluetooth),
                taximeterImageRes = R.drawable.background_dialog_transparent
            )
            locallySavedDevice = null
        }
    }

    private fun handleDeviceSelection(device: BluetoothInfo) {
        if (locallySavedDevice != null && device != locallySavedDevice) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowCustomDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.deselect_confirm),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            cancellable = false,
                            onAccept = { locallySavedDevice?.let { removeDevice(it) } },
                            onCancel = { _uiState.value = _uiState.value.copy(selectedDevice = locallySavedDevice) }
                        )
                    )
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                selectedDevice = device,
                titleText = mapTitle(device.name),
                taximeterImageRes = mapImage(device.name)
            )
        }
    }

    fun reduceButtonsFromShiftStatus(currentStatus: Int) {
        _uiState.value = _uiState.value.copy(
            buttonsState = BluetoothDiscoveryButtonsState.onShiftStatusChanged(
                current = _uiState.value.buttonsState,
                currentStatus = currentStatus
            )
        )
    }

    private fun clickOnAccept() {
        val selected = _uiState.value.selectedDevice
        when {
            selected != null -> {
                val device = selected.copy(gpsExternal = _uiState.value.externalGpsChecked)
                saveDevice(device)
            }
            else -> {
                changeExternalGps(_uiState.value.externalGpsChecked)
                viewModelScope.launch {
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                }
            }
        }
    }

    fun saveDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password

            if (username != null && password != null) {
                val bluetoothLicensing = licensingUseCase.addLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )

                if (device.gpsExternal != _uiState.value.externalGpsChecked) {
                    sessionUseCase.updateExternalGPS(device.gpsExternal)
                }

                if (bluetoothLicensing != null && bluetoothLicensing.nameBT.isNotEmpty()) {
                    bluetoothLocalUseCase.saveLocalBluetooth(
                        BluetoothInfo(
                            name = bluetoothLicensing.nameBT.checkTaximeterType(),
                            macAddress = bluetoothLicensing.addressMAC
                        )
                    )
                    bluetoothLocalUseCase.savePinLocalBluetooth(
                        pin = bluetoothLicensing.pin,
                        address = bluetoothLicensing.addressMAC
                    )
                    _finishSaveFlow.emit(Pair(bluetoothLicensing.pin ?: "NO PIN ERROR", true))
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ConnectTaximeter)
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                } else if (bluetoothLicensing != null) {
                    val hardcodePin = "1234"
                    if (device.name.startsWith("GPC")) {
                        bluetoothLocalUseCase.saveLocalBluetooth(
                            BluetoothInfo(name = device.name, macAddress = device.macAddress)
                        )
                        bluetoothLocalUseCase.savePinLocalBluetooth(
                            pin = hardcodePin,
                            address = device.macAddress
                        )
                        _finishSaveFlow.emit(Pair(hardcodePin, true))
                        _uiEffect.emit(BluetoothDiscoveryUiEffect.ConnectTaximeter)
                        _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                    } else {
                        val btMessage = getBluetoothMessage(
                            bluetoothLicensing.result.codiMissatge,
                            bluetoothLicensing.result.numSerial,
                            bluetoothLicensing.result.message
                        )
                        _uiEffect.emit(
                            BluetoothDiscoveryUiEffect.ShowCustomDialog(
                                BluetoothDiscoveryDialogState(
                                    title = context.getString(R.string.dialog_pin_bluetooth_title),
                                    description = btMessage,
                                    buttons = listOf(ButtonType.ACCEPT)
                                )
                            )
                        )
                        _finishSaveFlow.emit(Pair(btMessage, true))
                    }
                }
            }
        }
    }

    fun removeDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password

            if (username != null && password != null) {
                val bluetoothLicensing = licensingUseCase.removeLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )

                if (bluetoothLicensing != null) {
                    taximeterConnectUseCase.taximeterDisconnect()
                    bluetoothLocalUseCase.deleteLocalBluetooth()
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_ok))
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                    _finishSaveFlow.emit(Pair(context.getString(R.string.toast_bluetooth_remove_ok), false))
                } else {
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_server_error))
                    _finishSaveFlow.emit(Pair(context.getString(R.string.toast_bluetooth_remove_server_error), false))
                }
            }
        }
    }

    fun changeExternalGps(isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionUseCase.getSession()?.let {
                if (it.useExternalGPS != isChecked) {
                    sessionUseCase.updateExternalGPS(isChecked)
                }
            }
        }
    }

    fun checkBluetoothActivated() {
        viewModelScope.launch {
            val enabled = isBluetoothEnabled()
            _hasBluetoothActivatedFlow.emit(enabled)
            if (!enabled) {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowCustomDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.dialog_activate_bt_desc),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            onAccept = {
                                val bluetoothManager =
                                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                                val bluetoothAdapter = bluetoothManager.adapter
                                if (!bluetoothAdapter.isEnabled) {
                                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        viewModelScope.launch {
                                            _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.activate_bt_toast))
                                        }
                                    }
                                    context.startActivity(enableBtIntent)
                                }
                            }
                        )
                    )
                )
            }
        }
    }

    fun checkLocationActivated() {
        viewModelScope.launch {
            val enabled = isLocationEnabled()
            _hasGPSActivatedFlow.emit(enabled)
            if (!enabled) {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowCustomDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.strLocalizacion),
                            description = context.getString(R.string.dialog_activate_location_desc),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            onAccept = {
                                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(enableLocationIntent)
                            }
                        )
                    )
                )
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun mapImage(deviceName: String): Int = when {
        deviceName.startsWith("SKYG") -> R.drawable.vinculacion_sky
        deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> R.drawable.vinculacion_urba
        deviceName.startsWith("BG40") -> R.drawable.vinculacion_bg_bl_11
        deviceName.startsWith("BL60") -> R.drawable.vinculacion_bg_bl_09
        else -> R.drawable.vinculacion_tx
    }

    private fun mapTitle(deviceName: String): String = when {
        deviceName.startsWith("SKYG") -> "TX80/SkyGlass"
        deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> "TX80/Urba"
        deviceName.startsWith("BG40") -> "BG40"
        deviceName.startsWith("BL60") -> "BL60"
        else -> deviceName
    }

    private fun getBluetoothMessage(messageCode: Int, serialNumber: String, msg: String): String {
        return when (messageCode) {
            Result.LCE_WRONG_PARAMETERS -> context.getString(R.string.lce_wrong_parameters)
            Result.LCE_BT_WRONG_HW_VERSION -> context.getString(R.string.lce_bt_wrong_hw_version)
            Result.LCE_TX_WRONG_HW_VERSION -> context.getString(R.string.lce_tx_wrong_hw_version)
            Result.LCE_BT_WRONG_DEVICE_TYPE -> context.getString(R.string.lce_bt_wrong_device_type)
            Result.LCE_SERIAL_NUMBER_OWNED_ANOTHER_USER ->
                String.format(context.getString(R.string.lce_serial_number_owned_another_user), serialNumber)
            Result.LCE_BT_DEVICE_ALREADY_LINKED -> context.getString(R.string.lce_bt_device_already_linked)
            else -> msg
        }
    }
}

// ------------------------------------------------------------
// COMPOSE SCREEN
// ------------------------------------------------------------

@Composable
fun BluetoothDiscoveryScreen(
    navController: NavController,
    viewModel: BluetoothDiscoveryComposeViewModel,
    sharedViewModel: BluetoothDiscoveryMainActivityViewModel,
    onRequestPermission: (PermissionRequest.PermissionTypeList) -> Unit = {},
    onShowToast: (Int) -> Unit = {},
    onConnectTaximeter: () -> Unit = {},
    onNavigateBack: NavDirections? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<BluetoothDiscoveryDialogState?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(BluetoothDiscoveryUiEvent.ScreenStarted)
        sharedViewModel.checkTaximeterAvailability()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                BluetoothDiscoveryUiEffect.NavigateBack -> {
                    if (onNavigateBack != null) {
                        navController.navigate(onNavigateBack)
                    } else {
                        navController.popBackStack()
                    }
                }
                BluetoothDiscoveryUiEffect.ConnectTaximeter -> onConnectTaximeter()
                is BluetoothDiscoveryUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is BluetoothDiscoveryUiEffect.RequestPermission -> onRequestPermission(effect.permissionType)
                is BluetoothDiscoveryUiEffect.OpenSystemIntent -> {
                    when (effect.intentType) {
                        SystemIntentType.EnableBluetooth -> {
                            context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }
                        SystemIntentType.OpenLocationSettings -> {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    }
                }
                is BluetoothDiscoveryUiEffect.ShowCustomDialog -> dialogState = effect.dialog
            }
        }
    }

    LaunchedEffect(Unit) {
        sharedViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                BluetoothDiscoverySharedUiEffect.NavigateBack -> {
                    if (onNavigateBack != null) navController.navigate(onNavigateBack) else navController.popBackStack()
                }
                BluetoothDiscoverySharedUiEffect.ConnectTaximeter -> onConnectTaximeter()
                is BluetoothDiscoverySharedUiEffect.UpdateButtonsFromShiftStatus -> {
                    viewModel.reduceButtonsFromShiftStatus(effect.currentStatus)
                }
            }
        }
    }

    LaunchedEffect(sharedState.currentShiftStatus) {
        viewModel.reduceButtonsFromShiftStatus(sharedState.currentShiftStatus)
    }

    Scaffold { padding ->
        BluetoothDiscoveryContent(
            modifier = Modifier.padding(padding),
            state = state,
            onEvent = viewModel::onEvent
        )
    }

    dialogState?.let { dialog ->
        AlertDialog(
            onDismissRequest = { if (dialog.cancellable) dialogState = null },
            title = { Text(dialog.title) },
            text = { Text(dialog.description) },
            confirmButton = {
                if (dialog.buttons.contains(ButtonType.ACCEPT)) {
                    TextButton(onClick = {
                        dialog.onAccept?.invoke()
                        dialogState = null
                    }) { Text("Accept") }
                }
            },
            dismissButton = {
                if (dialog.buttons.contains(ButtonType.CANCEL)) {
                    TextButton(onClick = {
                        dialog.onCancel?.invoke()
                        dialogState = null
                    }) { Text("Cancel") }
                }
            }
        )
    }
}

@Composable
private fun BluetoothDiscoveryContent(
    modifier: Modifier = Modifier,
    state: BluetoothDiscoveryUiState,
    onEvent: (BluetoothDiscoveryUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (state.taximeterImageRes != 0) state.taximeterImageRes else R.drawable.background_dialog_transparent
            ),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(text = state.titleText, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        if (state.showExternalGpsCheckbox) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.externalGpsChecked,
                    onCheckedChange = { onEvent(BluetoothDiscoveryUiEvent.ExternalGpsChanged(it)) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "GPS externo Bluetooth")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.devices, key = { it.macAddress }) { device ->
                BluetoothDeviceRow(
                    device = device,
                    selected = state.selectedDevice?.macAddress == device.macAddress,
                    onClick = { onEvent(BluetoothDiscoveryUiEvent.DeviceSelected(device)) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        BluetoothDiscoveryButtons(
            state = state.buttonsState,
            onAccept = { onEvent(BluetoothDiscoveryUiEvent.AcceptClicked) },
            onCancel = { onEvent(BluetoothDiscoveryUiEvent.CancelClicked) },
            onDiscover = { onEvent(BluetoothDiscoveryUiEvent.DiscoverClicked) }
        )
    }
}

@Composable
private fun BluetoothDeviceRow(
    device: BluetoothInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = device.name)
            Text(text = device.macAddress, style = MaterialTheme.typography.bodySmall)
            if (selected) {
                Text(text = "Selected", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun BluetoothDiscoveryButtons(
    state: BluetoothDiscoveryButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onDiscover: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DiscoveryButton(
                text = "Cancel",
                visualState = state.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancel
            )
            DiscoveryButton(
                text = "Accept",
                visualState = state.accept,
                modifier = Modifier.weight(1f),
                onClick = onAccept
            )
        }

        DiscoveryButton(
            text = "Discover",
            visualState = state.discover,
            modifier = Modifier.fillMaxWidth(),
            onClick = onDiscover
        )
    }
}

@Composable
private fun DiscoveryButton(
    text: String,
    visualState: ButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val enabled = visualState == ButtonVisualState.Enabled
    val background = when (visualState) {
        ButtonVisualState.Enabled -> Color(0xFF1E88E5)
        ButtonVisualState.Disabled -> Color(0xFFBDBDBD)
        ButtonVisualState.Loading -> Color(0xFF90CAF9)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color.White,
            disabledContainerColor = background,
            disabledContentColor = Color.White
        )
    ) {
        if (visualState == ButtonVisualState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
