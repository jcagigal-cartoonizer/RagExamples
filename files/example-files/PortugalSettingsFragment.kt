package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPortugalSettingsBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.PortugalSettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PortugalSettingsFragment :
    BaseFragment<FragmentPortugalSettingsBinding, PortugalSettingsViewModel>(
        R.layout.fragment_portugal_settings
    ) {
    private val vModel: PortugalSettingsViewModel by viewModel()
    private val TAG = "PortugalSettingsFragment"

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPortugalSettingsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)

        vModel.getPortugalData()
        setButtons()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.portugalDataFlow.collect {
                        it?.let { data ->
                            vBinding.apply {
                                etATCUD.setText(data.atcud)
                                etSerie.setText(data.sequenceNumber.toString())
                                etDocumento.setText(data.numTicketPrint.toString())
                            }
                        }
                    }
                }

                launch {
                    vModel.backDataFlow.collect {
                        iMainActivity.navigateBack()
                    }
                }

                launch {
                    vModel.pinCallbackFlow.collect {
                        if (it) {
                            openNewPinDialog()
                        } else {
                            try {
                                iMainActivity.showToast(R.string.pin_incorrecto)
                            } catch (e: IllegalStateException) {
                                Logs.d(TAG, "Couldn't show toast: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openNewPinDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                response.editTextString?.let {
                    vModel.updatePortugalPin(
                        it
                    )
                }
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.change_pin),
                editTextTypePin = true,
                description = getString(R.string.nuevo_pin),
                editText = "",
                editTextMaxLength = 4,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ), callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (!vBinding.cbResetHash.isChecked) {
                    vModel.updatePortugalData(
                        atcud = etATCUD.text.toString(),
                        sequenceNumber = etSerie.text.toString().toInt(),
                        document = etDocumento.text.toString().toInt(),
                    )
                } else {
                    checkResetHashData()
                }

            }
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
            btnChangePortugalPin.setOnClickListener {
                changePortugalPin()
            }
        }
    }

    private fun changePortugalPin() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.checkPinPortugal(response.editTextString)
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.pin_actual),
                description = getString(R.string.pin_actual_hint),
                editText = "",
                editTextTypePin = true,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ), callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun checkResetHashData() {
        val numTicket = try {
            vBinding.etDocumento.text.toString().toInt()
        } catch (e: Exception) {
            1
        }

        if (numTicket > 1) {
            try {
                iMainActivity.showToast(R.string.reset_hash_documento_1)
            } catch (e: IllegalStateException) {
                Logs.d(TAG, "Couldn't show toast: ${e.message}")
            }
        } else {
            vModel.updatePortugalData(
                atcud = vBinding.etATCUD.text.toString(),
                sequenceNumber = vBinding.etSerie.text.toString().toInt(),
                document = vBinding.etDocumento.text.toString().toInt(),
            )
        }
    }
}