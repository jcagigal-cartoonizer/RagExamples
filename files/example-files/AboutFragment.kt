package ifac.td.taxi.ui.screen

import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.location.AlfaLocation
import ifac.td.taxi.framework.util.Logs
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentAboutBinding
import ifac.td.taxi.framework.util.ApkUtils
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.AboutViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutFragment : BaseFragment<FragmentAboutBinding, AboutViewModel>(
    R.layout.fragment_about
) {
    private val TAG = "AboutFragment"
    private val vModel: AboutViewModel by viewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentAboutBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)

        setTextViews()
        setButtons()
    }

    private var counter = 0

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                iMainActivity.navigateBack()
            }
            logotaxitronic.setOnClickListener {
                counter++
                Logs.d("AboutFragment", "logotaxitronic onClick")
                if (counter == 5) {
                    W2CLocation.setTrackingState(AlfaLocation.ESTADO_SEGUIMIENTO_ALARMA, "1")
                    counter = 0
                }
            }
        }
    }

    private fun setTextViews() {
        vBinding.apply {
            val privacyText = getString(R.string.privacy_policy)
            val spannableString = SpannableString(privacyText).apply {
                setSpan(UnderlineSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            tvPrivacyPolicy.apply {
                text = spannableString
                isClickable = true
                setOnClickListener {
                    Logs.d("AboutFragment", "tvPrivacyPolicy onClick")
                    vModel.clickPrivacy()
                }
            }

            val name = context?.let {
                ApkUtils.getAppNameVersionDate(
                    name = true,
                    version = false,
                    date = false,
                    context = it
                )
            }

            val version = context?.let {
                ApkUtils.getAppNameVersionDate(
                    name = false,
                    version = true,
                    date = false,
                    context = it
                )
            }

            val date = context?.let {
                ApkUtils.getAppNameVersionDate(
                    name = false,
                    version = false,
                    date = true,
                    context = it
                )
            }

            val appInfo = getString(R.string.app_info_format, name, version, date)
            tvNameVersion.text = appInfo

            vModel.checkSavedDevice()
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.deviceFlow.collect { (isDeviceAvailable, bluetoothInfo) ->
                        Logs.d(TAG, "deviceFlow: Received device update. isDeviceAvailable: $isDeviceAvailable, bluetoothInfo: $bluetoothInfo")

                        if (isDeviceAvailable && bluetoothInfo != null) {
                            Logs.d(TAG, "deviceFlow: Device is available and bluetoothInfo is not null.")
                            val taximeter = Taximeter.getInstance()
                            val deviceInfo = buildString {
                                appendLine("${bluetoothInfo.name} [${bluetoothInfo.btPIN}]")
                                Logs.d(TAG, "deviceFlow: Appended device name and PIN: ${bluetoothInfo.name} [${bluetoothInfo.btPIN}]")

                                val firmware = taximeter.taximeterVersionFirmware
                                val hardware = taximeter.taximeterVersionHardware
                                if (firmware.isNotEmpty() && hardware.isNotEmpty()) {
                                    appendLine("$firmware - $hardware")
                                    Logs.d(TAG, "deviceFlow: Appended firmware and hardware versions: $firmware - $hardware")
                                } else {
                                    Logs.d(TAG, "deviceFlow: Firmware or hardware version is empty.")
                                }

                                val answerK62 = taximeter.answerK62
                                if (answerK62.isNotEmpty()) {
                                    appendLine(answerK62)
                                    Logs.d(TAG, "deviceFlow: Appended answerK62: $answerK62")
                                } else {
                                    Logs.d(TAG, "deviceFlow: answerK62 is empty.")
                                }
                            }
                            vBinding.tvBluetoothVersion.text = deviceInfo
                            Logs.d(TAG, "deviceFlow: Set tvBluetoothVersion text to: $deviceInfo")
                            Logs.d(TAG, "deviceFlow: deviceInfo: $deviceInfo")
                        } else {
                            Logs.d(TAG, "deviceFlow: Device is not available or bluetoothInfo is null.")
                            vBinding.tvBluetoothVersion.visibility = android.view.View.GONE
                            Logs.d(TAG, "deviceFlow: Set tvBluetoothVersion visibility to GONE.")
                        }
                        Logs.d(TAG, "deviceFlow: Finished processing device update.")

                        vModel.warningFlow.collect {
                            iMainActivity.showToast(R.string.warning_not_browser)
                        }
                    }
                }
            }
        }
    }
}