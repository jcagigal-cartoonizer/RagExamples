package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentTotalizersBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.TotalizersViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class TotalizersFragment : BaseFragment<FragmentTotalizersBinding, TotalizersViewModel>(R.layout.fragment_totalizers) {

    private val TAG = "TotalizersFragment"

    private val vModel: TotalizersViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private var rawTotalizersContent: String? = null

    override fun getViewModel(): TotalizersViewModel = vModel

    override fun getViewBinding(): FragmentTotalizersBinding = FragmentTotalizersBinding.inflate(layoutInflater)

    override fun setupComponents() {
        vModel.loadTotalizersTicket(sharedViewModel.taximeterTotalizersFlow.value)
        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                Logs.d(TAG, "btnBack: User clicked back")
                iMainActivity.navigateBack()
            }

            btnPrint.setAction {
                Logs.d(TAG, "btnPrint: User clicked print. Is content available = ${!rawTotalizersContent.isNullOrEmpty()}")
                rawTotalizersContent.takeIf { !it.isNullOrEmpty() }?.let {
                    sharedViewModel.printTicket(it.addTicketLines())
                } ?: run {
                    Logs.d(TAG, "btnPrint: No totalizers to print")
                    iMainActivity.showToast(R.string.no_totalizers_to_print)
                }
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.totalizersStringFlow.collect { totalizersTicket ->
                    Logs.d(TAG, "totalizersStringFlow collect: Received totalizers data = ${totalizersTicket != null}, ticket = $totalizersTicket")

                    rawTotalizersContent = totalizersTicket
                    val totalizersContent = totalizersTicket.takeIf { !it.isNullOrEmpty() }
                        ?: getString(R.string.no_active_totalizers_with_data)

                    totalizersTicket?.let {
                        vBinding.ticketViewerTotalizers.setTicketContent(totalizersContent)
                    } ?: run {

                    }
                }
            }
        }
    }
}