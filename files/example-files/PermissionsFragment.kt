package ifac.td.taxi.ui.screen

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPermissionsBinding
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.*
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.redSysDialog.RedSysCustomDialog
import ifac.td.taxi.viewmodel.PermissionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class PermissionsFragment :
    BaseFragment<FragmentPermissionsBinding, PermissionsViewModel>(R.layout.fragment_permissions) {

    private val vModel: PermissionsViewModel by viewModel()
    private val safeArgs: PermissionsFragmentArgs by navArgs()

    private val TAG = "PermissionsFragment"

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPermissionsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        setupSwitchListeners()
        vModel.checkRedSysInstalled()
        vModel.checkPhoneNumberConfigured()
        checkSafeArgs(safeArgs)


    }

    override fun onResume() {
        vModel.checkPermissions()
        vModel.checkOverloadPermission()
        vModel.checkSettingsDevicePermission()
        super.onResume()
    }

    private fun checkSafeArgs(args: PermissionsFragmentArgs?) {
        vBinding.apply {
            val blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_animation)
            val permissionBottomMap = mapOf(
                PERMISSION_BLUETOOTH to clBluetoothPermission,
                PERMISSION_CAMERA to clCameraPermission,
                PERMISSION_PHONE_CALL to clPhoneCallsPermission,
                PERMISSION_NOTIFICATION to clNotificationPermission,
                PERMISSION_LOCATION to clLocationPermission,
                PERMISSION_BACKGROUND_LOCATION to clBLPermission,
                PERMISSION_IGNORE_BATTERY_OPTIMIZATIONS to clBatteryPermission,
                PERMISSION_MICROPHONE to clMicrophonePermission,
            )

            args?.permissionType?.let { permissionType ->
                permissionBottomMap[permissionType]?.startAnimation(blinkAnimation)
                svPermissions?.post {
                    permissionBottomMap[permissionType]?.bottom?.let {
                        svPermissions.scrollTo(
                            0,
                            it
                        )
                    }
                }
            } ?: run {
                Logs.d(TAG, "checkSafeArgs: No se encontró el tipo de permiso")
            }
        }
    }


    private fun setupSwitchListeners() {
        vBinding.switchBluetoothPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchBluetoothPermission clicked")
            if (vBinding.switchBluetoothPermission.isChecked) {
                iMainActivity.requestPermission(PERMISSION_BLUETOOTH)
                vBinding.switchBluetoothPermission.isEnabled = false
            }
        }

        vBinding.switchCameraPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchCameraPermission clicked")
            if (vBinding.switchCameraPermission.isChecked) {
                iMainActivity.requestPermission(PERMISSION_CAMERA)
                vBinding.switchCameraPermission.isEnabled = false
            }
        }

        vBinding.switchSettingsSystem.setOnClickListener {
            Logs.d("PermissionsFragment", "switchSettingsSystem clicked")
            if (vBinding.switchSettingsSystem.isChecked) {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context?.packageName}")
                    )
                    context?.startActivity(intent)
                }
                vBinding.switchSettingsSystem.isEnabled = false
            }
        }

        vBinding.switchPhoneCallsPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchPhoneCallsPermission clicked")
            if (vBinding.switchPhoneCallsPermission.isChecked) {
                iMainActivity.requestPermission(PERMISSION_PHONE_CALL)
                vBinding.switchPhoneCallsPermission.isEnabled = false
            }
        }

        vBinding.switchNotificationPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchNotificationPermission clicked")
            if (vBinding.switchNotificationPermission.isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    iMainActivity.requestPermission(PERMISSION_NOTIFICATION)
                } else {
                    Logs.e(TAG, "Android Version < 33, Notificaciones Activadas por defecto")
                    iMainActivity.showToast(R.string.toast_notif_activadas_por_defecto)
                }
                vBinding.switchNotificationPermission.isEnabled = false
            }
        }

        vBinding.switchLocationPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchLocationPermission clicked")
            if (vBinding.switchLocationPermission.isChecked) {
                iMainActivity.requestPermission(PERMISSION_LOCATION)
                vBinding.switchLocationPermission.isEnabled = false
            }
        }

        vBinding.switchBatteryPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchBatteryPermission clicked")
            if (vBinding.switchBatteryPermission.isChecked) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    requestIgnoreBatteryOptimizations()
                }
                vBinding.switchBatteryPermission.isEnabled = false
            }
        }

        vBinding.switchMicrophonePermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchMicrophonePermission clicked")
            if (vBinding.switchMicrophonePermission.isChecked) {
                iMainActivity.requestPermission(PERMISSION_MICROPHONE)
                vBinding.switchMicrophonePermission.isEnabled = false
            }
        }

        vBinding.switchBLPermission.setOnClickListener {
            Logs.d("PermissionsFragment", "switchBLPermission clicked")
            if (vBinding.switchBLPermission.isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    iMainActivity.requestPermission(PERMISSION_BACKGROUND_LOCATION)
                } else {
                    Logs.e(TAG, "Android Version < 30, Localización en 2o Plano")
                    iMainActivity.showToast(R.string.toast_android_antiguo_ubicacion_seg_plano)
                }
                vBinding.switchBLPermission.isEnabled = false
            }
        }

        vBinding.switchOverloadSystem.setOnClickListener {
            Logs.d("PermissionsFragment", "switchOverloadSystem clicked")
            if (vBinding.switchOverloadSystem.isChecked) {
                iMainActivity.launchOverlayDisplayPermissionIntent()
                vBinding.switchOverloadSystem.isEnabled = false
            }
        }



        vBinding.switchRedSysPassword.setOnClickListener {
            Logs.d(TAG, "switchRedSysPassword clicked")
            if (vBinding.switchRedSysPassword.isChecked) {
                iMainActivity.requestPermission(PERMISSION_BLUETOOTH)
                if (TemporalData.passwordRedSys.isBlank() || TemporalData.passwordRedSys.isEmpty()) {
                    val callback: (RedSysCustomDialog.RedSysCustomDialogResponse) -> Unit =
                        { response ->
                            var userT = false
                            var passT = false
                            when (response.buttonPressed) {
                                ButtonType.ACCEPT -> {
                                    response.etRedSysUsername?.let { user ->
                                        if (user.isNotEmpty()) {
                                            vModel.saveUsernameRedSys(user)
                                            userT = true
                                        }
                                    }

                                    response.etRedSysPassword?.let { pwd ->
                                        if (pwd.isNotEmpty()) {
                                            TemporalData.passwordRedSys = pwd
                                            passT = true
                                        }
                                    }
                                    if (userT && passT) {
                                        vModel.checkLoginRedsys(response.etRedSysUsername ?: "") { error ->
                                            when (error) {
                                                is RedSysLoginResponse.Error -> {
                                                    if (error.errorCode == RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD) {
                                                        iMainActivity.showToast(R.string.red_sys_pwd_expired)
                                                        iMainActivity.navigateTo(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3)
                                                    } else {
                                                        iMainActivity.showToast(R.string.incorrect_login)
                                                        vBinding.switchRedSysPassword.isChecked = false
                                                    }
                                                }

                                                is RedSysLoginResponse.Success -> {
                                                    iMainActivity.showToast(R.string.toast_guardada_contrasena)
                                                }
                                            }
                                        }
                                    }
                                }
                                ButtonType.CANCEL -> {}
                                else -> {}
                            }
                        }

                    iMainActivity.openRedSysDialog(
                        model = RedSysCustomDialog.RedSysCustomDialogModel(
                            title = getString(R.string.dialog_guardar_contrasena_title),
                            description = getString(R.string.save_red_sys_pwd),
                            hintRedSysUsername = context?.getString(R.string.red_sys_username),
                            editTextRedSysUsername = vModel.redSysUsernameFlow.value,
                            hintRedSysPassword = context?.getString(R.string.contrasena_hint),
                            buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            tvCallback = {
                                iMainActivity.navigateTo(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3)
                            }
                        ),
                        response = callback
                    )
                } else {
                    iMainActivity.showToast(R.string.toast_contrasena_ya_guardada)
                }
            } else {
                TemporalData.passwordRedSys = ""
                vModel.saveUsernameRedSys("")
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            val requestIgnoreBatteryOptimizations = Intent()
            requestIgnoreBatteryOptimizations.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context?.startActivity(requestIgnoreBatteryOptimizations)
        } catch (e: Exception) {
            Logs.e(TAG, "requestIgnoreBatteryOptimizations: $e")
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.permissionGrantedFlow.collect { permissionStatus ->
                        val hasGranted = permissionStatus.first
                        val permissionString = permissionStatus.second

                        Logs.d(TAG, "Recibido $hasGranted para $permissionString")

                        if (hasGranted != null && permissionString != null) {
                            when (permissionString) {
                                permission.BLUETOOTH -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchBluetoothPermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchBluetoothPermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchBluetoothPermission Changed to: $hasGranted"
                                    )
                                }

                                permission.CAMERA -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchCameraPermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchCameraPermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchCameraPermission Changed to: $hasGranted"
                                    )
                                }

                                permission.CALL_PHONE -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchPhoneCallsPermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchPhoneCallsPermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchPhoneCallsPermission Changed to: $hasGranted"
                                    )
                                }

                                permission.POST_NOTIFICATIONS -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        withContext(Dispatchers.Main) {
                                            vBinding.switchNotificationPermission.isChecked =
                                                hasGranted

                                            if (hasGranted) {
                                                vBinding.switchNotificationPermission.isEnabled =
                                                    false
                                            }
                                        }

                                        Logs.d(
                                            TAG,
                                            "Switch switchNotificationPermission Changed to: $hasGranted"
                                        )
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            vBinding.switchNotificationPermission.isChecked = true
                                            vBinding.switchNotificationPermission.isEnabled = false
                                        }
                                    }
                                }

                                permission.ACCESS_BACKGROUND_LOCATION -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        withContext(Dispatchers.Main) {
                                            vBinding.switchBLPermission.isChecked = hasGranted

                                            if (hasGranted) {
                                                vBinding.switchBLPermission.isEnabled = false
                                            }
                                        }

                                        Logs.d(
                                            TAG,
                                            "Switch switchNotificationPermission Changed to: $hasGranted"
                                        )
                                    }
                                }

                                permission.ACCESS_FINE_LOCATION -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchLocationPermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchLocationPermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchLocationPermission Changed to: $hasGranted"
                                    )
                                }

                                permission.RECORD_AUDIO -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchMicrophonePermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchMicrophonePermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchMicrophonePermission Changed to: $hasGranted"
                                    )
                                }

                                permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                                    withContext(Dispatchers.Main) {
                                        vBinding.switchBatteryPermission.isChecked = hasGranted

                                        if (hasGranted) {
                                            vBinding.switchBatteryPermission.isEnabled = false
                                        }
                                    }

                                    Logs.d(
                                        TAG,
                                        "Switch switchBatteryPermission Changed to: $hasGranted"
                                    )
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.redSysConfigurationFlow.collect {
                        if (it) {
                            vModel.getUsernameRedSys()
                            withContext(Dispatchers.Main) {
                                vBinding.clRedSysPassword.visibility = View.VISIBLE

                                if (TemporalData.passwordRedSys.isBlank() || TemporalData.passwordRedSys.isEmpty()) {
                                    vBinding.switchRedSysPassword.isChecked = false
                                } else {
                                    vBinding.switchRedSysPassword.isChecked = true
                                }
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                vBinding.clRedSysPassword.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    vModel.phoneNumberConfigurationFlow.collect {
                        withContext(Dispatchers.Main) {
                            if (it) {
                                vBinding.clPhoneCallsPermission.visibility = View.VISIBLE
                            } else {
                                vBinding.clPhoneCallsPermission.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    vModel.overloadPermissionFlow.collect {
                        withContext(Dispatchers.Main) {
                            vBinding.switchOverloadSystem.isChecked = it
                            vBinding.switchOverloadSystem.isEnabled = !it
                        }
                    }
                }

                launch {
                    vModel.deviceSettingsPermissionFlow.collect {
                        withContext(Dispatchers.Main) {
                            vBinding.switchSettingsSystem.isChecked = it
                            vBinding.switchSettingsSystem.isEnabled = !it
                        }
                    }
                }
            }
        }
    }
}
