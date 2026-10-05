package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.ChangeDriverPinComposeViewModel
import ifac.td.taxi.ui.screen.ChangeDriverPinScreen
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
class ChangeDriverPinNavigationFragment : Fragment() {

    private val viewModel: ChangeDriverPinComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
val uiState = viewModel.uiState.value
        return ComposeView(requireContext()).apply {
            setContent {
                ChangeDriverPinScreen(
onShowToast = {},
onNavigateBack = {},
modifier = Modifier,
viewModel = viewModel,

                )
            }
        }
    }
}
