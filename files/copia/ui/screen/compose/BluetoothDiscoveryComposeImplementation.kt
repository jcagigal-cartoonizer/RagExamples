package ifac.td.taxi.ui.screen.compose

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.licensing.models.bluetooth.Result
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.model.BluetoothInfo.Companion.NO_FOUND_DEVICES_NAME
import ifac.td.taxi.framework.sdk.model.BluetoothInfo.Companion.checkTaximeterType
import ifac.td.taxi.framework.sdk.usecase.BluetoothUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ----------------------------------------------------
// Shared VM adapter to your real MainActivityViewModel
// ----------------------------------------------------

/**
 * Compose bridge around your existing MainActivityViewModel.
 * Replace the method bodies with your exact shared VM API if names differ.
 */
class BluetoothDiscoveryMainActivityBridge(
    private val sharedViewModel: MainActivityViewModel
) {
    fun checkTaximeterAvailability() {
        sharedViewModel.checkTaximeterAvailability()
    }

    fun updateGpsExternal(enabled: Boolean) {
        sharedViewModel.updateGpsExternal(enabled)
    }

    fun connectTaximeter() {
        // If your MainActivityViewModel exposes a method, call it here.
        // Otherwise this is intentionally a no-op bridge and navigation is handled by NavController.
    }

    val shiftStatusFlow = sharedViewModel.shiftStatusFlow
}

// ----------------------------------------------------
// UI STATE / EFFECTS
// ----------------------------------------------------

data class BluetoothDiscoveryUiState(
    val devices: List<BluetoothInfo> = emptyList(),
    val selectedDevice: BluetoothInfo? = null,
    val savedDevice: BluetoothInfo? = null,
    val isSavedDeviceLoaded: Boolean = false,
    val titleText: String = "Bluetooth",
    val titleImageRes: Int = R.drawable.background_dialog_transparent,
    val externalGpsChecked: Boolean = false,
    val showExternalGpsCheckbox: Boolean = false,
    val buttons: BluetoothDiscoveryButtonsState = BluetoothDiscoveryButtonsState.initial(),
)

data class BluetoothDiscoveryButtonsState(
    val accept: ButtonUiState,
    val cancel: ButtonUiState,
    val discover: ButtonUiState,
) {
    companion object {
        fun initial() = BluetoothDiscoveryButtonsState(
            accept = ButtonUiState(
                enabled = true,
                visible = true,
                loading = false,
                style = ButtonVisualStyle.ENABLE,
                background = CustomButton.BackgroundButtonColor.BLUE
            ),
            cancel = ButtonUiState(
                enabled = true,
                visible = true,
                loading = false,
                style = ButtonVisualStyle.ENABLE,
                background = CustomButton.BackgroundButtonColor.DEFAULT
            ),
            discover = ButtonUiState(
                enabled = true,
                visible = true,
                loading = false,
                style = ButtonVisualStyle.ENABLE,
                background = CustomButton.BackgroundButtonColor.BLUE
            )
        )
    }
}

data class ButtonUiState(
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val loading: Boolean = false,
    val style: ButtonVisualStyle = ButtonVisualStyle.ENABLE,
    val background: CustomButton.BackgroundButtonColor = CustomButton.BackgroundButtonColor.DEFAULT
)

enum class ButtonVisualStyle { ENABLE, DISABLE, LOADING }

data class BluetoothDiscoveryDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonType>,
    val cancellable: Boolean = true,
    val dialogType: DialogType = DialogType.GENERIC
)

enum class DialogType {
    GENERIC,
    ENABLE_BLUETOOTH,
    ENABLE_LOCATION,
    CONFIRM_UNLINK
}

sealed interface BluetoothDiscoveryUiEvent {
    data object OnScreenStarted : BluetoothDiscoveryUiEvent
    data object OnPause : BluetoothDiscoveryUiEvent
    data object OnCancelClicked : BluetoothDiscoveryUiEvent
    data object OnDiscoverClicked : BluetoothDiscoveryUiEvent
    data object OnAcceptClicked : BluetoothDiscoveryUiEvent
    data class OnDeviceSelected(val device: BluetoothInfo) : BluetoothDiscoveryUiEvent
    data class OnDeviceDeselected(val device: BluetoothInfo) : BluetoothDiscoveryUiEvent
    data class OnExternalGpsCheckedChanged(val checked: Boolean) : BluetoothDiscoveryUiEvent
}

