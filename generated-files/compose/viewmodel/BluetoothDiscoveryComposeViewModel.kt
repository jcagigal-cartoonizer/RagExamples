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
// # Block 99-3: import android.app.Application
class BluetoothDiscoveryComposeViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bluetoothUseCase: BluetoothUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(BluetoothDiscoveryUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<BluetoothDiscoveryUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    init {
        viewModelScope.launch {
            licensingUseCase.getLicensingParameters()?.let {
                _uiState.update { s -> s.copy(showExternalGpsCheckbox = it.isAskInternalGPS) }
            }
            sessionUseCase.getSession()?.let { session ->
                _uiState.update { s -> s.copy(externalGpsChecked = session.useExternalGPS ?: false) }
            }
        }
    }
    fun onScreenStarted(hasBluetoothPermission: Boolean, hasLocationPermission: Boolean) {
        _uiState.update {
            it.copy(
                hasBluetoothPermission = hasBluetoothPermission,
                hasLocationPermission = hasLocationPermission
            )
        }
        checkBluetoothActivated()
        checkLocationActivated()
        getSavedDevice(hasBluetoothPermission)
    }
    fun onDiscoverClicked() {
        if (!PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, context)) {
            emitDialogForPermission(
                title = context.getString(R.string.bluetooth),
                description = context.getString(R.string.dialog_no_bt_permission_desc),
                permissionType = PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH
            )
            return
        }
        _uiState.update { it.copy(isDiscovering = true) }
        viewModelScope.launch {
            bluetoothLocalUseCase.getNearbyDevices(
                // if your use case pushes directly to a flow, keep the old pattern by exposing it externally
                // here we only preserve architecture; implementation depends on your existing use case API
            )
        }
    }
    fun onDiscoveryDeviceReceived(device: BluetoothInfo?) {
        if (device == null) return
        if (device.name == BluetoothInfo.NO_FOUND_DEVICES_NAME) {
            viewModelScope.launch {
                _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.bluetooth_no_devices_found))
            }
            _uiState.update { it.copy(isDiscovering = false) }
            return
        }
        _uiState.update { state ->
            val merged = state.discoveredDevices.toMutableList()
            if (merged.none { it.macAddress == device.macAddress }) merged.add(device)
            state.copy(discoveredDevices = merged, isDiscovering = false)
        }
    }
    fun onLocalDevicesLoaded(savedDevices: List<BluetoothInfo?>) {
        val distinct = savedDevices.filterNotNull().distinctBy { it.macAddress }
        val paired = distinct.find { it.type == BluetoothInfo.BluetoothDiscoveryType.PAIRED }
        _uiState.update { state ->
            state.copy(
                savedDevice = paired,
                isSavedDeviceLoaded = paired != null && paired.name.isNotBlank(),
                titleText = paired?.name ?: "Bluetooth",
                discoveredDevices = distinct
            )
        }
    }
    fun onExternalGpsCheckedChanged(isChecked: Boolean) {
        _uiState.update { it.copy(externalGpsChecked = isChecked) }
    }
    fun onAcceptClicked(selectedDevice: BluetoothInfo?) {
        if (selectedDevice != null) {
            saveDevice(selectedDevice.copy(gpsExternal = uiState.value.externalGpsChecked))
        } else {
            changeExternalGps(uiState.value.externalGpsChecked)
            viewModelScope.launch { _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack) }
        }
    }
    fun onCancelClicked() {
        viewModelScope.launch { _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack) }
    }
    fun onDeviceSelected(device: BluetoothInfo) {
        _uiState.update { it.copy(selectedDevice = device) }
    }
    fun onDeviceDeselected(device: BluetoothInfo, shouldContinue: (Boolean) -> Unit) {
        val current = uiState.value.savedDevice
        if (uiState.value.isSavedDeviceLoaded && device == current) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.unlink),
                            buttons = listOf(
                                ifac.td.taxi.ui.screen.bluetooth.BluetoothDialogButton.Cancel,
                                ifac.td.taxi.ui.screen.bluetooth.BluetoothDialogButton.Accept
                            ),
                            cancellable = false
                        )
                    )
                )
            }
            shouldContinue(false)
        } else {
            shouldContinue(true)
            _uiState.update { it.copy(titleText = "Bluetooth") }
        }
    }
    fun confirmRemoveSavedDevice(device: BluetoothInfo) {
        removeDevice(device)
    }
    fun checkBluetoothActivated() {
        _uiState.update {
            it.copy(isBluetoothEnabled = isBluetoothEnabled())
        }
        if (!uiState.value.isBluetoothEnabled) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.bluetooth),
                            description = context.getString(R.string.dialog_activate_bt_desc)
                        )
                    )
                )
            }
        }
    }
    fun checkLocationActivated() {
        _uiState.update {
            it.copy(isLocationEnabled = isLocationEnabled())
        }
        if (!uiState.value.isLocationEnabled) {
            viewModelScope.launch {
                _uiEffect.emit(
                    BluetoothDiscoveryUiEffect.ShowDialog(
                        BluetoothDiscoveryDialogState(
                            title = context.getString(R.string.strLocalizacion),
                            description = context.getString(R.string.dialog_activate_location_desc)
                        )
                    )
                )
            }
        }
    }
    fun onDialogAccepted(title: String) {
        when (title) {
            context.getString(R.string.bluetooth) -> {
                if (!uiState.value.hasBluetoothPermission) {
                    viewModelScope.launch {
                        _uiEffect.emit(BluetoothDiscoveryUiEffect.RequestBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT))
                    }
                } else if (!isBluetoothEnabled()) {
                    viewModelScope.launch {
                        _uiEffect.emit(
                            BluetoothDiscoveryUiEffect.OpenBluetoothSettings(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        )
                    }
                }
            }
            context.getString(R.string.strLocalizacion) -> {
                if (!uiState.value.hasLocationPermission) {
                    viewModelScope.launch {
                        _uiEffect.emit(BluetoothDiscoveryUiEffect.RequestLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                } else if (!isLocationEnabled()) {
                    viewModelScope.launch {
                        _uiEffect.emit(
                            BluetoothDiscoveryUiEffect.OpenLocationSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                }
            }
        }
    }
    fun saveDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualSession = sessionUseCase.getSession()
            val username = actualSession?.username
            val password = actualSession?.password
            if (username != null && password != null) {
                val bluetoothLicensing = licensingUseCase.addLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )
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
                    if (!bluetoothLicensing.pin.isNullOrBlank() && bluetoothLicensing.pin != "null") {
                        _uiEffect.emit(
                            BluetoothDiscoveryUiEffect.ShowDialog(
                                BluetoothDiscoveryDialogState(
                                    title = context.getString(R.string.dialog_pin_bluetooth_title),
                                    description = context.getString(
                                        R.string.dialog_bluetooth_device_success_desc,
                                        bluetoothLicensing.result.numSerial,
                                        bluetoothLicensing.pin
                                    ),
                                    buttons = listOf(ifac.td.taxi.ui.screen.bluetooth.BluetoothDialogButton.Accept)
                                )
                            )
                        )
                    }
                    _uiState.update { it.copy(savedDevice = device, titleText = device.name) }
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
                        _uiEffect.emit(BluetoothDiscoveryUiEffect.NavigateBack)
                    } else {
                        val btMessage = getBluetoothMessage(
                            bluetoothLicensing.result.codiMissatge,
                            bluetoothLicensing.result.numSerial,
                            bluetoothLicensing.result.message
                        )
                        _uiEffect.emit(
                            BluetoothDiscoveryUiEffect.ShowDialog(
                                BluetoothDiscoveryDialogState(
                                    title = context.getString(R.string.dialog_pin_bluetooth_title),
                                    description = btMessage,
                                    buttons = listOf(ifac.td.taxi.ui.screen.bluetooth.BluetoothDialogButton.Accept)
                                )
                            )
                        )
                    }
                }
            }
        }
    }
    fun removeDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualSession = sessionUseCase.getSession()
            val username = actualSession?.username
            val password = actualSession?.password
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
                } else {
                    _uiEffect.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_server_error))
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
    fun getSavedDevice(hasPermission: Boolean) {
        viewModelScope.launch {
            val bluetoothPaired = bluetoothLocalUseCase.getLocalBluetooth()
            val devices = mutableListOf<BluetoothInfo>()
            if (bluetoothPaired != null) devices.add(bluetoothPaired)
            if (hasPermission) devices.addAll(bluetoothLocalUseCase.getBondedDevices())
            _uiState.update {
                it.copy(
                    savedDevice = bluetoothPaired,
                    discoveredDevices = devices.distinctBy { d -> d.macAddress },
                    isSavedDeviceLoaded = bluetoothPaired != null
                )
            }
        }
    }
    fun emitDialogForPermission(
        title: String,
        description: String,
        permissionType: PermissionRequest.PermissionTypeList
    ) {
        viewModelScope.launch {
            _uiEffect.emit(
                BluetoothDiscoveryUiEffect.ShowDialog(
                    BluetoothDiscoveryDialogState(
                        title = title,
                        description = description
                    )
                )
            )
        }
    }
    fun getBluetoothMessage(messageCode: Int, serialNumber: String, msg: String): String {
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
    fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}
