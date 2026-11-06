package ifac.td.taxi.ui.screen

import android.animation.ObjectAnimator
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentInfoDispatchBinding
import ifac.td.taxi.databinding.TabCustomInfoDispatchBinding
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCaseImpl
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.DestinyAdapter
import ifac.td.taxi.ui.adapter.SimpleListAdapter
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.util.ConfigurationUtils
import ifac.td.taxi.viewmodel.InfoDispatchViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoDispatchFragment : BaseFragment<FragmentInfoDispatchBinding, InfoDispatchViewModel>(
    R.layout.fragment_info_dispatch
) {

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: InfoDispatchViewModel by viewModel()

    private val TAG = "InfoDispatchFragment"

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentInfoDispatchBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        iMainActivity.showBottomBar(true)
        setButtons()
        vModel.checkNoClientButton()
        vModel.getMaxBridgeCalls()
    }

    private fun isBridgeCall(): Boolean {
        var isBridgeCall = false

        try {
            Logs.d(TAG, "isBridgeCall: Checking customerPhoneNumberExit")
            sharedViewModel.dispatchFlow.value?.customerPhoneNumberExit?.let {
                val customPhoneNumberExit = Integer.parseInt(it)
                Logs.d(TAG, "isBridgeCall: customerPhoneNumberExit = $customPhoneNumberExit")
                if (customPhoneNumberExit == 4) {
                    Logs.d(TAG, "isBridgeCall: customerPhoneNumberExit is 4")
                    sharedViewModel.dispatchFlow.value?.customerPhoneNumber?.let { phone ->
                        Logs.d(TAG, "isBridgeCall: customerPhoneNumber = $phone")
                        isBridgeCall = phone.length > 1
                    }
                }
            }
        } catch (e: Exception) {
            Logs.e(TAG, "isBridgeCall: $e")
        }

        Logs.d(TAG, "isBridgeCall: isBridgeCall = $isBridgeCall")
        return isBridgeCall
    }


    private fun setButtons() {
        vBinding.apply {
            vBinding.btnNotifications.setAction {
                if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                    try {
                        iMainActivity.showToast(
                            R.string.itop_functionality_restricted
                        )
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    return@setAction
                }
                openNotificationDialog()
            }

            vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.DISABLE)

            btnNavigate.setAction {
                vModel.stopTTS()
                iMainActivity.navigateTo(InfoDispatchFragmentDirections.actionInfoDispatchFragmentToDirectionsFragment())
            }

            btnVoiceCall.setAction {
                val isMultitrip = (sharedViewModel.multiDispatchFlow.value?.size ?: 0) > 1
                if (isMultitrip) {
                    val callbackCall: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            startVoiceCall()
                        }
                    }

                    val dispatch = sharedViewModel.dispatchFlow.value
                    val name = dispatch?.dispatchName ?: ""
                    val address = dispatch?.pickUpAdress ?: ""
                    var numDispatch = dispatch?.longDispatchNumber.orEmpty()

                    vModel.extraDataFlow.value?.let { extra ->
                        if (vModel.isCurrentBluetoothITop() && extra.externalTripId != 0) {
                            numDispatch = extra.externalTripId.toString()
                        }
                    }

                    iMainActivity.openDialog(
                        model = CustomDialog.CustomDialogModel(
                            title = getString(R.string.btn_call), buttons = arrayListOf(
                                ButtonType.CANCEL, ButtonType.ACCEPT
                            ),
                            description =  "$name\n$address\n$numDispatch"
                        ), response = callbackCall,
                        fragmentManager = childFragmentManager
                    )
                } else {
                    startVoiceCall()
                }
            }

            btnPrint.setAction {
                sharedViewModel.dispatchFlow.value?.let { model ->
                    vModel.printInfoDispatch(model)
                }
            }

            btnNoClient.setAction {
                if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                    try {
                        iMainActivity.showToast(R.string.itop_functionality_restricted)
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

    private fun startVoiceCall() {
        //check Permissions
        if (PermissionRequest.checkHavePermission(PermissionRequest.PermissionTypeList.PERMISSION_PHONE_CALL, context)) {
            sharedViewModel.dispatchFlow.value?.longDispatchNumber?.let { dispatchNumber ->
                Logs.d(TAG, "btnVoiceCall.setAction: customerPhoneNumberExit: ${sharedViewModel.dispatchFlow.value?.customerPhoneNumberExit}")
                Logs.d(TAG, "btnVoiceCall.setAction: customerPhoneNumber: ${sharedViewModel.dispatchFlow.value?.customerPhoneNumber}")
                if (isBridgeCall()) {
                    Logs.d(TAG, "startVoiceCall: isBridgeCall is true, bridgeCallState: ${sharedViewModel.bravoStateFlow.value.bridgeCallState}")
                    when (sharedViewModel.bravoStateFlow.value.bridgeCallState) {
                        1 -> {
                            Logs.d(TAG, "startVoiceCall: cancelling bridge call, state is 1, dispatchNumber: $dispatchNumber")
                            vModel.cancelBridgeCall(dispatchNumber)
                        }
                        2 -> {
                            Logs.d(TAG, "startVoiceCall: cancelling bridge call, state is 2, dispatchNumber: $dispatchNumber")
                            vModel.cancelBridgeCall(dispatchNumber)
                        }
                        0 -> {
                            Logs.d(TAG, "startVoiceCall: bridgeCallState is 0, initiating bridge call to dispatch: $dispatchNumber")
                            vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.LOADING)
                            vModel.makeBridgeCall(dispatchNumber)
                        }

                        else -> {
                            //Do nothing
                        }
                    }
                } else {
                    //normal call
                    sharedViewModel.dispatchFlow.value?.customerPhoneNumber?.let {
                        vModel.startCall(it)
                        Logs.d(TAG, "btnVoiceCall.setAction: No bridge call, starting normal call")
                    }
                }
            }
        } else {
            iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_PHONE_CALL)
        }
    }

    private fun refreshMultiDispatchMenuButtons(infoMultiDispatch: ArrayList<InfoDispatchModel>) {
        val orientation = resources.configuration.orientation

        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE||
            ConfigurationUtils.isTablet(resources)) {

            Logs.d(TAG, "refreshMultiDispatchMenuButtons - device is tablet and in landscape")
            vBinding.apply {
                if (infoMultiDispatch.size > 1) {
                    Logs.d(TAG, "refreshMultiDispatchMenuButtons infoMultiDispatch.size > 1: true")
                    lytContainerMultiDispatchMenu?.visibility = View.VISIBLE
                    infoMultiDispatch.forEachIndexed { index, dispatch ->
                        val tabId = index

                        // Verificar si ya existe una pestaña con este ID
                        val tabCount = tabsMultiDispatch?.tabCount ?: 0
                        val exists = (0 until tabCount).any { i ->
                            tabsMultiDispatch?.getTabAt(i)?.id == tabId
                        }

                        if (!exists) {
                            val binding = TabCustomInfoDispatchBinding.inflate(LayoutInflater.from(context), null, false)

                            binding.root.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            val tab = tabsMultiDispatch?.newTab()
                            tab?.customView = binding.root

                            tab?.id = index

                            tab?.let {
                                tabsMultiDispatch?.addTab(tab)
                            }
                        }


                        if (tabsMultiDispatch?.getTabAt(index)?.id == tabId) {
                            val viewTab = tabsMultiDispatch.getTabAt(index)?.view
                            val imgStatus = viewTab?.findViewById<ImageView>(R.id.imgTab)
                            val nameTab = viewTab?.findViewById<TextView>(R.id.tvNameUser)

                            val imgRes = when {
                                dispatch.isCancelled -> R.drawable.baseline_do_not_disturb_24
                                dispatch.isEnded -> R.drawable.baseline_flag_24
                                dispatch.isRiderInCabNotificationSent == true -> R.drawable.baseline_face_192
                                dispatch.isAtDoorNotificationSent == true -> R.drawable.outline_door_front_24
                                else -> R.drawable.baseline_emoji_people_24
                            }

                            imgStatus?.setImageDrawable(ResourcesCompat.getDrawable(resources, imgRes, null))

                            nameTab?.text = dispatch.dispatchName ?: ""
                        }
                    }

                    tabsMultiDispatch?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: Tab?) {
                            try {
                                val tabId = tab?.id
                                tabId?.let { _ ->
                                    infoMultiDispatch[tabId].longDispatchNumber
                                }?.let { longDispatchNumber ->
                                    infoMultiDispatch[tabId].routeId?.let { routeId ->
                                        Logs.d(TAG, "refreshMultiDispatchMenuButtons updateSelectedDispatch longDispatchNumber: $longDispatchNumber")
                                        vModel.updateSelectedDispatch(longDispatchNumber,
                                            routeId
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Logs.e(TAG, "petoak")
                            }

                        }

                        override fun onTabUnselected(tab: Tab?) {

                        }

                        override fun onTabReselected(tab: Tab?) {
                            try {
                                val tabId = tab?.id
                                tabId?.let { _ ->
                                    infoMultiDispatch[tabId].longDispatchNumber
                                }?.let { longDispatchNumber ->
                                    infoMultiDispatch[tabId].routeId?.let { routeId ->
                                        Logs.d(TAG, "refreshMultiDispatchMenuButtons updateSelectedDispatch longDispatchNumber: $longDispatchNumber")
                                        vModel.updateSelectedDispatch(longDispatchNumber,
                                            routeId
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Logs.e(TAG, "petoak")
                            }
                        }
                    })

                    if ((infoMultiDispatch.size) > 0) {
                        val selectedItem = infoMultiDispatch.firstOrNull { it.selected }
                        selectedItem?.let {
                            val position = infoMultiDispatch.indexOf(it)
                            moveIndicatorTab(tabsMultiDispatch?.getTabAt(position))
                        }
                    }
                } else {
                    Logs.d(TAG, "refreshMultiDispatchMenuButtons multidispatch tabs visibility set to GONE")
                    lytContainerMultiDispatchMenu?.visibility = View.GONE
                }
            }
        }
    }

    private fun moveIndicatorTab(tab: Tab?) {

        vBinding.root.doOnLayout {
            val position = tab?.position ?: 0

            // Mover el indicator
            if (vBinding.tabsMultiDispatch != null) {
                val tabView = (vBinding.tabsMultiDispatch?.getChildAt(0) as ViewGroup).getChildAt(position)

                Logs.d(TAG, "moveIndicatorTab: $position")
                val indicator = vBinding.indicator

                indicator?.layoutParams?.width = tabView.width

                val animator = ObjectAnimator.ofFloat(indicator, "translationX", tabView.left.toFloat())
                animator.duration = 200
                animator.start()

                // Cambiar ancho del indicador para que coincida con la pestaña

                indicator?.requestLayout()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.hasTaximeterConnectionFlow.collect {
                        vModel.checkNoClientButton()
                        when (sharedViewModel.shiftStatusFlow.value?.currentStatus) {
                            ifConstants.STATE_DISPATCHED -> showTopBarIconRightFromDispatched()
                            ifConstants.STATE_HIRED_DISPATCHED -> topBarIconRightFromHired()
                        }
                    }
                }
                launch {
                    sharedViewModel.dispatchFlow.collect {
                            it?.let { dispatch ->
                            Logs.d(TAG, "dispatchFlow: $dispatch")
                            sharedViewModel.checkCustomerCallAvailable(dispatch)

                            val hasAtDoorAction = dispatch.isAtDoorNotificationEnabled() && dispatch.isAtDoorNotificationSent == false
                            val hasRiderInCabAction = (dispatch.riderInCab ?: false) && dispatch.isRiderInCabNotificationSent == false

                            if ((hasAtDoorAction && hasRiderInCabAction)) {
                                vBinding.btnNotifications.setButtonType(ButtonType.AVISOS.value)
                                vBinding.btnNotifications.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                vBinding.btnNotifications.setAction {
                                    if (vModel.hasITopTaximeterConnected() && sharedViewModel.shiftStatusFlow.value?.isManual == false) {
                                        try {
                                            iMainActivity.showToast(R.string.itop_functionality_restricted)
                                        } catch (e: IllegalStateException) {
                                            Logs.d(TAG, "Couldn't show toast: ${e.message}")
                                        }
                                        return@setAction
                                    }
                                    openNotificationDialog()
                                }
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
                                    val isMultitrip = (sharedViewModel.multiDispatchFlow.value?.size ?: 0) > 1
                                    if (isMultitrip) {
                                        val callbackCall: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                                            if (response.buttonPressed == ButtonType.ACCEPT) {
                                                vModel.sendInCabNotification(sharedViewModel.dispatchFlow.value)
                                            }
                                        }

                                        val name = dispatch.dispatchName ?: ""
                                        val address = dispatch.pickUpAdress ?: ""
                                        val numDispatch = dispatch.longDispatchNumber ?: ""

                                        iMainActivity.openDialog(
                                            model = CustomDialog.CustomDialogModel(
                                                title = getString(R.string.rider_in_cab), buttons = arrayListOf(
                                                    ButtonType.CANCEL, ButtonType.ACCEPT
                                                ),
                                                description =  "$name\n$address\n$numDispatch"
                                            ), response = callbackCall,
                                            fragmentManager = childFragmentManager
                                        )
                                    } else {
                                        vModel.sendInCabNotification(sharedViewModel.dispatchFlow.value)
                                    }
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

                            vModel.retrieveExtraData(dispatch.id)
                            bindDataDispatch(dispatch)

                            Logs.d(
                                TAG,
                                "Taximeter Bluetooth State: " + Taximeter.getInstance().bluetoothState
                            )

                            vModel.checkTTSOnDispatch(dispatch)
                            if ((dispatch.isConcertedPrice == true || dispatch.isConcertedMaximumPrice == true) && !vModel.hasShowedConcertedPriceDialog) {
                                showConcertedPriceWarningDialog()
                                vModel.hasShowedConcertedPriceDialog = true
                            }

                                if (dispatch.flightCode.isNotBlank()) {
                                    vBinding.lytFlightCode.visibility = View.VISIBLE
                                    vBinding.tvFlightCode.text = dispatch.flightCode
                                } else {
                                    vBinding.lytFlightCode.visibility = View.GONE
                                    vBinding.tvFlightCode.text = ""
                                }


                            /*if (dispatch.routeId != null && dispatch.routeId?.isNotEmpty() == true && dispatch.routeId?.isNotBlank() == true) {
                                dispatch.routeId?.let {
                                    vModel.isMultiDispatchInfo(it)
                                }
                            }*/
                        }
                    }
                }

                launch {
                    sharedViewModel.bravoStateFlow.collect {
                        if (vModel.hasITopTaximeterConnected()) {
                            vBinding.btnNoClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            vBinding.btnReturn.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            when (it.showReturnDispatchButton) {
                                true -> {
                                    vBinding.btnReturn.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    setReturnOnClick()
                                }

                                false -> vBinding.btnReturn.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                            when (it.clientButton) {
                                false -> vBinding.btnNoClient.changeBackground(CustomButton.BackgroundButtonColor.RED)
                                true -> {
                                    vBinding.btnNoClient.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                                }
                            }

                            when (it.noClientButtonEnabled) {
                                false -> vBinding.btnNoClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                true -> vBinding.btnNoClient.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }


                        when (it.bridgeCallState) {
                            2 -> {
                                iMainActivity.showToast(R.string.call_starting)
                                vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.RED)
                                vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                            0 -> {
                                vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            }
                            else -> {
                                iMainActivity.showToast(R.string.wait_central_confirm)
                                vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                                vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.shiftStatusFlow.collect { hiredDispatched ->
                        hiredDispatched?.let {
                            vModel.checkNoClientButton()
                            when (it.currentStatus) {
                                ifConstants.STATE_DISPATCHED -> showTopBarIconRightFromDispatched()
                                ifConstants.STATE_HIRED_DISPATCHED -> topBarIconRightFromHired()
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.dispatchNotificationModel.isDoorNotificationReceivedByCentralFlow.collect {
                        iMainActivity.showToast(R.string.notify_in_door_received)
                    }
                }

                launch {
                    sharedViewModel.dispatchNotificationModel.isRiderInCabNotificationReceivedByCentralFlow.collect {
                        iMainActivity.showToast(R.string.notify_rider_in_cab_received)
                    }
                }

                launch {
                    sharedViewModel.dispatchNotificationModel.isNoClientReceivedByCentralFlow.collect {
                        iMainActivity.showToast(R.string.notify_no_client_received)
                    }
                }

                launch {
                    vModel.timerFlow.collect { tick ->
                        if (tick == null) {
                            iMainActivity.changeTextTopBar(CustomTopBar.TextType.TIMER, "")
                        } else {
                            iMainActivity.changeTextTopBar(
                                CustomTopBar.TextType.TIMER,
                                tick.toString()
                            )
                        }
                    }
                }

                launch {
                    vModel.extraDataFlow.collect { dispatchExtra ->
                        dispatchExtra?.let {
                            if (vModel.isCurrentBluetoothITop()) {
                                val dispatch = sharedViewModel.dispatchFlow.value
                                var dispatchIdentification = ""
                                dispatch?.dispatchName?.let { dispatchName ->
                                    dispatchIdentification = dispatchName
                                }

                                dispatchIdentification = if (dispatchIdentification.isNotEmpty()) {
                                    if (it.externalTripId != null && it.externalTripId != 0) {
                                        dispatchIdentification.plus(" - ").plus(it.externalTripId)
                                    } else {
                                        dispatchIdentification.plus(" - ").plus(dispatch?.longDispatchNumber)
                                    }
                                } else {
                                    dispatch?.idDispatch ?: "0"
                                }

                                vBinding.tvDespatchNumber.text = formatDispatchNumber(dispatchIdentification)
                                vBinding.tvDespatchNumber.setOnClickListener {
                                    iMainActivity.navigateTo(InfoDispatchFragmentDirections.actionInfoDispatchFragmentToMeetingSignFragment(
                                        vModel.meetingSignColors.value.first, vModel.meetingSignColors.value.second
                                    ))
                                }
                            }
                            //Tolls
                            Logs.d("DISPATCH_AMOUNT", "trigger collector")
                            vModel.bravoConfigurationLogin.value?.let { bravo ->
                                if (bravo.showTripAmount == true) {
                                    sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                        val tolls = dispatchExtra.tolls ?: 0
                                        vBinding.tvAmount.text = formatAmount(
                                            amount = dispatch.amount ?: 0
                                        )
                                        if (tolls > 0) {
                                            vBinding.tvTolls.text = formatAmount(tolls)
                                            vBinding.lytTolls.visibility = View.VISIBLE
                                            val total = dispatch.amount?.plus(tolls) ?: 0
                                            vBinding.tvTotal.text = formatAmount(total)
                                            vBinding.lytTotal.visibility = View.VISIBLE
                                        }
                                        if ((dispatch.amount ?: 0) > 0) {
                                            vBinding.lytAmount.visibility = View.VISIBLE
                                        }
                                    }
                                } else {
                                    vBinding.lytAmount.visibility = View.GONE
                                }
                            }
                            //Requirement
                            it.customerRequirements?.let { requirements ->
                                if (requirements.isNotBlank()) {
                                    vBinding.lytRequirements.visibility = View.VISIBLE
                                    vBinding.tvRequirements.text = requirements.replace(", ", "\n")
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.noClientButtonFlow.collect {
                        Logs.d(TAG, "noClientButtonFlow: $it")
                        val canChange = it == false && sharedViewModel.shiftStatusFlow.value?.currentStatus == ifConstants.STATE_HIRED_DISPATCHED
                        Logs.d(TAG, "noClientButtonFlow: canChange: $canChange")
                        if (canChange) {
                            vBinding.btnNoClient.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    vModel.phoneCallErrorFlow.collect { handlePhoneCallError(it) }
                }

                launch {
                    sharedViewModel.phoneCallErrorFlow.collect { handlePhoneCallError(it) }
                }

                launch {
                    vModel.bridgeCallTimeoutFlow.collect {
                        Logs.e(TAG, "bridgeCallTimeoutFlow: timeout received, resetting button from LOADING to ENABLE")
                        vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }

            launch {
                    sharedViewModel.customerCallAvailable.collect { isAvailable ->
                        Logs.d(TAG, "customerCallAvailable: $isAvailable")
                        if (isAvailable) vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE) else vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    }
                }

                launch {
                    sharedViewModel.customerCallButtonStateAvailable.collect { isAvailable ->
                        Logs.d(TAG, "customerCallButtonAvailable: $isAvailable")
                        if (isAvailable && sharedViewModel.customerCallAvailable.value) vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE) else vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.LOADING)
                    }
                }

                launch {
                    combine(
                        sharedViewModel.canMakeCallsFlow,
                        sharedViewModel.bravoStateFlow
                    ) { canMakeCalls, bravoState ->
                        canMakeCalls to bravoState.bridgeCallState
                    }.collect { (canMakeCalls, bridgeCallState) ->
                        Logs.d(TAG, "canMakeCalls: $canMakeCalls, bridgeCallState: $bridgeCallState")

                        vModel.saveBridgeCallState(bridgeCallState)

                        // FASE 1: si el cliente no tiene opción de llamar, el botón queda
                        // deshabilitado y no se re-habilita aunque queden llamadas disponibles.
                        if (!sharedViewModel.customerCallAvailable.value) {
                            vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else if (canMakeCalls || bridgeCallState != 0) {
                            vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } else {
                            vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                /*launch {
                    vModel.multiDispatchFlow.collect { infoMultiDispatch ->
                        infoMultiDispatch?.let {
                            Logs.d(TAG, "multiDispatchFlow: $it")
                            refreshMultiDispatchMenuButtons(it)
                        }
                    }
                }*/
                
                launch { 
                    sharedViewModel.multiDispatchFlow.collect {
                        it?.let { infoList ->
                            refreshMultiDispatchMenuButtons(infoList)
                        }
                    }
                }
            }
        }
    }

    private fun showTopBarIconRightFromDispatched() {
        sharedViewModel.hasTaximeterConnectionFlow.value.let { taximeterConnection ->
            if (vModel.canGoToHiredManual(taximeterConnection)) {
                showTopBarIconRight()
            } else {
                hideTopBarIconRight()
            }
        }

    }

    private fun setReturnOnClick() {
        vBinding.btnReturn.setAction {
            val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    iMainActivity.returnDispatch()
                }
            }

            iMainActivity.openDialog(
                model = CustomDialog.CustomDialogModel(
                    title = getString(R.string.dialog_return_dispatch_title),
                    description = getString(R.string.dialog_return_dispatch),
                    buttons = arrayListOf(
                        ButtonType.CANCEL, ButtonType.ACCEPT
                    )
                ), response = callback,
                fragmentManager = childFragmentManager
            )
        }
        vBinding.btnReturn.changeBackground(CustomButton.BackgroundButtonColor.RED)
    }

    private fun showConcertedPriceWarningDialog() {
        val title = getString(R.string.dialog_warning_title_fixed_price)
        val description = if (sharedViewModel.dispatchFlow.value?.isConcertedPrice == true) {
            getString(R.string.concerted_trip_with_fixed_price)
        } else {
            getString(R.string.concerted_trip_with_maximum_price)
        }
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {

        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = title,
                description = description,
                icon = R.drawable.round_warning_24,
                buttons = arrayListOf(ButtonType.ACCEPT),
            ), callback
        )
    }

    private fun bindDataDispatch(dispatch: InfoDispatchModel) {
        vBinding.tvDespatchNumber.setOnClickListener {
            iMainActivity.navigateTo(InfoDispatchFragmentDirections.actionInfoDispatchFragmentToMeetingSignFragment(
                vModel.meetingSignColors.value.first, vModel.meetingSignColors.value.second
            ))
        }

        if (dispatch.pickUpZone != null) {
            vBinding.tvZona.visibility = View.VISIBLE
            vBinding.tvZona.text = dispatch.pickUpZone
        } else {
            vBinding.tvZona.visibility = View.GONE
        }

        var dispatchIdentification = ""
        dispatch.dispatchName?.let {
            dispatchIdentification = it
        }

        dispatchIdentification = if (dispatchIdentification.isNotEmpty()) {
            dispatchIdentification.plus(" - ").plus(dispatch.longDispatchNumber)
        } else {
            dispatch.idDispatch
        }
        if (!dispatch.hasExtraData || !vModel.hasITopTaximeterConnected()) {
            vBinding.tvDespatchNumber.text = formatDispatchNumber(dispatchIdentification)
        }

        if (dispatch.pickUpTime != null) {
            val pickUpTimeText = if (!dispatch.etaTime.isNullOrBlank()) {
                dispatch.pickUpTime.plus(" (${dispatch.etaTime})")
            } else {
                dispatch.pickUpTime
            }
            vBinding.lytTime.visibility = View.VISIBLE
            vBinding.tvPickupTime.text = pickUpTimeText

        } else {
            vBinding.lytTime.visibility = View.GONE
        }

        val dispatchAmount = dispatch.amount
        if (dispatchAmount != null && dispatchAmount != 0) {
            vModel.bravoConfigurationLogin.value?.let { bravo ->
                if (bravo.showTripAmount == true) {
                    vBinding.lytAmountContado.visibility = View.VISIBLE
                    vBinding.tvAmountContado.text = formatAmount(dispatchAmount)
                } else {
                    vBinding.lytAmountContado.visibility = View.GONE
                }
            }
        } else {
            vBinding.lytAmountContado.visibility = View.GONE
        }

        val clientType = dispatch.clientType
        if (clientType != null && clientType > 0) {
            vBinding.lytSubscriber.visibility = View.VISIBLE
            if (!vModel.isCurrentBluetoothITop()) {
                vBinding.tvSubscriberNameTitleValue.text =
                    listOfNotNull(dispatch.subscriber, dispatch.dispatchName)
                        .joinToString(" ")
                vBinding.tvSubscriberUserName.text = dispatch.subscriberUserName
            } else {
                vBinding.tvSubscriberNameTitleValue.text = dispatch.dispatchName
                vBinding.tvSubscriberUserName.text = dispatch.subscriberUserName
            }
        } else {
            vBinding.lytSubscriber.visibility = View.GONE
        }

        val autorizacion = dispatch.autorizacion
        if (!autorizacion.isNullOrBlank() && clientType != null && clientType > 0 && !vModel.isCurrentBluetoothITop()) {
            vBinding.lytSubscriberAuth.visibility = View.VISIBLE
            vBinding.tvSubscriberAutorization.text = autorizacion
        } else {
            vBinding.lytSubscriberAuth.visibility = View.GONE
        }

        val pickUpAdress = dispatch.pickUpAdress
        if (pickUpAdress != null) {
            vBinding.pickupContainer.visibility = View.VISIBLE
            vBinding.tvPickup.text = pickUpAdress
        } else {
            vBinding.pickupContainer.visibility = View.GONE
        }

        val destinyAdress = dispatch.destinyAdress
        if (!destinyAdress.isNullOrEmpty()) {
            vBinding.rvDestinations.visibility = View.VISIBLE
            vBinding.separatorDirections.visibility = View.VISIBLE
            val adapter = DestinyAdapter(destinyAdress)
            vBinding.rvDestinations.layoutManager = LinearLayoutManager(requireContext())
            vBinding.rvDestinations.adapter = adapter
        } else {
            vBinding.rvDestinations.visibility = View.GONE
            vBinding.separatorDirections.visibility = View.GONE
        }

        val numberPassengers = dispatch.numberPassengers
        if (!numberPassengers.isNullOrBlank() && numberPassengers != "1") {
            vBinding.lytPeople.visibility = View.VISIBLE
            vBinding.tvNumPassengers.text = numberPassengers
        } else {
            vBinding.lytPeople.visibility = View.GONE
        }

        val numberBaggages = dispatch.numberBaggages
        if (!numberBaggages.isNullOrBlank() && numberBaggages != "0") {
            vBinding.lytBaggages.visibility = View.VISIBLE
            vBinding.tvNumBaggage.text = numberBaggages
        } else {
            vBinding.lytBaggages.visibility = View.GONE
        }

        val isConcertedPrice = dispatch.isConcertedPrice
        if (isConcertedPrice == true) {
            vBinding.tvTipoServicio.visibility = View.VISIBLE
            vBinding.concertadoAmountContainer.visibility = View.VISIBLE
            vBinding.tvTipoServicio.text = getString(R.string.concerted_trip_with_fixed_price)

            vBinding.lytAmountContado.visibility = View.GONE
        } else {
            vBinding.concertadoAmountContainer.visibility = View.GONE
            vBinding.tvTipoServicio.visibility = View.GONE
        }

        if (dispatch.isPaymentByApp == true) {
            vBinding.tvTipoServicio.visibility = View.VISIBLE
            vBinding.tvTipoServicio.text = getString(R.string.payment_by_app)
        } else {
            vBinding.tvTipoServicio.visibility = View.GONE
        }

        val observations = dispatch.observations
        if (observations != null && observations.isNotEmpty()) {
            vBinding.lytObservations.visibility = View.VISIBLE
            val adapter = SimpleListAdapter(observations.dropLastWhile { it.isBlank() })
            vBinding.rvObservations.layoutManager = LinearLayoutManager(requireContext())
            vBinding.rvObservations.adapter = adapter
            vModel.getProvider(dispatch.id, adapter.dataToString())
        } else {
            vBinding.lytObservations.visibility = View.GONE
        }

        val destinationZone = getDispatchDestinationZoneInformation(dispatch)


        if (destinationZone != null) {
            val zone = W2CLocation.getZoning().getZoneById(destinationZone.first, destinationZone.second)

            if (zone != null) {
                vBinding.tvDropOffZoneText.visibility = View.VISIBLE
                vBinding.tvDropOffZoneText.text = getString(R.string.drop_off_zone_text, zone.nombreZone)
            } else {
                vBinding.tvDropOffZoneText.visibility = View.GONE
            }
        } else {
            vBinding.tvDropOffZoneText.visibility = View.GONE
        }
    }

    private fun getDispatchDestinationZoneInformation(dispatch: InfoDispatchModel): Pair<Int, Int>? {
        val macroZone = try {
            dispatch.macroZone?.toInt()
        } catch (e: NumberFormatException) {
            0
        }

        val zone = try {
            dispatch.zone?.toInt()
        } catch (e: NumberFormatException) {
            0
        }

        if (macroZone == null || macroZone == 0 || zone == null || zone == 0) {
            return null
        }

        return Pair(macroZone, zone)
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            iMainActivity.navigateTo(
                HomeDirections.goToOnTripFragment()
            )

            vModel.stopTTS()
        }
    }

    override fun updateTopBarIconRight() {
        //Do nothing. The screen controls the button itself
    }

    private fun showTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, true) {
            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    vModel.stopTTS()

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
        }
    }

    private fun hideTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, false)
    }

    private fun topBarIconRightFromHired() {
        sharedViewModel.hasTaximeterConnectionFlow.value.let { taximeterConnection ->
            if (vModel.isCurrentBluetoothITop()) {
                hideTopBarIconRight()
            } else if (!taximeterConnection) {
                showTopBarIconRightToPayment()
            } else if (sharedViewModel.shiftStatusFlow.value?.isManual == true) {
                showTopBarIconRightToPayment()
            } else {
                hideTopBarIconRight()
            }
        }
    }

    private fun showTopBarIconRightToPayment() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.NEXT, true) {
            vModel.stopTTS()
            vModel.changeStateToPaymentManual()
        }
    }

    private fun openInCabNotification() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.sendInCabNotification(sharedViewModel.dispatchFlow.value)
            }
        }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_cliente_en_puerta_title), buttons = arrayListOf(
                    ButtonType.CANCEL, ButtonType.ACCEPT
                )
            ), response = callback,
            fragmentManager = childFragmentManager
        )
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
            sharedViewModel.dispatchFlow.value?.isAtDoorNotificationSent == false
        ) {
            buttons.add(ButtonType.AT_DOOR)
        }

        if (sharedViewModel.dispatchFlow.value?.riderInCab == true && sharedViewModel.dispatchFlow.value?.isRiderInCabNotificationSent == false) {
            buttons.add(ButtonType.RIDER_IN_CAB)
        }

        val dispatch = sharedViewModel.dispatchFlow.value
        val name = dispatch?.dispatchName ?: ""
        val address = dispatch?.pickUpAdress ?: ""
        var numDispatch = dispatch?.longDispatchNumber.orEmpty()

        vModel.extraDataFlow.value?.let { extra ->
            if (vModel.isCurrentBluetoothITop() && extra.externalTripId != 0) {
                numDispatch = extra.externalTripId.toString()
            }
        }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.select_notification), buttons = ArrayList(buttons),
                description =  "$name\n$address\n$numDispatch"
            ), response = callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun formatAmount(amount: Int): String {
        return amount.toCurrency()
    }

    private fun handlePhoneCallError(error: PhoneCallUseCaseImpl.PhoneCallError?) {
        when (error) {
            PhoneCallUseCaseImpl.PhoneCallError.NO_SIM_CARD -> iMainActivity.showToast(R.string.error_no_sim)
            PhoneCallUseCaseImpl.PhoneCallError.NO_PERMISSION -> iMainActivity.showToast(R.string.phone_permission_missing)
            PhoneCallUseCaseImpl.PhoneCallError.INVALID_PHONE_NUMBER -> iMainActivity.showToast(
                R.string.invalid_phone_number
            )

            PhoneCallUseCaseImpl.PhoneCallError.NETWORK_ERROR -> iMainActivity.showToast(R.string.network_error)
            PhoneCallUseCaseImpl.PhoneCallError.UNKNOWN -> iMainActivity.showToast(R.string.default_error)
            null -> {
                vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.RED)
                vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.ENABLE)
            }
        }
    }

    private fun formatDispatchNumber(text: String): SpannableString {
        val spannableString = SpannableString(text)
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        
        val dashIndex = text.indexOf(" - ")
        
        if (dashIndex != -1) {
            spannableString.setSpan(UnderlineSpan(), 0, dashIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(
                ForegroundColorSpan(whiteColor),
                dashIndex,
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        return spannableString
    }
}