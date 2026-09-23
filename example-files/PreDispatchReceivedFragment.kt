package ifac.td.taxi.ui.screen

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentDispatchReceivedBinding
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.model.PreDispatchModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PreDispatchReceivedViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.showButtonCancelDispatch
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreDispatchReceivedFragment :
    BaseFragment<FragmentDispatchReceivedBinding, PreDispatchReceivedViewModel>(
        R.layout.fragment_dispatch_received
    ) {
    private val vModel: PreDispatchReceivedViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val safeArgs: PreDispatchReceivedFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentDispatchReceivedBinding.inflate(layoutInflater)

    lateinit var preDispatch: PreDispatchModel
    override fun setupComponents() {
        preDispatch = PreDispatchModel.toObject(safeArgs.preDispatch)
        iMainActivity.showHeader(false)
        setButtons()
        vModel.checkIsDirRecogidaAceptaDespacho()

        iMainActivity.closeDialog(CustomDialog.CustomDialogTAG.LOCATE_ON_STAND_DIALOG)
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            //Don't let the user to navigate back
        }
    }

    private fun setPreDispatchInfo(configuration: BravoConfigurationVariableEntity) {
        var txtMessage = ""

        preDispatch.let {
            vBinding.apply {
                txPickUpZone.text = it.pickUpZone
                txtMessage.plus(it.pickUpZone.plus("\n"))
                if (vModel.showPickupAddress()) {
                    val smallPickUpAdress = it.getSmallAddress()
                    if (smallPickUpAdress.isNotBlank()) {
                        vBinding.txSimpleAdress.text = smallPickUpAdress
                        vBinding.txSimpleAdress.visibility = View.VISIBLE
                        txtMessage = txtMessage.plus(smallPickUpAdress.plus("\n"))
                    }

                    if (it.pickUpCity.isNotBlank()) {
                        vBinding.txCity.text = it.pickUpCity
                        vBinding.txCity.visibility = View.VISIBLE
                        txtMessage = txtMessage.plus(it.pickUpCity.plus("\n"))

                    }
                }
                if (configuration.showPickupTime) {
                    txHour.text = it.pickUpTime
                    txtMessage = txtMessage.plus(it.pickUpTime.plus("\n"))
                }
                when (configuration.showCustomerName) {
                    1 -> {
                        txPassengerName.text = it.id
                        txtMessage = txtMessage.plus(it.id.plus("\n"))
                    }
                    2 -> {
                        txPassengerName.text = it.id
                        txtMessage = txtMessage.plus(it.id.plus("\n"))
                    }
                }

                when (it.canReject.showButtonCancelDispatch()) {
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

                vModel.savePreDispatchMessage(txtMessage)
            }
        }
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                iMainActivity.acceptPreDispatch(preDispatch.id)
                //vModel.acceptDispatch(preDispatch.id)
                //vModel.stopCallTimer()
            }

            btnCancel.setAction {
                iMainActivity.rejectPreDispatch(preDispatch.id)
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.bravoConfigurationLoginFlow.collect {
                        it?.let { config ->
                            setPreDispatchInfo(config)
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
                                    vBinding.btnAccept.stopTimer()
                                    vBinding.btnAccept.hideTimerButton()
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

    override fun onPause() {
        vBinding.btnCancel.stopTimer()
        super.onPause()
    }

    override fun onDestroyView() {
        vBinding.btnCancel.stopTimer()
        super.onDestroyView()
    }
}