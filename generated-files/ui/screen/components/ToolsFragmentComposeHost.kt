package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 413-5: import android.os.Bundle
class ToolsFragmentComposeHost : Fragment() {
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
                // obtain your ViewModel from Koin/Hilt/manual DI here
                // example:
                // val viewModel: ToolsComposeViewModel = koinViewModel()
                // ToolsRoute(viewModel = viewModel, navController = findNavController())
            }
        }
    }
}
Your original fragment did:
btnRequirements.setAction {
    vModel.clickRequirements()
}
btnMeetingSign.setAction {
    iMainActivity.navigateTo(
        ToolsFragmentDirections.actionToolsFragmentToMeetingSignFragment(
            vModel.meetingSignColors.value.first,
            vModel.meetingSignColors.value.second
        )
    )
}
The Compose version preserves this by:
Then the composable handles navigation with `NavController`, preserving Jetpack Navigation / Safe Args behavior.
1. a **more exact XML-to-Compose visual match** for the buttons and dialog,  
2. a version using **Koin Compose** injection, or  
3. a full **`ToolsFragment` to `ToolsScreen` migration example** with navigation graph updates.
