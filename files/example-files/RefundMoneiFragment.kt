package ifac.td.taxi.ui.screen

import ifac.td.taxi.framework.util.Logs
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentRefundMoneiBinding
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.PaymentMoneiViewModel
import ifac.td.taxi.viewmodel.RefundMoneiViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RefundMoneiFragment : BaseFragment<FragmentRefundMoneiBinding, RefundMoneiViewModel>(
    R.layout.fragment_refund_monei
) {
    private val vModel: RefundMoneiViewModel by viewModel()

    private val safeArgs: RefundMoneiFragmentArgs by navArgs()
    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentRefundMoneiBinding.inflate(layoutInflater)

    private val TAG = this.javaClass.simpleName
    private var checkStatus = false

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        vModel.startRunnable(safeArgs.tripId)
        setButtons()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.tripFlow.collect {
                        it?.let { trip ->
                            vBinding.tvAmount.text = trip.totalAmount.toCurrency()
                        }
                    }
                }

                launch {
                    vModel.infoPaymentFlow.collect {
                        Logs.d(TAG, "infoPaymentFlow Collect: $it")

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
            Logs.d(TAG, "updateUIFromStatus: $status.name")
            tvStatus.text = status.name
            when (status) {
                PaymentMoneiViewModel.StatusPayments.SUCCEEDED -> {
                    tvStatus.text = resources.getString(R.string.monei_complete)
                    context?.let {
                        tvStatus.setBackgroundColor(it.getColor(R.color.blue))
                    }
                }

                PaymentMoneiViewModel.StatusPayments.REFUNDED -> {
                    tvStatus.text = resources.getString(R.string.monei_refunded)
                    context?.let {
                        tvStatus.setBackgroundColor(it.getColor(R.color.green))
                    }
                }

                else -> {

                }
            }
        }

    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }

            btnRefund.setAction {
                vModel.refund()
            }
        }
    }
}
