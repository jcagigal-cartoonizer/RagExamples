package ifac.td.taxi.ui.screen

import android.graphics.Bitmap
import ifac.td.taxi.framework.util.Logs
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPaymentMoneiBinding
import ifac.td.taxi.domain.utils.NumberUtils
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.framework.util.tickets.Tickets
import ifac.td.taxi.framework.util.tickets.TicketsPrinter.get_numRecibo
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PaymentMoneiViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentMoneiFragment : BaseFragment<FragmentPaymentMoneiBinding, PaymentMoneiViewModel>(
    R.layout.fragment_payment_monei
) {
    private val vModel: PaymentMoneiViewModel by viewModel()

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPaymentMoneiBinding.inflate(layoutInflater)

    private val TAG = this.javaClass.simpleName
    private var checkStatus = false

    override fun setupComponents() {
        iMainActivity.showHeader(false)

        setViews()
        setButtons()
    }

    private fun setViews() {
        val totalAmount = sharedViewModel.tripFlow.value?.totalAmount ?: 0
        val isoMoneda = NumberUtils.currencySymbol()
        val orderId = "${System.currentTimeMillis()}_${get_numRecibo(10000000)}"

        vModel.initVM(totalAmount, isoMoneda, orderId)

        vBinding.tvAmount.text = sharedViewModel.tripFlow.value?.totalAmount?.toCurrency()

    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.generatePaymentFlow.collect {
                        Logs.d(TAG, "generatePaymentFlow Collect: $it")
                        it?.urlToPay?.let { url ->
                            vBinding.pgLoadingQr.visibility = View.GONE
                            vBinding.ivPaymentQR.visibility = View.VISIBLE
                            vBinding.ivPaymentQR.setImageBitmap(getQrFromString(url))
                        }

                        it?.status?.let { status ->
                            Logs.d(TAG, "generate: ")
                            vModel.updateActualStatus(status)
                        }
                    }
                }

                launch {
                    vModel.infoPaymentFlow.collect {
                        Logs.d(TAG, "infoPaymentFlow Collect: $it")
                        it?.nextUrl?.let { url ->
                            vBinding.pgLoadingQr.visibility = View.GONE
                            vBinding.ivPaymentQR.visibility = View.VISIBLE
                            vBinding.ivPaymentQR.setImageBitmap(getQrFromString(url))
                        }

                        it?.status?.let { status ->
                            vModel.updateActualStatus(status)
                        }
                    }
                }

                launch {
                    vModel.actualStatusFlow.collect {
                        updateUIFromStatus(it)
                    }
                }
            }
        }
    }


    private fun updateUIFromStatus(status: PaymentMoneiViewModel.StatusPayments) {
        vBinding.apply {
            tvStatus.text = status.name
            when (status) {
                PaymentMoneiViewModel.StatusPayments.SUCCEEDED -> {
                    vBinding.btnCancel.setButtonType(ButtonType.FINISH.value)
                    pgLoadingStatus.visibility = View.GONE
                    ivPaymentQR.visibility = View.GONE
                    tvInfoQR.visibility = View.GONE
                    lytAmount.visibility = View.GONE
                    pgLoadingQr.visibility = View.GONE
                    tvStatus.visibility = View.GONE

                    tvResultOk.visibility = View.VISIBLE
                }

                PaymentMoneiViewModel.StatusPayments.PENDING -> {
                    tvStatus.text = resources.getString(R.string.pending)
                    context?.let {
                        tvStatus.setBackgroundColor(it.getColor(R.color.yellow))
                    }
                }

                PaymentMoneiViewModel.StatusPayments.FAILED -> {
                    context?.let {
                        tvStatus.setBackgroundColor(it.getColor(R.color.red))
                    }
                    pgLoadingStatus.visibility = View.GONE
                }

                PaymentMoneiViewModel.StatusPayments.EXPIRED -> {
                    context?.let {
                        tvStatus.setBackgroundColor(it.getColor(R.color.red))
                    }
                    iMainActivity.navigateBack()
                }

                else -> {}
            }
        }

    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                when (vModel.actualStatusFlow.value) {
                    PaymentMoneiViewModel.StatusPayments.FAILED,
                    PaymentMoneiViewModel.StatusPayments.PENDING -> {
                        checkStatus = false
                        iMainActivity.navigateBack()
                    }

                    PaymentMoneiViewModel.StatusPayments.SUCCEEDED -> {
                        sharedViewModel.tripFlow.value?.let { trip ->
                            vModel.finishPayment(trip)
                        }
                    }

                    else -> {
                        Logs.d(
                            TAG,
                            "btnCancelClick when StatusPayment: ${vModel.actualStatusFlow.value} "
                        )
                    }
                }
            }

            btnPrint.setAction {
                vModel.generatePaymentFlow.value?.let {
                    it.urlToPay?.let { url ->
                        sharedViewModel.tripFlow.value?.totalAmount?.let { totalAmount ->
                            var bufTicket = "Bizum\r\n"
                            bufTicket += resources.getString(R.string.strAmountPascalCase) + " " + Tickets.formatear(Tickets.FORM_MON, 12, totalAmount.toString()) + "\r\n"
                            bufTicket += Tickets.getQrCode(url)
                            sharedViewModel.printTicket(bufTicket)
                        }
                    }
                }
            }
        }
    }

    private fun getQrFromString(info: String): Bitmap? {
        val barcodeEncoder = BarcodeEncoder()
        try {
            return barcodeEncoder.encodeBitmap(info, BarcodeFormat.QR_CODE, 400, 400)
        } catch (e: WriterException) {
            e.printStackTrace()
            iMainActivity.showToast(R.string.toast_onerror_generatepaymentrequest)
        }
        return null
    }
}
