package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.interfacom.sdk.taximeter.licensing.Licensing
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.NavGraphDirections
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentReceiptHistoryBinding
import ifac.td.taxi.domain.model.RedSysOperation
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.CustomDialog.CustomDialogTAG.INVOICE_CHOOSE_OPTION_DIALOG
import ifac.td.taxi.ui.custom.dialog.redSysDialog.RedSysCustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.ReceiptHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReceiptHistoryFragment : BaseFragment<FragmentReceiptHistoryBinding, ReceiptHistoryViewModel>(
    R.layout.fragment_receipt_history
) {

    private val vModel: ReceiptHistoryViewModel by viewModel()
    private val TAG = "ReceiptHistoryFragment"

    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val safeArgs: ReceiptHistoryFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentReceiptHistoryBinding.inflate(layoutInflater)

    private var invoiceConfig: ReceiptHistoryViewModel.InvoiceConfig =
        ReceiptHistoryViewModel.InvoiceConfig()

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        vModel.checkNavigateWithStatus(sharedViewModel.shiftStatusFlow.value?.currentStatus)
        setButtons()
    }

    override fun onResume() {
        super.onResume()
        Logs.d(TAG, "onResume: Fetching ticket with ID ${safeArgs.ticketId}")
        vModel.getTicket(safeArgs.ticketId)

        Logs.d(TAG, "onResume: Checking voucher configuration")
        vModel.checkVoucherConfiguration()

        Logs.d(TAG, "onResume: Checking fiscal licensing")
        vModel.checkFiscalLicensing()
        vModel.loadLicensingInvoice()
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            val currentState = vModel.stateNavigateFlow.value
            val fromHistory = vModel.fromHistoryFragmentFlow.value

            Logs.d(TAG, "updateBackButton: State: ${currentState}, FromHistory: $fromHistory")

            if (fromHistory) {
                iMainActivity.navigateTo(R.id.action_receiptHistoryFragment_to_tripHistoryFragment)
                return@customBackPressed
            }

            when (currentState) {
                0 -> Logs.e(TAG, "updateBackButton: Back press in Default state, flow may not have updated as expected.")
                1 -> iMainActivity.navigateTo(HomeDirections.goToHomeFragment())
                2 -> iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
            }
        }
    }

    private fun setButtons() {
        vBinding.apply {

            btnPrint.setAction {
                val trip = vBinding.ticketViewerReceipts.getTrip()
                if (trip != null && !vBinding.ticketViewerReceipts.isEmpty()) {
                    vModel.printActualTicket(trip)
                } else {
                    iMainActivity.showToast(R.string.error_no_trip_loaded)
                }
            }

            btnPrevious.setAction {
                iMainActivity.navigateTo(R.id.action_receiptHistoryFragment_to_tripHistoryFragment)
            }

            btnPartials.setAction {
                vModel.clickPartials()
            }

            btnCard.setAction {
                val trip = vBinding.ticketViewerReceipts.getTrip()

                if (trip == null) {
                    iMainActivity.showToast(R.string.error_no_trip_loaded)
                    return@setAction
                }

                val ticketBuffer = trip.ticketBufferSumUp
                if (ticketBuffer.isNullOrEmpty()) {
                    vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.LOADING)
                    vModel.checkCardInfo(trip)
                } else {
                    sharedViewModel.printTicket(ticketBuffer)
                }
            }

            btnBill.setAction {
                onInvoiceClick()
            }

            btnFoto.setAction {
                val trip = vBinding.ticketViewerReceipts.getTrip()
                if (trip == null) {
                    iMainActivity.showToast(R.string.error_no_trip_loaded)
                    return@setAction
                }

                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.warning),
                        description = getString(R.string.subscriber_photo_confirmation),
                        buttons = arrayListOf(ButtonType.IMPRIMIR, ButtonType.ACCEPT),
                        isCancellable = false
                    ),
                    response = { response ->
                        when (response.buttonPressed) {
                            ButtonType.ACCEPT -> {
                                trip.id.let { vModel.sendPhotoVoucher(it) }
                            }

                            ButtonType.IMPRIMIR -> {
                                vModel.printActualTicket(trip)
                            }

                            else -> {}
                        }
                    },
                    fragmentManager = childFragmentManager
                )
            }
        }
    }

    private fun onInvoiceClick() {
        Logs.d(TAG, "onInvoiceClick: Invoked")

        val trip = vBinding.ticketViewerReceipts.getTrip()
        if (trip == null) {
            Logs.d(TAG, "onInvoiceClick: No trip found for current item")
            iMainActivity.showToast(R.string.error_no_trip_loaded)
            return
        }

        if (!canGenerateInvoice(trip)) {
            Logs.d(TAG, "onInvoiceClick: Cannot generate invoice for trip with id: ${trip.id}")
            iMainActivity.showToast(R.string.cannot_generate_invoice)
            return
        }

        when {
            invoiceConfig.isInvoiceEnabled && invoiceConfig.isShowInvoiceButton -> {
                Logs.d(TAG, "onInvoiceClick: Showing invoice dialog")
                showInvoiceDialog()
            }

            invoiceConfig.isInvoiceEnabled -> {
                Logs.d(
                    TAG,
                    "onInvoiceClick: Navigating to online invoice fragment for trip ID: ${trip.id}"
                )
                vModel.goToOnlineInvoiceFragment(trip.id)
            }

            invoiceConfig.isShowInvoiceButton -> {
                Logs.d(
                    TAG,
                    "onInvoiceClick: Navigating to offline invoice fragment for trip ID: ${trip.id}"
                )
                vModel.gotoOfflineInvoiceFragment(trip.id)
            }

            else -> {
                Logs.d(TAG, "onInvoiceClick: No valid condition met")
            }
        }
    }

    private fun canGenerateInvoice(trip: Trip?): Boolean {
        Logs.d(TAG, "canGenerateInvoice: Checking if invoice can be generated for trip id: ${trip?.id}")

        if (trip == null) {
            Logs.d(TAG, "canGenerateInvoice: Trip is null")
            return false
        }

        if (vModel.licensingFiscalFlow.value) {
            if (!vBinding.ticketViewerReceipts.isLastTrip()) {
                Logs.d(TAG, "canGenerateInvoice: Not the first/last trip")
                return false
            }
        }

        if (trip.invoiceAlreadyGenerated) {
            Logs.d(TAG, "canGenerateInvoice: Invoice already generated for trip: ${trip.id}")
            return false
        }

        val result = if (invoiceConfig.isInvoiceEnabled || invoiceConfig.isShowInvoiceButton) {
            if (trip.totalAmount > 0 && trip.paymentMethod != PaymentMethod.SUBSCRIBER.paymentString) {
                true
            } else {
                false
            }
        } else {
            false
        }

        Logs.d(TAG, "canGenerateInvoice: Result for trip: ${trip.id} is $result")
        return result
    }


    private fun showInvoiceDialog() {
        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_invoice_options_title),
                description = getString(R.string.dialog_invoice_options_desc),
                buttons = arrayListOf(ButtonType.EMAIL, ButtonType.IMPRIMIR)
            ),
            tag = INVOICE_CHOOSE_OPTION_DIALOG,
            response = { response ->
                when (response.buttonPressed) {
                    ButtonType.EMAIL -> {
                        vBinding.ticketViewerReceipts.getTrip()?.let {
                            vModel.goToOnlineInvoiceFragment(it.id)
                        }
                    }

                    ButtonType.IMPRIMIR -> {
                        vBinding.ticketViewerReceipts.getTrip()?.let {
                            vModel.gotoOfflineInvoiceFragment(it.id)
                        }
                    }

                    else -> {}
                }
            },
            fragmentManager = childFragmentManager
        )
    }

    override fun updateTopBarIconRight() {
        iMainActivity.configureIconsTopBar(
            iconType = CustomTopBar.IconType.SHARE,
            visibility = true
        ) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val uri = vBinding.ticketViewerReceipts.getImageFromView()
                uri?.let { iMainActivity.shareImage(it) } ?: run {
                    iMainActivity.showToast(R.string.toast_loaded_ticket)
                }
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.lastReceiptFlow.collect { pair ->
                        if (pair != null) {
                            val lastTrip = pair.first
                            val isLastTrip = pair.second
                            if (lastTrip != null) {
                                Logs.d(TAG, "lastReceiptFlow: RECEIVED. Trip: ${lastTrip?.id}")

                                vBinding.tvReceipts.visibility = View.GONE

                                vBinding.ticketViewerReceipts.setTrip(lastTrip, isLastTrip)
                                vBinding.ticketViewerReceipts.visibility = View.VISIBLE

                                if (canGenerateInvoice(lastTrip)) {
                                    vBinding.btnBill.setButtonType(ButtonType.FACTURA.value)
                                    vBinding.btnBill.setAction {
                                        onInvoiceClick()
                                    }
                                } else {
                                    vBinding.btnBill.setButtonType(ButtonType.EMPTY.value)
                                    vBinding.btnBill.setAction {}
                                }


                                if (vBinding.ticketViewerReceipts.isEmpty()) {
                                    Logs.d(TAG, "ticketViewerReceipts: EMPTY")

                                    vBinding.tvReceipts.visibility = View.VISIBLE
                                    vBinding.ticketViewerReceipts.visibility = View.GONE

                                    vBinding.tvReceipts.text =
                                        resources.getString(R.string.strRecibo_sin_viajes)
                                } else {
                                    Logs.d(TAG, "adapter checkBtnCapture")
                                    checkBtnCapture()
                                    checkPortugalAvailability(lastTrip)
                                }
                            } else {
                                Logs.d(TAG, "lastReceiptFlow: RECEIVED. Trip is null")

                                vBinding.tvReceipts.visibility = View.VISIBLE
                                vBinding.ticketViewerReceipts.visibility = View.GONE
                                vBinding.tvReceipts.text =
                                    resources.getString(R.string.strRecibo_sin_viajes)

                                vBinding.btnBill.setButtonType(ButtonType.EMPTY.value)
                                vBinding.btnBill.setAction {}
                                vBinding.btnPrint.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                        }
                    }
                }

                launch {
                    vModel.hasMoneiInfoCallbackFlow.collect {
                        val hasMoneiInfo = it > 0
                        if (hasMoneiInfo) {
                            vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            iMainActivity.navigateTo(
                                ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToRefundMoneiFragment(
                                    it
                                )
                            )
                        } else {
                            //iMainActivity.showToast(resources.getString(R.string.not_monei_trip))
                            vModel.checkPinPadRedSysEnabled()
                        }
                    }
                }

                launch {
                    vModel.pinPadRedSysEnabledFlow.collect {
                        val requirementOK = it.first
                        val userSaved = it.second

                        if (requirementOK && userSaved) {
                            vModel.loginRedSys(null, null)
                        } else if (requirementOK) {
                            vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            vModel.showRedSysCustomDialog { dialogCallback, loadedUser, savedPassword ->
                                val myDialogCallback: (RedSysCustomDialog.RedSysCustomDialogResponse) -> Unit = { response ->
                                    if (response.buttonPressed == ButtonType.ACCEPT) {
                                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.LOADING)
                                    }
                                    dialogCallback(response)
                                }

                                iMainActivity.openRedSysDialog(
                                    model = RedSysCustomDialog.RedSysCustomDialogModel(
                                        title = context?.getString(R.string.red_sys_title),
                                        description = context?.getString(R.string.red_sys_description),
                                        hintRedSysUsername = context?.getString(R.string.red_sys_username),
                                        editTextRedSysUsername = loadedUser,
                                        hintRedSysPassword = context?.getString(R.string.contrasena_hint),
                                        editTextRedSysPassword = savedPassword,
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                        tvCallback = {
                                            iMainActivity.navigateTo(R.id.action_receiptHistoryFragment_to_changePasswordRedSysFragment2)
                                        }
                                    ),
                                    response = myDialogCallback
                                )
                            }
//                            iMainActivity.navigateTo(ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToReceiptRedSysFragment(
//                                emptyArray()
//                            ))
                        } else {
                            vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            iMainActivity.showToast(R.string.not_match_requirements)
                        }
                    }
                }

                launch {
                    vModel.loginRedSysFlow.collect {
                        vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        if (it != null) {
                            val arrayList: Array<RedSysOperation> = it.toTypedArray()
                            Logs.d(TAG, "loginRedSysFlow.collect: $arrayList")
                            iMainActivity.navigateTo(
                                ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToReceiptRedSysFragment(
                                    arrayList
                                )
                            )
                        } else {
                            Logs.d(TAG, "loginRedSysFlow.collect: null")
                            vBinding.btnCard.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            iMainActivity.showToast(R.string.red_sys_login_error)
                        }
                    }
                }

                launch {
                    vModel.printReceiptFlow.collect { shouldPrint ->
                        //Portugal
                        if (shouldPrint) {
                            vBinding.btnPrint.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } else {
                            vBinding.btnPrint.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    vModel.voucherConfigurationFlow.collect {
                        Logs.d(TAG, "voucherConfigurationFlow: checked voucher configuration btnFoto enabled: $it")
                        if (it) {
                            checkBtnCapture()
//                            vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } else {
                            vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    vModel.longDispatchFlow.collect {
                        Logs.d(TAG, "longDispatchFlow.collect: $it")
                        if (it != null) {
                            iMainActivity.navigateTo(HomeDirections.goToCropImageViewFragment(it))
                        } else {
                            Logs.d(TAG, "longDispatchFlow.collect: No dispatch found")
                        }
                    }
                }

                launch {
                    vModel.invoiceConfig.collect {
                        invoiceConfig = it
                    }
                }
            }
        }
    }

    private fun checkBtnCapture() {
        val trip = vBinding.ticketViewerReceipts.getTrip()

        if (trip == null) {
            Logs.d(TAG, "checkBtnCapture: trip is null, disabling photo button")
            vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        val isSubscriber = trip.paymentMethod == PaymentMethod.SUBSCRIBER.paymentString
        val isFromDispatch = trip.fromDispatch == true

        Logs.d(TAG, "checkBtnCapture: isSubscriber = $isSubscriber, isFromDispatch = $isFromDispatch")

        if (isSubscriber && isFromDispatch) {
            Logs.d(TAG, "checkBtnCapture: Checking voucher configuration")
            vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.ENABLE)
        } else {
            Logs.d(TAG, "checkBtnCapture: Disabling photo button")
            vBinding.btnFoto.setButtonStyle(CustomButton.StyleButton.DISABLE)
        }
    }

    private fun checkPortugalAvailability(trip: Trip?) {
        if (vModel.licensingFiscalFlow.value) {
            Logs.d(TAG, "checkPortugalAvailability: checking for trip = ${trip?.id}")
            vModel.isPrintableTicked(trip)
        } else {
            if (trip != null) {
                vBinding.btnPrint.setButtonStyle(CustomButton.StyleButton.ENABLE)
            } else {
                vBinding.btnPrint.setButtonStyle(CustomButton.StyleButton.DISABLE)
            }
        }
    }
}