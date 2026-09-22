package ifac.td.taxi.ui.screen

import ifac.td.taxi.framework.util.Logs
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentOpenPartialBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.OpenPartialViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OpenPartialFragment :
    BaseFragment<FragmentOpenPartialBinding, OpenPartialViewModel>(R.layout.fragment_open_partial) {

    private val TAG = "OpenPartialFragment"

    private val openPartialArgs: OpenPartialFragmentArgs by navArgs()
    private val vModel: OpenPartialViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private var rawPartialContent: String? = null

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentOpenPartialBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vModel.checkClosuresPermission()
        showLoading(true)
        vModel.getPartial()
    }

    private fun setButtons() {
        vBinding.apply {
            btnBack.setAction {
                Logs.d(TAG, "btnBack: User clicked back")
                iMainActivity.navigateBack()
            }

            btnPrint.setAction {
                Logs.d(TAG, "btnPrint: User clicked print. Content available: ${!rawPartialContent.isNullOrEmpty()}")
                rawPartialContent.takeIf { !it.isNullOrEmpty() }?.let {
                    sharedViewModel.printTicket(it.addTicketLines())
                } ?: run {
                    Logs.d(TAG, "btnPrint: No partials to print")
                    iMainActivity.showToast(R.string.no_partials_to_print)
                }
            }

            btnClose.setAction {
                Logs.d(TAG, "btnClose: User clicked close partial")
                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.alert),
                        description = requireContext().getString(R.string.alert_close_partial_dialog),
                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                    ),
                    response = { response ->
                        when (response.buttonPressed) {
                            ButtonType.ACCEPT -> {
                                Logs.d(TAG, "btnClose: Dialog ACCEPT pressed. Closing partials...")
                                vModel.closePartials()
                            }
                            else -> Logs.d(TAG, "btnClose: Dialog canceled")
                        }
                    },
                    fragmentManager = childFragmentManager
                )
            }

            btnTotalizers.setAction {
                Logs.d(TAG, "btnTotalizers: User clicked totalizers")
                iMainActivity.navigateTo(R.id.action_openPartialFragment_to_totalizersFragment)
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.partialFlow.collect { partial ->
                        Logs.d(TAG, "partialFlow collect: Received partial data: ${partial != null}")
                        if (partial != null) {
                            rawPartialContent = partial.bufLastTicketParciales
                            val partialContent = partial.bufLastTicketParciales.takeIf { !it.isNullOrEmpty() }
                                ?: getString(R.string.no_active_partials_with_data)

                            vBinding.ticketViewerReceipts.setTicketContent(partialContent)
                            showLoading(false)

                            vBinding.scrollViewOpenPartial.apply {
                                post {
                                    fullScroll(View.FOCUS_DOWN)
                                }
                            }
                        } else {
                            Logs.d(TAG, "partialFlow collect: Partial is null, showing loading")
                            vBinding.ticketViewerReceipts.setTicketContent(getString(R.string.no_active_partials))
                            showLoading(true)
                        }
                    }
                }

                launch {
                    vModel.partialButtonFlow.collect { canClosePermission ->
                        val canClose = canClosePermission == true && openPartialArgs.canClose
                        val isTaximeterConnected = vModel.isTaximeterConnectedFlow.value
                        val hasTotalizers = sharedViewModel.taximeterTotalizersFlow.value != null

                        vBinding.btnClose.apply {
                            visibility = if (canClose) View.VISIBLE else View.GONE
                            setButtonStyle(if (canClose) CustomButton.StyleButton.ENABLE else CustomButton.StyleButton.DISABLE)
                        }

                        vBinding.btnTotalizers.apply {
                            visibility = if (!canClose && sharedViewModel.shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISCONNECTED && isTaximeterConnected) View.VISIBLE else View.GONE

                            if (!canClose) {
                                val style = if (isTaximeterConnected && hasTotalizers) {
                                    CustomButton.StyleButton.ENABLE
                                } else {
                                    CustomButton.StyleButton.DISABLE
                                }
                                setButtonStyle(style)
                            }
                        }

                        Logs.d(TAG, "partialButtonFlow: canClose=$canClose, isTaximeterConnected=$isTaximeterConnected, hasTotalizers=$hasTotalizers")
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        vBinding.ticketContainer.isVisible = !isLoading
        vBinding.pbPartial.isVisible = isLoading
    }
}