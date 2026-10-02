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
// # Block 3-1: import android.os.Bundle
class LoginUserNavigationFragment : Fragment() {
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: LoginUserViewModel by viewModels()
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
                val buttonsState = viewModel.buttonsState.collectAsStateWithLifecycle().value
                val dialogState = viewModel.dialogState.collectAsStateWithLifecycle().value
                LoginUserScreen(
                    uiState = uiState,
                    buttonsState = buttonsState,
                    dialogState = dialogState,
                    onEvent = { event -> viewModel.onEvent(event) },
                    onDialogDismiss = { viewModel.onDialogDismiss() },
                    onDialogConfirmPin = { pin -> viewModel.onDialogConfirmPin(pin) },
                    autoDownloadConfigurationFromMigration = requireArguments()
                        .getBoolean("autoDownloadConfigurationFromMigration", false)
                )
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.showHeader(false)
        viewModel.checkSavedData()
        viewModel.checkHasSettingsPassword()
    }
}
Replace the old destination:
<fragment
    android:id="@+id/loginUserFragment"
    android:name="ifac.td.taxi.ui.screen.LoginUserFragment"
    android:label="LoginUserFragment"
    tools:layout="@layout/fragment_login_user">
with:
<fragment
    android:id="@+id/loginUserFragment"
    android:name="ifac.td.taxi.ui.screen.LoginUserNavigationFragment"
    android:label="LoginUserNavigationFragment"
    tools:layout="@layout/fragment_login_user">
    <argument
        android:name="autoDownloadConfigurationFromMigration"
        android:defaultValue="false"
        app:argType="boolean" />
</fragment>
Your current `LoginUserScreen(...)` composable already contains a `LaunchedEffect(autoDownloadConfigurationFromMigration)` that calls:
onEvent(LoginUserEvent.AutoDownloadRequested)
So the fragment only needs to pass the boolean argument through.
and methods like:
// # Block 92-2: import android.os.Bundle
class LoginUserNavigationFragment : Fragment() {
    private val viewModel: LoginUserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            val buttonsState = viewModel.buttonsState.collectAsStateWithLifecycle().value
            val dialogState = viewModel.dialogState.collectAsStateWithLifecycle().value
            LoginUserScreen(
                uiState = uiState,
                buttonsState = buttonsState,
                dialogState = dialogState,
                onEvent = viewModel::onEvent,
                onDialogDismiss = viewModel::onDialogDismiss,
                onDialogConfirmPin = viewModel::onDialogConfirmPin,
                autoDownloadConfigurationFromMigration = arguments?.getBoolean(
                    "autoDownloadConfigurationFromMigration",
                    false
                ) ?: false
            )
        }
    }
}
