package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.LoginUserScreen
import ifac.td.taxi.compose.viewModel.LoginUserComposeViewModel
import ifac.td.taxi.viewModel.LoginUserViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
