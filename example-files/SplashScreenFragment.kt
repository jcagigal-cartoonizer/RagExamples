package ifac.td.taxi.ui.screen

import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import ifac.td.taxi.framework.util.Logs
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentSplashScreenBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.SplashScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenFragment :
    BaseFragment<FragmentSplashScreenBinding, SplashScreenViewModel>(R.layout.fragment_splash_screen) {

    private val vModel: SplashScreenViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val TAG = "SplashScreenFragment"
    private val SPLASH_TIMEOUT = 4800L

    private var isSkipped = false
    private var splashHandler: Handler? = null

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentSplashScreenBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.apply {
            showHeader(false)
            showBottomBar(false)
        }

        vModel.checkIsReconnecting(sharedViewModel.reconnectionFlow.value)
    }

    override fun onResume() {
        super.onResume()
        setUpVideo()
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            //Don't let the user to navigate back
        }
    }

    private fun setUpVideo() {
        val videoView = vBinding.vvSplashScreen
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.new_splash}")

        videoView.apply {
            setZOrderOnTop(true)
            setBackgroundColor(resources.getColor(R.color.splash_background, null))

            setOnPreparedListener { mediaPlayer ->
                Logs.d(TAG, "setUpVideo -> Video prepared, starting")
                setBackgroundColor(Color.TRANSPARENT)
                mediaPlayer.isLooping = false
                mediaPlayer.start()
            }

            setOnCompletionListener {
                Logs.d(TAG, "setUpVideo -> Video ended")
                if (!isSkipped) {
                    endSplash()
                }
            }

            setOnErrorListener { _, what, extra ->
                Logs.e(TAG, "Error during video playback: what=$what, extra=$extra")
                if (!isSkipped) {
                    endSplash()
                }
                true
            }
        }

        try {
            videoView.setVideoURI(videoUri)
            splashHandler = Handler(Looper.getMainLooper())
            splashHandler?.postDelayed({
                if (!isSkipped) {
                    endSplash()
                }
            }, SPLASH_TIMEOUT)
        } catch (e: Exception) {
            Logs.e(TAG, "ERROR setUpVideo: ${e.localizedMessage}")
            if (!isSkipped) {
                endSplash()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        splashHandler?.removeCallbacksAndMessages(null)
    }

    private fun endSplash() {
        vBinding.vvSplashScreen.visibility = View.GONE
        sharedViewModel.showLegalTextWindow(false)
    }

    private fun endSplashWithoutCheck() {
        splashHandler?.removeCallbacksAndMessages(null)
        isSkipped = true

        vBinding.vvSplashScreen.visibility = View.GONE
        sharedViewModel.showLegalTextWindow(true)
        iMainActivity.navigateTo(R.id.action_splashScreenFragment_to_welcomeFragment)
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.showLegalTextFlow.collect {
                        it?.let { showLegal ->
                            if (showLegal) {
                                iMainActivity.navigateTo(R.id.action_splashScreenFragment_to_legalTextFragment)
                            } else {
                                iMainActivity.navigateTo(R.id.action_splashScreenFragment_to_welcomeFragment)
                            }
                        }
                    }
                }

                launch {
                    vModel.serviceActiveFlow.collect { shouldSkipSplash ->
                        Logs.d(TAG, "checkIsReconnecting shouldSkipSplash: $shouldSkipSplash")
                        if (shouldSkipSplash && iMainActivity.getBravoService() == null) {
                            endSplashWithoutCheck()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        splashHandler?.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}
