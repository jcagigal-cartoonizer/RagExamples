package ifac.td.taxi.ui.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentWelcomeBinding
import ifac.td.taxi.domain.model.itop.ITopMeterStateEnum
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_LOCATION
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_NOTIFICATION
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.WelcomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class WelcomeFragment : BaseFragment<FragmentWelcomeBinding, WelcomeViewModel>(
    R.layout.fragment_welcome
) {
    private var batteryOptimizationAlreadyLogged: Boolean = false
    private val TAG = javaClass.name

    private val vModel: WelcomeViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun updateTopBarIcon() {
        iMainActivity.configureIconsTopBar(
            iconType = CustomTopBar.IconType.EXIT,
            visibility = true,
        ) {
            if (sharedViewModel.reconnectionFlow.value) {
                Logs.d(TAG, "topBarIcon pressed ignored - Is in recovery mode")
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentWelcomeBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        vModel.checkClosuresPermission()

        showPrivacyDialog()

        // setUpPermissions()
        vModel.makeMigrationV2()
//        iMainActivity.showReconnectionLayout(sharedViewModel.reconnectionFlow.value)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logs.d("WELCOME", "onConfigurationChanged: $newConfig")
    }

    override fun onResume() {
        super.onResume()
        setButtons()
        vModel.checkUserLogIn()
        vModel.haveShifts()
        vModel.checkAllPermissions()
    }

    private fun setButtons() {
        vBinding.apply {
            if (!vModel.hasITopMeterPaired()) {
                btnInicio.setAction {
                    checkAdditionalPermissions()
                }
            }

            btnAjustes.setAction {
                vModel.clickSettings()
            }
            btnTurnos.setAction {
                vModel.clickShifts()
            }
            btnEstadisticas.setAction {
                vModel.clickStatistics()
            }
            btnConfiguracion.setAction {
                vModel.clickConfiguration()
            }

            btnPermisos?.setAction {
//                val permissionType = PermissionRequest.PermissionTypeList.PERMISSION_IGNORE_BATTERY_OPTIMIZATIONS
//                val navDeepLink = NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/permissionsFragment/$permissionType".toUri()).build()
//                iMainActivity.navigateTo(navDeepLink)
                vModel.clickPermissions()
            }

            btnParciales.setAction {
                vModel.clickParciales()
            }

            btnSalir.setAction {
                exitDialog()
            }

        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.hasShiftsFlow.collect {
                        it?.let { hasShifts ->
                            if (hasShifts) {
                                vBinding.btnTurnos.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            } else {
                                vBinding.btnTurnos.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                        }
                    }
                }

                launch {
                    vModel.partialButtonFlow.collect {
                        Logs.d("WelcomeFragment", "partialButtonFlow.value: $it")
                        if (it == false) {
                            vBinding.btnParciales.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else if (it == true) {
                            vBinding.btnParciales.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        }
                    }
                }

                launch {
                    vModel.loginWithoutCentralWelcomeFlow.collect {
                        iMainActivity.connectTaximeter()
                        iMainActivity.navigateTo(R.id.action_welcomeFragment_to_homeFragment)
                    }
                }

                launch {
                    vModel.userLogInFlow.collect { userLoggedIn ->
                        if (userLoggedIn) {
                            setButtons()
                            vBinding.btnParciales.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } else {
                            setButtonsUserLoggedOff()
                        }
                    }
                }

                launch {
                    sharedViewModel.downloadingBravoConfigurationFlow.collect {
                        if (it) {
                            iMainActivity.showToast(R.string.toast_downloading_configurations)
                            vBinding.btnInicio.setButtonStyle(CustomButton.StyleButton.LOADING)
                        } else {
                            if (!vModel.hasITopMeterPaired()) {
                                vBinding.btnInicio.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }
                    }
                }
                launch {
                    vModel.hasAllPermissionsFlow.collect { hasAllPermissions ->
                        if (hasAllPermissions) {
                            vBinding.btnPermisos.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                        } else {
                            vBinding.btnPermisos.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                        }
                    }
                }

                launch {
                    sharedViewModel.lastITopStateReceived.collect {
                        if (vModel.hasITopMeterPaired()) {
                            when(it?.taximeterState) {
                                null,
                                ITopMeterStateEnum.DISCONNECTED.state -> {
                                    vBinding.btnInicio.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                }
                                else -> {
                                    vBinding.btnInicio.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnInicio.setAction {
                                        //Login directo
                                        vBinding.btnInicio.setButtonStyle(CustomButton.StyleButton.LOADING)
                                        vModel.tryLoginDriver(sharedViewModel.lastITopStateReceived.value)
                                    }
                                }
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.correctLoginFlow.collect {
                        if (!it.first) {
                            val buttonStyle = if (vModel.hasITopMeterPaired() &&
                                (sharedViewModel.lastITopStateReceived.value?.taximeterState == ITopMeterStateEnum.DISCONNECTED.state
                                || sharedViewModel.lastITopStateReceived.value?.taximeterState == null)) {
                                CustomButton.StyleButton.DISABLE
                            } else {
                                CustomButton.StyleButton.ENABLE
                            }
                            vBinding.btnInicio.setButtonStyle(buttonStyle)
                        }
                    }
                }

                launch {
                    vModel.neddMigrationFlow.collect {
                        if (it) {

                        }
                    }
                }

                launch {
                    vModel.requestSecurePinFlow.collect { destination ->
                        showSecurePinDialog(destination)
                    }
                }

                launch {
                    vModel.wrongSecurePinFlow.collect {
                        iMainActivity.showToast(R.string.pin_incorrecto)
                    }
                }
            }

        }
    }

    private fun showSecurePinDialog(destination: WelcomeViewModel.SecurePinDestination) {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.verifyCurrentSecurePin(response.editTextString ?: "", destination)
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.user_preferences_btn_secure_pin),
                hint = getString(R.string.pin_actual_hint),
                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                editTextTypePin = true,
            ),
            callBack,
            fragmentManager = childFragmentManager
        )
    }

    private fun setButtonsUserLoggedOff() {
        vBinding.apply {
            if (!vModel.hasITopMeterPaired()) {
                btnInicio.setAction {
                    vModel.clickConfiguration()
                }
            }


            btnTurnos.setAction {
                vModel.clickConfiguration()
            }

            btnParciales.setAction {
                vModel.clickConfiguration()
            }

            btnEstadisticas.setAction {
                vModel.clickConfiguration()
            }
        }
        vBinding.btnParciales.setButtonStyle(CustomButton.StyleButton.DISABLE)
    }

    private fun setUpPermissions() {
//        iMainActivity.requestPermission(PERMISSION_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            iMainActivity.requestListPermission(
                arrayOf(
                    PERMISSION_LOCATION,
                    PERMISSION_NOTIFICATION,
                )
            )
        } else {
            iMainActivity.requestListPermission(
                arrayOf(
                    PERMISSION_LOCATION,
                )
            )
        }
    }

    private fun checkAdditionalPermissions() {
        if (!batteryOptimizationAlreadyLogged) {
            checkBatteryOptimization()
        } else {
            vModel.clickMenuLogin()
        }
    }

    private fun showPrivacyDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            val hasAcceptedPrivacyPolicy =
                iMainActivity.getSharedPreferencesValue("PrivacyPolicyAccepted") as? Boolean ?: false

            val hasShowedPrivacyPolicy =
                iMainActivity.getSharedPreferencesValue("PrivacyPolicyDialogShowed") as? Boolean ?: false

            if (!hasAcceptedPrivacyPolicy && !hasShowedPrivacyPolicy) {
                iMainActivity.saveSharedPreferencesValue("PrivacyPolicyDialogShowed", true)

                val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        iMainActivity.saveSharedPreferencesValue("PrivacyPolicyAccepted", true)

                        if (isAdded) { //Porque puede crashear si el fragment no está completamente activo
                            iMainActivity.showToast(R.string.toast_aceptado)
                        }

                        setUpPermissions()
                    }
                }

                withContext(Dispatchers.Main) {
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = getString(R.string.dialog_privacy_title),
                            description = getString(R.string.dialog_privacy_desc),
                            hrefText = getString(R.string.dialog_privacy_href),
                            href = {
                                val privacyPolicyUri = getString(R.string.privacy_policy_link)
                                val intent = Intent(Intent.ACTION_VIEW, privacyPolicyUri.toUri())
                                try {
                                    context?.startActivity(intent)
                                } catch (e: android.content.ActivityNotFoundException) {
                                    iMainActivity.showToast(R.string.warning_not_browser)
                                    Logs.e(TAG, "No Activity found to handle viewing the privacy policy: $e")
                                }
                            },
                            checkBoxText = getString(R.string.dialog_privacy_accept),
                            checkDisableButton = true,
                            buttons = arrayListOf(ButtonType.ACCEPT)
                        ),
                        callBack
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    setUpPermissions()
                }
            }
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            exitDialog()
        }
    }

    private fun exitDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
            if (it.buttonPressed == ButtonType.ACCEPT) {
                iMainActivity.exitApp()
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.close_app),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            val requestIgnoreBatteryOptimizations = Intent()
            requestIgnoreBatteryOptimizations.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context?.startActivity(requestIgnoreBatteryOptimizations)
        } catch (e: Exception) {
            Logs.e("WelcomeFragment", "requestIgnoreBatteryOptimizations: $e")
        }
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context?.packageName) == true
    }

    private fun checkBatteryOptimization() {
        if (!isBatteryOptimizationDisabled()) {
            val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                { response ->
                    batteryOptimizationAlreadyLogged = true
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        requestIgnoreBatteryOptimizations()
                        vModel.clickMenuLogin()
                    }
                }

            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = getString(R.string.dialog_no_permission_title),
                    description = getString(R.string.dialog_no_permission_desc),
                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                ),
                callBack,
                fragmentManager = childFragmentManager
            )
        } else {
            vModel.clickMenuLogin()
        }
    }
}
