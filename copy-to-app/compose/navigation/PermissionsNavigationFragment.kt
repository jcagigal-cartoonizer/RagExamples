package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PermissionsScreen
import ifac.td.taxi.compose.viewModel.PermissionsComposeViewModel
import ifac.td.taxi.viewModel.PermissionsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class PermissionsNavigationFragment : Fragment() {
    private val viewModel: PermissionsViewModel by viewModels()
    private val args: PermissionsNavigationFragmentArgs by navArgs()
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
                TaxiTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    PermissionsScreen(
                        uiState = uiState,
                        safePermissionType = args.permissionType,
                        onEvent = { event ->
                            viewModel.onEvent(event)
                        }
                    )
                }
            }
        }
    }
}
<argument
    android:name="permissionType"
    android:defaultValue="PERMISSION_DEFAULT"
    app:argType="ifac.td.taxi.framework.PermissionRequest$PermissionTypeList" />
the generated Safe Args class will be:
PermissionsNavigationFragmentArgs
<fragment
    android:id="@+id/permissionsFragment"
    android:name="ifac.td.taxi.ui.screen.PermissionsNavigationFragment"
    android:label="PermissionsNavigationFragment">
val uiState: StateFlow<PermissionsUiState>
fun onEvent(event: PermissionsUiEvent)
That logic should move into the `PermissionsViewModel` and be triggered via `PermissionsUiEvent`.
override fun onResume() {
    super.onResume()
    viewModel.onEvent(PermissionsUiEvent.OnScreenResumed)
}
Then handle that event in the ViewModel.
Example:
override fun onResume() {
    super.onResume()
    viewModel.onEvent(PermissionsUiEvent.OnScreenResumed)
}
Here is a version with `LaunchedEffect`-style state collection safety and without any app-specific theme assumption beyond Compose:
// # Block 95-2: import android.os.Bundle
class PermissionsNavigationFragment : Fragment() {
    private val viewModel: PermissionsViewModel by viewModels()
    private val args: PermissionsNavigationFragmentArgs by navArgs()
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
                val uiState by viewModel.uiState.collectAsState()
                PermissionsScreen(
                    uiState = uiState,
                    safePermissionType = args.permissionType,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.onEvent(PermissionsUiEvent.OnScreenResumed)
    }
}
