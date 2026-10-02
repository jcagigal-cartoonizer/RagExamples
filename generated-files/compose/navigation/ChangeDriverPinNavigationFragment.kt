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
class ChangeDriverPinNavigationFragment : Fragment() {
    private val viewModel: ChangeDriverPinComposeViewModel by viewModels()
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
                ChangeDriverPinScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        findNavController().navigateUp()
                    },
                    onShowToast = { message ->
                        // If you already have an app-level toast system, hook it here.
                        // For now this is left empty intentionally.
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
Replace the destination:
<fragment
    android:id="@+id/changeDriverPinFragment"
    android:name="ifac.td.taxi.ui.screen.ChangeDriverPinNavigationFragment"
    android:label="fragment_change_driver_pin"
    tools:layout="@layout/fragment_change_driver_pin" />
So now the graph points to:
android:name="ifac.td.taxi.ui.screen.ChangeDriverPinNavigationFragment"
instead of:
android:name="ifac.td.taxi.ui.screen.ChangeDriverPinFragment"
Your current composable already handles:
So the Fragment above only needs to host it.
// # Block 73-2: import android.os.Bundle
class ChangeDriverPinNavigationFragment : Fragment() {
    private val viewModel: ChangeDriverPinComposeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // no-op
    }
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ChangeDriverPinScreen(
                viewModel = viewModel,
                onNavigateBack = { findNavController().navigateUp() },
                onShowToast = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
Your composable uses:
viewModel.uiEffect.collect { effect -> ... }
That is fine, but in a Compose screen it is usually safer to collect with lifecycle awareness, like:
LaunchedEffect(Unit) {
    viewModel.uiEffect.collectLatest { ... }
}
