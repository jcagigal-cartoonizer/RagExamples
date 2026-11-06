package ifac.td.taxi.ui.screen

import android.widget.ArrayAdapter
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPortugalInvoiceBinding
import ifac.td.taxi.domain.utils.CountryUtils
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.PortugalInvoiceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PortugalInvoiceFragment :
    BaseFragment<FragmentPortugalInvoiceBinding, PortugalInvoiceViewModel>(
        R.layout.fragment_portugal_invoice
    ) {
    private val vModel: PortugalInvoiceViewModel by viewModel()
    private val TAG = "PortugalInvoiceFragment"

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPortugalInvoiceBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
    }

    override fun setupObservers() {
        /*
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {

                }
            }
        }

         */
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setOnClickListener {
                iMainActivity.navigateBack()
            }

            btnAccept.setOnClickListener {
                val isExternalCustomer = vModel.externalCustomerFlow.value
                Logs.d(TAG, "btnAccept externalCustomer: $isExternalCustomer")
                btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
                if (spnCountry.selectedItem.equals(CountryUtils.arrayCountries[0])
                    && !isExternalCustomer) {
                    if (edNif.isValid(FieldType.NIF_PORTUGAL)) {
                        //PortugalUseCase.calculaHashPortugal
                        vModel.acceptInvoice(
                            edNif.text.toString(),
                            edName.text.toString(),
                            edLocalidad.text.toString(),
                            CountryUtils.arrayCountries[spnCountry.selectedItemPosition]
                        )
                    } else {
                        //ASK cliente externo
                        val tag = CustomDialog.CustomDialogTAG.PORTUGAL_EXTERNAL_CUSTOMER_DIALOG
                        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                            if (it.buttonPressed == ButtonType.ACCEPT) {
                                vModel.setExternalCustomer(true)
                                btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                iMainActivity.closeDialog(tag)
                            }
                        }

                        iMainActivity.openDialog(
                            CustomDialog.CustomDialogModel(
                                description = getString(R.string.external_client),
                                buttons = arrayListOf(
                                    ButtonType.CANCEL,
                                    ButtonType.ACCEPT,
                                ),
                            ),
                            callback,
                            tag,
                            fragmentManager = childFragmentManager
                        )
                    }
                } else {
                    // portugal not selected in spinner or external customer dialog accepted
                    vModel.acceptInvoice(
                        edNif.text.toString(),
                        edName.text.toString(),
                        edLocalidad.text.toString(),
                        CountryUtils.arrayCountries[spnCountry.selectedItemPosition]
                    )
                }
            }

            val countries: List<String> = CountryUtils.arrayCountries
            context?.let {
                val adapter = ArrayAdapter<String>(
                    it,
                    android.R.layout.simple_spinner_item, countries
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnCountry.adapter = adapter
            }
        }
    }
}