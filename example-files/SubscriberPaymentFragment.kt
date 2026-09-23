package ifac.td.taxi.ui.screen

import android.text.InputFilter
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentSubscriberPaymentBinding
import ifac.td.taxi.domain.model.Subscriber
import ifac.td.taxi.domain.model.SubscriberQR
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.VoucherQR
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.MainActivity.OnScannerResultCallback
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.SubscriberPaymentViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubscriberPaymentFragment :
    BaseFragment<FragmentSubscriberPaymentBinding, SubscriberPaymentViewModel>(
        R.layout.fragment_subscriber_payment
    ) {

    private val TAG = "SubscriberPaymentFragment"
    private val vModel: SubscriberPaymentViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private var actualTrip: Trip? = null
    private var actualDispatch: InfoDispatchModel? = null
    private val args: SubscriberPaymentFragmentArgs by navArgs()

    private var subscriberPetitionSent: Boolean = false


    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentSubscriberPaymentBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        setData()
        vBinding.ivQR.setOnClickListener {
            Logs.d(TAG, "ivQR onClick.")
            val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                when (response.buttonPressed) {
                    ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA -> {
                        val cameraPosition =
                            if (response.buttonPressed == ButtonType.FRONT_CAMERA) 1 else 0

                        iMainActivity.openScanner(object : OnScannerResultCallback {
                            override fun onSuccess(result: String) {
                                handleResult(result)
                            }
                        }, cameraPosition)
                    }

                    else -> {}
                }
            }

            iMainActivity.openDialog(
                model = CustomDialog.CustomDialogModel(
                    title = resources.getString(R.string.dialog_open_camera),
                    description = resources.getString(R.string.dialog_select_camera),
                    buttons = arrayListOf(ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA)
                ), response = callback
            )
        }
    }

    private fun setData() {
        if (!args.user.isNullOrBlank()) {
            vBinding.etUsr.setText(args.user)
        }

        if (!args.subscriber.isNullOrBlank()) {
            vBinding.etAbn.setText(args.subscriber)
        }
    }

    private fun lockEditTexts() {
        vBinding.apply {
            Logs.d(TAG, "lockEditTexts: Locking EditTexts")
            etAbn.isEnabled = false
            etUsr.isEnabled = false
        }
    }

    private fun setButtons() {
        vBinding.apply {
            sharedViewModel.tripFlow.value?.let { trip ->
                actualTrip = trip
            }

            sharedViewModel.dispatchFlow.value?.let {
                actualDispatch = it
            }

            if (actualTrip?.fromDispatch == true && actualDispatch?.subscriber?.isNotEmpty() == true) {
                ivQR.visibility = View.GONE

                sharedViewModel.dispatchFlow.value?.let { dispatch ->
                    etAbn.setText(dispatch.subscriber)
                    etUsr.setText(dispatch.user)

                    // change max auth code length to 40
                    val filters = etAut.filters.toMutableList()
                    filters.removeAll { it is InputFilter.LengthFilter }
                    filters.add(InputFilter.LengthFilter(40))
                    etAut.filters = filters.toTypedArray()
                    etAut.setText(dispatch.autorizacion)

                    if (!dispatch.autorizacion.isNullOrEmpty()) {
                        lockEditTexts()
                    }
                }

                btnAccept.setAction {
                    if (vBinding.etAbn.isValid(FieldType.SUBSCRIBER) && vBinding.etUsr.isValid(FieldType.SUBSCRIBER)) {
                        clickedOkSubscriberDispatch()

                        btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
                    }
                }

            } else {
                btnAccept.setAction {
                    if (etAbn.isValid(FieldType.SUBSCRIBER) && etUsr.isValid(FieldType.SUBSCRIBER)) {
                        clickedOkSubscriberStreet()

                        btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
                    }
                }
            }

            btnCancel.setAction {
                if (subscriberPetitionSent) {
                    Logs.d(TAG, "btnCancel: Canceling subscriber petition")
                    vModel.cancelSubscriberPetition()
                }

                iMainActivity.navigateBack()
            }
        }
    }

    private fun clickedOkSubscriberDispatch() {
        Logs.d(TAG, "clickedOkSubscriberDispatch: Checking PIN")

        if (vBinding.etPin.text.toString() == actualDispatch?.pin) {
            Logs.d(TAG, "clickedOkSubscriberDispatch: PIN is correct")

            actualDispatch?.let { infoDispatch ->
                actualTrip?.let { trip ->
                    val subscriber = saveSubscriber()
                    Logs.d(TAG, "clickedOkSubscriberDispatch: Dispatch and Trip are not null")
                    vModel.subscriberFromDispatch(infoDispatch, trip, subscriber)
                    subscriberPetitionSent = true
                }
            }
        } else {
            Logs.d(TAG, "clickedOkSubscriberDispatch: PIN is incorrect")
            StaticConfiguration.subscriberFailPin = true
            showErrorDialog(getString(R.string.pin_incorrecto)) {
                iMainActivity.navigateBack()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**launch {
                vModel.subscriberFlow.collect { subscriber ->
                subscriber?.let {
                vBinding.apply {
                etAbn.setText(it.manualSubscriber.toString())
                etUsr.setText(it.manualUser.toString())
                etAut.setText(it.manualAuthorization)
                etPin.setText(it.manualPin)
                }
                }
                }
                }*/

                launch {
                    sharedViewModel.loadingStateFlow.collect { status ->
                        if (status) {
                            vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
                        } else {
                            vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            vBinding.btnAccept.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                        }
                    }
                }

                launch {
                    vModel.checkSubscriberCreditLimitFlow.collect {
                        val validCredit = it.toBoolean1or0()
                        Logs.d(TAG, "checkSubscriberCreditLimitFlow: validCredit = $validCredit")
                        if (validCredit) {
                            iMainActivity.showToast(R.string.toast_valid_credit)
                            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch details: requireSignature=${dispatch.requireSignature}, requireVoucher=${dispatch.requireVoucher}, requireQr=${dispatch.requireQr}")
                                if (dispatch.requireSignature) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch requires signature")
                                    vModel.dispatchRequireSignature(dispatch)
                                } else if (dispatch.requireVoucher) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch requires voucher")
                                    vModel.dispatchRequireVoucher(dispatch)
                                } else if (dispatch.requireQr) {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch requires QR")
                                    vModel.dispatchRequireQR(dispatch)
                                } else {
                                    Logs.d(TAG, "checkSubscriberCreditLimitFlow: Dispatch does not require signature, voucher, or QR")
                                    vModel.validCredit(
                                        sharedViewModel.tripFlow.value,
                                        sharedViewModel.updatePrintFlowCallback
                                    )
                                }
                            }
                        } else {
                            iMainActivity.showToast(R.string.toast_not_enough_credit)
                            Logs.d(TAG, "checkSubscriberCreditLimitFlow: Not enough credit")
                        }

                        vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                    }
                }
            }
        }
    }

    private fun clickedOkSubscriberStreet() {
        val subscriber = saveSubscriber()
        if (subscriber != null) {
            Logs.d(TAG, "clickedOkSubscriberStreet: Subscriber saved = $subscriber")
            vModel.subscriberLogic(args.tripId, subscriber, args.isFromIngenico)
            subscriberPetitionSent = true
        } else {
            Logs.e(TAG, "clickedOkSubscriberStreet: Subscriber not saved, because it is null")
        }
    }

    private fun saveSubscriber(): Subscriber? {
        val etSubscriber = vBinding.etAbn
        val etUser = vBinding.etUsr
        val subscriberText = etSubscriber.text.toString().trim()
        val userText = etUser.text.toString().trim()
        Logs.d(TAG, "saveSubscriber: subscriberText=$subscriberText, userText=$userText")

        if (subscriberText.isNotEmpty() || userText.isNotEmpty()) {
            val subscriberValue = subscriberText.toIntOrNull()
            val userValue = userText.toIntOrNull()

            Logs.d(TAG, "saveSubscriber: subscriberValue=$subscriberValue, userValue=$userValue")

            vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)

            if (subscriberValue != null && userValue != null && subscriberValue > 0 && userValue > 0) {
                val subscriber = getSubscriber(subscriberValue, userValue)
                Logs.d(TAG, "saveSubscriber: subscriber saved with subscriberValue=$subscriberValue, userValue=$userValue")
                vModel.insertSubscriber(subscriber)
                return subscriber
            }
        }

        return null
    }

    private fun showErrorDialog(description: String, action: (() -> Unit)? = null) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            action?.invoke()
        }
        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = resources.getString(R.string.dialog_error_title),
                description = description,
                buttons = arrayListOf(ButtonType.ACCEPT)
            ), response = callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun getSubscriber(subscriberValue: Int, userValue: Int): Subscriber {
        val etAuth = vBinding.etAut
        val etPin = vBinding.etPin
        val subscriber = Subscriber(
            tripId = sharedViewModel.tripFlow.value?.id,
            isManual = sharedViewModel.shiftStatusFlow.value?.isManual ?: false,
            manualSubscriber = subscriberValue,
            manualUser = userValue,
            manualAuthorization = etAuth.text.toString(),
            manualPin = etPin.text.toString().ifBlank { "" },
            companyId = vModel.scannedCompanyId?.toString() ?: ""
        )
        return subscriber
    }


    fun handleResult(result: String) {
        val gson = Gson()

        try {
            val subscriberQR = gson.fromJson(result, SubscriberQR::class.java)
            if (subscriberQR.type == 1) {
                Logs.d(TAG, "handleResult: SubscriberQR detected: $subscriberQR")
                vModel.saveSubscriberQR(subscriberQR)
                vBinding.apply {
                    etAbn.setText(subscriberQR.accountId)
                    etUsr.setText(subscriberQR.userId?.toString() ?: "1")
                }
                return
            }
        } catch (e: JsonSyntaxException) {
            Logs.d(TAG, "handleResult: Not a valid SubscriberQR: ${e.message}")
        }

        try {
            val voucherQR = gson.fromJson(result, VoucherQR::class.java)
            Logs.d(TAG, "handleResult: VoucherQR detected: $voucherQR")
            vModel.saveVoucherQR(voucherQR)
            vBinding.apply {
                etAbn.setText(voucherQR.accountId)
                etUsr.setText(voucherQR.userId?.toString() ?: "1")
            }
        } catch (e: JsonSyntaxException) {
            Logs.e(TAG, "handleResult: Failed to parse QR code: ${e.message}")
        }
    }
}
