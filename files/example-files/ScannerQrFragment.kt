package ifac.td.taxi.ui.screen

import ifac.td.taxi.framework.util.Logs
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import ifac.td.taxi.NavGraphDirections
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentScannerQrBinding
import ifac.td.taxi.domain.model.VoucherQR
import ifac.td.taxi.domain.utils.StaticConfiguration
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.MainActivity.OnScannerResultCallback
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.ScannerQrViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScannerQrFragment :
    BaseFragment<FragmentScannerQrBinding, ScannerQrViewModel>(R.layout.fragment_scanner_qr) {

    val vModel: ScannerQrViewModel by viewModel()
    val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val TAG = "ScannerQrFragment"

    private val args: ScannerQrFragmentArgs by navArgs()

    private var serviceId: String? = null


    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentScannerQrBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        serviceId = args.serviceId
        Logs.d(TAG, "setupComponents: serviceId = $serviceId")
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                StaticConfiguration.subscriberFailPin = true
                iMainActivity.navigateBack()
            }

            btnScanner.setAction {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    when (response.buttonPressed) {
                        ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA -> {
                            val cameraPosition =
                                if (response.buttonPressed == ButtonType.FRONT_CAMERA) 1 else 0

                            Logs.d(TAG, "setButtons: Opening scanner with cameraPosition = $cameraPosition")

                            iMainActivity.openScanner(object : OnScannerResultCallback {
                                override fun onSuccess(result: String) {
                                    Logs.d(TAG, "setButtons: QR scanned successfully: $result")
                                    vBinding.btnScanner.setButtonStyle(CustomButton.StyleButton.LOADING)
                                    handleResult(result)
                                }
                            }, cameraPosition)
                        }

                        else -> Logs.d(TAG, "setButtons: No valid button pressed")
                    }
                }

                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = resources.getString(R.string.dialog_open_camera),
                        description = resources.getString(R.string.dialog_select_camera),
                        buttons = arrayListOf(ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA)
                    ), response = callback
                )
            }
        }
    }


    fun handleResult(result: String) {
        try {
            val gson = Gson()
            val qrVoucher = gson.fromJson(result, VoucherQR::class.java)
            Logs.d(TAG, "handleResult: QR parsed successfully: $qrVoucher")

            vModel.uploadVoucherId(
                args.serviceId,
                qrVoucher,
                sharedViewModel.dispatchFlow.value,
                sharedViewModel.tripFlow.value,
                sharedViewModel.updatePrintFlowCallback,
                navigateFunction = { isSuccess ->
                    if (isSuccess) {
                        Logs.d(TAG, "handleResult: Navigation successful, redirecting to Home")
                        iMainActivity.navigateTo(NavGraphDirections.goToHomeFragment())
                    } else {
                        Logs.d(TAG, "handleResult: Upload failed, enabling scanner button")
                        CoroutineScope(Dispatchers.Main).launch {
                            vBinding.btnScanner.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        }
                    }
                },
            )
        } catch (e: Exception) {
            Logs.e(TAG, "handleResult: Error reading QR: ${e.message}")
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.openDialogQrFlow.eventCollector {
                        if (it != null) {
                            Logs.d(TAG, "openDialogQrFlow: Opening QR dialog with model: ${it.first}")
                            iMainActivity.openDialog(it.first, it.second, fragmentManager = childFragmentManager)
                        }
                    }
                }
            }
        }
    }
}