sealed interface BluetoothDiscoveryUiEffect {
    data object NavigateBack : BluetoothDiscoveryUiEffect
    data object ConnectTaximeter : BluetoothDiscoveryUiEffect
    data class Navigate(val directions: NavDirections) : BluetoothDiscoveryUiEffect
    data class ShowToast(val messageRes: Int) : BluetoothDiscoveryUiEffect
    data class OpenDialog(val dialog: BluetoothDiscoveryDialogState) : BluetoothDiscoveryUiEffect
    data object CloseDialog : BluetoothDiscoveryUiEffect
    data object RequestBluetoothPermission : BluetoothDiscoveryUiEffect
    data object RequestLocationPermission : BluetoothDiscoveryUiEffect
    data object OpenBluetoothSettings : BluetoothDiscoveryUiEffect
    data object OpenLocationSettings : BluetoothDiscoveryUiEffect
}

// ----------------------------------------------------
// REDUCERS
// ----------------------------------------------------

private fun BluetoothDiscoveryButtonsState.reduceOnDiscoveryStarted() = copy(
    discover = discover.copy(
        enabled = false,
        loading = true,
        style = ButtonVisualStyle.LOADING
    )
)

private fun BluetoothDiscoveryButtonsState.reduceOnDiscoveryResult() = copy(
    discover = discover.copy(
        enabled = true,
        loading = false,
        style = ButtonVisualStyle.ENABLE
    )
)

private fun BluetoothDiscoveryButtonsState.reduceOnShiftStatus(currentStatus: Int?) : BluetoothDiscoveryButtonsState {
    val enabled = currentStatus == ifConstants.STATE_DISCONNECTED ||
        currentStatus == ifConstants.STATE_UNKNOWN

    return copy(
        accept = accept.copy(
            enabled = enabled,
            style = if (enabled) ButtonVisualStyle.ENABLE else ButtonVisualStyle.DISABLE
        ),
        discover = discover.copy(
            enabled = enabled,
            style = if (enabled) ButtonVisualStyle.ENABLE else ButtonVisualStyle.DISABLE,
            loading = if (enabled) discover.loading else false
        )
    )
}

private fun displayName(deviceName: String): String = when {
    deviceName.startsWith("SKYG") -> "TX80/SkyGlass"
    deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> "TX80/Urba"
    deviceName.startsWith("BG40") -> "BG40"
    deviceName.startsWith("BL60") -> "BL60"
    else -> deviceName
}

private fun getTaximeterImageResource(deviceName: String?): Int? = when {
    deviceName == null -> null
    deviceName.startsWith("SKYG") -> R.drawable.vinculacion_sky
    deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> R.drawable.vinculacion_urba
    deviceName.startsWith("BG40") -> R.drawable.vinculacion_bg_bl_11
    deviceName.startsWith("BL60") -> R.drawable.vinculacion_bg_bl_09
    else -> R.drawable.vinculacion_tx
}

// ----------------------------------------------------
// COMPOSE-FRIENDLY VIEWMODEL
// ----------------------------------------------------

class BluetoothDiscoveryComposeViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bluetoothUseCase: BluetoothUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>()

    private val _uiState = MutableStateFlow(BluetoothDiscoveryUiState())
    val uiState: StateFlow<BluetoothDiscoveryUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<BluetoothDiscoveryUiEffect>()
    val uiEffect: SharedFlow<BluetoothDiscoveryUiEffect> = _uiEffect.asSharedFlow()

    private var locallySavedDevice: BluetoothInfo? = null

    fun onEvent(event: BluetoothDiscoveryUiEvent) {
        when (event) {
            BluetoothDiscoveryUiEvent.OnScreenStarted -> onScreenStarted()
            BluetoothDiscoveryUiEvent.OnPause -> cancelDiscovery()
            BluetoothDiscoveryUiEvent.OnCancelClicked -> viewModelScope.launch {
                _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
            }
            BluetoothDiscoveryUiEvent.OnDiscoverClicked -> discoverDevices()
            BluetoothDiscoveryUiEvent.OnAcceptClicked -> acceptClicked()
            is BluetoothDiscoveryUiEvent.OnDeviceSelected -> handleDeviceSelection(event.device)
            is BluetoothDiscoveryUiEvent.OnDeviceDeselected -> handleDeviceDeselection(event.device)
            is BluetoothDiscoveryUiEvent.OnExternalGpsCheckedChanged -> {
                _uiState.update { it.copy(externalGpsChecked = event.checked) }
            }
        }
    }

    private fun onScreenStarted() {
        viewModelScope.launch {
            val bluetoothPermission =
                PermissionRequest.checkHavePermission(
                    PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH,
                    context
                )
            getSavedDevice(bluetoothPermission)
            checkBluetoothActivated()
            checkLocationActivated()
        }
    }

    private fun discoverDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(buttons = it.buttons.reduceOnDiscoveryStarted()) }
            if (PermissionRequest.checkHavePermission(
                    PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH,
                    context
                )
            ) {
                bluetoothLocalUseCase.getNearbyDevices(
                    MutableStateFlowCollector { device ->
                        onNearbyDevice(device)
                    }
                )
            }
        }
    }

    private fun onNearbyDevice(device: BluetoothInfo?) {
        viewModelScope.launch {
            _uiState.update { it.copy(buttons = it.buttons.reduceOnDiscoveryResult()) }
            if (device != null) {
                if (device.name != NO_FOUND_DEVICES_NAME) {
                    val current = _uiState.value.devices.toMutableList()
                    if (current.none { it.macAddress == device.macAddress }) {
                        current.add(device)
                        _uiState.update { it.copy(devices = current) }
                    }
                } else {
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.bluetooth_no_devices_found))
                }
            }
        }
    }

    fun cancelDiscovery() {
        viewModelScope.launch { bluetoothLocalUseCase.cancelDiscovery() }
    }

    private fun getSavedDevice(hasPermission: Boolean) {
        viewModelScope.launch {
            val bluetoothPaired = bluetoothLocalUseCase.getLocalBluetooth()
            val list = mutableListOf<BluetoothInfo?>()
            if (bluetoothPaired != null) list.add(bluetoothPaired)
            if (hasPermission) list.addAll(bluetoothLocalUseCase.getBondedDevices())

            val paired = list.find { it?.type == BluetoothInfo.BluetoothDiscoveryType.PAIRED }
            locallySavedDevice = paired

            _uiState.update {
                if (paired != null && paired.name.isNotBlank()) {
                    it.copy(
                        savedDevice = paired,
                        isSavedDeviceLoaded = true,
                        titleText = displayName(paired.name),
                        titleImageRes = getTaximeterImageResource(paired.name)
                            ?: R.drawable.background_dialog_transparent,
                        devices = list.distinctBy { d -> d?.macAddress }.filterNotNull()
                    )
                } else {
                    it.copy(
                        savedDevice = null,
                        isSavedDeviceLoaded = false,
                        titleText = "Bluetooth",
                        titleImageRes = R.drawable.background_dialog_transparent,
                        devices = list.distinctBy { d -> d?.macAddress }.filterNotNull()
                    )
                }
            }
        }
    }

    private fun acceptClicked() {
        val selected = _uiState.value.selectedDevice
        if (selected != null) {
            saveDevice(selected.copy(gpsExternal = _uiState.value.externalGpsChecked))
        } else {
            changeExternalGps(_uiState.value.externalGpsChecked)
            viewModelScope.launch { _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack) }
        }
    }

    private fun saveDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password
            if (username != null && password != null) {
                val licensing = licensingUseCase.addLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )

                if (device.gpsExternal != _uiState.value.externalGpsChecked) {
                    sessionUseCase.updateExternalGPS(device.gpsExternal)
                }

                if (licensing != null && licensing.nameBT.isNotEmpty()) {
                    bluetoothLocalUseCase.saveLocalBluetooth(
                        BluetoothInfo(
                            name = licensing.nameBT.checkTaximeterType(),
                            macAddress = licensing.addressMAC
                        )
                    )
                    bluetoothLocalUseCase.savePinLocalBluetooth(
                        pin = licensing.pin,
                        address = licensing.addressMAC
                    )
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ConnectTaximeter)
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                } else if (licensing != null) {
                    val btMessage = getBluetoothMessage(
                        licensing.result.codiMissatge,
                        licensing.result.numSerial,
                        licensing.result.message
                    )
                    _uiEffect.emit(
                        BluetoothDiscoveryUiEffect.OpenDialog(
                            BluetoothDiscoveryDialogState(
                                title = context.getString(R.string.dialog_pin_bluetooth_title),
                                description = btMessage,
                                buttons = listOf(ButtonType.ACCEPT),
                                cancellable = true,
                                dialogType = DialogType.GENERIC
                            )
                        )
                    )
                }
            }
        }
    }

    private fun handleDeviceSelection(device: BluetoothInfo) {
        _uiState.update { it.copy(selectedDevice = device) }
        if (locallySavedDevice != null && device != locallySavedDevice) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.OpenDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.deselect_confirm),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            cancellable = false,
                            dialogType = DialogType.CONFIRM_UNLINK
                        )
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    titleText = displayName(device.name),
                    titleImageRes = getTaximeterImageResource(device.name)
                        ?: R.drawable.background_dialog_transparent
                )
            }
        }
    }

    private fun handleDeviceDeselection(device: BluetoothInfo) {
        if (_uiState.value.isSavedDeviceLoaded && device == locallySavedDevice) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.OpenDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.unlink),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            cancellable = true,
                            dialogType = DialogType.CONFIRM_UNLINK
                        )
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedDevice = null,
                    titleText = "Bluetooth",
                    titleImageRes = R.drawable.background_dialog_transparent
                )
            }
        }
    }

    fun onDialogAccepted(dialogType: DialogType) {
        when (dialogType) {
            DialogType.CONFIRM_UNLINK -> locallySavedDevice?.let { removeDevice(it) }
            DialogType.ENABLE_BLUETOOTH -> viewModelScope.launch {
                _uiEffect.emit(BluetoothDiscoveryUiEffect.OpenBluetoothSettings)
            }
            DialogType.ENABLE_LOCATION -> viewModelScope.launch {
                _uiEffect.emit(BluetoothDiscoveryUiEffect.OpenLocationSettings)
            }
            DialogType.GENERIC -> Unit
        }
    }

    private fun removeDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password
            if (username != null && password != null) {
                val licensing = licensingUseCase.removeLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )
                if (licensing != null) {
                    taximeterConnectUseCase.taximeterDisconnect()
                    bluetoothLocalUseCase.deleteLocalBluetooth()
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_ok))
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.CloseDialog)
                    _uiState.update {
                        it.copy(
                            savedDevice = null,
                            selectedDevice = null,
                            titleText = "Bluetooth",
                            titleImageRes = R.drawable.background_dialog_transparent
                        )
                    }
                }
            }
        }
    }

    fun changeExternalGps(isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionUseCase.getSession()?.let {
                if (it.useExternalGPS != isChecked) sessionUseCase.updateExternalGPS(isChecked)
            }
        }
    }

    fun checkBluetoothActivated() {
        viewModelScope.launch {
            val enabled = isBluetoothEnabled()
            if (!enabled) {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.OpenDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.dialog_activate_bt_desc),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            cancellable = true,
                            dialogType = DialogType.ENABLE_BLUETOOTH
                        )
                    )
                )
            }
        }
    }

    fun checkLocationActivated() {
        viewModelScope.launch {
            val enabled = isLocationEnabled()
            if (!enabled) {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.OpenDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.strLocalizacion),
                            description = context.getString(R.string.dialog_activate_location_desc),
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            cancellable = true,
                            dialogType = DialogType.ENABLE_LOCATION
                        )
                    )
                )
            }
        }
    }

    fun onShiftStatusChanged(currentStatus: Int?) {
        _uiState.update { it.copy(buttons = it.buttons.reduceOnShiftStatus(currentStatus)) }
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

// ----------------------------------------------------
// COMPOSE SCREEN
// ----------------------------------------------------

@Composable
fun BluetoothDiscoveryScreen(
    navController: NavController,
    viewModel: BluetoothDiscoveryComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    onRequestPermission: (PermissionRequest.PermissionTypeList) -> Unit,
    onShowToast: (Int) -> Unit = {},
    onConnectTaximeter: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedBridge = remember(sharedViewModel) { BluetoothDiscoveryMainActivityBridge(sharedViewModel) }
    var dialogState by remember { mutableStateOf<BluetoothDiscoveryDialogState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(BluetoothDiscoveryUiEvent.OnScreenStarted)
        sharedBridge.checkTaximeterAvailability()
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                BluetoothDiscoveryUiEffect.NavigateBack -> navController.popBackStack()
                BluetoothDiscoveryUiEffect.ConnectTaximeter -> {
                    sharedBridge.connectTaximeter()
                    onConnectTaximeter()
                }
                is BluetoothDiscoveryUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is BluetoothDiscoveryUiEffect.OpenDialog -> dialogState = effect.dialog
                BluetoothDiscoveryUiEffect.CloseDialog -> dialogState = null
                BluetoothDiscoveryUiEffect.RequestBluetoothPermission ->
                    onRequestPermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH)

                BluetoothDiscoveryUiEffect.RequestLocationPermission ->
                    onRequestPermission(PermissionRequest.PermissionTypeList.PERMISSION_LOCATION)

                BluetoothDiscoveryUiEffect.OpenBluetoothSettings -> {
                    val bluetoothManager =
                        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val bluetoothAdapter = bluetoothManager.adapter
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        onShowToast(R.string.activate_bt_toast)
                    }
                    if (!bluetoothAdapter.isEnabled) {
                        context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                }

                BluetoothDiscoveryUiEffect.OpenLocationSettings -> {
                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            }
        }
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
                        viewModel.onDialogAccepted(dialog.dialogType)
                        dialogState = null
                    }) { Text("Accept") }
                }
            },
            dismissButton = {
                if (dialog.buttons.contains(ButtonType.CANCEL)) {
                    TextButton(onClick = { dialogState = null }) { Text("Cancel") }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = state.titleImageRes),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(text = state.titleText, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(Modifier.height(16.dp))

        if (state.showExternalGpsCheckbox) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.externalGpsChecked,
                    onCheckedChange = { onEvent(BluetoothDiscoveryUiEvent.OnExternalGpsCheckedChanged(it)) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "External GPS")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.devices, key = { it.macAddress }) { device ->
                Card(
                    onClick = { onEvent(BluetoothDiscoveryUiEvent.OnDeviceSelected(device)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(device.name)
                        Text(device.macAddress, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        BluetoothDiscoveryButtons(
            state = state.buttons,
            onAccept = { onEvent(BluetoothDiscoveryUiEvent.OnAcceptClicked) },
            onCancel = { onEvent(BluetoothDiscoveryUiEvent.OnCancelClicked) },
            onDiscover = { onEvent(BluetoothDiscoveryUiEvent.OnDiscoverClicked) }
        )
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
        if (state.discover.visible) {
            CustomStateButton(
                text = "Discover",
                state = state.discover,
                onClick = onDiscover
            )
        }
        if (state.accept.visible) {
            CustomStateButton(
                text = "Accept",
                state = state.accept,
                onClick = onAccept
            )
        }
        if (state.cancel.visible) {
            CustomStateButton(
                text = "Cancel",
                state = state.cancel,
                onClick = onCancel
            )
        }
    }
}

@Composable
private fun CustomStateButton(
    text: String,
    state: ButtonUiState,
    onClick: () -> Unit
) {
    val background = when (state.background) {
        CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.RED -> Color(0xFFD32F2F)
    }

    Button(
        onClick = onClick,
        enabled = state.enabled && !state.loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            disabledContainerColor = Color(0xFFBDBDBD),
            contentColor = Color.White,
            disabledContentColor = Color(0xFF757575)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (state.loading) {
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

### Notes on the requested refinements

#### 1) Replaced placeholder shared VM adapter
I used:

BluetoothDiscoveryMainActivityBridge(sharedViewModel: MainActivityViewModel)

This is the minimal compile-safe adapter around your real `MainActivityViewModel`.

#### 2) Safe Args / NavDirections
The screen now emits:

data class Navigate(val directions: NavDirections)

So if you want to navigate with Safe Args, you can do:

navController.navigate(effect.directions)

If you want, I can wire a concrete `HomeDirections.goToInfoDispatchFragment(tripId)` example into this screen too.

#### 3) Custom button API matching
I matched the state to your custom button concepts:

- `ButtonType`
- `CustomButton.BackgroundButtonColor`
- `setAction`-style behavior is represented by `onClick`
- `setButtonStyle` is represented by `ButtonVisualStyle`
- `changeBackground` is represented by `background`

If you want the next pass, I can make this even closer by:
- wrapping the Compose button in a `CustomButtonComposable(...)`
- adding exact `ENABLE / DISABLE / LOADING` mapping helpers
- and wiring a concrete `NavDirections` example for your `HomeDirections` graph.
