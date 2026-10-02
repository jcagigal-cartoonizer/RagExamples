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
// # Block 4-1: import android.os.Bundle
class SecurePinNavigationFragment : BaseFragment<View, SecurePinComposeViewModel>(0) {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun getViewModel(): SecurePinComposeViewModel = viewModel
    override fun getViewBinding(): View {
        // Not used because we override onCreateView below
        throw UnsupportedOperationException("ViewBinding is not used in SecurePinNavigationFragment")
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
                SecurePinScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onShowBottomBar = { show ->
                        iMainActivity.showBottomBar(show)
                    },
                    onShowHeader = { show ->
                        iMainActivity.showHeader(show)
                    }
                )
            }
        }
    }
}
Your current `BaseFragment` seems to be built around XML view binding, so the cleanest option is:
Create a dedicated fragment **without** `BaseFragment`, like this:
// # Block 54-2: import android.os.Bundle
class SecurePinNavigationFragment : Fragment() {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SecurePinScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onShowBottomBar = { show ->
                        (requireActivity() as? YourMainActivityType)?.showBottomBar(show)
                    },
                    onShowHeader = { show ->
                        (requireActivity() as? YourMainActivityType)?.showHeader(show)
                    }
                )
            }
        }
    }
}
Your composable does:
SecurePinUiEffect.NavigateBack -> navController.popBackStack()
So the fragment only needs to provide the `NavController`, which the code above does.
Here is a simpler version with a standard `Fragment`:
// # Block 119-3: import android.os.Bundle
class SecurePinNavigationFragment : Fragment() {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            SecurePinScreen(
                navController = findNavController(),
                viewModel = viewModel,
                onShowBottomBar = { (requireActivity() as YourMainActivityType).showBottomBar(it) },
                onShowHeader = { (requireActivity() as YourMainActivityType).showHeader(it) }
            )
        }
    }
}
