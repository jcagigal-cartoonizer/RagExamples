package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentOnlineInvoiceBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.OnlineInvoiceViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.view.isGone
import androidx.core.view.isVisible
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.model.FiscalData

class OnlineInvoiceFragment :
    BaseFragment<FragmentOnlineInvoiceBinding, OnlineInvoiceViewModel>(R.layout.fragment_online_invoice) {

    private val TAG = "OnlineInvoiceFragment"

    private val vModel: OnlineInvoiceViewModel by viewModel()
    private val args: OnlineInvoiceFragmentArgs by navArgs()

    private val inputFields by lazy {
        with(vBinding) {
            listOf(
                etClientCompanyName, etClientStreetName, etClientNumber,
                etClientCity, etClientPostalCode, etClientProvince,
                etClientNIF, etClientEmail, etClientCountry
            )
        }
    }

    private val containerFields by lazy {
        with(vBinding) {
            listOf(
                clCompanyName, clStreet, clPhoneNumber, clCity,
                clPostalCode, clProvince, clNIF, clEmail, clCountry
            )
        }
    }

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentOnlineInvoiceBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        getTrip()
    }

    private fun getTrip() {
        Logs.d(TAG, "getTrip: Fetching trip with ID = ${args.tripId}")
        showLoading()
        val id = args.tripId
        vModel.getTrip(id)
    }

    private fun generateInvoice() {
        Logs.d(TAG, "generateInvoice: Generating invoice")
        if (areFieldsEmpty()) {
            Logs.e(TAG, "generateInvoice: Fields are empty, aborting")
            return
        }
        showLoading()
        vModel.generateInvoice(buildFiscalData())
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                val nifNotEmpty = etClientNIF.text.isNotEmpty()

                if (nifNotEmpty && areFieldsEmpty()) {
                    Logs.d(TAG, "setButtons: Fetching fiscal data")
                    getFiscalData()
                } else if (nifNotEmpty && !areFieldsEmpty()) {
                    Logs.d(TAG, "setButtons: Proceeding to generate invoice")
                    generateInvoice()
                } else {
                    Logs.d(TAG, "setButtons: Fields are empty, aborting")
                    iMainActivity.showToast(R.string.toast_complete_todos_los_campos)
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
                    vModel.errorFlow.collect { uiException ->
                        Logs.e(TAG, "errorFlow: Received error - ${uiException.exception}")
                        hideLoading()
                        if (!areFieldsVisible()) {
                            setFormVisibility(true)
                        } else {
                            iMainActivity.openDialog(
                                model = CustomDialog.CustomDialogModel(
                                    title = getString(R.string.dialog_error_title),
                                    description = getString(uiException.stringResId),
                                    buttons = arrayListOf(ButtonType.ACCEPT)
                                ),
                                response = { iMainActivity.navigateBack() },
                                fragmentManager = childFragmentManager
                            )
                        }
                    }
                }
                launch {
                    vModel.tripFlow.collect { trip ->
                        Logs.d(TAG, "tripFlow: Received trip data - ${trip?.id}")
                        trip?.let {
                            hideLoading()
                            Logs.d(TAG, "tripFlow: tripId = ${trip.id}")
                            trip.nif?.let { nif ->
                                vBinding.etClientNIF.setText(nif)
                                if (!nif.isEmpty()) {
                                   getFiscalData()
                                }
                            }
                        }
                    }
                }
                launch {
                    vModel.invoiceFlow.collect {
                        Logs.d(TAG, "invoiceFlow: Invoice generation successful")
                        hideLoading()

                        iMainActivity.openDialog(
                            model = CustomDialog.CustomDialogModel(
                                title = getString(R.string.dialog_success),
                                description = getString(R.string.online_invoice_sent_success),
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            ),
                            response = {}
                        )
                        iMainActivity.navigateBack()
                    }
                }
                launch {
                    vModel.fiscalDataFlow.collect { fiscalData ->
                        fiscalData?.let {
                            Logs.d(TAG, "fiscalDataFlow: Received fiscal data: $fiscalData")
                            hideLoading()
                            setFormVisibility(true)

                            vBinding.apply {
                                etClientCity.setText(fiscalData.city)
                                etClientEmail.setText(fiscalData.email)
                                etClientNumber.setText(fiscalData.number)
                                etClientPostalCode.setText(fiscalData.postalCode)
                                etClientProvince.setText(fiscalData.province)
                                etClientStreetName.setText(fiscalData.streetName)
                                etClientCompanyName.setText(fiscalData.companyName)
                                etClientCountry.setText(fiscalData.country)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getFiscalData() {
        Logs.d(TAG, "getFiscalData: Fetching fiscal data for NIF = ${vBinding.etClientNIF.text}")
        hideKeyboard()
        showLoading()
        val fiscalId = vBinding.etClientNIF.text.toString().trim()
        vModel.getFiscalData(fiscalId = fiscalId)
    }

    private fun showLoading() {
        Logs.d(TAG, "showLoading: Showing loading indicator")
        setFieldsEnabled(false)
        vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
        vBinding.pbContainer.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        Logs.d(TAG, "hideLoading: Hiding loading indicator")
        setFieldsEnabled(true)
        vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
        vBinding.pbContainer.visibility = View.GONE
    }

    private fun areFieldsEmpty(): Boolean {
        Logs.d(TAG, "areFieldsEmpty: Checking if required fields are empty")
        return inputFields.any { it.text.isNullOrBlank() }
    }

    private fun setFieldsEnabled(isEnabled: Boolean) {
        Logs.d(TAG, "setFieldsEnabled: Enabled = $isEnabled")
        inputFields.forEach { it.isEnabled = isEnabled }
    }

    private fun areFieldsVisible(): Boolean {
        Logs.d(TAG, "areFieldsVisible: Checking if fields are visible")
        return containerFields.all { !it.isGone }
    }

    private fun setFormVisibility(isVisible: Boolean) {
        Logs.d(TAG, "setFormVisibility: Visible = $isVisible")
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        containerFields.forEach { it.visibility = visibility }
    }

    private fun buildFiscalData(): FiscalData {
        Logs.d(TAG, "buildFiscalData: Building fiscal data")
        return FiscalData(
            fiscalID = vBinding.etClientNIF.text.toString(),
            companyName = vBinding.etClientCompanyName.text.toString(),
            streetName = vBinding.etClientStreetName.text.toString(),
            number = vBinding.etClientNumber.text.toString(),
            city = vBinding.etClientCity.text.toString(),
            postalCode = vBinding.etClientPostalCode.text.toString(),
            province = vBinding.etClientProvince.text.toString(),
            country = vBinding.etClientCountry.text.toString(),
            email = vBinding.etClientEmail.text.toString()
        )
    }
}