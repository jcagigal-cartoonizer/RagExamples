package ifac.td.taxi.ui.screen

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentSettingsBinding
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class SettingsFragment(
) : BaseFragment<FragmentSettingsBinding, SettingsViewModel>(
    R.layout.fragment_settings
) {

    private val TAG = "SettingsFragment"
    private val vModel: SettingsViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        vModel.checkDeviceSettingsButton()
        setButtons()

        vModel.getSavedDevice()
        vModel.checkUserLogIn()
        vModel.checkWebViewButton()
        vModel.checkHasSettingsPassword()
        vModel.checkRequirementsButton()
    }

    private fun setButtons() {
        vBinding.apply {
            btnConfiguration.setButtonStyle(if (sharedViewModel.shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISCONNECTED && sharedViewModel.shiftStatusFlow.value?.currentStatus != null) { CustomButton.StyleButton.DISABLE } else { CustomButton.StyleButton.ENABLE })
            btnConfiguration.setAction {
                vModel.clickConfiguration()
            }

            btnDeviceSettings.setAction {
                vModel.clickDeviceSettings()
            }

            btnGPS.setAction {
                vModel.clickGPS()
            }

            btnAbout.setAction {
                vModel.clickAbout()
            }

            btnLight.setAction {
                vModel.clickLights()
            }

            btnRequirements.setAction {
                vModel.clickRequirements()
            }

            btnPREFERENCIAS.setAction {
                onClickPreferencias()
            }

            btnBLUETOOTH.setAction {
                onClickBluetooth()
            }
        }
    }

    override fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.bluetoothLocalFlow.collect { device ->
                        if (device != null) {
                            vModel.getStatus()
                        } else {
                            vBinding.btnLight.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

//                launch {
//                    vModel.iTopFlow.collect { isItop ->
//                        if (isItop == true) {
//
//                        } else {
//
//                        }
//                    }
//                }

                launch {
                    vModel.statusFlow.collect {
                        it?.let { status ->
                            vModel.checkConditions(vModel.bluetoothLocalFlow.value, status)
//                                    if (status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED) {
//                                        vBinding.btnBLUETOOTH.setButtonStyle(CustomButton.StyleButton.ENABLE)
//                                        vBinding.btnBLUETOOTH.setAction {
//                                            iMainActivity.showToast(getString(R.string.toast_bt_ya_conectado_reinicie))
//                                        }
//                                        vBinding.btnBLUETOOTH.setButtonStyle(CustomButton.StyleButton.DISABLE)
//                                        vBinding.btnConfiguration.setAction {
//                                            iMainActivity.showToast(getString(R.string.toast_bt_ya_conectado_reinicie))
//                                        }
//                                    }
                        }
                    }
                }

                launch {
                    vModel.conditionsLocalFlow.collect {
                        it?.let { isValid ->
                            if (isValid) {
                                vBinding.btnLight.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                vBinding.btnLight.setAction {
                                    vModel.clickLights()
                                }
                            } else {
                                vBinding.btnLight.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                vBinding.btnLight.setAction {

                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.userLogInFlow.collect { userLoggedIn ->
                        if (userLoggedIn) {
                            setButtons()
                        } else {
                            setButtonsUserLoggedOff()
                        }
                    }
                }

                launch {
                    vModel.webViewUrlFlow.collect { webViewUrl ->
                        if (!webViewUrl.isNullOrEmpty()) {
                            vBinding.btnWebView.visibility = View.VISIBLE
                            vBinding.btnWebView.setAction {
                                iMainActivity.navigateTo(SettingsFragmentDirections.actionSettingsFragmentToWebViewFragment(webViewUrl))
                            }
                        } else {
                            vBinding.btnWebView.visibility = View.INVISIBLE
                        }
                    }
                }

                launch {
                    vModel.configurationPasswordDialogFlow.collect { hasPassword ->
                        hasPassword?.let {
                            vBinding.apply {
                                if (it) {
                                    btnPREFERENCIAS.setAction {
                                        openConfigurationPasswordDialog {
                                            onClickPreferencias()
                                        }
                                    }
                                    btnBLUETOOTH.setAction {
                                        openConfigurationPasswordDialog {
                                            onClickBluetooth()
                                        }
                                    }
                                } else {
                                    btnPREFERENCIAS.setAction {
                                        onClickPreferencias()
                                    }
                                    btnBLUETOOTH.setAction {
                                        onClickBluetooth()
                                    }
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.correctPasswordFlow.collect {
                        val correctPassword = it.first
                        val callback = it.second
                        if (correctPassword) {
                            callback.invoke()
                        } else {
                            iMainActivity.showToast(R.string.pin_incorrecto)
                        }
                    }
                }

                launch {
                    vModel.canSeeDriverRequirementsFlow.collect { canSeeDriverRequirements ->
                        if (canSeeDriverRequirements) {
                            vBinding.btnRequirements.visibility = View.VISIBLE
                        } else {
                            vBinding.btnRequirements.visibility = View.INVISIBLE
                        }
                    }
                }

                launch {
                    vModel.showSoundBrightButtonFlow.collect { showButton ->
                        if (showButton) {
                            vBinding.btnDeviceSettings.visibility = View.VISIBLE
                            vBinding.btnConfiguration.visibility = View.GONE
                        } else {
                            vBinding.btnDeviceSettings.visibility = View.GONE
                            vBinding.btnConfiguration.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun setButtonsUserLoggedOff() {
        vBinding.apply {
            btnBLUETOOTH.setAction {
                vModel.clickConfiguration()
            }

            btnPREFERENCIAS.setAction {
                vModel.clickConfiguration()
            }

            btnGPS.setAction {
                vModel.clickConfiguration()
            }

            btnLight.setAction {
                vModel.clickConfiguration()
            }

            btnRequirements.setAction {
                vModel.clickConfiguration()
            }
        }
    }

    private fun onClickPreferencias() {
        iMainActivity.navigateTo(R.id.action_settingsFragment_to_userConfigurationFragment)
    }

    private fun onClickBluetooth() {
        checkBluetoothPermission {
            vModel.clickBluetooth()
        }
    }

    private fun openConfigurationPasswordDialog(callback: () -> Unit) {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                val password = response.editTextString
                if (!password.isNullOrEmpty()) {
                    vModel.checkSettingsPassword(password) {
                        callback.invoke()
                    }
                }
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.pin_actual),
                description = getString(R.string.pin_actual_hint),
                editText = "",
                editTextTypePin = true,
                editTextMaxLength = 4,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ), callBack
        )
    }

    private fun checkBluetoothPermission(function: () -> Unit) {
        Logs.d(TAG, "checkBluetoothPermission: Checking Bluetooth permission")
        context?.let {
            PermissionRequest.needBluetoothPermission(it) { hasPermission ->
                Logs.d(TAG, "checkBluetoothPermission: hasPermission = $hasPermission")
                if (!hasPermission) {
                    Logs.d(TAG, "checkBluetoothPermission: Bluetooth permission not granted")
                    val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            Logs.d(TAG, "checkBluetoothPermission: User accepted Bluetooth permission request")
                            iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH)
                        }
                    }
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = getString(R.string.bluetooth),
                            description = getString(R.string.dialog_no_bt_permission_desc),
                            buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                            isCancellable = false
                        ), callback
                    )
                } else {
                    Logs.d(TAG, "checkBluetoothPermission: Bluetooth permission already granted")
                    function.invoke()
                }
            }
        }
    }
}