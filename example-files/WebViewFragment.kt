package ifac.td.taxi.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentWebViewBinding
import ifac.td.taxi.repository.connections.receivers.utils.setThemeAsQueryParam
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.webView.GuideWebViewClient
import ifac.td.taxi.ui.custom.webView.GuideWebViewClient.GuideWebViewCallback
import ifac.td.taxi.viewmodel.WebViewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class WebViewFragment :
    BaseFragment<FragmentWebViewBinding, WebViewViewModel>(R.layout.fragment_web_view),
    GuideWebViewCallback {

    private val TAG = "WebViewFragment"

    private val vModel: WebViewViewModel by viewModel()

    private val safeArgs: WebViewFragmentArgs by navArgs()

    private val webViewClient by lazy {
        GuideWebViewClient()
    }

    private val homeUrl by lazy {
        safeArgs.webViewURL?.setThemeAsQueryParam(iMainActivity.isNightMode())
    }


    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentWebViewBinding.inflate(layoutInflater)

    override fun setupComponents() {
        Logs.d(TAG, "setupComponents: Initializing WebView and buttons")
        iMainActivity.showHeader(false)

        homeUrl?.let {
            Logs.d(TAG, "setupComponents: safeArgs.webViewURL is not null, loading homepage")
            loadHomepage(it)
        } ?: run {
            Logs.e(TAG, "setupComponents: safeArgs.webViewURL is null, displaying error layout")
            showErrorLayout()
        }



        initGuideWebView()
        setUpButtons()
        checkButtons()

        iMainActivity.showBottomBar(false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logs.d(TAG, "onAttach: Fragment attached")
        webViewClient.setOnPageFinishedCallback(this)
    }

    override fun onDestroyView() {
        Logs.d(TAG, "onDestroyView: Removing callback before view destruction")
        webViewClient.removeOnPageFinishedCallback()
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        Logs.d(TAG, "onDetach: Fragment detached")
    }

    override fun onPageFinished(view: WebView?, url: String?, hasReceivedErrorOnLastPageLoad: Boolean) {
        Logs.d(TAG, "onPageFinished: URL loaded - $url, Error received: $hasReceivedErrorOnLastPageLoad")

        checkButtons()

        showErrorLayout(hasReceivedErrorOnLastPageLoad)

        vBinding.lytError.isVisible = hasReceivedErrorOnLastPageLoad
        vBinding.webView.isVisible = !hasReceivedErrorOnLastPageLoad

        url?.let {
            Logs.d(TAG, "onPageFinished: Saving last visited URL - $it")
            iMainActivity.saveSharedPreferencesValue("GuideLastVisitedUrl", it)
        }
    }


    override fun onReceivedError(error: WebResourceError?) {
        Logs.e(TAG, "onReceivedError: Error code - ${error?.errorCode}, Description - ${error?.description}")

        if (error?.errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            showErrorLayout()
        }
    }

    private fun showErrorLayout(show: Boolean = true) {
        vBinding.lytError.isVisible = show
        vBinding.webView.isVisible = !show
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initGuideWebView() {
        Logs.d(TAG, "initGuideWebView: Setting up WebView configurations")

        vBinding.webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportZoom(false)
        }

        vBinding.webView.webViewClient = webViewClient
    }

    private fun loadHomepage(localHomeUrl: String? = homeUrl) {
        Logs.d(TAG, "loadHomepage: Loading home URL - $localHomeUrl")
        if (localHomeUrl != null) {
            webViewClient.clearHistory()
            vBinding.webView.loadUrl(localHomeUrl)
        } else {
            showErrorLayout()
        }
    }

    private fun setUpButtons() {
        Logs.d(TAG, "setUpButtons: Setting up button click listeners")

        vBinding.btnHome.setOnClickListener {
            Logs.d(TAG, "setUpButtons: Home button clicked")
            loadHomepage()
            checkButtons()
        }

        vBinding.btnGoBack.setOnClickListener {
            Logs.d(TAG, "setUpButtons: Go Back button clicked")

            if (vBinding.webView.canGoBack()) {
                vBinding.webView.goBack()
                checkButtons()
            }

        }

        vBinding.btnCancel.setOnClickListener {
            Logs.d(TAG, "setUpButtons: Cancel button clicked, navigating back")
            iMainActivity.navigateBack()
        }

        vBinding.btnReload.setOnClickListener {
            Logs.d(TAG, "setUpButtons: Reload button clicked, reloading page")
            vBinding.webView.reload()
        }
    }


    private fun checkButtons() {
        vBinding.apply {
            val canGoBack = webView.canGoBack()
            val isHomePage = webView.url == homeUrl

            Logs.d(TAG, "checkButtons: webView.canGoBack() = $canGoBack, isHomePage = $isHomePage")

            btnGoBack.setButtonStyle(
                if (canGoBack && !isHomePage) CustomButton.StyleButton.ENABLE
                else CustomButton.StyleButton.DISABLE
            )
        }
    }

}
