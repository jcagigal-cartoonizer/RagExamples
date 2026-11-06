package ifac.td.taxi.ui.screen

import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentInfoMinuteDispatchBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.model.PreDispatchModel
import ifac.td.taxi.viewmodel.DispatchMinuteReceivedViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.DispatchReceivedModel
import ifac.td.taxi.viewmodel.model.UtilsModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.showButtonCancelDispatch
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DispatchMinuteReceivedFragment :
    BaseFragment<FragmentInfoMinuteDispatchBinding, DispatchMinuteReceivedViewModel>(R.layout.fragment_info_minute_dispatch) {

    private val TAG = "DispatchMinuteReceivedFragment"

    private val vModel: DispatchMinuteReceivedViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val safeArgs: DispatchMinuteReceivedFragmentArgs by navArgs()
    private var preDispatch: PreDispatchModel? = null

    private val isPreDispatch: Boolean
        get() = safeArgs.idDispatch == -1L

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentInfoMinuteDispatchBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        iMainActivity.showBottomBar(false)
        setButtons()

        Logs.d(TAG, "setupComponents: Fetching dispatch with ID ${safeArgs.idDispatch}")
        vModel.getDispatch(safeArgs.idDispatch)
        vModel.checkIsDirRecogidaAceptaDespacho()
        vModel.checkCancelDialogPermission()

        safeArgs.preDispatch?.takeIf { it.isNotBlank() && it != "null" }?.let {
            Logs.d(TAG, "setupComponents: PreDispatch data found")
            preDispatch = PreDispatchModel.toObject(it)
        }

        iMainActivity.closeDialog(CustomDialog.CustomDialogTAG.LOCATE_ON_STAND_DIALOG)
        Logs.d(TAG, "setupComponents: isPreDispatch: $isPreDispatch")
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                if (isPreDispatch) {
                    Logs.d(TAG, "DispatchMinuteReceivedFragment: rejectPreDispatch")
                    iMainActivity.rejectPreDispatch(preDispatch?.id.toString())
                } else {
                    Logs.d(TAG, "DispatchMinuteReceivedFragment: rejectDispatch")
                    iMainActivity.rejectDispatch(safeArgs.idDispatch)
                }
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.dispatchFlow.collect { receivedModel ->
                        receivedModel?.let { dispatch ->
                            bindDataDispatch(dispatch)

                            if (dispatch.autoAccept?.toBoolean1or0() == true) {
                                try {
                                    val grid = vBinding.glFlowContainer
                                    for (i in 0..grid.childCount) {
                                        val button: CustomButton? = grid.getChildAt(i) as? CustomButton
                                        if (button == null || button.isGone) {
                                            continue
                                        }
                                        if (i == 1) {
                                            button.setButtonType(ButtonType.ACCEPT.value)
                                            button.setAction {
                                                iMainActivity.cancelAutoAcceptDispatch()
                                                iMainActivity.acceptDispatch(safeArgs.idDispatch)
                                            }
                                            continue
                                        }
                                        button.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                    }

                                    vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
                                } catch (e: Exception) {
                                    Logs.d("DispatchMinuteReceived", "setButtons: $e")
                                }

                                iMainActivity.autoAcceptDispatch(safeArgs.idDispatch)
                            }
//                dispatch.destinyAdress?.let {
//                    vBinding.tvStartDirection.text = "DESTINO: $it"
//                }
                        }
                    }

                }

                launch {
                    vModel.showDialogOnCancelFlow.collect {
                        if (it) {
                            vBinding.btnCancel.setAction {
                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT) {
                                            if (isPreDispatch) {
                                                Logs.d(TAG, "DispatchMinuteReceivedFragment: rejectPreDispatch")
                                                iMainActivity.rejectPreDispatch(preDispatch?.id.toString())
                                            } else {
                                                Logs.d(TAG, "DispatchMinuteReceivedFragment: rejectDispatch")
                                                iMainActivity.rejectDispatch(safeArgs.idDispatch)
                                            }
                                        }
                                    }

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = getString(R.string.warning),
                                        description = getString(R.string.dialog_reject_dispatch_desc),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ),
                                    callBack,
                                    fragmentManager = childFragmentManager
                                )
                            }
                        }
                    }
                }

                launch {
                    vModel.minutesFlow.collect {
                        it?.let { minutes ->
                            try {
                                val grid = vBinding.glFlowContainer
                                for (i in 0 until grid.childCount) {
                                    val button: CustomButton = grid.getChildAt(i) as? CustomButton ?: continue
                                    if (i < minutes.size) {
                                        val minute = minutes[i]
                                        button.visibility = View.VISIBLE
                                        button.changeTvOnlyText(minute.toString())
                                        button.changeTextColor(true)
                                        button.changeBackground(getColorForMinute(minute))
                                        button.setAction {
                                            val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                                { response ->
                                                    if (response.buttonPressed == ButtonType.ACCEPT) {
                                                        if (isPreDispatch) {
                                                            Logs.d(TAG, "DispatchMinuteReceivedFragment: acceptPreDispatch")
                                                            iMainActivity.acceptPreDispatch(preDispatch?.id.toString(), minute)
                                                        } else {
                                                            Logs.d(TAG, "DispatchMinuteReceivedFragment: acceptDispatch")
                                                            iMainActivity.acceptDispatch(safeArgs.idDispatch, minute)
                                                        }
                                                    }
                                                }

                                            iMainActivity.openDialog(
                                                CustomDialog.CustomDialogModel(
                                                    title = getString(R.string.dialog_aceptar_despacho_title),
                                                    description = getString(
                                                        R.string.dialog_aceptar_despacho_description,
                                                        minute.toString()
                                                    ),
                                                    buttons = arrayListOf(
                                                        ButtonType.CANCEL,
                                                        ButtonType.ACCEPT
                                                    )
                                                ),
                                                callBack,
                                                fragmentManager = childFragmentManager
                                            )
                                        }
                                    } else {
                                        button.visibility = View.GONE
                                    }
                                }
                            } catch (e: Exception) {
                                Logs.d("DispatchMinuteReceived", "setButtons: $e")
                            }
                        }
                    }
                }

                launch {
                    vModel.bravoConfigurationLoginFlow.collect { config ->
                        config?.let {
                            if (isPreDispatch) {
                                setPreDispatchInfo()
                            } else {
                                vModel.dispatchFlow.value?.let { dispatch ->
                                    bindDataDispatch(dispatch)
                                }
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.buttonTimerState.collect { timerState ->
                        if (timerState != null) {
                            when (timerState.buttonType) {
                                ButtonType.CANCEL -> {
                                    vBinding.btnCancel.showTimerButton(
                                        maxSeconds = timerState.maxSeconds,
                                        leftSeconds = timerState.remainingSeconds,
                                        countdownStartTime = timerState.startTime
                                    )
                                }

                                else -> {}
                            }
                        } else {
                            vBinding.btnCancel.stopTimer()
                            vBinding.btnCancel.hideTimerButton()
                        }
                    }
                }
            }
        }
    }

    private fun bindDataDispatch(dispatch: DispatchReceivedModel) {
        if (vModel.bravoConfigurationLoginFlow.value == null) {
            Logs.d(TAG, "BravoConfigurationLoginFlow is null, not setting data")
            return
        }

        Logs.d(TAG, "BravoConfigurationLoginFlow is not null, setting data")

        if (vModel.bravoConfigurationLoginFlow.value?.showPickupTime == true) {
            dispatch.pickUpTime?.let { time ->
                vBinding.txHour.visibility = View.VISIBLE
                vBinding.txHour.text = time
            }
        }

        when (vModel.bravoConfigurationLoginFlow.value?.showCustomerName) {
            1 -> {
                dispatch.passengerName?.let { passagerName ->
                    vBinding.txPassengerName.text = passagerName
                    vBinding.txPassengerName.visibility = View.VISIBLE
                }
            }

            2 -> {
                dispatch.passengerName?.let { passagerName ->
                    dispatch.subscriberUserName?.let { subscriberUserName ->
                        vBinding.txPassengerName.visibility = View.VISIBLE
                        if (subscriberUserName.isNotBlank()) {
                            vBinding.txPassengerName.text =
                                "$passagerName - $subscriberUserName"
                        } else {
                            vBinding.txPassengerName.text = passagerName
                        }
                    }
                }
            }
        }
        if (!dispatch.extra.isNullOrBlank()) {
            vBinding.txExtra.text = dispatch.extra
            vBinding.txExtra.visibility = View.VISIBLE
        }


        if (vModel.bravoConfigurationLoginFlow.value?.showDestination == true) {
            dispatch.dropOffAddress?.let { dropOff ->
                vBinding.txDestination?.text = dropOff
                vBinding.txDestination?.visibility = View.VISIBLE
            }
        }

        if (vModel.showPickupAddress()) {
            if (dispatch.smallPickUpAdress != null && dispatch.smallPickUpAdress?.isNotBlank() == true) {
                vBinding.txSimpleAdress.text = dispatch.smallPickUpAdress
                vBinding.txSimpleAdress.visibility = View.VISIBLE
            }

            if (dispatch.city != null && dispatch.city?.isNotBlank() == true) {
                vBinding.txCity.text = dispatch.city
                vBinding.txCity.visibility = View.VISIBLE
            }
        }

        if (dispatch.pickUpZone != null && dispatch.pickUpZone?.isNotBlank() == true) {
            vBinding.txPickUpZone.text = dispatch.pickUpZone
            vBinding.txPickUpZone.visibility = View.VISIBLE
        }
    }

    private fun getColorForMinute(minute: Int): CustomButton.BackgroundButtonColor {
        return if (minute < 5) {
            CustomButton.BackgroundButtonColor.GREEN
        } else if (minute in 5..9) {
            CustomButton.BackgroundButtonColor.BLUE
        } else if (minute in 10..15) {
            CustomButton.BackgroundButtonColor.YELLOW
        } else {
            CustomButton.BackgroundButtonColor.ORANGE
        }
    }

    private fun setPreDispatchInfo() {
        preDispatch?.let { dispatch ->
            vBinding.apply {
                if (vModel.bravoConfigurationLoginFlow.value?.showPickupTime == true) {
                    dispatch.pickUpTime.let { time ->
                        vBinding.txHour.visibility = View.VISIBLE
                        vBinding.txHour.text = time
                    }
                }
                if (vModel.showPickupAddress()) {
                    if (dispatch.pickUpZone.isNotBlank()) {
                        vBinding.txPickUpZone.text = dispatch.pickUpZone
                        vBinding.txPickUpZone.visibility = View.VISIBLE
                    }
                }

                txDestination.visibility = View.INVISIBLE

                if (vModel.showPickupAddress()) {
                    txSimpleAdress.text = dispatch.getSmallAddress()
                    vBinding.txSimpleAdress.visibility = View.VISIBLE
                }

                when (dispatch.canReject.showButtonCancelDispatch()) {
                    UtilsModel.Companion.NOT_ACCEPT_DISPATCH.NOT_SHOW -> {
                        btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        btnCancel.changeBackground(CustomButton.BackgroundButtonColor.DEFAULT)
                    }
                    UtilsModel.Companion.NOT_ACCEPT_DISPATCH.SHOW_RED -> {
                        btnCancel.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        btnCancel.changeBackground(CustomButton.BackgroundButtonColor.RED)
                    }
                    UtilsModel.Companion.NOT_ACCEPT_DISPATCH.SHOW_BLUE -> {
                        btnCancel.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        btnCancel.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                    }
                }
            }
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            //Don't let the user to navigate back
        }
    }

    override fun onPause() {
        vBinding.btnCancel.stopTimer()
        super.onPause()
    }

    override fun onDestroyView() {
        vBinding.btnCancel.stopTimer()
        super.onDestroyView()
    }

}