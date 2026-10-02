package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ChangePasswordRedSysScreen
import ifac.td.taxi.compose.viewModel.ChangePasswordRedSysComposeViewModel
import ifac.td.taxi.viewModel.ChangePasswordRedSysViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 3-1: import android.os.Bundle
class ChangePasswordRedSysNavigationFragment : Fragment() {
    private val viewModel: ChangePasswordRedSysComposeViewModel by viewModels()
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
                val dialogState by viewModel.dialogState.collectAsState()
                ChangePasswordRedSysScreen(
                    uiState = uiState,
                    dialogState = dialogState,
                    onDismissDialog = viewModel::onDismissDialog,
                    onDialogAccepted = {
                        viewModel.onDialogAccepted()
                        navigateBack()
                    },
                    onCancel = {
                        viewModel.onCancelClicked()
                        navigateBack()
                    },
                    onAccept = viewModel::onAcceptClicked,
                    onUserChanged = viewModel::onUserChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onNewPasswordChanged = viewModel::onNewPasswordChanged,
                    onRepeatNewPasswordChanged = viewModel::onRepeatNewPasswordChanged
                )
            }
        }
    }
    fun navigateBack() {
        (activity as? MainActivity)?.navigateBack()
    }
}
