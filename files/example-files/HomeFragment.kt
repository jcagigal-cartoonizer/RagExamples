package ifac.td.taxi.ui.screen

import android.annotation.SuppressLint
import android.media.ToneGenerator
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentHomeBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.HomeViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment :
    BaseFragment<FragmentHomeBinding, HomeViewModel>(
        R.layout.fragment_home
    ) {

    private val TAG = "HomeFragment"

    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val vModel: HomeViewModel by viewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)

    private var pendingServicesButton: Boolean? = null

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        vBinding.btnMessages.setButtonStyle(CustomButton.StyleButton.DISABLE)
        vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.DISABLE)
        vModel.getMessages()
        sharedViewModel.resetDispatchFlow()
        sharedViewModel.resetCurrentAmountFlow()
    }

    override fun onResume() {
        Log.d("HomeFragment", "Init HomeFragment")
        super.onResume()
        if (W2CLocation.isLocationAllowedByCentral()) {
            setButtons()
            vModel.checkForLocationPermission()
            vModel.checkPendingServiceOnForHirePermission()
        } else {
            setButtonWithoutLocationCentral()
        }
        vModel.checkKeepScreenOn()
    }

    override fun checkKkeepScreenActive() {
        //keep
    }

    private fun setButtons() {
        vBinding.apply {
            btnZoning.setAction {
//                iMainActivity.navigateTo(
//                    directions = HomeFragmentDirections.actionHomeFragmentToMeetingSignFragment(vModel.meetingSignColors.value.first, vModel.meetingSignColors.value.second)
//                )
                sharedViewModel.emitManualZoningNavigation(true)
                vModel.navigateToZoning()
            }


            btnPending.setAction {
                vModel.canOpenPendingTripsFragment(sharedViewModel._pendingTripsListFlow)
            }

            //QUE hace esto?
            //checkW2CloudLocation()

            //vBinding.btnLocation.setButtonStyle(CustomButton.StyleButton.ENABLE)
            //vBinding.btnLocation.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
            vBinding.btnLocation.setAction {
                if (sharedViewModel.locationEnabledFlow.value.second) {
                    openLocationDialog()
                } else {
                    if (sharedViewModel.lastITopMeterBreak.value == true) {
                        iMainActivity.showToast(R.string.do_not_locate_on_tx_break)
                    } else if (sharedViewModel.lastShortBreakForced.value) {
                        iMainActivity.showToast(R.string.connect_taximeter)
                    } else if (sharedViewModel.roofLightDelocation.value) {
                        iMainActivity.showToast(R.string.error_rooflight_off)
                    } else {
                        iMainActivity.showToast(R.string.do_not_locate_on_sanction)
                    }
                }
            }

            btnReceipts.setAction {
                vModel.navigateToReceiptHistory()
            }

            btnMessages.setAction {
                vModel.navigateToMessage()
            }

            btnCentral.setAction {
                vModel.navigateToContactCentral()
            }

            btnDashboard.setAction {
                iMainActivity.navigateTo(R.id.action_homeFragment_to_dashboardFragment)
            }

            btnFixedPrice.setAction {
                vModel.navigateToEstimateFixedPriceScreen()
            }
        }
    }

    private fun setButtonWithoutLocationCentral() {
        vBinding.apply {
            btnLocation.customFunctionValue = ButtonType.EMPTY.value
            btnLocation.setButtonType(ButtonType.EMPTY.value)

            btnZoning.customFunctionValue = ButtonType.EMPTY.value
            btnZoning.setButtonType(ButtonType.EMPTY.value)

            btnPending.customFunctionValue = ButtonType.EMPTY.value
            btnPending.setButtonType(ButtonType.EMPTY.value)

            btnLocateStand.customFunctionValue = ButtonType.EMPTY.value
            btnLocateStand.setButtonType(ButtonType.EMPTY.value)

            btnCentral.customFunctionValue = ButtonType.EMPTY.value
            btnCentral.setButtonType(ButtonType.EMPTY.value)

            btnReceipts.setAction {
                vModel.navigateToReceiptHistory()
            }
            btnMessages.setAction {
                vModel.navigateToMessage()
            }
        }

    }


    @SuppressLint("DefaultLocale")
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Logs.d(
                    TAG,
                    "setupObservers: ${this.coroutineContext} \n ${Thread.currentThread().name}"
                )
                launch {
                    sharedViewModel.bravoStateFlow.collect {
                        Logs.d(TAG, "setupObservers: $it")
                        when (it.voiceValue) {
                            true -> vBinding.btnCentral.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                            false -> vBinding.btnCentral.changeBackground(CustomButton.BackgroundButtonColor.RED)
                        }
                    }
                    /*
                    sharedViewModel.voiceValueFlow.collect { value ->
                        if (value) {
                            vBinding.btnCentral.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                        } else {
                            vBinding.btnCentral.changeBackground(CustomButton.BackgroundButtonColor.RED)
                        }
                    }

                     */
                }

                launch {
                    sharedViewModel.roofLightFlow.collect {
                        vModel.checkRoofLight(it)
                    }
                }

                launch {
                    sharedViewModel.locationEnabledFlow.collect {
                        Logs.d(TAG, "locationEnabledFlow collected: ${it.first}")
                        if (vModel.returnLocationPermission() == true) {
                            when (it.first) {
                                true -> {
                                    vBinding.btnLocation.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                                    vBinding.btnLocation.changeText(getString(R.string.btn_location_off))
                                }

                                else -> {
                                    vBinding.btnLocation.changeBackground(CustomButton.BackgroundButtonColor.RED)
                                    vBinding.btnLocation.changeText(getString(R.string.btn_location_on))
                                }
                            }
                            vModel.checkZoningButton(locationEnabled = it.first)
                        }
                    }
                }

                launch {
                    vModel.hasMessagesFlow.collect { hasMessages ->
                        hasMessages?.let {
                            when(it) {
                                MessageUIEnum.NO_MESSAGES -> {
                                    vBinding.btnMessages.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                }
                                MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW -> {
                                    vBinding.btnMessages.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnMessages.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                                }
                                MessageUIEnum.HAS_NEW_MESSAGES -> {
                                    vBinding.btnMessages.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnMessages.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.roofLightFlow.collect {

                        val lightState = it?.first
                        val isDisableLuminous = it?.second

                        if (isDisableLuminous == true) {
                            when (lightState) {
                                true -> {
                                    vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnRoofLight.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                                    vBinding.btnRoofLight.setAction {
                                        vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.LOADING)
                                        vModel.setRoofLightOff()
                                    }
                                }

                                false -> {
                                    vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnRoofLight.changeBackground(CustomButton.BackgroundButtonColor.RED)
                                    vBinding.btnRoofLight.setAction {
                                        vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.LOADING)
                                        vModel.setRoofLightOn()
                                    }
                                }

                                else -> {
                                    vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                }
                            }
                        } else {
                            vBinding.btnRoofLight.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }


                launch {
                    vModel.showLocationButtonFlow.collect {
                        if (it == false) {
                            vBinding.btnLocation.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }



                launch {
                    vModel.pendingServicesButtonFlow.collect {
                        pendingServicesButton = it
                        checkPendingButtonEnabled()
                    }
                }

                launch {
                    sharedViewModel.locatedOnStop.collect {
                        Logs.d(TAG, "collect locate button $it")
                        if (it) {
                            vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            vBinding.btnLocateStand.setAction {
                                triggerLocateOnStand()
                            }
                            if (sharedViewModel.locationType.value == "P") {
                                vBinding.btnLocateStand.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            } else {
                                vBinding.btnLocateStand.changeBackground(CustomButton.BackgroundButtonColor.RED)
                            }
                        } else {
                            vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    sharedViewModel.locationType.collect {
                        onLocationType(it)
                    }
                }

                launch {
                    sharedViewModel.hasTaximeterConnectionFlow.collect {
                        updateTopBarIconRight()
                    }
                }

                launch {
                    sharedViewModel.orangeBtnPendingFlow.collect { btnPendingShouldBeOrange ->
                        Logs.d(TAG, "btnPendingShouldBeOrange: $btnPendingShouldBeOrange")
                        if (btnPendingShouldBeOrange == true) {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                        } else {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonPressedCallback.collect {
                        if (it) {
                            iMainActivity.navigateTo(R.id.action_homeFragment_to_pendingTripsFragment)
                        } else {
                            if (sharedViewModel.pendingTripsListFlow.value?.isEmpty() == true) {
                                //iMainActivity.showToast(getString(R.string.no_pending_services))
                                iMainActivity.showToast(R.string.toast_no_hay_pendientes)
                            } else {
                                iMainActivity.navigateTo(R.id.action_homeFragment_to_pendingTripsFragment)
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.shortBreakStatus.collect {
                        checkPendingButtonEnabled()
                    }
                }
                launch {
                    vModel.keepOnScreenFlow.collect {
                        iMainActivity.keepScreenActive(it)
                    }
                }
                launch {
                    vModel.hasDashboardButtonOn.collect {
                        if (it) {
                            vBinding.btnDashboard.visibility = View.VISIBLE
                        } else {
                            vBinding.btnDashboard.visibility = View.GONE
                        }
                    }
                }
                launch {
                    sharedViewModel.updateMessageUI.collect {
                        vModel.getMessages()
                    }
                }

                launch {
                    vModel.timeControlFlow.collect {
                        sharedViewModel.updateTimeControlFlow(it)
                    }
                }

                launch {
                    vModel.showEstimateFixedPriceButtonFlow.collect {
                        if (it) {
                            vBinding.btnFixedPrice.visibility = View.VISIBLE
                            vBinding.btnReceipts.visibility = View.GONE
                        } else {
                            vBinding.btnFixedPrice.visibility = View.GONE
                            vBinding.btnReceipts.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    vModel.zoningButtonStateFlow.collect {
                        if (it) {
                            vBinding.btnZoning.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } else {
                            vBinding.btnZoning.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    sharedViewModel.zoneFlow.collect {
                        if (it.isNullOrBlank()) {
                            Logs.d(TAG, "zoneFlow collect: null or blank")
                            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            checkPendingButtonEnabled()
                        }
                    }
                }
            }
        }
    }

    private fun checkPendingButtonEnabled() {
        if (sharedViewModel.zoneFlow.value.isNullOrEmpty()) {
            Logs.d(TAG, "sharedViewModel.zoneFlow: NullOrEmpty")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        val isInShortBreak = sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK ||
                sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        // Si está en pausa corta, desactivar el botón
        if (isInShortBreak) {
            Logs.d(TAG, "checkPendingButtonEnabled: isInShortBreak")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        // Si pendingServicesButton es false o no está permitido por ubicación, desactivar el botón
        if (!W2CLocation.isLocationAllowedByCentral()) {
            Logs.d(TAG, "isLocationAllowedByCentral: false")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        // Si está en soonInZone activar el botón
        if (W2CLocation.getIsInSoonInZone()) {
            Logs.d(TAG, "isInSoonInZone: true")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.ENABLE)
            return
        }

        // Si pendingServicesButton es true, activar el botón
        if (pendingServicesButton == true) {
            Logs.d(TAG, "pendingServicesButton: true")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.ENABLE)
            return
        }

        Logs.d(TAG, "btnPending disable by default")
        // Caso por defecto
        vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
    }

    private fun onLocationType(it: String) {
        val zone = "Z"
        val stand = "P"

        when (it) {
            zone -> {
                if (sharedViewModel.locatedOnStop.value) {
                    vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    vBinding.btnLocateStand.changeBackground(CustomButton.BackgroundButtonColor.RED)
                } else {
                    if (W2CLocation.locationStopAllowed()) {
                        vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        vBinding.btnLocateStand.changeBackground(CustomButton.BackgroundButtonColor.RED)

                        vBinding.btnLocateStand.setAction {
                            triggerLocateOnStand()
                        }
                    } else {
                        vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    }
                }

                vBinding.btnLocateStand.changeText(getString(R.string.btn_locate_stop))
            }

            stand -> {
                if (!vModel.checkStartAutomaticLocation()) {
                    vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    vBinding.btnLocateStand.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                } else {
                    vBinding.btnLocateStand.setButtonStyle(CustomButton.StyleButton.DISABLE)
                }
                vBinding.btnLocateStand.setAction {
                    triggerLocateOnStand()
                }
                vBinding.btnLocateStand.changeText(getString(R.string.btn_locate_stop_exit))
            }

            else -> {}
        }
    }

    private fun locateStop() {
        W2CLocation.sendLocateStop()
        W2CLocation.setTryingToRelocateInStand(false)
    }

    private fun openDeLocateOnStandDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                W2CLocation.sendLocateZone()
            }
        }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_exit_onStand_title),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ),
            callback
        )
    }

    private fun triggerLocateOnStand() {
        if (W2CLocation.getLocationTypeSent() == 'P') {
            openDeLocateOnStandDialog()
        } else {
            locateStop()
        }
    }

    private fun checkW2CloudLocation() {
        if (W2CLocation.isLocationAllowedByCentral()) {
            vBinding.btnLocation.setAction {
                openLocationDialog()
            }
            vBinding.btnLocation.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            handleBackPressed()
        }
    }

    override fun updateTopBarIcon() {
        //If Itop device, dont show icon
        //For all other devices, show icon
        val visibility = !vModel.isCurrentBluetoothITop()
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.EXIT, visibility) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun updateTopBarIconRight() {
        if (vModel.canGoToHiredManual(sharedViewModel.hasTaximeterConnectionFlow.value)) {
            openDialogManualTrip()
        } else {
            return super.updateTopBarIconRight()
        }
    }

    private fun openDialogManualTrip() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, true) {
            val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    vModel.changeStateHiredManual()
                }
            }
            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = getString(R.string.dialog_hired_manual_title),
                    description = getString(R.string.confirm_change_to_hired),
                    buttons = arrayListOf(
                        ButtonType.CANCEL,
                        ButtonType.ACCEPT
                    )
                ),
                callback,
                fragmentManager = childFragmentManager
            )
        }
    }

    private fun handleBackPressed() {
        if (vModel.isCurrentBluetoothITop()) {
            return
        }

        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
            if (it.buttonPressed == ButtonType.ACCEPT) {
                vModel.logoff()
                iMainActivity.changeTextTimeControlTopBar(null)

                val navDeepLink = NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/welcome".toUri()).build()
                iMainActivity.navigateTo(navDeepLink)
//                iMainActivity.navigateTo(R.id.action_homeFragment_to_welcomeFragment)
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.confirm_close_shift),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun openLocationDialog() {
        sharedViewModel.locationEnabledFlow.value.let { enabled ->
            if (enabled.first) {
                if (sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                    iMainActivity.showToast(R.string.connect_taximeter)
                    return@let
                }
                
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                    if (it.buttonPressed == ButtonType.ACCEPT) {
                        sharedViewModel.updateLocationEnabledFlow(enabled = false, canEnable = true)
                        sharedViewModel.updateLocationManuallyDeLocatedFlow(true)
                        //XXX ManuallyDelocate
                        vModel.checkForHireAfterDelocation()
                        iMainActivity.beep(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP)
                        iMainActivity.locationEnabledManually()
                    }
                }
                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = getString(R.string.confirm_deactivate_location),
                        buttons = arrayListOf(
                            ButtonType.CANCEL,
                            ButtonType.ACCEPT
                        )
                    ),
                    callback,
                    fragmentManager = childFragmentManager
                )
            } else {
                if (W2CLocation.isLocationAllowedByCentral()) {
                    val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                        if (it.buttonPressed == ButtonType.ACCEPT) {
                            if (sharedViewModel.locationEnabledFlow.value.second) {
                                W2CLocation.resetTtsUb()
                                sharedViewModel.updateLocationEnabledFlow(
                                    enabled = true,
                                    canEnable = true
                                )
                                sharedViewModel.updateLocationManuallyDeLocatedFlow(false)
                                iMainActivity.locationEnabledManually()
                            } else {
                                try {
                                    iMainActivity.showToast(R.string.location_disabled)
                                } catch (e: IllegalStateException) {
                                    Logs.d(TAG, "Couldn't show toast: ${e.message}")
                                }
                            }
                        }
                    }
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = getString(R.string.confirm_activate_location),
                            buttons = arrayListOf(
                                ButtonType.CANCEL,
                                ButtonType.ACCEPT
                            )
                        ),
                        callback,
                        fragmentManager = childFragmentManager
                    )
                }
            }
        }
    }
}