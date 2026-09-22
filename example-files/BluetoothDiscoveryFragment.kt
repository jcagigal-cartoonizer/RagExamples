package ifac.td.taxi.ui.screen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentBluetoothDiscoveryBinding
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.model.BluetoothInfo.Companion.NO_FOUND_DEVICES_NAME
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.BluetoothDiscoveryAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.BluetoothDiscoveryViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BluetoothDiscoveryFragment :
    BaseFragment<FragmentBluetoothDiscoveryBinding, BluetoothDiscoveryViewModel>(
        R.layout.fragment_bluetooth_discovery
    ) {

    private val vModel: BluetoothDiscoveryViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val TAG = "BluetoothDiscoveryFragment"

    private lateinit var adapter: BluetoothDiscoveryAdapter
    private lateinit var deviceBT: BluetoothInfo
    private var locallySavedDevice: BluetoothInfo? = null
    private var isLoaded: Boolean = false

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentBluetoothDiscoveryBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
        checkBluetoothPermissions()
        checkLocationPermissions()
        initRecyclerView()
        setUpListeners()


        val bluetoothPermission = PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH, context)
        vModel.getSavedDevice(bluetoothPermission)
        sharedViewModel.checkTaximeterAvailability()
    }

    override fun onPause() {
        vModel.cancelDiscovery()
        super.onPause()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                clickOnAccept()
            }

            btnCancel.setAction {
                iMainActivity.navigateBack()
            }

            btnDiscover.setAction {
                vModel.getDevices()
                btnDiscover.setButtonStyle(CustomButton.StyleButton.LOADING)
            }
        }
    }

    private fun checkBluetoothPermissions() {
        context?.let {
            PermissionRequest.needBluetoothPermission(it) { hasPermission ->
                if (!hasPermission) {
                    showPermissionDialog(
                        it.getString(R.string.bluetooth),
                        it.getString(R.string.dialog_no_bt_permission_desc),
                        PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH
                    )
                } else {
                    vModel.checkBluetoothActivated()
                }
            }
        }
    }

    private fun checkLocationPermissions() {
        context?.let {
            PermissionRequest.needLocationPermission(it) { hasPermission ->
                if (!hasPermission) {
                    showPermissionDialog(
                        it.getString(R.string.strLocalizacion),
                        it.getString(R.string.no_location_permission),
                        PermissionRequest.PermissionTypeList.PERMISSION_LOCATION
                    )
                } else {
                    vModel.checkLocationActivated()
                }
            }
        }
    }

    private fun showPermissionDialog(
        title: String,
        description: String,
        permissionType: PermissionRequest.PermissionTypeList
    ) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                iMainActivity.requestPermission(permissionType)
                if (permissionType == PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH) {
                    vModel.checkBluetoothActivated()
                } else {
                    vModel.checkLocationActivated()
                }
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = title,
                description = description,
                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
            ), callback,
            fragmentManager = childFragmentManager
        )
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.bluetoothDiscoveryFlow.collect { device ->
                        Logs.d(TAG, "device: $device")
                        vBinding.btnDiscover.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        if (device != null) {
                            if (device.name != NO_FOUND_DEVICES_NAME) {
                                val devices = adapter.getDevices().toMutableList()
                                if (devices.find { it.macAddress == device.macAddress } == null) {
                                    devices.add(device)
                                    adapter.updateDevices(devices)
                                }
                            } else {
                                iMainActivity.showToast(R.string.bluetooth_no_devices_found)
                            }
                        }
                    }
                }

                launch {
                    vModel.bluetoothLocalFlow.collect { savedDevices ->
                        val paired = savedDevices.find { it?.type == BluetoothInfo.BluetoothDiscoveryType.PAIRED}

                        isLoaded = paired != null && paired.name != ""

                        context?.let {
                            if (isLoaded && paired != null) {
                                locallySavedDevice = paired
                                deviceBT = paired
                                //iMainActivity.showToast(it.getString(R.string.toast_obtenido, paired.name))
                                animateTextAndImage(paired.name, getTaximeterImageResource(paired.name))
                                //adapter.addDevice(paired)
                            } else {
                                locallySavedDevice = null
                                animateTextAndImage(it.getString(R.string.bluetooth), R.drawable.background_dialog_transparent)
                            }
                        }

                        adapter.updateDevices(savedDevices.distinctBy { it?.macAddress })
                    }
                }

                launch {
                    vModel.finishSaveFlow.collect {
                        it?.let { (message, goBack) ->
                            sharedViewModel.checkTaximeterAvailability()
                            if (goBack) {
                                iMainActivity.connectTaximeter()
                                iMainActivity.navigateBack()
                            } else {
                                locallySavedDevice = null
                                if (adapter.isDeviceSelected()) {
                                    val selectedDevice = adapter.getSelectedDevice()
                                    animateTextAndImage(selectedDevice?.name ?: (context?.getString(R.string.bluetooth) ?: "Bluetooth"), getTaximeterImageResource(selectedDevice?.name) ?: R.drawable.background_dialog_transparent)
                                }
                            }
                        }
                    }
                }
                launch {
                    vModel.hasExternalGpsFlow.collect {
                        it?.let { hasExternalGps ->
                            vBinding.cbGPSExternalBluetooth.isChecked = hasExternalGps
                        }
                    }
                }
                launch {
                    vModel.hasExternalGpsPermissionFlow.collect {
                        it?.let { permission ->
                            vBinding.cbGPSExternalBluetooth.visibility =
                                if (permission) View.VISIBLE else View.GONE
                        }
                    }
                }
                launch {
                    vModel.hasBluetoothActivatedFlow.collect { isBluetoothActivated ->
                        if (!isBluetoothActivated) {
                            context?.let { context ->
                                val bluetoothManager =
                                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                                val bluetoothAdapter = bluetoothManager.adapter
                                val callback: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT && !bluetoothAdapter.isEnabled) {
                                            val enableBtIntent =
                                                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                            if (ActivityCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.BLUETOOTH_CONNECT
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                iMainActivity.showToast(R.string.activate_bt_toast)
                                            }
                                            context.startActivity(enableBtIntent)
                                        }
                                    }
                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = context.getString(R.string.bluetooth),
                                        description = context.getString(R.string.dialog_activate_bt_desc),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ), callback
                                )
                            }
                        }
                    }
                }

                launch {
                    vModel.hasGPSActivatedFlow.collect { isGPSActivated ->
                        if (!isGPSActivated) {
                            context?.let { context ->
                                val callback: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT) {
                                            val locationManager =
                                                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                                val enableLocationIntent =
                                                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                                context.startActivity(enableLocationIntent)
                                            }
                                        }
                                    }
                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = context?.getString(R.string.strLocalizacion),
                                        description = context?.getString(R.string.dialog_activate_location_desc),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ), callback
                                )
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.shiftStatusFlow.collect {
                        it?.let { status ->
                            when(status.currentStatus) {
                                ifConstants.STATE_DISCONNECTED,
                                ifConstants.STATE_UNKNOWN -> {
                                    vBinding.btnDiscover.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                }
                                else -> {
                                    vBinding.btnDiscover.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                    vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpListeners() {
        vBinding.apply {
            cbGPSExternalBluetooth.setOnCheckedChangeListener { _, isChecked ->
                Logs.d(TAG, "cbGPSExternalBluetooth. isChecked: $isChecked")
            }
        }
    }

    private fun clickOnAccept() {
        val selectedDevice = adapter.getSelectedDevice()
        when {
            selectedDevice != null -> {
                deviceBT = selectedDevice
                deviceBT.gpsExternal = vBinding.cbGPSExternalBluetooth.isChecked
                vModel.saveDevice(deviceBT)
                sharedViewModel.updateGpsExternal(vBinding.cbGPSExternalBluetooth.isChecked)
            }
            else -> {
                sharedViewModel.checkTaximeterAvailability()
                vModel.changeExternalGps(vBinding.cbGPSExternalBluetooth.isChecked)
                iMainActivity.navigateBack()
                sharedViewModel.updateGpsExternal(vBinding.cbGPSExternalBluetooth.isChecked)
            }
        }
    }

    private fun initRecyclerView() {
        adapter = BluetoothDiscoveryAdapter(
            onDeviceSelected = { device -> handleDeviceSelection(device) },
            onDeviceDeselected = { device, shouldContinue ->
                if (sharedViewModel.shiftStatusFlow.value?.currentStatus == ifConstants.STATE_DISCONNECTED) {
                    handleDeviceDeselection(device, shouldContinue)
                }
            }
        )

        vBinding.rvBluetoothDiscovery.layoutManager = LinearLayoutManager(requireContext())
        vBinding.rvBluetoothDiscovery.adapter = adapter
    }

    fun handleDeviceSelection(device: BluetoothInfo) {
        if (vBinding.tvBluetoothTitle.text != device.name) {
            deviceBT = device

            if (locallySavedDevice != null) {
                if (device != locallySavedDevice) {
                    val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            locallySavedDevice?.let {
                                vModel.removeDevice(it)
                            }

                            iMainActivity.showToast(R.string.toast_dispositivo_eliminado)

                        } else {
                            adapter.cancelAndBackSelection()
                        }
                    }
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = context?.getString(R.string.bluetooth),
                            description = context?.getString(R.string.deselect_confirm),
                            buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            isCancellable = false
                        ), callBack,
                        fragmentManager = childFragmentManager
                    )
                }
            } else {
                val imageResource: Int?
                val newText: String

                if (adapter.isDeviceSelected()) {
                    imageResource = getTaximeterImageResource(device.name)
                    newText = device.name
                } else {
                    imageResource = R.drawable.background_dialog_transparent
                    newText = context?.getString(R.string.bluetooth) ?: "Bluetooth"
                }

                animateTextAndImage(newText, imageResource)
            }
        }
    }

    private fun handleDeviceDeselection(device: BluetoothInfo, shouldContinue: (Boolean) -> Unit) {
        Logs.d(TAG, "handleDeviceDeselection: $device")
        if (isLoaded && device == locallySavedDevice) {
            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    vModel.removeDevice(device)
                    iMainActivity.showToast(R.string.toast_dispositivo_eliminado)

                    shouldContinue(true) // Confirmar deselección
                    animateTextAndImage(context?.getString(R.string.bluetooth) ?: "Bluetooth", R.drawable.background_dialog_transparent)
                } else {
                    shouldContinue(false) // Cancelar deselección
                }
            }
            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = context?.getString(R.string.bluetooth),
                    description = context?.getString(R.string.unlink),
                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                ), callBack,
                fragmentManager = childFragmentManager
            )
        } else {
            shouldContinue(true)
            animateTextAndImage(context?.getString(R.string.bluetooth) ?: "Bluetooth", R.drawable.background_dialog_transparent)
        }
    }

    private fun getTaximeterImageResource(deviceName: String?): Int? {
        return when {
            deviceName == null -> null
            deviceName.startsWith("SKYG") -> R.drawable.vinculacion_sky
            deviceName.startsWith("URBA") || deviceName.startsWith("URB1") -> R.drawable.vinculacion_urba
            deviceName.startsWith("BG40") -> R.drawable.vinculacion_bg_bl_11
            deviceName.startsWith("BL60") -> R.drawable.vinculacion_bg_bl_09
            else -> R.drawable.vinculacion_tx
        }
    }


    private fun animateTextAndImage(newText: String, imageResource: Int?) {
        vBinding.tvBluetoothTitle.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                vBinding.tvBluetoothTitle.text = when {
                    newText.startsWith("SKYG") -> "TX80/SkyGlass"
                    newText.startsWith("URBA") || newText.startsWith("URB1") -> "TX80/Urba"
                    newText.startsWith("BG40") -> "BG40"
                    newText.startsWith("BL60") -> "BL60"
                    else -> newText
                }

                vBinding.tvBluetoothTitle.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        vBinding.ivTaximeterType.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                vBinding.ivTaximeterType.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        imageResource ?: R.drawable.vinculacion_tx,
                        null
                    )
                )
                vBinding.ivTaximeterType.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
}
