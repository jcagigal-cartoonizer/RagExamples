package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PredefinedMessageScreen
import ifac.td.taxi.compose.viewModel.PredefinedMessageComposeViewModel
import ifac.td.taxi.viewModel.PredefinedMessageViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.os.Bundle
class PredefinedMessageNavigationFragment : BaseFragment<Any, PredefinedMessageViewModel>() {
    private val args: PredefinedMessageNavigationFragmentArgs by navArgs()
    private val viewModel: PredefinedMessageViewModel by viewModels()
    override fun getViewModel() = viewModel
    // Not used because we render Compose UI
    override fun getViewBinding() = throw UnsupportedOperationException("ComposeView only")
    override fun setupComponents() {
        iMainActivity.showHeader(false)
        viewModel.initVM(args.messageId)
    }
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiEffect.collect { effect ->
                        when (effect) {
                            PredefinedMessageUiEffect.NavigateBack -> {
                                findNavController().popBackStack()
                            }
                            is PredefinedMessageUiEffect.OpenEditableDialog -> {
                                // handled inside Compose screen
                            }
                            is PredefinedMessageUiEffect.OpenPredefinedDialog -> {
                                // handled inside Compose screen
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                PredefinedMessageScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    messageId = args.messageId,
                    onHeaderVisibleChange = { visible ->
                        iMainActivity.showHeader(visible)
                    }
                )
            }
        }
    }
}
Your `PredefinedMessageScreen(...)` already contains:
So the fragment only needs to host the Compose content.
However, since your current screen already receives a `NavController` and `viewModel`, the fragment can simply call it from `setContent`.
A simpler version is:
// # Block 86-2: import android.os.Bundle
class PredefinedMessageNavigationFragment : BaseFragment<Any, PredefinedMessageViewModel>() {
    private val args: PredefinedMessageNavigationFragmentArgs by navArgs()
    private val viewModel: PredefinedMessageViewModel by viewModels()
    override fun getViewModel() = viewModel
    override fun getViewBinding() = throw UnsupportedOperationException("Compose-only fragment")
    override fun setupComponents() {
        iMainActivity.showHeader(false)
        viewModel.initVM(args.messageId)
    }
    override fun setupObservers() = Unit
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                PredefinedMessageScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    messageId = args.messageId,
                    onHeaderVisibleChange = { visible ->
                        iMainActivity.showHeader(visible)
                    }
                )
            }
        }
    }
}
