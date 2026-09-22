package ifac.td.taxi.ui.screen

import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentOnTripBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.OnTripViewModel
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class OnTripFragment :
    BaseFragment<FragmentOnTripBinding, OnTripViewModel>(
        R.layout.fragment_on_trip
    ) {

    private val TAG = this.javaClass.simpleName

    private val vModel: OnTripViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentOnTripBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        if (W2CLocation.isLocationAllowedByCentral()) {
            setButtons()
            vModel.checkNoClientButton()
        } else {
            setButtonWithoutLocationCentral()
        }

        vModel.getMessages()

        //sharedViewModel.updateTripFlow(safeArgs.tripId)
    }

    private fun setButtonWithoutLocationCentral() {
        vBinding.apply {
            btnFixedPrice.setAction {
                vModel.navigateToEstimateFixedPriceScreen()
            }

            btnNotifications.customFunctionValue = ButtonType.EMPTY.value
            btnNotifications.setButtonType(ButtonType.EMPTY.value)

            btnZoning.customFunctionValue = ButtonType.EMPTY.value
            btnZoning.setButtonType(ButtonType.EMPTY.value)

            btnDispatchInfo.customFunctionValue = ButtonType.EMPTY.value
            btnDispatchInfo.setButtonType(ButtonType.EMPTY.value)

            btnCentral.customFunctionValue = ButtonType.EMPTY.value
            btnCentral.setButtonType(ButtonType.EMPTY.value)

            btnClient.customFunctionValue = ButtonType.EMPTY.value
            btnClient.setButtonType(ButtonType.EMPTY.value)

            btnReceipts.setAction {
                vModel.navigateToReceiptHistory()
            }

            btnMessages.setAction {
                vModel.navigateToMessage()
            }
        }
    }

    private fun setButtons() {
        vBinding.apply {

            btnFixedPrice.setAction {
                vModel.navigateToEstimateFixedPriceScreen()
            }

            vBinding.btnNotifications.setAction {
                if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                    try {
                        iMainActivity.showToast(
                            R.string.itop_functionality_restricted,
                            Toast.LENGTH_LONG
                        )
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    return@setAction
                }
                openNotificationDialog()
            }

            btnNotifications.setButtonStyle(CustomButton.StyleButton.DISABLE)

            btnZoning.setAction {
                Logs.d(TAG, "zoningAction -> locationOnHired: ${sharedViewModel.hiredZone.value != null}")
                sharedViewModel.emitManualZoningNavigation(true)
                vModel.actionZoningFragment(sharedViewModel.hiredZone.value != null)
            }

            btnNavigate.setAction {
                vModel.openNavigatorApp()
            }

            btnDispatchInfo.setAction {
                sharedViewModel.tripFlow.value?.let {
                    iMainActivity.navigateTo(
                        OnTripFragmentDirections.actionOnTripFragmentToInfoDispatchFragment()
                    )
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

            btnClient.setAction {
                if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                    try {
                        iMainActivity.showToast(
                            R.string.itop_functionality_restricted,
                            Toast.LENGTH_LONG
                        )
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    return@setAction
                }
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        vModel.sendNoClientNotification(sharedViewModel.dispatchFlow.value?.id)
                    }
                }

                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.confirm_no_client), buttons = arrayListOf(
                            ButtonType.CANCEL, ButtonType.ACCEPT
                        )
                    ), response = callback,
                    fragmentManager = childFragmentManager
                )
            }
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            //Don't let the user to navigate back
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.showEstimateFixedPriceButtonFlow.collect {
                        if (it == true) {
                            vBinding.btnFixedPrice.visibility = View.VISIBLE
                            vBinding.btnReceipts.visibility = View.GONE
                        } else {
                            vBinding.btnFixedPrice.visibility = View.GONE
                            vBinding.btnReceipts.visibility = View.VISIBLE
                        }
                    }
                }
                launch {
                    vModel.notifyLocationOnHired.collect {
                        Logs.d(TAG, "notifyLocationOnHired: $it")
                        if (it == true) {
                            sharedViewModel.saveHiredZone(Zone())
                        } else if (it == false) {
                            sharedViewModel.saveHiredZone(null)
                        }
                    }
                }
                launch {
                    sharedViewModel.hasTaximeterConnectionFlow.collect {
                        updateTopBarIconRight()
                    }
                }
                launch {
                    sharedViewModel.hiredZone.collect {
                        if (it != null) {
                            vBinding.btnZoning.changeText(getString(R.string.btn_delocate))
                            vBinding.btnZoning.changeButtonIcon(R.drawable.ubdesact)
                            vBinding.btnZoning.changeBackground(CustomButton.BackgroundButtonColor.RED)
                        } else if (W2CLocation.isLocationAllowedByCentral()) {
                            vBinding.btnZoning.changeText(getString(R.string.btn_soon_to_clear))
                            vBinding.btnZoning.changeButtonIcon(R.drawable.ubactivar)
                            vBinding.btnZoning.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                        } else {
                            vBinding.btnZoning.setButtonType(ButtonType.EMPTY.value)
                        }
                    }
                }

                launch {
                    vModel.noClientButtonFlow.collect {
                        Logs.d(TAG, "currentStatus: ${sharedViewModel.shiftStatusFlow.value?.currentStatus}")
                        if (it == false && sharedViewModel.shiftStatusFlow.value?.currentStatus == ifConstants.STATE_HIRED_DISPATCHED) {
                            vBinding.btnClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    sharedViewModel.shiftStatusFlow.collect {
                        Logs.d(TAG, "shiftStatusFlow checking status to check clientButton -> ${it?.currentStatus}")
                        vModel.checkNoClientButton()
                        if (vModel.isHired(it?.currentStatus)) {
                            vBinding.btnNavigate.visibility = View.VISIBLE
                            vBinding.btnRoofLight.visibility = View.GONE
                        } else {
                            vBinding.btnNavigate.visibility = View.GONE
                            vBinding.btnRoofLight.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    sharedViewModel.tripFlow.collect { trip ->
                        trip?.let {
                            Logs.d(TAG, "dispatch?: ${it.fromDispatch}")
                            it.fromDispatch.let { fromDispatch ->
                                if (fromDispatch) {
                                    vBinding.btnDispatchInfo.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    vBinding.btnNavigate.setAction {
                                        iMainActivity.navigateTo(OnTripFragmentDirections.actionOnTripFragmentToDirectionsFragment())
                                    }
                                } else if (W2CLocation.isLocationAllowedByCentral()) {
                                    vBinding.btnDispatchInfo.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                } else {
                                    vBinding.btnDispatchInfo.setButtonType(ButtonType.EMPTY.value)
                                    vBinding.btnClient.setButtonType(ButtonType.EMPTY.value)
                                    vBinding.btnNotifications.setButtonType(ButtonType.EMPTY.value)
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
                    sharedViewModel.dispatchFlow.collect {
                        it?.let { dispatch ->
                            val hasAtDoorAction = dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false
                            val hasRiderInCabAction = (dispatch.riderInCab ?: false) && dispatch.isRiderInCabNotificationSent == false

                            if ((hasAtDoorAction && hasRiderInCabAction)) {
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            } else if (!hasAtDoorAction && hasRiderInCabAction) {
                                vBinding.btnNotifications.setButtonType(ButtonType.RIDER_IN_CAB.value)
                                vBinding.btnNotifications.setAction {
                                    if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                                        try {
                                            iMainActivity.showToast(
                                                R.string.itop_functionality_restricted,
                                                Toast.LENGTH_LONG
                                            )
                                        } catch (e: IllegalStateException) {
                                            Logs.d(TAG, "Couldn't show toast: ${e.message}")
                                        }
                                        return@setAction
                                    }
                                    vModel.sendInCabNotification(sharedViewModel.dispatchFlow.value)
                                }
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            } else if (hasAtDoorAction && dispatch.isRiderInCabNotificationSent == false) {
                                vBinding.btnNotifications.setButtonType(ButtonType.AT_DOOR.value)
                                vBinding.btnNotifications.setAction {
                                    vModel.sendAtTheDoorNotification(sharedViewModel.dispatchFlow.value)
                                }
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            } else {
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }

                            if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.bravoStateFlow.collect { state ->
                        if (vModel.hasITopTaximeterConnected()) {
                            vBinding.btnClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            when (state.clientButton) {
                                false -> vBinding.btnClient.changeBackground(CustomButton.BackgroundButtonColor.RED)
                                true -> vBinding.btnClient.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            }

                            when (state.noClientButtonEnabled) {
                                false -> vBinding.btnClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                true -> vBinding.btnClient.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }
                    }
                }

                launch {
                    vModel.navigatorFlow.collect {
                        it?.let { intent ->
                            iMainActivity.launchIntent(intent)
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
                    vModel.isManualFlow.collect {
                        it.let { isManual ->
                            if (isManual) {
                                showTopBarIconRight()
                            } else {
                                iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, false)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.updateMessageUI.collect {
                        vModel.getMessages()
                    }
                }

                launch {
                    sharedViewModel.roofLightFlow.collect {
                        vModel.checkRoofLight(it)
                    }
                }
            }
        }
    }

    private fun openNotificationDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.AT_DOOR) {
                vModel.sendAtTheDoorNotification(sharedViewModel.dispatchFlow.value)
            } else if (response.buttonPressed == ButtonType.RIDER_IN_CAB) {
                vModel.sendInCabNotification(sharedViewModel.dispatchFlow.value)
            }
        }

        val buttons = mutableListOf<ButtonType>()

        if (sharedViewModel.dispatchFlow.value?.isAtDoorNotificationEnabled() == true &&
            sharedViewModel.dispatchFlow.value?.isAtDoorNotificationSent == false) {
            buttons.add(ButtonType.AT_DOOR)
        }
        if (sharedViewModel.dispatchFlow.value?.riderInCab == true && sharedViewModel.dispatchFlow.value?.isRiderInCabNotificationSent == false) {
            buttons.add(ButtonType.RIDER_IN_CAB)
        }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.select_notification), buttons = ArrayList(buttons)
            ), response = callback,
            fragmentManager = childFragmentManager
        )

    }

    override fun updateTopBarIcon() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.BACK, false) {

        }
    }

    override fun updateTopBarIconRight() {
        sharedViewModel.hasTaximeterConnectionFlow.value.let { taximeterConnection ->
            if (!taximeterConnection && vModel.isHired(sharedViewModel.shiftStatusFlow.value?.currentStatus)) {
                showTopBarIconRight()
            } else {
                sharedViewModel.shiftStatusFlow.value?.let {
                    Logs.d(TAG, "updateTopBarIconRight: isHired and TxWithoutProtocol: ${vModel.isHired(it.currentStatus) && Taximeter.getInstance().isTaximeterWithoutProtocol}")
                    Logs.d(TAG, "updateTopBarIconRight: isDispatched and Can Do Manual Trips: ${it.currentStatus == ifConstants.STATE_DISPATCHED && vModel.canGoToHiredManual(taximeterConnection)}")
                    if (it.isManual || (vModel.isHired(it.currentStatus) && Taximeter.getInstance().isTaximeterWithoutProtocol && !vModel.hasITopTaximeterConnected()) ||(it.currentStatus == ifConstants.STATE_DISPATCHED && vModel.canGoToHiredManual(taximeterConnection))) {
                        showTopBarIconRight()
                    } else {
                        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, false)
                    }
                }
            }
        }
    }

    private fun showTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, true) {
            sharedViewModel.shiftStatusFlow.value?.currentStatus?.let { currentStatus ->
                if (currentStatus == ifConstants.STATE_DISPATCHED) {
                    val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            sharedViewModel.tripFlow.value?.let { trip ->
                                vModel.goToHiredManual(trip.id)
                            }
                        }
                    }
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = getString(R.string.dialog_hired_manual_title),
                            description = getString(R.string.confirm_change_to_hired),
                            buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                        ), callBack,
                        fragmentManager = childFragmentManager
                    )
                } else {
                    vModel.changeStateToPaymentManual()
                }
            }

        }
    }
}