package ifac.td.taxi.ui.screen

import android.content.res.Configuration
import ifac.td.taxi.framework.util.Logs
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentTccBinding
import ifac.td.taxi.domain.model.Dispatch
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.TccViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TccFragment : BaseFragment<FragmentTccBinding, TccViewModel>(R.layout.fragment_tcc) {

    private val TAG = "TccFragment"

    private val vModel: TccViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val safeArgs: TccFragmentArgs? by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentTccBinding.inflate(layoutInflater)

    override fun setupComponents() {
        getDispatchModel()
    }

    private fun getDispatchModel() {
        showLoading(true)
        vModel.getDispatch(safeArgs?.tripId)
    }

    override fun updateBackButton() {
        StaticConfiguration.subscriberFailPin = true
        super.updateBackButton()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.dispatchModelFlow.collect { dispatch ->
                        try {
                            if (dispatch == null) {
                                iMainActivity.showToast(R.string.dialog_error_title)
                            } else {
                                hideUselessViews(dispatch)
                                showLoading(false)

                                vBinding.btnAccept.setAction {
                                    vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)

                                    // Save spinner values before proceeding
                                    val att1 =
                                        if (!dispatch.attribute1Title.isNullOrEmpty()) vBinding.spnAtts1.selectedItemPosition.toString() else null
                                    val att2 =
                                        if (!dispatch.attribute2Title.isNullOrEmpty()) vBinding.spnAtts2.selectedItemPosition.toString() else null
                                    val att3 =
                                        if (!dispatch.attribute3Title.isNullOrEmpty()) vBinding.spnAtts3.selectedItemPosition.toString() else null
                                    val att4 =
                                        if (!dispatch.attribute4Title.isNullOrEmpty()) vBinding.spnAtts4.selectedItemPosition.toString() else null

                                    dispatch.attribute1Value = att1
                                    dispatch.attribute2Value = att2
                                    dispatch.attribute3Value = att3
                                    dispatch.attribute4Value = att4

                                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                        dispatch.id?.toLong()?.let { id ->
                                            vModel.updateTCCDispatch(id, att1, att2, att3, att4)
                                        }

                                        val requiresSignature = dispatch.requisiteSignature == "1"
                                        val dispatchId = dispatch.id?.toLong()

                                        if (requiresSignature) {
                                            Logs.d(
                                                TAG,
                                                "finishTccService: TCC opening Signature View"
                                            )
                                            dispatchId?.let {
                                                vModel.dispatchRequireSignature(dispatch.toInfoDispatchModel())
                                            }
                                        } else {
                                            Logs.d(
                                                TAG,
                                                "finishTccService: TCC Signature not required"
                                            )
                                            vModel.finishTccService(
                                                safeArgs?.tripId,
                                                dispatchId?.let { dispatch.toInfoDispatchModel() },
                                                sharedViewModel.updatePrintFlowCallback
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Logs.e(TAG, "Error collecting dispatch data")
                            iMainActivity.showToast(R.string.dialog_error_title)
                            showLoading(false)
                            vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        }
                    }
                }

                launch {
                    vModel.checkSubscriberCreditLimitFlow.collect { creditLimitResult ->
                        val validCredit = creditLimitResult.toBoolean1or0()
                        Logs.d(TAG, "Collected credit limit check result: validCredit = $validCredit")

                        if (validCredit) {
                            Logs.d(TAG, "Valid credit for subscriber")
                            iMainActivity.showToast(R.string.toast_valid_credit)

                            sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                Logs.d(TAG, "Handling dispatch based on requirements: requireSignature = ${dispatch.requireSignature}, requireVoucher = ${dispatch.requireVoucher}, requireQr = ${dispatch.requireQr}")
                                if (dispatch.requireSignature) {
                                    vModel.dispatchRequireSignature(dispatch)
                                } else if (dispatch.requireVoucher) {
                                    vModel.dispatchRequireVoucher(dispatch)
                                } else if (dispatch.requireQr) {
                                    vModel.dispatchRequireQR(dispatch)
                                } else {
                                    vModel.validCredit(
                                        sharedViewModel.tripFlow.value,
                                        dispatch,
                                        sharedViewModel.updatePrintFlowCallback
                                    )
                                }
                            }
                        } else {
                            iMainActivity.showToast(R.string.toast_not_enough_credit)
                            Logs.d(TAG, "Insufficient credit for subscriber")
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        vBinding.pbTcc.visibility = if (show) View.VISIBLE else View.GONE
        vBinding.container.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }


    private fun hideUselessViews(dispatch: Dispatch) {
        val orientation = resources.configuration.orientation
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

        val zeroToNineAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.zero_to_nine,
            R.layout.custom_spinner_item
        ).apply {
            setDropDownViewResource(R.layout.custom_spinner_item)
        }

        // Attribute 1
        if (dispatch.attribute1Title.isNullOrEmpty()) {
            Logs.d(TAG, "hideUselessViews: Hiding Attribute 1 - No title provided")
            vBinding.clAttributes1.visibility = if (isLandscape) View.GONE else View.INVISIBLE
        } else {
            Logs.d(TAG, "hideUselessViews: Showing Attribute 1 with title: ${dispatch.attribute1Title}")
            vBinding.tvAtts1.text = dispatch.attribute1Title
            vBinding.spnAtts1.adapter = zeroToNineAdapter
            vBinding.spnAtts1.setSelection(1, true)
        }

        // Attribute 2
        if (dispatch.attribute2Title.isNullOrEmpty()) {
            Logs.d(TAG, "hideUselessViews: Hiding Attribute 2 - No title provided")
            vBinding.clAttributes2.visibility = if (isLandscape) View.GONE else View.INVISIBLE
        } else {
            Logs.d(TAG, "hideUselessViews: Showing Attribute 2 with title: ${dispatch.attribute2Title}")
            vBinding.tvAtts2.text = dispatch.attribute2Title
            vBinding.spnAtts2.adapter = zeroToNineAdapter
        }

        // Attribute 3
        if (dispatch.attribute3Title.isNullOrEmpty()) {
            Logs.d(TAG, "hideUselessViews: Hiding Attribute 3 - No title provided")
            vBinding.clAttributes3.visibility = if (isLandscape) View.GONE else View.INVISIBLE
        } else {
            Logs.d(TAG, "hideUselessViews: Showing Attribute 3 with title: ${dispatch.attribute3Title}")
            vBinding.tvAtts3.text = dispatch.attribute3Title
            vBinding.spnAtts3.adapter = zeroToNineAdapter
        }

        // Attribute 4
        if (dispatch.attribute4Title.isNullOrEmpty()) {
            Logs.d(TAG, "hideUselessViews: Hiding Attribute 4 - No title provided")
            vBinding.clAttributes4.visibility = if (isLandscape) View.GONE else View.INVISIBLE
        } else {
            Logs.d(TAG, "hideUselessViews: Showing Attribute 4 with title: ${dispatch.attribute4Title}")
            vBinding.tvAtts4.text = dispatch.attribute4Title
            vBinding.spnAtts4.adapter = zeroToNineAdapter
        }
    }

}