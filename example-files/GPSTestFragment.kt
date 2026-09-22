package ifac.td.taxi.ui.screen

import android.graphics.Color
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.log.Log as SdkLog
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentGpsTestBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.GPSTestViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class GPSTestFragment :
    BaseFragment<FragmentGpsTestBinding, GPSTestViewModel>(R.layout.fragment_gps_test) {

    private val TAG = "GPSTestFragment"

    private val vModel: GPSTestViewModel by viewModel()

    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentGpsTestBinding.inflate(layoutInflater)
    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
        setFontAwesome()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                iMainActivity.navigateBack()
            }

            btnGps.setAction {
                vModel.openNavigatorApp()
            }
        }
    }

    private fun setFontAwesome() {
        val font = ResourcesCompat.getFont(requireContext(), R.font.font_awesome)
        vBinding.apply {
            textKey.typeface = font
            textKey.setTextColor(Color.GRAY)

            textAlarm.typeface = font
            textAlarm.setTextColor(Color.GRAY)
        }

    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.alarmFlow.collect {
                        it?.let { alarm ->
                            val alarmColor = if (alarm) {
                                Color.GREEN
                            } else {
                                Color.RED
                            }
                            vBinding.textAlarm.setTextColor(alarmColor)
                        }
                    }
                }

                launch {
                    vModel.navigatorFlow.collect {
                        it?.let { intent ->
                            iMainActivity.launchIntent(intent)
                        }
                    }
                }

                launch {
                    vModel.gpsDataFlow.collect { gpsData ->
                        gpsData?.let {

                            //Titulo GPS
                            val gpsType = context?.getString(R.string.strGPSAndroid)

                            vBinding.apply {
                                vModel.externalGPSFlow.value?.let { useExternalGPS ->
                                    if (useExternalGPS) {
                                        clSpeed.visibility = View.VISIBLE
                                        clAngle.visibility = View.VISIBLE
                                        tvGpsType.text = vModel.bluetoothInfo?.name ?: gpsType
                                        if (W2CLocation.isReplacingGPS()) {
                                            tvGpsType.text = gpsType
                                        }
                                        //android.util.Log.d("MARC", tvGpsType.text.toString());
                                    } else {
                                        clSpeed.visibility = View.GONE
                                        clAngle.visibility = View.GONE
                                        tvGpsType.text = gpsType
                                    }
                                }

                                tvValueHour.text = it.utcTime
                                tvValueSatelites.text = it.satellites.toString()

                                if (W2CLocation.get_tipoGps().equals("S")) {
                                    val value = "dB [max " + W2CLocation.getMaxMediaSNR() + "]"
                                    tvValuePower.text =
                                        W2CLocation.getMediaSNR().toString().plus(value)

                                } else {
                                    clPower.visibility = View.GONE
                                }
                                tvValueHdop.text = String.format(Locale.US, "%d", it.hdop.toInt())
                                tvValueLat.text = it.latitude
                                tvValueLon.text = it.longitude

                                if (SdkLog.is_debug()) {
                                    tvValueSpeed.text = it.speed
                                    tvValueAngle.text = it.heading.plus("°")
                                } else {
                                    clSpeed.visibility = View.GONE
                                    clAngle.visibility = View.GONE
                                }
                            }
                        }
                    }
                }


                launch {
                    vModel.keyFlow.collect { key ->
                        val keyColor = if (key) {
                            Color.GREEN
                        } else {
                            Color.RED
                        }
                        vBinding.textKey.setTextColor(keyColor)
                    }
                }

                launch {
                    vModel.alarmFlow.collect { alarm ->
                        val alarmColor = if (alarm) {
                            Color.GREEN
                        } else {
                            Color.RED
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        vModel.checkExternalGPS()
        vModel.initViewModel()
        super.onResume()
    }

    override fun onPause() {
        vModel.removeHandlerCallback()
        super.onPause()
    }
}