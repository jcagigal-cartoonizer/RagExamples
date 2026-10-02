package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.LegalTextScreen
import ifac.td.taxi.compose.viewModel.LegalTextComposeViewModel
import ifac.td.taxi.viewModel.LegalTextViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 3-1: import android.os.Bundle
class LegalTextNavigationFragment : Fragment() {
    private val viewModel: LegalTextViewModel by viewModels()
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
                MaterialTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    LegalTextScreen(
                        uiState = uiState,
                        onAcceptClick = {
                            viewModel.clickOnAccept()
                        },
                        onDismissDialog = {
                            viewModel.onDismissDialog()
                        },
                        onConfirmDialog = {
                            viewModel.onConfirmDialog()
                        }
                    )
                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setLegalText()
    }
}
