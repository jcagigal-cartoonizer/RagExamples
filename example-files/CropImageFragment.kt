package ifac.td.taxi.ui.screen

import ifac.td.taxi.framework.util.Logs
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentCropImageViewBinding
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.CropImageViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class CropImageViewFragment :
    BaseFragment<FragmentCropImageViewBinding, CropImageViewModel>(R.layout.fragment_crop_image_view) {

    private val TAG = "CropImageViewFragment"

    private val vModel: CropImageViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val args: CropImageViewFragmentArgs by navArgs()
    private val serviceId by lazy { args.serviceId }

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentCropImageViewBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()

        vModel.uriImageFlow.value?.let {
            vBinding.ivPreview.setImageURI(it)
        } ?: run {
            Logs.d(TAG, "setupComponents: No image URI found")
        }

        iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_CAMERA)
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (vModel.uriImageFlow.value == null) {
                    Logs.d(TAG, "setButtons: No image selected")
                    iMainActivity.showToast(R.string.dialog_voucher_required)
                } else {
                    args.serviceId?.let {
                        Logs.d(TAG, "setButtons: serviceId set to $serviceId")
                    }

                    vModel.sendImageToAlpha(serviceId)
                }
            }
            btnCrop.setAction {
                iMainActivity.openCropImage(vModel.uriImageFlow.value)
            }
            btnSelectImage.setAction {
                iMainActivity.openImageSelect()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.uploadImageCallbackFlow.collect { value ->
                        Logs.d(TAG, "uploadImageCallbackFlow: Received value: $value")

                        if (value) {
                            if (sharedViewModel.isVacant() || (vModel.isHired(sharedViewModel.shiftStatusFlow.value?.currentStatus ?: 0))) {
                                sharedViewModel.navigateBack()
                            }
                            Logs.d(TAG, "uploadImageCallbackFlow: Upload successful")
                            if (serviceId == sharedViewModel.dispatchFlow.value?.longDispatchNumber) {
                                Logs.d(TAG, "uploadImageCallbackFlow: Dispatching subscriber for serviceId: $serviceId")
                                sharedViewModel.dispatchFlow.value?.let { dispatch ->
                                    sharedViewModel.tripFlow.value?.let {
                                        vModel.dispatchSubscriberNextStep(dispatch, it, sharedViewModel.updatePrintFlowCallback)
                                    }
                                }
                            } else if (vModel.isPayment(sharedViewModel.shiftStatusFlow.value?.currentStatus ?: 0)) {
                                Logs.d(TAG, "uploadImageCallbackFlow: Sending subscriber auth for serviceId: $serviceId")
                                vModel.sendSubscriberAuth(serviceId, sharedViewModel.tripFlow.value?.id)
                            }
                        } else {
                            Logs.d(TAG, "uploadImageCallbackFlow: Upload failed, setting subscriberFailPin to true")
                            StaticConfiguration.subscriberFailPin = true
                        }

                        val descriptionResId = if (value) R.string.dialog_send_image_description else R.string.dialog_error_send_image_description
                        Logs.d(TAG, "uploadImageCallbackFlow: Showing toast with message: $descriptionResId")
                        iMainActivity.showToast(descriptionResId)
                    }
                }

                launch {
                    sharedViewModel.cropImageFlow.collect {
                        it?.let { uriContent ->
                            Logs.d(TAG, "cropImageFlow: Received new image URI: $uriContent")
                            vModel.saveFileFromUri(uriContent)
                            vBinding.ivPreview.setImageURI(uriContent)
                        }
                    }
                }
            }
        }
    }

}
