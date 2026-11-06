package ifac.td.taxi.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import com.interfacom.sdk.taximeter.taximeter.models.taximeterstatus.StatusTaximeter
import com.sumup.merchant.api.SumUpAPI
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPaymentBinding
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ALWAYS
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ONLY_IMPORT_0
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD_COMPULSORY
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_TCC
import ifac.td.taxi.domain.usecase.TTSUseCaseImpl
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.domain.utils.StaticConfiguration.Companion.subscriberFailPin
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.framework.sdk.PrimeManager
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.then
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.MainActivity
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PaymentViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.RedSysPaymentState
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentFragment :
    BaseFragment<FragmentPaymentBinding, PaymentViewModel>(R.layout.fragment_payment) {

    val TAG = "PaymentFragment"

    private val vModel: PaymentViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPaymentBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        //Taximeter.getInstance().askTripEndData()
        vModel.initPaymentView()
        vModel.setUpTTS()
        vModel.checkConditions()
        vModel.saveTripEndLocation(sharedViewModel.tripFlow.value)
        vModel.askTaximeterPrimeConfig()
        Logs.d(TAG, "trip: ${sharedViewModel.tripFlow.value}")
    }

    override fun onResume() {
        super.onResume()
        setButtons()
        setupMenu()

        if (sharedViewModel.tripFlow.value?.fromDispatch == true) {
            Logs.d(TAG, "setupComponents: from dispatch, checking subscriber attempts")
            vModel.checkSubscriberAttempts(sharedViewModel.tripFlow.value?.id)
        }

        sharedViewModel.resetCurrentAmountFlow()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCard.setAction {
                handleCardPayment()
            }

            btnCash.setAction {
                if (sharedViewModel.tripFlow.value?.totalAmount == 0) {
                    val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                        if (it.buttonPressed == ButtonType.ACCEPT) {
                            handleCashPayment()
                        }
                    }
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = context?.getString(R.string.warning),
                            description = context?.getString(R.string.dialog_service_zero),
                            buttons = arrayListOf(
                                ButtonType.CANCEL, ButtonType.ACCEPT
                            ),
                        ), callback,
                        fragmentManager = childFragmentManager
                    )
                } else {
                    handleCashPayment()
                }
            }

            btnSubscriber.setAction {
                Logs.d(TAG, "Vaciamos TemporalData.redsysLogin")
                if (sharedViewModel.tripFlow.value?.totalAmount == 0) {
                    val callback: (CustomDialog.CustomDialogResponse) -> Unit = {}
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = context?.getString(R.string.warning),
                            description = context?.getString(R.string.dialog_total_amount),
                            buttons = arrayListOf(
                                ButtonType.ACCEPT
                            ),
                        ), callback,
                        fragmentManager = childFragmentManager
                    )
                } else {
                    handleSubscriberPayment()
                }
            }

            ivAddButton.setOnClickListener {
                Logs.d("PaymentFragment", "ivAddButton onClick.")
                navigateWithAmount()
            }

            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                vModel.checkBackToDispatched(dispatch.id)
                fabBackToDispatch.setOnClickListener {
                    Logs.d("PaymentFragment", "fabBackToDispatch onClick.")
                    val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            vModel.stopCountdown()

                            vModel.backToDispatch(dispatch.id, sharedViewModel.tripFlow.value)
                        }
                    }

                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = "",
                            description = getString(R.string.retorno_despachado),
                            buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                        ), callBack,
                        fragmentManager = childFragmentManager
                    )
                }
            }

            btnCancel.setAction {
                lytLbAppPayment.visibility = View.GONE
                container.visibility = View.VISIBLE
                vBinding.btnAppPayment.visibility = View.VISIBLE
                val isConcertedOrMax = sharedViewModel.dispatchFlow.value?.let { d ->
                    d.isConcertedPrice == true || d.isConcertedMaximumPrice == true
                } ?: false
                vBinding.btnOthersPayment.visibility = View.VISIBLE
                if (isConcertedOrMax) {
                    vBinding.btnOthersPayment.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    vBinding.btnOthersPayment.isClickable = false
                }
                btnCancel.visibility = View.GONE
                ivAddButton.isClickable = true
                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                    { response ->
                        if (response.buttonPressed == ButtonType.OTHERS) {
                            vBinding.btnAppPayment.visibility = View.GONE
                            vBinding.btnOthersPayment.visibility = View.GONE
                            vBinding.lytContainerFlowMenu.visibility = View.VISIBLE
                            if (vBinding.btnCancel.visibility == View.VISIBLE) {
                                vBinding.btnCancel.visibility = View.GONE
                            }
                            vBinding.btnSubscriber.visibility = View.VISIBLE
                            vBinding.btnCard.visibility = View.VISIBLE
                            vBinding.btnCash.visibility = View.VISIBLE
                            vBinding.lytLbAppPayment?.visibility = View.GONE
                            container.visibility = View.VISIBLE
                            clExtraButtons.visibility = View.VISIBLE
                            vModel.setIsWaitingAppPaymentResponse(false)
                        } else if (response.buttonPressed == ButtonType.RETRY) {
                            restartPetition()
                        }
                    }

                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = context?.getString(R.string.dialog_app_payment_title),
                        description = context?.getString(R.string.dialog_app_payment_canceled),
                        centerText = true,
                        isCancellable = false,
                        buttons = arrayListOf(
                            ButtonType.OTHERS, ButtonType.RETRY
                        ),
                    ), callBack
                )
            }
        }
    }

    private fun setupMenu() {
        sharedViewModel.dispatchFlow.value?.let { dispatch ->
            when (dispatch.clientType) {
                SUBSCRIBER_CASH_NO_CARD -> {
                    vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                }

                SUBSCRIBER_CASH_CARD -> {
                    vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                }

                SUBSCRIBER_CREDIT_NO_CARD -> {
                    if (!subscriberFailPin) {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    } else {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }

                SUBSCRIBER_CREDIT_CARD -> {
                    if (!subscriberFailPin) {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    } else {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }

                SUBSCRIBER_CREDIT_CARD_COMPULSORY -> {
                    if (!subscriberFailPin) {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    } else {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }

                SUBSCRIBER_TCC -> {
                    if (!subscriberFailPin) {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    } else {
                        vBinding.btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }

                else -> {
                    //No abonado
                }

            }

            when (dispatch.isPaymentByApp == true && vModel.isWaitingAppPaymentResponseFlow.value) {
                true -> {
                    when (dispatch.paymentByApp) {
                        "1" -> {
                            Logs.d(TAG, "(dispatch.paymentByApp = 1) == to pay")
                        }

                        "2" -> {
                            Logs.d(TAG, "(dispatch.paymentByApp = 2) == payed")
                        }
                    }

                    vBinding.apply {
                        btnBizum.visibility = View.GONE
                        btnSubscriber.visibility = View.GONE
                        btnCard.visibility = View.GONE
                        btnCash.visibility = View.GONE

                        btnAppPayment.apply {
                            setButtonStyle(CustomButton.StyleButton.ENABLE)
                            visibility = View.VISIBLE
                            setAction {
                                vModel.stopCountdown()
                                visibility = View.GONE
                                btnCancel.visibility = View.GONE
                                lytContainerFlowMenu.visibility = View.GONE
                                clExtraButtons.visibility = View.INVISIBLE
                                lytLbAppPayment?.visibility = View.VISIBLE
                                container.visibility = View.INVISIBLE
                                ivAddButton.isClickable = false
                                Logs.d(
                                    TAG,
                                    "sharedViewModel.tripFlow.value?.totalAmount: ${sharedViewModel.tripFlow.value?.totalAmount}"
                                )
                                sharedViewModel.tripFlow.value?.totalAmount?.let {
                                    iMainActivity.sendAppPaymentAuthRequest(it)
                                }
                            }
                        }

                        btnOthersPayment.apply {
                            val isConcertedOrMax = dispatch.isConcertedPrice == true || dispatch.isConcertedMaximumPrice == true
                            if (isConcertedOrMax) {
                                setButtonStyle(CustomButton.StyleButton.DISABLE)
                                visibility = View.VISIBLE
                                isClickable = false
                            } else {
                                setButtonStyle(CustomButton.StyleButton.ENABLE)
                                visibility = View.VISIBLE
                            }
                            setAction {
                                if (isConcertedOrMax) return@setAction
                                vModel.stopCountdown()
                                val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                                    when (it.buttonPressed) {
                                        ButtonType.ACCEPT -> {
                                            Logs.d(TAG, "inAppPaymentResponseFlow: Button OTHERS pressed")
                                            vBinding.apply {
                                                btnAppPayment.visibility = View.GONE
                                                btnOthersPayment?.visibility = View.GONE
                                                vBinding.lytContainerFlowMenu.visibility = View.VISIBLE
                                                btnSubscriber.visibility = View.VISIBLE
                                                clExtraButtons.visibility = View.VISIBLE
                                                container.visibility = View.VISIBLE
                                                btnCard.visibility = View.VISIBLE
                                                btnCash.visibility = View.VISIBLE
                                                vModel.setIsWaitingAppPaymentResponse(false)
                                            }
                                        }

                                        else -> {

                                        }
                                    }
                                }

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = context?.getString(R.string.dialog_app_payment_title),
                                        description = context?.getString(R.string.app_payment_change),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                        isCancellable = false
                                    ),
                                    callback,
                                    fragmentManager = childFragmentManager
                                )
                            }
                        }
                    }
                }

                else -> {
                    Logs.d(TAG, "dispatch.isPaymentByApp != true || !isWaitingAppPaymentResponse")
                }
            }

        }
    }

    override fun updateTopBarIcon() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.BACK, false) {}
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {}
    }

    private fun setupPaymentView(trip: Trip) {
        updateAmountView(trip)
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.bravoConfigurationFlow.collect { bravoConfig ->
                        bravoConfig?.let {
                            val isFromDispatch = sharedViewModel.tripFlow.value?.fromDispatch == true
                            val allowStreetSubscriber = it.allowsSubscriber?.and(0x02) != 0
                            Logs.d(TAG, "bravoConfigurationFlow: collect isFromDispatch = $isFromDispatch, allowStreetSubscriber = $allowStreetSubscriber")
                            if (isFromDispatch || allowStreetSubscriber) {
                                Logs.d(TAG, "bravoConfigurationFlow: enable subscriber button")
                                vBinding.btnSubscriber.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            } else {
                                Logs.d(TAG, "bravoConfigurationFlow: disable subscriber button")
                                vBinding.btnSubscriber.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.tripFlow.collect {
                        it?.let { trip ->
                            Logs.d(TAG, "setupObservers: collect tripFlow payment")
                            setupPaymentView(trip)
                        }
                    }
                }
                launch {
                    sharedViewModel.dispatchFlow.collect {
                        if (it == null) {
                            vBinding.fabBackToDispatch.visibility = View.GONE
                        }
                    }
                }
                launch {
                    vModel.countdownCallbackFlow.collect {
                        handleCashPayment()
                    }
                }

                launch {
                    vModel.conditionsLocalFlow.collect {
                        it?.let { values ->
                            updateConditionViews(values.first, values.second)
                        }
                    }
                }
//                de momento, como no conocemos el estado actual del luminoso, no mostrar ningún cambio en el botón
//                launch {
//                    sharedViewModel.courtesyLightFlow.collect { isEnabled ->
//                        Logs.d(TAG, "courtesyLightFlow: Courtesy light state changed: $isEnabled")
//                        updateCourtesyLightView(isEnabled)
//                    }
//                }

                launch {
                    vModel.uvLightFlow.collect { isEnabled ->
                        updateUvLightView(isEnabled)
                    }
                }

                launch {
                    vModel.timerFlow.collect { tick ->
                        if (tick == null) {
                            iMainActivity.changeTextTimerTopBar(null)
                        } else {
                            iMainActivity.changeTextTimerTopBar(tick.toString())
                        }
                    }
                }

                launch {
                    vModel.paymentModifiersFlow.collect {
                        val withouProtocol = Taximeter.getInstance()?.isTaximeterWithoutProtocol
                        val isManual = sharedViewModel.shiftStatusFlow.value?.isManual
                        Logs.d(TAG, "withouProtocol=$withouProtocol isManual=$isManual")
                        it?.let { paymentModifiers ->
                            if (paymentModifiers.isAllowTips ||
                                paymentModifiers.isAllowTolls ||
                                paymentModifiers.isAllowModifyCash == ONLY_IMPORT_0 ||
                                paymentModifiers.isAllowModifyCash == ALWAYS ||
                                paymentModifiers.isAllowModifySubscriberAmounts ||
                                isManual == true ||
                                withouProtocol == true
                            ) {
                                vBinding.ivAddButton.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                launch {
                    vModel.checkSubscriberCreditLimitFlow.collect {
                        vBinding.btnSubscriber.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        val validCredit = it.toBoolean1or0()
                        Logs.d(TAG, "checkSubscriberCreditLimitFlow: validCredit = $validCredit")

                        if (validCredit) {
                            iMainActivity.showToast(R.string.toast_valid_credit)
                            Logs.d(TAG, "checkSubscriberCreditLimitFlow: Credit is valid")

                            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch received with clientType = ${dispatch.clientType}")

                                if (dispatch.clientType == SUBSCRIBER_CREDIT_CARD_COMPULSORY) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Launching checkIngenicoAuth")
                                    vModel.checkIngenicoAuth()
                                } else if (dispatch.requireSignature) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Signature required, dispatching")
                                    vModel.dispatchRequireSignature(dispatch)
                                } else if (dispatch.requireVoucher) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Voucher required, dispatching")
                                    vModel.dispatchRequireVoucher(dispatch)
                                } else if (dispatch.requireQr) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: QR required, dispatching")
                                    vModel.dispatchRequireQR(dispatch)
                                } else {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Valid credit path, dispatching validCredit")
                                    vModel.validCredit(
                                        sharedViewModel.tripFlow.value,
                                        dispatch,
                                        sharedViewModel.updatePrintFlowCallback
                                    )
                                }
                            } ?: Logs.e(TAG, "checkSubscriberCreditLimitFlow: Dispatch is null")
                        } else {
                            Logs.d(TAG, "checkSubscriberCreditLimitFlow: Not enough credit")
                            iMainActivity.showToast(R.string.toast_not_enough_credit)

                            Logs.d(TAG, "checkSubscriberCreditLimitFlow: Not enough credit -> Enabling buttons")
                            vBinding.apply {
                                btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.redsysPaymentResultFlow.collect { result ->
                        when (result) {
                            is MainActivity.RedsysPaymentResult.Error -> {
                                Logs.e(TAG, "onError: Payment failed")
                                TemporalData.redsysLogin = null
                                //Esto
                                vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                            is MainActivity.RedsysPaymentResult.Success -> {
                                val tickets = result.tickets
                                val isPinOk = result.isPinOk
                                val rtsIdentifier = result.rtsIdentifier

                                vModel.setRedSysPaymentState(RedSysPaymentState.SUCCESS)
                                Logs.d(TAG, "redSysPaymentState onSuccess: Payment completed, needSignature=$isPinOk")
                                val (clientTicket, commerceTicketForSign) = tickets
                                val totalAmount = sharedViewModel.tripFlow.value?.totalAmount ?: 0
                                Logs.d(TAG, "onSuccess: totalAmount=$totalAmount")
                                vModel.finishPayment(
                                    trip = sharedViewModel.tripFlow.value,
                                    dispatch = sharedViewModel.dispatchFlow.value,
                                    paymentMethod = PaymentMethod.CARD,
                                ).then {
                                    Logs.d(TAG, "finishPayment: Printing client ticket")
                                    vModel.setTicketBufferRedSys(sharedViewModel.tripFlow.value?.id, clientTicket, rtsIdentifier)
                                    // sharedViewModel.printTicket(clientTicket, true)
                                    if (vModel.userPreferencesFlow.value?.printRedSysCommerceTicketAlways == true) {
                                        Logs.d(TAG, "finishPayment: Printing commerce ticket")
                                        sharedViewModel.printTicket(commerceTicketForSign, true)
                                    }
                                }
                            }
                        }

                    }
                }

                launch {
                    sharedViewModel.sumUpResponseFlow.collect {
                        val success =
                            it.first == SumUpAPI.Response.ResultCode.TRANSACTION_SUCCESSFUL
                        //dejo esto para luego hacer cambios en el layout
                        if (success) {
                            sharedViewModel.tripFlow.value?.let { it1 ->
                                vModel.sumUpGetReceipt(
                                    it1,
                                    it.second?.getStringExtra(SumUpAPI.Response.TX_CODE))
                            }

                            vModel.finishPayment(
                                trip = sharedViewModel.tripFlow.value,
                                paymentMethod = PaymentMethod.CARD,
                                dispatch = sharedViewModel.dispatchFlow.value)

                            vModel.turnCourtesyLightOff()
                        } else {
                            vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            try {
                                iMainActivity.showToast(
                                    R.string.error_sumup,
                                    Toast.LENGTH_LONG
                                )
                            } catch (e: IllegalStateException) {
                                Logs.d(TAG, "Couldn't show toast: ${e.message}")
                            }
                        }
                    }
                }

                launch {
                    vModel.sumUpTicketFlow.collect {
                        sharedViewModel.printTicket(it)
                    }
                }

                launch {
                    vModel.noCardConfiguredFlow.collect {
                        val hideDialog = iMainActivity.getSharedPreferencesValue(
                            getString(R.string.hide_dialog_external_pos)
                        ) as? Boolean ?: false
                        if (hideDialog) {
                            vModel.finishPayment(
                                trip = sharedViewModel.tripFlow.value,
                                dispatch = sharedViewModel.dispatchFlow.value,
                                paymentMethod = PaymentMethod.CARD)
                        } else {
                            if (!vModel.isIngenicoInstalledFlow.value) {
                                Logs.d(TAG, "setupObservers.noCardConfiguredFlow: Ingenico not installed")
                                showDialogNoCardConfigured()
                            }
                        }
                    }
                }

                launch {

                    vModel.redSysStartFlow.collect { (amount, redSysTicket) ->
                        Logs.d(TAG, "redSysStartFlow: Collecting amount=$amount, ticket=$redSysTicket")
                        iMainActivity.processRedSysPayment(amount, redSysTicket)
                    }
                }

                launch {
                    vModel.redSysPaymentStateFlow.collect { state ->
                        when (state) {
                            RedSysPaymentState.SUCCESS -> {
                                val totalAmount = sharedViewModel.tripFlow.value?.totalAmount ?: 0
                                Logs.d(TAG, "redSysPaymentState: payment success popup avoided")
                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = resources.getString(R.string.red_sys_pascalcase),
                                        description = String.format(
                                            resources.getString(R.string.with_placeholder_payment_complete),
                                            formatAmount(totalAmount)
                                        ),
                                        buttons = arrayListOf(ButtonType.ACCEPT),
                                        icon = R.drawable.aceptar_green
                                    ), null
                                )
                                iMainActivity.resetBottomBarText()
                                /**iMainActivity.showToast(
                                    getString(
                                        R.string.toast_red_sys_amount,
                                        formatAmount(totalAmount)
                                    )
                                )*/
                                vModel.resetRedSysPaymentState()
                            }
                            RedSysPaymentState.ERROR -> {
                                val callback: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        when (state.code) {
                                            RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD -> {
                                                Logs.d(
                                                    TAG,
                                                    "redSysPaymentState: Navigating to change password screen"
                                                )
                                                iMainActivity.navigateTo(PaymentFragmentDirections.actionPaymentFragmentToChangePasswordRedSysFragment())
                                            }

                                            else -> {
                                                Logs.d(
                                                    TAG,
                                                    "redSysPaymentState: Navigation skipped for other error codes: ${state.code}: ${state.name} "
                                                )
                                            }
                                        }
                                    }
                                Logs.d(TAG, "redSysPaymentState: ERROR popup shown codes: ${state.code}: ${state.name} ")

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = getString(R.string.red_sys_pascalcase),
                                        description = getString(R.string.no_response_error),
                                        buttons = arrayListOf(ButtonType.ACCEPT)
                                    ), response = callback
                                )
                                vModel.resetRedSysPaymentState()
                             }
                            else -> {
                                Logs.d(TAG, "redSysPaymentState: Navigation skipped for other states codes: ${state.code}: ${state.name} ")
                            }
                        }
                    }
                }

                launch {
                    vModel.showDialogFlow.collect {
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        iMainActivity.openDialog(it.first, it.second, fragmentManager = childFragmentManager)
                    }
                }

                launch {
                    vModel.showTTSFlow.collect {
                        when (it) {
                            TTSUseCaseImpl.TTSBlindOption.NONE.value -> {
                                vBinding.fabLocution.visibility = View.GONE
                            }

                            TTSUseCaseImpl.TTSBlindOption.AUTO.value,
                            TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value-> {
                                speakTTS(true)
                            }

                            TTSUseCaseImpl.TTSBlindOption.MANUAL.value -> {
                                speakTTS(false)
                            }
                        }
                    }
                }

                launch {
                    vModel.backToDispatchedFlow.collect {
                        it.first?.let { pendings ->
                            Logs.d(TAG, "setupObservers: collect backToDispatchedFlow: $pendings")
                            if (pendings > 0) {
                                vBinding.fabBackToDispatch.visibility = View.VISIBLE
                            } else {
                                vBinding.fabBackToDispatch.visibility = View.GONE
                            }
                        }

                        if (it.second) {
                            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                iMainActivity.backToDispatched(dispatch.id)
                            }
                        }
                    }
                }

                launch {
                    vModel.dispatchExtraDataFlow.collect {
                        it?.let { extraData ->
                            sharedViewModel.tripFlow.value?.let { trip ->
                                sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                    Logs.d("DISPATCH_AMOUNT", "dispatchextraData flow trigger: ")
                                    trip.tolls = extraData.tolls ?: 0
                                    trip.tips = extraData.tips ?: 0
                                    trip.totalAmount = (dispatch.amount ?: 0) + (extraData.tolls
                                        ?: 0) + (extraData.tips ?: 0)

                                    vBinding.tvAmount.visibility = View.VISIBLE
                                    vBinding.tvAmount.text = formatAmount(trip.totalAmount)
                                    vModel.updateTripAmount(trip)

                                }

                            }

                        }
                    }
                }

                launch {
                    vModel.hasMoneiAccount.collect { hasAccount ->
                        val clientType = sharedViewModel.dispatchFlow.value?.clientType
                        if (clientType != null && clientType > 0 && !StaticConfiguration.subscriberFailPin) {
                            return@collect
                        }
                        if (hasAccount) {
                            setUpLayoutMenu()
                            modifyButtonsPositions()
                        }
                    }
                }

                launch {
                    sharedViewModel.inAppPaymentResponseFlow.collect {
                        it?.let { response ->
                            val success = response.first
                            val text = response.second

                            Logs.d(TAG, "inAppPaymentResponseFlow: Response received - success: $success, text: $text")

                            iMainActivity.stopAppPaymentCounter()

                            vBinding.apply {
                                lytLbAppPayment.visibility = View.GONE
                                lytContainerFlowMenu.visibility = View.VISIBLE
                                ivAddButton.isClickable = true
                                clExtraButtons.visibility = View.VISIBLE
                                btnCancel.visibility = View.GONE
                                btnAppPayment.visibility = View.VISIBLE
                                btnAppPayment.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                btnOthersPayment.visibility = View.VISIBLE
                                val isConcertedOrMax = sharedViewModel.dispatchFlow.value?.let { d ->
                                    d.isConcertedPrice == true || d.isConcertedMaximumPrice == true
                                } ?: false
                                if (isConcertedOrMax) {
                                    btnOthersPayment.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                    btnOthersPayment.isClickable = false
                                } else {
                                    btnOthersPayment.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                }
                            }

                            if (vModel.isWaitingAppPaymentResponseFlow.value) {
                                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { dialogResponse ->
                                    when (dialogResponse.buttonPressed) {
                                        ButtonType.ACCEPT -> {
                                            Logs.d(TAG, "inAppPaymentResponseFlow: Button ACCEPT pressed")
                                            vModel.navigatePostPayment(sharedViewModel.dispatchFlow.value)
                                        }
                                        ButtonType.RETRY -> {
                                            Logs.d(TAG, "inAppPaymentResponseFlow: Button RETRY pressed")
                                            restartPetition()
                                        }
                                        ButtonType.OTHERS -> {
                                            Logs.d(TAG, "inAppPaymentResponseFlow: Button OTHERS pressed")
                                            val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                                                when (it.buttonPressed) {
                                                    ButtonType.ACCEPT -> {
                                                        vBinding.apply {
                                                            btnAppPayment.visibility = View.GONE
                                                            btnOthersPayment?.visibility = View.GONE
                                                            vBinding.lytContainerFlowMenu.visibility = View.VISIBLE
                                                            btnSubscriber.visibility = View.VISIBLE
                                                            clExtraButtons.visibility = View.VISIBLE
                                                            container.visibility = View.VISIBLE
                                                            btnCard.visibility = View.VISIBLE
                                                            btnCash.visibility = View.VISIBLE
                                                            vModel.setIsWaitingAppPaymentResponse(false)
                                                        }
                                                    }

                                                    else -> {

                                                    }
                                                }
                                            }

                                            iMainActivity.openDialog(
                                                CustomDialog.CustomDialogModel(
                                                    title = context?.getString(R.string.dialog_app_payment_title),
                                                    description = context?.getString(R.string.app_payment_change),
                                                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                                    isCancellable = false
                                                ),
                                                callback,
                                                fragmentManager = childFragmentManager
                                            )
                                        }
                                        else -> {}
                                    }
                                }

                                if (success) {
                                    Logs.d(TAG, "inAppPaymentResponseFlow: Payment successful")
                                    handleInAppPayment()

                                    val extraText = if (!text.isNullOrBlank()) "\n$text\n" else ""
                                    val description = buildString {
                                        appendLine()
                                        append(context?.getString(R.string.dialog_app_payment_success))
                                        append(extraText)
                                    }

                                    iMainActivity.openDialog(
                                        CustomDialog.CustomDialogModel(
                                            title = context?.getString(R.string.dialog_app_payment_title),
                                            description = description.trim(),
                                            buttons = arrayListOf(ButtonType.ACCEPT),
                                            isCancellable = false,
                                            centerText = true
                                        ),
                                        callback,
                                        fragmentManager = childFragmentManager
                                    )

                                } else {
                                    Logs.e(TAG, "inAppPaymentResponseFlow: Payment not authorized")

                                    vBinding.lytContainerFlowMenu.visibility = View.VISIBLE
                                    vBinding.clExtraButtons.visibility = View.VISIBLE
                                    vBinding.container.visibility = View.VISIBLE

                                    val extraText = if (!text.isNullOrBlank()) "\n$text\n" else ""
                                    val description = buildString {
                                        append(context?.getString(R.string.dialog_app_payment_not_authorized))
                                        append(extraText)
                                    }

                                    iMainActivity.openDialog(
                                        CustomDialog.CustomDialogModel(
                                            title = context?.getString(R.string.dialog_app_payment_title),
                                            description = description.trim(),
                                            centerText = true,
                                            buttons = arrayListOf(ButtonType.OTHERS, ButtonType.RETRY)
                                        ),
                                        callback,
                                        fragmentManager = childFragmentManager
                                    )
                                }

                            } else {
                                vBinding.lytContainerFlowMenu.visibility = View.VISIBLE
                                val paymentButtons = listOf(
                                    vBinding.btnCash,
                                    vBinding.btnCard,
                                    vBinding.btnSubscriber,
                                    vBinding.btnBizum,
                                )

                                if (paymentButtons.all { button -> button.visibility == View.GONE }) {
                                    vBinding.btnSubscriber.visibility = View.VISIBLE
                                    // vBinding.btnCancel.visibility = View.VISIBLE
                                    vBinding.btnCard.visibility = View.VISIBLE
                                    vBinding.btnCash.visibility = View.VISIBLE
                                }

                                vBinding.btnAppPayment.visibility = View.GONE
                                vBinding.btnOthersPayment?.visibility = View.GONE

                            }
                        }
                    }
                }

                launch {
                    vModel.ingenicoSubscriberFlow.collect {
                        Logs.d(TAG, "ingenicoSubscriberFlow: launch intent $it")
                        resultSubscriber.launch(it)

                        //iMainActivity.launchIntentForResult(it)
                    }
                }

                launch {
                    vModel.ingenicoCardFlow.collect {
                        Logs.d(TAG, "ingenicoCardFlow: launch intent $it")
                        resultCard.launch(it)

//                        iMainActivity.launchIntentForResult(it)
                    }
                }

                launch {
                    vModel.multiDispatchNavigate.collect {
                        if (it == true) {
                            iMainActivity.goToNextInfoDispatch()
                        }
                    }
                }

                launch {
                    vModel.subscriberAttemptsFlow.collect { attempts ->
                        Logs.d(TAG, "setupObservers: collect subscriberAttemptsFlow: $attempts")
                        if (sharedViewModel.tripFlow.value?.fromDispatch == true) {
                            if (attempts == 0) {
                                Logs.d(TAG, "setupObservers: subscriber attempts equal to 0, enabling buttons")
                                vBinding.apply {
                                    btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                    btnCash.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                }
                            } else {
                                Logs.d(TAG, "setupObservers: subscriber attempts: $attempts")
                            }
                        }
                    }
                }

                launch {
                    vModel.cardButtonStatusFlow.collect {
                        vBinding.btnCard.setButtonStyle(it)
                    }
                }
            }
        }
    }

    private val resultSubscriber = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //Parse
            result.data?.let { intent ->
                Logs.d(TAG, "resultSubscriber: Parsing intent data")
                sharedViewModel.getIngenicoIntentData(intent)
            }
            Logs.d(TAG, "resultSubscriber: Result OK")
        } else {
            Logs.e(TAG, "resultSubscriber: Result not OK")
        }
    }

    private val resultCard = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                handleResultCardOk(it)
            } ?: Logs.e(TAG, "resultCard: Result OK but Intent data was null")
        } else {
            handleResultCardError()
        }
    }

    private fun handleResultCardOk(intent: Intent) {
        Logs.d(TAG, "handleResultCardOk: Creating Ingenico ticket")
        sharedViewModel.tripFlow.value?.let { trip ->
            iMainActivity.showToast(R.string.operation_authorized)
            vModel.ingenicoAccepted(trip , sharedViewModel.dispatchFlow.value, intent).then {
                vModel.finishPayment(trip = trip, dispatch = sharedViewModel.dispatchFlow.value, paymentMethod = PaymentMethod.CARD, isIngenico = true)
            }
        }
        Logs.d(TAG, "handleResultCardOk: Result OK")
    }

    private fun handleResultCardError() {
        Logs.e(TAG, "handleResultCardError: Result not OK")

        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)

        vModel.ingenicoRejected()
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { dialogResponse ->
            when (dialogResponse.buttonPressed) {
                else -> {}
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = context?.getString(R.string.warning),
                description = context?.resources?.getString(R.string.operacionDenegada),
                buttons = arrayListOf(ButtonType.ACCEPT)
            ),
            callback,
        )
    }

    private fun modifyButtonsPositions() {
        vBinding.btnBizum.visibility = View.VISIBLE
        (vBinding.btnBizum.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(1, 1, 1f)
            rowSpec = GridLayout.spec(1, 1, 1f)
        }.also { vBinding.btnBizum.layoutParams = it }

        (vBinding.btnCard.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(0, 1, 1f)
            rowSpec = GridLayout.spec(1, 1, 1f)
        }.also { vBinding.btnCard.layoutParams = it }

        (vBinding.btnCash.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(0, 1, 1f)
            rowSpec = GridLayout.spec(0, 1, 1f)
        }.also { vBinding.btnCash.layoutParams = it }

        (vBinding.btnSubscriber.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(1, 1, 1f)
            rowSpec = GridLayout.spec(0, 1, 1f)
        }.also { vBinding.btnSubscriber.layoutParams = it }

        //Por si acaso
        vBinding.btnAppPayment.visibility = View.GONE
        (vBinding.btnAppPayment.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(1, 1, 1f)
            rowSpec = GridLayout.spec(0, 1, 1f)
        }.also { vBinding.btnAppPayment.layoutParams = it }

        vBinding.btnOthersPayment?.visibility = View.GONE
        (vBinding.btnOthersPayment?.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(2, 1, 1f)
            rowSpec = GridLayout.spec(0, 1, 1f)
        }.also { vBinding.btnOthersPayment?.layoutParams = it }

        //Por si acaso
        vBinding.btnCancel.visibility = View.GONE
        (vBinding.btnCancel.layoutParams as GridLayout.LayoutParams).apply {
            columnSpec = GridLayout.spec(1, 1, 1f)
            rowSpec = GridLayout.spec(0, 1, 1f)
        }.also { vBinding.btnCancel.layoutParams = it }

        vBinding.btnBizum.setAction {
            iMainActivity.stopPaymentCounter()
            iMainActivity.navigateTo(R.id.action_paymentFragment_to_paymentMoneiFragment)
        }
    }

    private fun setUpLayoutMenu() {
        vBinding.lytContainerFlowMenu.rowCount = 4
        vBinding.lytContainerFlowMenu.columnCount = 4
        (vBinding.lytContainerFlowMenu.layoutParams as ConstraintLayout.LayoutParams).apply {
            val orientation = resources.configuration.orientation
            dimensionRatio =
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    "1:1"
                } else {
                    "5:3"
                }
        }.also { vBinding.lytContainerFlowMenu.layoutParams = it }
    }

    private fun restartPetition() {
        vBinding.apply {
            btnCancel.visibility = View.GONE
            lytContainerFlowMenu.visibility = View.GONE
            btnAppPayment.visibility = View.GONE
            btnOthersPayment?.visibility = View.GONE
            clExtraButtons.visibility = View.INVISIBLE
            lytLbAppPayment?.visibility = View.VISIBLE
            container.visibility = View.INVISIBLE
            ivAddButton.isClickable = false
        }
        sharedViewModel.tripFlow.value?.totalAmount?.let { amount ->
            iMainActivity.sendAppPaymentAuthRequest(amount)
        }
    }

    private fun speakTTS(auto: Boolean) {
        val trip = sharedViewModel.tripFlow.value
        val serviceAmount = sharedViewModel.dispatchFlow.value?.carreraMinima?.takeUnless { it.isBlank() }?.toIntOrNull() ?: trip?.taximeterAmount
        val extraAmount = trip?.extras
        val tollAmount = trip?.tolls
        val tipAmount = trip?.tips
        val totalAmount = trip?.totalAmount

        vBinding.fabLocution.visibility = View.VISIBLE
        vBinding.fabLocution.setOnClickListener {
            Logs.d("PaymentFragment", "fabLocution onClick.")
            if (serviceAmount != null && extraAmount != null && tollAmount != null && tipAmount != null && totalAmount != null) {
                vModel.useTTSForAmount(
                    serviceAmountLocal = serviceAmount,
                    extraAmountLocal = extraAmount,
                    tollAmountLocal = tollAmount,
                    tipAmountLocal = tipAmount,
                    totalAmountLocal = totalAmount,
                )
            }
        }

        if (auto) {
            if (serviceAmount != null && extraAmount != null && tollAmount != null && tipAmount != null && totalAmount != null) {
                vModel.useTTSForAmount(
                    serviceAmountLocal = serviceAmount,
                    extraAmountLocal = extraAmount,
                    tollAmountLocal = tollAmount,
                    tipAmountLocal = tipAmount,
                    totalAmountLocal = totalAmount
                )
            }
        }

    }

    private fun handlePayment(paymentType: PaymentMethod) {
        Logs.d(TAG, "handlePayment: Starting payment process for $paymentType")
        vModel.stopCountdown()
        val totalAmount = sharedViewModel.tripFlow.value?.totalAmount ?: 0
        Logs.d(TAG, "handlePayment: Total amount is $totalAmount")

        when (paymentType) {
            PaymentMethod.CARD -> {
                Logs.d(TAG, "handlePayment: Payment method is CARD")
                sharedViewModel.tripFlow.value?.let {
                    vModel.cardPayment(it.totalAmount, sharedViewModel.launchSumUpPayment, "")
                    Logs.d(TAG, "handlePayment: Card payment initiated")
                }
            }

            PaymentMethod.CASH -> {
                Logs.d(TAG, "handlePayment: Payment method is CASH")
                vModel.finishPayment(
                    trip = sharedViewModel.tripFlow.value,
                    dispatch = sharedViewModel.dispatchFlow.value,
                    paymentMethod = paymentType
                )
            }

            PaymentMethod.APP -> {
                Logs.d(TAG, "handlePayment: Payment method is APP")
                vModel.finishPayment(
                    trip = sharedViewModel.tripFlow.value,
                    paymentMethod = paymentType,
                    dispatch = sharedViewModel.dispatchFlow.value,
                    navigate = false
                )
            }

            PaymentMethod.SUBSCRIBER -> {
                Logs.d(TAG, "handlePayment: Payment method is SUBSCRIBER")
                if (sharedViewModel.dispatchFlow.value?.askAuthCode == true && sharedViewModel.dispatchFlow.value?.typedAuthCode?.isBlank() == true) {
                    Logs.d(TAG, "handlePayment: Asking for auth code")
                    openAskAuthCodeDialog(sharedViewModel.dispatchFlow.value!!)
                } else if (sharedViewModel.tripFlow.value?.fromDispatch == true && sharedViewModel.dispatchFlow.value?.subscriber?.isNotBlank() == true) {
                    Logs.d(TAG, "handlePayment: Using subscriber from dispatch")
                    vModel.useSubscriberAttempt(sharedViewModel.tripFlow.value)
                    vModel.subscriberFromDispatch(
                        sharedViewModel.tripFlow.value, sharedViewModel.dispatchFlow.value
                    )
                } else {
                    Logs.d(TAG, "handlePayment: Subscriber payment initiated")
                    vModel.subscriberPayment(sharedViewModel.tripFlow.value)
                }
            }

            PaymentMethod.MONEI -> {
                Logs.d(TAG, "handlePayment: Payment method is MONEI")
            }
            PaymentMethod.EMPTY -> {
                Logs.d(TAG, "handlePayment: Payment method is EMPTY")
            }
        }

        vModel.turnCourtesyLightOff()
        Logs.d(TAG, "handlePayment: Courtesy light turned off")

        iMainActivity.resetBottomBarText()
        //iMainActivity.showToast(formatAmount(totalAmount))
        Logs.d(TAG, "handlePayment: Payment process completed")
    }

    private fun openAskAuthCodeDialog(dispatch : InfoDispatchModel) {
        Logs.d(TAG, "openAskAuthCodeDialog: Opening dialog to ask for auth code")
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                if (response.editTextString?.isBlank() == true || response.editTextString == null) {
                    try {
                        iMainActivity.showToast(R.string.auth_code_empty)
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    openAskAuthCodeDialog(dispatch)
                } else {
                    response.editTextString?.let { authCode ->
                        sharedViewModel.dispatchFlow.value?.typedAuthCode = authCode
                        vModel.saveTypedAuthCode(dispatch.id, authCode)
                        handleSubscriberPayment()
                    }
                }
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = context?.getString(R.string.ask_auth_code),
                description = "${context?.getString(R.string.srtAbonado)}: ${dispatch.subscriber}\n" +
                        "${context?.getString(R.string.strUser)}: ${dispatch.user}\n" +
                        "${context?.getString(R.string.hint_auth_code)}",
                editTextMaxLength = if(sharedViewModel.tripFlow.value?.fromDispatch == true) 40 else 14,
                hint = context?.getString(R.string.hint_auth_code),
                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun hasToShowPrimeTripTypeSelectionDialog() : Boolean {
        val primeConfig = sharedViewModel.primeConfigurationFlow.value
        var show = (primeConfig != null && primeConfig.hasPrime() && PrimeManager.showTripTypeSelectionDialog && !vModel.isManualService)
        if (show) {
            if (primeConfig?.tripTypes.isNullOrEmpty()) {
                show = false
            }
        }
        return show
    }


    private fun handleCashPayment() {
        Logs.d(TAG, "handleCashPayment: Checking if Prime Trip Type Selection Dialog needs to be shown")
        if (hasToShowPrimeTripTypeSelectionDialog()) {
            Logs.d(TAG, "handleCashPayment: Showing Prime Dialog for CASH payment")
            showPrimeDialog(PaymentMethod.CASH) {
                Logs.d(TAG, "handleCashPayment: Handling CASH payment after Prime Dialog")
                handlePayment(PaymentMethod.CASH)
            }
            return
        }
        Logs.d(TAG, "handleCashPayment: Handling CASH payment directly")
        handlePayment(PaymentMethod.CASH)
    }

    private fun handleInAppPayment() {
        Logs.d(TAG, "handleInAppPayment: Checking if Prime Trip Type Selection Dialog needs to be shown")
        if (hasToShowPrimeTripTypeSelectionDialog()) {
            Logs.d(TAG, "handleInAppPayment: Showing Prime Dialog for APP payment")
            showPrimeDialog(PaymentMethod.APP) {
                Logs.d(TAG, "handleInAppPayment: Handling APP payment after Prime Dialog")
                handlePayment(PaymentMethod.APP)
            }
            return
        }
        Logs.d(TAG, "handleInAppPayment: Handling APP payment directly")
        handlePayment(PaymentMethod.APP)
    }

    private fun handleCardPayment() {
        Logs.d(TAG, "handleCardPayment: Checking if Prime Trip Type Selection Dialog needs to be shown")
        if (hasToShowPrimeTripTypeSelectionDialog()) {
            Logs.d(TAG, "handleCardPayment: Showing Prime Dialog for CARD payment")
            showPrimeDialog(PaymentMethod.CARD) {
                Logs.d(TAG, "handleCardPayment: Handling CARD payment after Prime Dialog")
                handlePayment(PaymentMethod.CARD)
            }
            return
        }
        Logs.d(TAG, "handleCardPayment: Handling CARD payment directly")
        handlePayment(PaymentMethod.CARD)
    }

    private fun handleSubscriberPayment() {
        Logs.d(TAG, "handleSubscriberPayment: Checking if Prime Trip Type Selection Dialog needs to be shown")
        if (hasToShowPrimeTripTypeSelectionDialog()) {
            Logs.d(TAG, "handleSubscriberPayment: Showing Prime Dialog for SUBSCRIBER payment")
            showPrimeDialog(PaymentMethod.SUBSCRIBER) {
                Logs.d(TAG, "handleSubscriberPayment: Handling SUBSCRIBER payment after Prime Dialog")
                handlePayment(PaymentMethod.SUBSCRIBER)
            }
            return
        }
        Logs.d(TAG, "handleSubscriberPayment: Handling SUBSCRIBER payment directly")
        handlePayment(PaymentMethod.SUBSCRIBER)
    }

    private fun showPrimeDialog(pymentMethod: PaymentMethod, onAcceptCallback: () -> Unit) {
        vModel.stopCountdown()

        val description = if (sharedViewModel.primeConfigurationFlow.value?.tripTypes.isNullOrEmpty()) {
            getString(R.string.prime_trip_type_not_configured)
        } else {
            getString(R.string.prime_trip_type_select)
        }

        val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    response.selectedOption?.id?.let {
                        vModel.sendPrimeTripSelected(it)
                        android.util.Log.d(TAG, "sendPrimeTripSelected")
                        //vModel.sendSelectedPrimePaymentType(pymentMethod)
                        //android.util.Log.d(TAG, "sendSelectedPrimePaymentType")
                    }
                    onAcceptCallback()
                }
            }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = context?.getString(R.string.warning),
                description = description,
                listOptions = sharedViewModel.primeConfigurationFlow.value?.tripTypes,
                isCancellable = false,
                dialogTAG = CustomDialog.CustomDialogTAG.PRIME_PAYMENT_DIALOG,
                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
            ), callBack,
            fragmentManager = childFragmentManager
        )
    }

    private fun updateAmountView(it: Trip) {
        if (it.fromDispatch) {
            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                if (dispatch.isConcertedPrice == true) {
                    //Check extras from extraData
                    vModel.retrieveDispatchExtraData(dispatch.id)
                }
            }
        }

        if (it.totalAmount == 0) {
            it.totalAmount = it.taximeterAmount + it.tolls + it.tips + it.extras
            Logs.d(TAG, "total: " + it.totalAmount)
        }


            Logs.d(TAG, "updateView")
            vBinding.tvAmount.visibility = View.VISIBLE
            Logs.d(
                "DISPATCH_AMOUNT",
                "updateAmountView changes tvAmount ${formatAmount(it.totalAmount)}"
            )
            vBinding.tvAmount.text = formatAmount(it.totalAmount)
            //vModel.updateTripAmount(it)
         
    }


    private fun updateConditionViews(device: BluetoothInfo?, status: Int) {
        when {
            device?.name?.startsWith("SKYG") == true && status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED -> {
                showCourtesyLightView()
                showUvLightView()
                setupCourtesyLightClickListener()
                setupUvLightClickListener()

                //En principio lo ha el tx automáticamente, no hace falta encenderlo nosotros
                //vModel.turnCourtesyLightOn()
            }

            device?.name?.startsWith("SHER") == true && status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED -> {
                hideCourtesyLightView()
                showUvLightView()
                setupUvLightClickListener()
            }

            else -> {
                hideCourtesyLightView()
                hideUvLightView()
            }
        }
    }

    private fun showCourtesyLightView() {
        vBinding.fabCortesyLight.visibility = View.VISIBLE
    }

    private fun hideCourtesyLightView() {
        vBinding.fabCortesyLight.visibility = View.GONE
    }

    private fun setupCourtesyLightClickListener() {
        vBinding.fabCortesyLight.setOnClickListener {
            Logs.d("setupCourtesyLightClickListener", "fabCortesyLight clicked.")
            vModel.turnCourtesyLight()
        }
    }

    private fun showUvLightView() {
        vBinding.fabUvLight.visibility = View.VISIBLE
    }

    private fun hideUvLightView() {
        vBinding.fabUvLight.visibility = View.GONE
    }

    private fun setupUvLightClickListener() {
        vBinding.fabUvLight.setOnClickListener {
            Logs.d("PaymentFragment", "fabUvLight onClick.")
            vBinding.fabUvLight.isEnabled = false
            vBinding.fabUvLight.foreground =
                ResourcesCompat.getDrawable(resources, R.drawable.luz_ultravioleta_blue, null)
            vBinding.fabCortesyLight.isEnabled = false
            vModel.uvLight()
        }
    }

    private fun updateCourtesyLightView(value: Boolean?) {
        val isEnabled = value == true
        Logs.d("updateCourtesyLightView", "Updating courtesy light view. Enabled: $isEnabled")
        vBinding.fabCortesyLight.foreground = ResourcesCompat.getDrawable(resources, if (isEnabled) R.drawable.lumact_yellow else R.drawable.lumact, null)
        //vBinding.fabUvLight.isEnabled = !isEnabled
    }

    private fun updateUvLightView(isEnabled: Boolean) {
        if (!isEnabled) {
            vBinding.fabUvLight.foreground =
                ResourcesCompat.getDrawable(resources, R.drawable.luz_ultravioleta, null)
            vBinding.fabCortesyLight.isEnabled = true
            vBinding.fabUvLight.isEnabled = true
        }
    }

    private fun navigateWithAmount() {
        vModel.stopCountdown()
        val directions = sharedViewModel.tripFlow.value?.let {
            PaymentFragmentDirections.actionPaymentFragmentToAddAmountFragment(
                it.id
            )
        }
        if (directions != null) {
            iMainActivity.navigateTo(directions)
        }
    }

    private fun formatAmount(taximeterAmount: Int): String {
        return taximeterAmount.toCurrency()
    }

    private fun showDialogNoCardConfigured() {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                val isCheckBoxMarked = response.checkBoxStatus ?: false
                iMainActivity.saveSharedPreferencesValue(
                    getString(R.string.hide_dialog_external_pos),
                    isCheckBoxMarked
                )
                vModel.finishPayment(
                    trip = sharedViewModel.tripFlow.value,
                    dispatch = sharedViewModel.dispatchFlow.value,
                    paymentMethod = PaymentMethod.CARD)
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = context?.getString(R.string.warning),
                description = context?.getString(R.string.sumup_not_configured),
                buttons = arrayListOf(
                    ButtonType.CANCEL, ButtonType.ACCEPT
                ),
                checkBoxText = getString(R.string.dialog_no_card_configured_checkbox)
            ), callBack,
            fragmentManager = childFragmentManager
        )
    }
}
