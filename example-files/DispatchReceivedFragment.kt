package ifac.td.taxi.ui.screen

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentDispatchReceivedBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.DispatchReceivedViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DispatchReceivedFragment :
    BaseFragment<FragmentDispatchReceivedBinding, DispatchReceivedViewModel>(
        R.layout.fragment_dispatch_received
    ) {
    private val vModel: DispatchReceivedViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val saveArgs: DispatchReceivedFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentDispatchReceivedBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        iMainActivity.showBottomBar(false)
        //setButtons()
        vModel.getDispatch(saveArgs.idDispatch)
        vModel.checkCancelDialogPermission()

        iMainActivity.closeDialog(CustomDialog.CustomDialogTAG.LOCATE_ON_STAND_DIALOG)
    }

    /*

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setOnClickListener {
                vModel.acceptDispatch()
                vModel.stopCallTimer()
            }

            btnCancel.setOnClickListener {
                vModel.rejectManualDispatch()
                vModel.stopCallTimer()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.dispatchFlow.collect {
                        it?.let { dispatch ->
                            dispatch.pickUpTime?.let { time ->
                                vBinding.txHour.text = time
                            }

                            dispatch.pickUpZone?.let { pikUpZone ->
                                vBinding.txPickUpZone.text = pikUpZone
                            }

                            dispatch.smallPickUpAdress?.let { address ->
                                vBinding.txSimpleAdress.text = address
                            }

                            dispatch.city?.let {
                                vBinding.txCity.text = it
                            }

                            dispatch.passengerName?.let {
                                vBinding.txPassengerName.text = it
                            }
                        }
                    }
                }
                launch {
                    vModel.phoneCallPermissionFlow.collect {
                        it?.let {
                            if (it) {
                                vModel.isNumberPhoneIn()
                            } else {
                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT) {
                                            iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_PHONE_CALL)
                                        }
                                    }

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = getString(R.string.dialog_warning_title),
                                        description = getString(R.string.dialog_warning_description_1) + "\n" +
                                                getString(R.string.dialog_warning_description_2) + "\n" +
                                                getString(R.string.dialog_warning_description_3) + "\n" +
                                                getString(R.string.dialog_warning_description_4) + "\n" +
                                                getString(R.string.dialog_warning_description_5),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ),
                                    callBack
                                )
                            }
                        }
                    }
                }
                launch {
                    vModel.tripIdFlow.collect { tripId ->
                        //sharedViewModel.updateDispatchFlow(tripId)
                        iMainActivity.navigateTo(HomeDirections.goToInfoDispatchFragment(tripId))
                    }
                }
                launch {
                    vModel.showDialogOnCancelFlow.collect {
                        if (it) {
                            vBinding.btnCancel.setOnClickListener {
                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT) {
                                            vModel.rejectManualDispatch()
                                            vModel.stopCallTimer()
                                        }
                                    }

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = getString(R.string.dialog_warning_title),
                                        description = "¿Quieres rechazar este despacho?",
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ),
                                    callBack
                                )
                            }
                        }
                    }
                }
            }
        }
    }

     */
}