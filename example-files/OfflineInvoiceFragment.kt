package ifac.td.taxi.ui.screen

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentOfflineInvoiceBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.OfflineInvoiceViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class OfflineInvoiceFragment :
    BaseFragment<FragmentOfflineInvoiceBinding, OfflineInvoiceViewModel>(R.layout.fragment_offline_invoice) {

    private val TAG = "OfflineInvoiceFragment"

    private val vModel: OfflineInvoiceViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val args: OfflineInvoiceFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentOfflineInvoiceBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vModel.checkUserPreferencesSettings()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (etClientNIF.text.isNullOrEmpty() && areFieldsEmpty()) {
                    iMainActivity.showToast(R.string.toast_complete_todos_los_campos)
                } else if (!etClientNIF.text.isNullOrEmpty() && !areFieldsEmpty() && vBinding.etClientNIF.isValid(FieldType.DNI)) {
                    Logs.d(TAG, "setButtons: Proceeding to generate invoice")
                    vModel.doBilling(
                        args.tripId,
                        etDriverDirection.text.toString(),
                        etDriverPostalCode.text.toString(),
                        etDriverCity.text.toString(),
                        etClientNameAndSurname.text.toString(),
                        etClientNIF.text.toString(),
                        etClientDirection.text.toString(),
                        etClientPostalCode.text.toString(),
                        etClientCity.text.toString(),
                    )
                } else {
                    Logs.d(TAG, "setButtons: Invalid NIF")
                    iMainActivity.showToast(R.string.check_fields)
                }
            }

            btnCancel.setAction {
                Logs.d(TAG, "setButtons: Cancel button clicked")
                iMainActivity.navigateBack()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.invoiceFlow.collect { (receipt, brokenDownTaxTicket) ->
                        Logs.d(TAG, "invoiceFlow: Invoice generated successfully, printing...")
                        iMainActivity.showToast(R.string.printing_invoice)
                        sharedViewModel.printTicket(receipt, false)
                        sharedViewModel.printTicket(brokenDownTaxTicket, true)
                        Logs.d(TAG, "invoiceFlow: Printing invoice and broken down ticket")
                        vModel.updateTripInvoiceStatus(args.tripId)

                        // Desea generar copia
                        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                            when (response.buttonPressed) {
                                ButtonType.ACCEPT -> {
                                    iMainActivity.showToast(R.string.invoice_printing_copy)
                                    sharedViewModel.printTicket(receipt, false)
                                    sharedViewModel.printTicket(brokenDownTaxTicket, true)

                                    iMainActivity.navigateBack()
                                }
                                ButtonType.CANCEL -> {
                                    iMainActivity.navigateBack()
                                }

                                else -> {}
                            }
                        }

                        iMainActivity.openDialog(
                            model = CustomDialog.CustomDialogModel(
                                title = resources.getString(R.string.btn_invoice),
                                description = getString(R.string.ask_invoice_copy),
                                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                                isCancellable = false
                            ), callBack,
                            fragmentManager = childFragmentManager
                        )
                    }
                }

                launch {
                    vModel.preferencesFlow.collect { preferences ->
                        if (preferences != null) {
                            Logs.d(TAG, "setupObservers: preferencesFlow getting user preferences and checking if they are empty")

                            if (preferences.invoiceIssuerCity.isNotEmpty()) {
                                Logs.d(TAG, "setupObservers: preferencesFlow invoiceIssuerCity: ${preferences.invoiceIssuerCity}")
                                vBinding.etDriverCity.setText(preferences.invoiceIssuerCity)
                            }

                            if (preferences.invoiceIssuerAddress.isNotEmpty()) {
                                Logs.d(TAG, "setupObservers: preferencesFlow invoiceIssuerAddress: ${preferences.invoiceIssuerAddress}")
                                vBinding.etDriverDirection.setText(preferences.invoiceIssuerAddress)
                            }

                            if (preferences.invoiceIssuerZipCode.isNotEmpty()) {
                                Logs.d(TAG, "setupObservers: preferencesFlow invoiceIssuerZipCode: ${preferences.invoiceIssuerZipCode}")
                                vBinding.etDriverPostalCode.setText(preferences.invoiceIssuerZipCode)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun areFieldsEmpty(): Boolean {
        val fields = listOf(
            vBinding.etDriverDirection,
            vBinding.etDriverPostalCode,
            vBinding.etDriverCity,
            vBinding.etClientNameAndSurname,
            vBinding.etClientNIF,
            vBinding.etClientDirection,
            vBinding.etClientPostalCode,
            vBinding.etClientCity
        )

        for (field in fields) {
            if (field.text.isNullOrBlank()) {
                return true
            }
        }
        return false
    }
}