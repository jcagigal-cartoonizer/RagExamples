package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.DispatchReceivedComposeViewModel
import ifac.td.taxi.ui.screen.DispatchReceivedScreen
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
class DispatchReceivedNavigationFragment : Fragment() {

    private val viewModel: DispatchReceivedComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
val uiState = viewModel.uiState.value
val buttonsState = viewModel.uiState.buttonsState
val dialogState = viewModel.uiState.dialogState
        return ComposeView(requireContext()).apply {
            setContent {
                DispatchReceivedScreen(

                )
            }
        }
    }
}
