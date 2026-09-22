package ifac.td.taxi.ui.screen

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentLightsTestBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.LightsTestViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class LightsTestFragment :
    BaseFragment<FragmentLightsTestBinding, LightsTestViewModel>(R.layout.fragment_lights_test) {

    private val vModel: LightsTestViewModel by viewModel()

    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentLightsTestBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        setUpOnClicks()
        setUpBeeper()
    }

//    override fun updateTopBarIcon() {
//        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.BACK, true) {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    vModel.uvLightFlow.collect { lights ->
                        lights.let {
                            if (!it) {
                                vBinding.ibUVLight.setImageDrawable(
                                    ResourcesCompat.getDrawable(resources, R.drawable.luz_ultravioleta, null)
                                )
                                vBinding.ibUVLight.isEnabled = true
                                vBinding.ibCourtesyLight.isEnabled = true
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.courtesyLightFlow.collect { isOn ->
                        Logs.d("LightsTestFragment", "Courtesy light state changed: $isOn")
                        if (isOn == true) {
                            vBinding.ibCourtesyLight.setImageDrawable(
                                ResourcesCompat.getDrawable(resources, R.drawable.lumact_yellow, null)
                            )
                            vBinding.ibUVLight.isEnabled = false
                        } else {
                            vBinding.ibCourtesyLight.setImageDrawable(
                                ResourcesCompat.getDrawable(resources, R.drawable.lumact, null)
                            )
                            vBinding.ibUVLight.isEnabled = true
                        }
                    }
                }

                launch {
                    vModel.beeperLightFlow.collect { beeperValue ->
                        vBinding.sbBIPVolume.progress = beeperValue ?: 50
                    }
                }
            }
        }



    }

    private fun setUpOnClicks() {
        vBinding.ibUVLight.setOnClickListener {
            Logs.d("LightsTestFragment", "ibUVLight onClick")
            vModel.turnUVLight()
            vBinding.ibUVLight.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.luz_ultravioleta_blue, null)
            )
            vBinding.ibCourtesyLight.isEnabled = false
            vBinding.ibUVLight.isEnabled = false

        }

        vBinding.ibCourtesyLight.setOnClickListener {
            vModel.turnCourtesyLight()
            val isCourtesyLightOn = sharedViewModel.courtesyLightFlow.value
            Logs.d("LightsTestFragment", "ibCourtesyLight onClick. isCourtesyLightOn: $isCourtesyLightOn")

            if (isCourtesyLightOn == true) {
                vBinding.ibCourtesyLight.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.lumact_yellow, null)
                )
                vBinding.ibUVLight.isEnabled = false
            } else {
                vBinding.ibCourtesyLight.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.lumact, null)
                )
                vBinding.ibUVLight.isEnabled = true
            }

        }
    }

    private fun setUpBeeper() {
        val beeper = vBinding.sbBIPVolume

        vModel.getBeeperVolume()

        beeper.max = 100
        beeper.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    vModel.setBeeperVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


}