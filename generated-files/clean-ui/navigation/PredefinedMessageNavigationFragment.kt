package ifac.td.taxi.compose.navigation
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
