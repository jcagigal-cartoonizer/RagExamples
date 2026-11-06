package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentClosedPartialBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.viewmodel.ClosedPartialViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ClosedPartialFragment :
    BaseFragment<FragmentClosedPartialBinding, ClosedPartialViewModel>(R.layout.fragment_closed_partial) {

    private val TAG = "ClosedPartialFragment"

    private val vModel: ClosedPartialViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val safeArgs: ClosedPartialFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentClosedPartialBinding.inflate(layoutInflater)

    override fun setupComponents() {
        Logs.d(TAG, "setupComponents: Initializing ClosedPartialFragment, justClosed=${safeArgs.justClosed}")
        iMainActivity.showHeader(true)
        vModel.checkTaximeterStatus()
        setButtons()
        vModel.getClosedPartial()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                Logs.d(TAG, "btnCancel: User clicked back/cancel")
                vModel.manageBack(safeArgs.justClosed)
            }
            btnPrint.setAction {
                Logs.d(TAG, "btnPrint: User clicked print")
                vModel.printPartial()
            }

            btnTotalizers.setAction {
                Logs.d(TAG, "btnTotalizers: User clicked totalizers")
                iMainActivity.navigateTo(R.id.action_closedPartialFragment_to_totalizersFragment)
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.partialFlow.collect { partial ->
                        Logs.d(TAG, "partialFlow collect: Received partial ${if (partial != null) "data" else "null"}")

                        val ticketContent = partial?.bufLastTicketCierre?.takeIf { it.isNotEmpty() }
                            ?: getString(R.string.no_closed_partials)

                        vBinding.ticketViewerReceipts.setTicketContent(ticketContent)

                        if (partial != null) {
                            vBinding.scrollViewClosedPartial.post {
                                if (isAdded && view != null) {
                                    vBinding.scrollViewClosedPartial.fullScroll(View.FOCUS_DOWN)
                                }
                            }
                        }

                        updateTotalizersButtonState()
                    }
                }
            }
        }
    }

    private fun updateTotalizersButtonState() {
        val isConnected = vModel.isTaximeterConnectedFlow.value
        val hasTotalizers = sharedViewModel.taximeterTotalizersFlow.value != null
        val currentStatus = sharedViewModel.shiftStatusFlow.value?.currentStatus

        val shouldShow = currentStatus != ifConstants.STATE_DISCONNECTED && isConnected

        vBinding.btnTotalizers.apply {
            visibility = if (shouldShow) View.VISIBLE else View.GONE

            if (shouldShow) {
                val style = if (isConnected && hasTotalizers) {
                    CustomButton.StyleButton.ENABLE
                } else {
                    CustomButton.StyleButton.DISABLE
                }
                setButtonStyle(style)
            }
        }
    }
}