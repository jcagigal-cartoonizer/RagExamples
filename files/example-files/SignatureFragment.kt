package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.github.gcacace.signaturepad.views.SignaturePad
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentSignatureBinding
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.SignatureViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignatureFragment : BaseFragment<FragmentSignatureBinding, SignatureViewModel>(
    R.layout.fragment_signature
) {

    private val TAG = "SignatureFragment"

    val vModel: SignatureViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val args: SignatureFragmentArgs by navArgs()

    var serviceId: String? = null

    override fun getViewModel(): SignatureViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentSignatureBinding {
        return FragmentSignatureBinding.inflate(layoutInflater)
    }

    private fun clearSignature() {
        Logs.d(TAG, "clearSignature: Clearing signature pad")
        vBinding.signaturePad.clear()
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        setupSignatureView()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (vModel.hasSigned()) {
                    serviceId = args.serviceId
                    Logs.d(TAG, "setButtons: serviceId set to $serviceId")

                    btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)


                    val fromDispatch = (sharedViewModel.tripFlow.value?.fromDispatch == true && sharedViewModel.dispatchFlow.value?.requireSignature == true)
                    Logs.d(TAG, "setButtons: fromDispatch = $fromDispatch")

                    vModel.sendSignature(
                        vBinding.signaturePad.signatureBitmap,
                        serviceId,
                        fromDispatch = fromDispatch
                    )
                } else {
                    Logs.d(TAG, "setButtons: No signature detected, showing toast")
                    iMainActivity.showToast(R.string.toast_sign_first)
                }
            }

            btnClear.setAction {
                clearSignature()
                vModel.signedFlag(false)
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.signatureResponseFlow.collect { value ->
                        Logs.d(TAG, "signatureResponseFlow: Received value: $value")

                        if (value) {
                            //Dispatch subscriber
                            Logs.d(TAG, "signatureResponseFlow: Signature validation successful")
                            if (serviceId == sharedViewModel.dispatchFlow.value?.longDispatchNumber) {
                                Logs.d(TAG, "signatureResponseFlow: Dispatching subscriber for serviceId: $serviceId")
                                sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                    sharedViewModel.tripFlow.value?.let {
                                        vModel.dispatchSubscriberNextStep(dispatch, it)
                                    }
                                }
                            } else {
                                Logs.d(TAG, "signatureResponseFlow: Sending subscriber auth for serviceId: $serviceId")
                                vModel.sendSubscriberAuth(serviceId, sharedViewModel.tripFlow.value?.id)
                            }

                        } else {
                            Logs.d(TAG, "signatureResponseFlow: Signature validation failed, setting subscriberFailPin to true")
                            StaticConfiguration.subscriberFailPin = true
                        }

                        val descriptionResId = if (value) R.string.dialog_signature_valid_desc else R.string.dialog_signature_invalid_desc
                        Logs.d(TAG, "signatureResponseFlow: Showing toast with message: $descriptionResId")
                        iMainActivity.showToast(descriptionResId)
                    }
                }

            }
        }
    }

    private fun setupSignatureView() {
        vBinding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                Logs.d(TAG, "setupSignatureView: User started signing")
            }

            override fun onSigned() {
                Logs.d(TAG, "setupSignatureView: Signature detected")
                vModel.signedFlag(true)
            }

            override fun onClear() {
                Logs.d(TAG, "setupSignatureView: Signature pad cleared")
            }
        })
    }
}