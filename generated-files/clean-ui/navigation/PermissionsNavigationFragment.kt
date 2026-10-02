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
