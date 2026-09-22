package ifac.td.taxi.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.lifecycle.viewModelScope
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
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothDiscoveryViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bluetoothUseCase: BluetoothUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "BluetoothDiscoveryViewModel"


    private val BLUETOOTH_AUTOCAB: String = "GPC"
    private val BLUETOOTH_STRUCTAB: String = "iTop"

    private val _bluetoothDiscoveryFlow = MutableStateFlow<BluetoothInfo?>(null)
    val bluetoothDiscoveryFlow = _bluetoothDiscoveryFlow.asStateFlow()

    private val _bluetoothLocalFlow = MutableSharedFlow<List<BluetoothInfo?>>()
    val bluetoothLocalFlow = _bluetoothLocalFlow.asSharedFlow()

    private val _finishSaveFlow = MutableStateFlow<Pair<String, Boolean>?>(null)
    val finishSaveFlow = _finishSaveFlow.asStateFlow()

    private val _hasExternalGpsFlow = MutableStateFlow<Boolean?>(null)
    val hasExternalGpsFlow = _hasExternalGpsFlow.asStateFlow()

    private val _hasExternalGpsPermissionFlow = MutableStateFlow<Boolean?>(null)
    val hasExternalGpsPermissionFlow = _hasExternalGpsPermissionFlow.asStateFlow()

    private val _hasBluetoothActivatedFlow = MutableSharedFlow<Boolean>()
    val hasBluetoothActivatedFlow = _hasBluetoothActivatedFlow.asSharedFlow()

    private val _hasGPSActivatedFlow = MutableSharedFlow<Boolean>()
    val hasGPSActivatedFlow = _hasGPSActivatedFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            licensingUseCase.getLicensingParameters()?.let {
                _hasExternalGpsPermissionFlow.emit(it.isAskInternalGPS)
            }
            sessionUseCase.getSession()?.let { session ->
                session.useExternalGPS?.let {
                    _hasExternalGpsFlow.emit(it)
                }
            }
        }
    }

    fun getDevices() {
        viewModelScope.launch {
            if (PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, context)) {
                Logs.d(TAG, "getDevices: Con permisos")
                bluetoothLocalUseCase.getNearbyDevices(_bluetoothDiscoveryFlow)
            } else {
                Logs.d(TAG, "getDevices: Sin permisos")
                //_permissionsLiveData.postValue(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH)
            }
            licensingUseCase.getLicensingParameters()?.isAllowFakeGPS
            licensingUseCase.getLicensingParameters()?.isAskInternalGPS
        }
    }

    fun cancelDiscovery() {
        viewModelScope.launch {
            bluetoothLocalUseCase.cancelDiscovery()
        }
    }

    fun saveDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "saveDevice: Start saving device")

            val actualSession = sessionUseCase.getSession()
            val username = actualSession?.username
            val password = actualSession?.password

            Logs.d(TAG, "saveDevice: Session retrieved - username: $username")

            if (username != null && password != null) {
                Logs.d(TAG, "saveDevice: Adding licensing bluetooth config")

                val bluetoothLicensing = licensingUseCase.addLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )

                val isItop = BluetoothInfo.isItop(device.name)
                android.util.Log.d(TAG,"isItop: $isItop")
                if (isItop) {
                    if (device.gpsExternal) {
                        device.gpsExternal = false
                        Logs.d(TAG,"isItop: true -> gpsExternal = false")
                    }
                }
                if (device.gpsExternal != hasExternalGpsFlow.value) {
                    //android.util.Log.d(TAG,"saveDevice: Updating external GPS setting $device.gpsExternal")
                    Logs.d(TAG, "saveDevice: Updating external GPS setting $device.gpsExternal\")")
                    sessionUseCase.updateExternalGPS(device.gpsExternal)
                }

                if (bluetoothLicensing != null && bluetoothLicensing.nameBT.isNotEmpty()) {
                    Logs.d(TAG, "saveDevice: Licensing returned with valid nameBT")

                    bluetoothLicensing.let {
                        bluetoothLocalUseCase.saveLocalBluetooth(
                            BluetoothInfo(
                                name = bluetoothLicensing.nameBT.checkTaximeterType(),
                                macAddress = bluetoothLicensing.addressMAC
                            )
                        )
                        Logs.d(TAG, "saveDevice: Saved local bluetooth info")

                        bluetoothLocalUseCase.savePinLocalBluetooth(
                            pin = bluetoothLicensing.pin,
                            address = bluetoothLicensing.addressMAC
                        )
                        Logs.d(TAG, "saveDevice: Saved local bluetooth PIN")

                        if (bluetoothLicensing.pin != null && !bluetoothLicensing.pin.equals("null") && bluetoothLicensing.pin.isNotBlank()) {
                            Logs.d(TAG, "saveDevice: Showing dialog with valid PIN")
                            showDialog(
                                Pair(
                                    CustomDialog.CustomDialogModel(
                                        title = context.getString(R.string.dialog_pin_bluetooth_title),
                                        description = context.getString(
                                            R.string.dialog_bluetooth_device_success_desc,
                                            bluetoothLicensing.result.numSerial,
                                            bluetoothLicensing.pin
                                        ),
                                        buttons = arrayListOf(
                                            ButtonType.ACCEPT,
                                        )
                                    ),
                                ) { response ->

                                }
                            )
                        }

                        _finishSaveFlow.emit(Pair(bluetoothLicensing.pin ?: "NO PIN ERROR", true))
                        Logs.d(TAG, "saveDevice: Emitted finish save flow with PIN")
                    }

                } else if (bluetoothLicensing != null) {
                    Logs.d(TAG, "saveDevice: Licensing returned without nameBT, using fallback")

                    val hardcodePin = "1234"
                    if (device.name.startsWith(BLUETOOTH_AUTOCAB)) {
                        bluetoothLocalUseCase.saveLocalBluetooth(
                            BluetoothInfo(
                                name = device.name,
                                macAddress = device.macAddress
                            )
                        )
                        Logs.d(TAG, "saveDevice: Saved fallback local bluetooth info")

                        bluetoothLocalUseCase.savePinLocalBluetooth(
                            pin = hardcodePin,
                            address = device.macAddress
                        )
                        Logs.d(TAG, "saveDevice: Saved fallback PIN")

                        _finishSaveFlow.emit(Pair(bluetoothLicensing.pin ?: hardcodePin, true))
                        Logs.d(TAG, "saveDevice: Emitted finish save flow with fallback PIN")
                    } else {
                        Logs.d(TAG, "saveDevice: Showing error dialog for failed licensing")

                        val btMessage = getBluetoothMessage(
                            bluetoothLicensing.result.codiMissatge,
                            bluetoothLicensing.result.numSerial,
                            bluetoothLicensing.result.message
                        )

                        showDialog(
                            Pair(
                                CustomDialog.CustomDialogModel(
                                    title = context.getString(R.string.dialog_pin_bluetooth_title),
                                    description = btMessage,
                                    buttons = arrayListOf(
                                        ButtonType.ACCEPT,
                                    )
                                ),
                            ) { response ->

                            }
                        )

                        _finishSaveFlow.emit(Pair(btMessage, true))
                        Logs.d(TAG, "saveDevice: Emitted finish save flow with error message")
                    }
                } else {
                    Logs.e(TAG, "saveDevice: Licensing failed - null result")
                }
            } else {
                Logs.e(TAG, "saveDevice: Session is null or missing credentials")
            }
        }
    }

    private fun getBluetoothMessage(messageCode: Int, serialNumber: String, msg: String): String {
        Logs.d(TAG, "getBluetoothMessage: codiMissatge = $messageCode msg $msg")
        var translated = msg
        when (messageCode) {
            Result.LCE_WRONG_PARAMETERS -> translated =
                context.getString(R.string.lce_wrong_parameters)

            Result.LCE_BT_WRONG_HW_VERSION -> translated =
                context.getString(R.string.lce_bt_wrong_hw_version)

            Result.LCE_TX_WRONG_HW_VERSION -> translated =
                context.getString(R.string.lce_tx_wrong_hw_version)

            Result.LCE_BT_WRONG_DEVICE_TYPE -> translated =
                context.getString(R.string.lce_bt_wrong_device_type)

            Result.LCE_SERIAL_NUMBER_OWNED_ANOTHER_USER -> translated =
                String.format(
                    context.getString(R.string.lce_serial_number_owned_another_user),
                    serialNumber
                )

            Result.LCE_BT_DEVICE_ALREADY_LINKED -> translated =
                context.getString(R.string.lce_bt_device_already_linked)
        }
        return translated
    }

    fun removeDevice(device: BluetoothInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "removeDevice: Start removing device")
            val actualSession = sessionUseCase.getSession()
            val username = actualSession?.username
            val password = actualSession?.password

            Logs.d(TAG, "removeDevice: Session retrieved - username: $username")

            if (username != null && password != null) {
                Logs.d(TAG, "removeDevice: Calling removeLicensingBluetoothConfig")

                val bluetoothLicensing = licensingUseCase.removeLicensingBluetoothConfig(
                    nameBT = device.name.checkTaximeterType(),
                    macAddress = device.macAddress,
                    user = username,
                    password = password
                )

                if (bluetoothLicensing != null) {
                    Logs.d(TAG, "removeDevice: Licensing removal successful")

                    taximeterConnectUseCase.taximeterDisconnect()
                    Logs.d(TAG, "removeDevice: Disconnected taximeter")

                    deleteDevice()
                    Logs.d(TAG, "removeDevice: Deleted local device info")

                    showDialog(
                        Pair(
                            CustomDialog.CustomDialogModel(
                                title = context.getString(R.string.dialog_pin_bluetooth_title),
                                description = context.getString(R.string.dialog_bluetooth_device_unistall_desc, bluetoothLicensing.result.numSerial),
                                buttons = arrayListOf(
                                    ButtonType.ACCEPT,
                                )
                            ),
                        ) { response -> }
                    )

                    Logs.d(TAG, "removeDevice: Showing success dialog")
                    _finishSaveFlow.emit(Pair(context.getString(R.string.toast_bluetooth_remove_ok), false))
                    Logs.d(TAG, "removeDevice: Emitted finish flow with success message")

                } else {
                    Logs.e(TAG, "removeDevice: Licensing removal failed - server error")

                    showDialog(
                        Pair(
                            CustomDialog.CustomDialogModel(
                                title = context.getString(R.string.dialog_pin_bluetooth_title),
                                description = context.getString(R.string.dialog_bluetooth_device_unistall_error_desc),
                                buttons = arrayListOf(
                                    ButtonType.ACCEPT,
                                )
                            ),
                        ) { response -> }
                    )

                    Logs.d(TAG, "removeDevice: Showing error dialog")
                    _finishSaveFlow.emit(Pair(context.getString(R.string.toast_bluetooth_remove_server_error), false))
                    Logs.d(TAG, "removeDevice: Emitted finish flow with error message")
                }
            } else {
                Logs.e(TAG, "removeDevice: Session is null or missing credentials")
            }
        }
    }

    fun getSavedDevice(hasPermission: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "getSavedDevice: Start retrieving saved devices")
            val bluetoothPaired = bluetoothLocalUseCase.getLocalBluetooth()
            Logs.d(TAG, "getSavedDevice: Retrieved local bluetooth = $bluetoothPaired")

            val newMutableList: MutableList<BluetoothInfo> = mutableListOf()
            if (bluetoothPaired != null) {
                newMutableList.add(bluetoothPaired)
                Logs.d(TAG, "getSavedDevice: Added local bluetooth to list")
            }

            if (hasPermission) {
                Logs.d(TAG, "getSavedDevice: Has permission, retrieving bonded devices")
                val listBluetoothDevice = bluetoothLocalUseCase.getBondedDevices()
                Logs.d(TAG, "getSavedDevice: Retrieved bonded devices = ${listBluetoothDevice.size}")
                newMutableList.addAll(listBluetoothDevice)
            } else {
                Logs.d(TAG, "getSavedDevice: No permission to retrieve bonded devices")
            }

            _bluetoothLocalFlow.emit(newMutableList)
            Logs.d(TAG, "getSavedDevice: Emitted bluetooth list with size = ${newMutableList.size}")
        }
    }


    private fun deleteDevice() {
        viewModelScope.launch {
            bluetoothLocalUseCase.deleteLocalBluetooth()
        }
    }

    fun changeExternalGps(isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionUseCase.getSession()
            session?.let {
                if (it.useExternalGPS != isChecked) {
                    sessionUseCase.updateExternalGPS(isChecked)
                }
            }
        }
    }

    fun checkBluetoothActivated() {
        viewModelScope.launch(Dispatchers.Main) {
            PermissionRequest.needBluetoothPermission(context) { hasPermission ->
                if (!isBluetoothEnabled()) {
                    viewModelScope.launch {
                        _hasBluetoothActivatedFlow.emit(false)
                    }
                } else {
                    viewModelScope.launch {
                        _hasBluetoothActivatedFlow.emit(true)
                    }
                }
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    fun checkLocationActivated() {
        viewModelScope.launch(Dispatchers.Main) {
            PermissionRequest.needLocationPermission(context) { hasPermission ->
                if (!isLocationEnabled()) {
                    viewModelScope.launch {
                        _hasGPSActivatedFlow.emit(false)
                    }
                } else {
                    viewModelScope.launch {
                        _hasGPSActivatedFlow.emit(true)
                    }
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
    }
}