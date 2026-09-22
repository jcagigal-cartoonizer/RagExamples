package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.model.BluetoothInfo.Companion.checkTaximeterType
import ifac.td.taxi.framework.sdk.usecase.BluetoothUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class BluetoothDiscoveryComposeViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bluetoothUseCase: BluetoothUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext
    private val _uiState = MutableStateFlow(BluetoothDiscoveryUiState())
    val uiState: StateFlow<BluetoothDiscoveryUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<BluetoothDiscoveryUiEffect>()
    val effects: SharedFlow<BluetoothDiscoveryUiEffect> = _effects.asSharedFlow()
    fun onScreenStarted() {
        viewModelScope.launch {
            loadSavedDevices()
            refreshActivationState()
            refreshPermissionsUi()
        }
    }
    fun onUiEvent(event: BluetoothDiscoveryUiEvent) {
        when (event) {
            BluetoothDiscoveryUiEvent.ScreenStarted -> onScreenStarted()
            BluetoothDiscoveryUiEvent.DiscoverClicked -> discoverDevices()
            BluetoothDiscoveryUiEvent.AcceptClicked -> accept()
            BluetoothDiscoveryUiEvent.CancelClicked -> emitEffect(BluetoothDiscoveryUiEffect.NavigateBack)
            is BluetoothDiscoveryUiEvent.DeviceSelected -> handleDeviceSelected(event.device)
            is BluetoothDiscoveryUiEvent.DeviceDeselected -> handleDeviceDeselected(event.device)
            is BluetoothDiscoveryUiEvent.ExternalGpsChanged -> {
                _uiState.update { it.copy(externalGpsEnabled = event.checked) }
                changeExternalGps(event.checked)
            }
            is BluetoothDiscoveryUiEvent.BluetoothPermissionResult -> {
                if (event.granted) checkBluetoothActivated() else refreshPermissionsUi()
            }
            is BluetoothDiscoveryUiEvent.LocationPermissionResult -> {
                if (event.granted) checkLocationActivated() else refreshPermissionsUi()
            }
            BluetoothDiscoveryUiEvent.BluetoothDialogAccept -> emitEffect(BluetoothDiscoveryUiEffect.OpenBluetoothSettings)
            BluetoothDiscoveryUiEvent.LocationDialogAccept -> emitEffect(BluetoothDiscoveryUiEffect.OpenLocationSettings)
            BluetoothDiscoveryUiEvent.PermissionDialogCancel -> emitEffect(BluetoothDiscoveryUiEffect.HideDialog)
        }
    }
    fun onDialogAction(action: BluetoothDiscoveryDialogAction) {
        when (action) {
            BluetoothDiscoveryDialogAction.Accept -> {
                _uiState.value.pendingRemoveDevice?.let { removeDevice(it) }
                _uiState.value.pendingDeselectDevice?.let { pending ->
                    _uiState.update { it.copy(pendingDeselectDevice = null) }
                }
                emitEffect(BluetoothDiscoveryUiEffect.HideDialog)
            }
            BluetoothDiscoveryDialogAction.Cancel,
            BluetoothDiscoveryDialogAction.Dismiss -> {
                _uiState.update { it.copy(pendingRemoveDevice = null, pendingDeselectDevice = null) }
                emitEffect(BluetoothDiscoveryUiEffect.HideDialog)
            }
        }
    }
    fun discoverDevices() {
        if (!PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, appContext)) {
            emitDialog(
                BluetoothDiscoveryDialogType.BluetoothPermission,
                appContext.getString(R.string.bluetooth),
                appContext.getString(R.string.dialog_no_bt_permission_desc)
            )
            return
        }
        _uiState.update { it.copy(buttons = it.buttons.copy(discover = BluetoothDiscoveryButtonVisualState.PrimaryLoading), isDiscovering = true) }
        viewModelScope.launch {
            bluetoothLocalUseCase.getNearbyDevices(null)
            _uiState.update { it.copy(buttons = it.buttons.copy(discover = BluetoothDiscoveryButtonVisualState.PrimaryEnabled), isDiscovering = false) }
        }
    }
    fun accept() {
        val selected = _uiState.value.selectedDevice
        if (selected != null) {
            saveDevice(selected)
        } else {
            changeExternalGps(_uiState.value.externalGpsEnabled)
            viewModelScope.launch { emitEffect(BluetoothDiscoveryUiEffect.NavigateBack) }
        }
    }
    fun handleDeviceSelected(device: BluetoothInfo) {
        val currentSaved = _uiState.value.savedDevice
        if (currentSaved != null && device != currentSaved) {
            _uiState.update { it.copy(pendingRemoveDevice = currentSaved) }
            emitDialog(
                BluetoothDiscoveryDialogType.ReplaceSavedDevice,
                appContext.getString(R.string.bluetooth),
                appContext.getString(R.string.deselect_confirm)
            )
            return
        }
        _uiState.update {
            it.copy(
                selectedDevice = device,
                title = displayTitleFor(device.name),
                taximeterImageRes = taximeterImageFor(device.name)
            )
        }
    }
    fun handleDeviceDeselected(device: BluetoothInfo) {
        val saved = _uiState.value.savedDevice
        if (_uiState.value.isLoaded && saved != null && device == saved) {
            _uiState.update { it.copy(pendingRemoveDevice = device) }
            emitDialog(
                BluetoothDiscoveryDialogType.UnlinkSavedDevice,
                appContext.getString(R.string.bluetooth),
                appContext.getString(R.string.unlink)
            )
        } else {
            _uiState.update {
                it.copy(selectedDevice = null, title = appContext.getString(R.string.bluetooth), taximeterImageRes = R.drawable.background_dialog_transparent)
            }
        }
    }
    fun loadSavedDevices() {
        viewModelScope.launch {
            val bluetoothPaired = bluetoothLocalUseCase.getLocalBluetooth()
            val bonded = if (PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, appContext)) {
                bluetoothLocalUseCase.getBondedDevices()
            } else emptyList()
            val merged = buildList {
                if (bluetoothPaired != null) add(bluetoothPaired)
                addAll(bonded)
            }.distinctBy { it.macAddress }
            _uiState.update {
                it.copy(
                    devices = merged,
                    savedDevice = bluetoothPaired,
                    isLoaded = bluetoothPaired != null && !bluetoothPaired.name.isNullOrEmpty(),
                    selectedDevice = bluetoothPaired,
                    title = bluetoothPaired?.name?.takeIf { n -> n.isNotBlank() }?.let(::displayTitleFor)
                        ?: appContext.getString(R.string.bluetooth),
                    taximeterImageRes = bluetoothPaired?.name?.let(::taximeterImageFor)
                        ?: R.drawable.background_dialog_transparent,
                    externalGpsEnabled = sessionUseCase.getSession()?.useExternalGPS ?: false
                )
            }
        }
    }
    fun refreshPermissionsUi() {
        val hasBt = PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, appContext)
        val hasLoc = PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_LOCATION, appContext)
        _uiState.update {
            it.copy(
                buttons = resolveButtons(it.buttons, hasBt, hasLoc)
            )
        }
    }
    fun refreshActivationState() {
        checkBluetoothActivated()
        checkLocationActivated()
    }
    fun checkBluetoothActivated() {
        val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        _uiState.update { it.copy(isBluetoothEnabled = manager.adapter?.isEnabled == true) }
        if (!manager.adapter?.isEnabled.orFalse()) {
            emitDialog(
                BluetoothDiscoveryDialogType.EnableBluetooth,
                appContext.getString(R.string.bluetooth),
                appContext.getString(R.string.dialog_activate_bt_desc)
            )
        }
    }
    fun checkLocationActivated() {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        _uiState.update { it.copy(isLocationEnabled = enabled) }
        if (!enabled) {
            emitDialog(
                BluetoothDiscoveryDialogType.EnableLocation,
                appContext.getString(R.string.strLocalizacion),
                appContext.getString(R.string.dialog_activate_location_desc)
            )
        }
    }
    fun saveDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password
            if (username == null || password == null) return@launch
            val licensingBluetooth = licensingUseCase.addLicensingBluetoothConfig(
                nameBT = device.name.checkTaximeterType(),
                macAddress = device.macAddress,
                user = username,
                password = password
            )
            if (device.gpsExternal != (_uiState.value.externalGpsEnabled)) {
                sessionUseCase.updateExternalGPS(device.gpsExternal)
            }
            if (licensingBluetooth != null && licensingBluetooth.nameBT.isNotEmpty()) {
                bluetoothLocalUseCase.saveLocalBluetooth(
                    BluetoothInfo(
                        name = licensingBluetooth.nameBT.checkTaximeterType(),
                        macAddress = licensingBluetooth.addressMAC
                    )
                )
                bluetoothLocalUseCase.savePinLocalBluetooth(
                    pin = licensingBluetooth.pin,
                    address = licensingBluetooth.addressMAC
                )
                _effects.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_dispositivo_eliminado))
                _effects.emit(BluetoothDiscoveryUiEffect.ConnectTaximeterAndBack)
            } else {
                _effects.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_server_error))
            }
        }
    }
    fun removeDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            val username = session?.username
            val password = session?.password
            if (username == null || password == null) return@launch
            val result = licensingUseCase.removeLicensingBluetoothConfig(
                nameBT = device.name.checkTaximeterType(),
                macAddress = device.macAddress,
                user = username,
                password = password
            )
            if (result != null) {
                taximeterConnectUseCase.taximeterDisconnect()
                bluetoothLocalUseCase.deleteLocalBluetooth()
                _effects.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_dispositivo_eliminado))
                _effects.emit(BluetoothDiscoveryUiEffect.NavigateBack)
            } else {
                _effects.emit(BluetoothDiscoveryUiEffect.ShowToast(R.string.toast_bluetooth_remove_server_error))
            }
        }
    }
    fun changeExternalGps(isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionUseCase.updateExternalGPS(isChecked)
        }
    }
    fun emitDialog(type: BluetoothDiscoveryDialogType, title: String, description: String) {
        viewModelScope.launch {
            _effects.emit(
                BluetoothDiscoveryUiEffect.ShowDialog(
                    BluetoothDiscoveryDialogState(
                        type = type,
                        title = title,
                        description = description
                    )
                )
            )
        }
    }
    fun emitEffect(effect: BluetoothDiscoveryUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
    fun resolveButtons(
        current: BluetoothDiscoveryButtonsState,
        hasBtPermission: Boolean,
        hasLocationPermission: Boolean
    ): BluetoothDiscoveryButtonsState {
        return current.copy(
            discover = if (hasBtPermission) BluetoothDiscoveryButtonVisualState.PrimaryEnabled else BluetoothDiscoveryButtonVisualState.PrimaryDisabled,
            accept = BluetoothDiscoveryButtonVisualState.PrimaryEnabled,
            cancel = BluetoothDiscoveryButtonVisualState.SecondaryEnabled
        )
    }
    fun displayTitleFor(deviceName: String): String = when {
        deviceName.startsWith("SKYG") -> "TX80/SkyGlass"
        deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> "TX80/Urba"
        deviceName.startsWith("BG40") -> "BG40"
        deviceName.startsWith("BL60") -> "BL60"
        else -> deviceName
    }
    fun taximeterImageFor(deviceName: String?): Int = when {
        deviceName == null -> R.drawable.background_dialog_transparent
        deviceName.startsWith("SKYG") -> R.drawable.vinculacion_sky
        deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> R.drawable.vinculacion_urba
        deviceName.startsWith("BG40") -> R.drawable.vinculacion_bg_bl_11
        deviceName.startsWith("BL60") -> R.drawable.vinculacion_bg_bl_09
        else -> R.drawable.vinculacion_tx
    }
    fun Boolean?.orFalse() = this == true
}
import androidx.compose.foundation.Image
