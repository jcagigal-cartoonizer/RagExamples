package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.LoginUserComposeViewModel
import ifac.td.taxi.ui.screen.LoginUserScreen
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.compose.ui.Modifier
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
class LoginUserNavigationFragment : Fragment() {

    private val viewModel: LoginUserComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LoginUserScreen(
uiState = {},
onDialogConfirmPin = {},
buttonsState = {},
onEvent = {},
dialogState = {},
autoDownloadConfigurationFromMigration = {},
onDialogDismiss = {},

                )
            }
        }
    }
}
