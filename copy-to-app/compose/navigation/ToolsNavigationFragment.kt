package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ToolsScreen
import ifac.td.taxi.compose.viewModel.ToolsComposeViewModel
import ifac.td.taxi.viewModel.ToolsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ToolsNavigationFragment : Fragment() {
    private val viewModel: ToolsViewModel by viewModels()
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
                ToolsScreen(
                    uiState = viewModel.uiState,
                    onEvent = { event ->
                        when (event) {
                            ToolsUiEvent.RequirementsClicked -> {
                                viewModel.clickRequirements()
                                findNavController().navigate(
                                    R.id.action_toolsFragment_to_requirementsFragment
                                )
                            }
                            ToolsUiEvent.MeetingSignClicked -> {
                                val colors = viewModel.meetingSignColors.value
                                findNavController().navigate(
                                    ToolsNavigationFragmentDirections
                                        .actionToolsFragmentToMeetingSignFragment(
                                            colors.first,
                                            colors.second
                                        )
                                )
                            }
                            ToolsUiEvent.DialogDismissed -> {
                                viewModel.onDialogDismissed()
                            }
                            ToolsUiEvent.DialogConfirmed -> {
                                viewModel.onDialogConfirmed()
                            }
                        }
                    }
                )
            }
        }
    }
}
Replace this:
<fragment
    android:id="@+id/toolsFragment"
    android:name="ifac.td.taxi.ui.screen.ToolsFragment"
    android:label="ToolsFragment" >
with:
<fragment
    android:id="@+id/toolsFragment"
    android:name="ifac.td.taxi.ui.screen.ToolsNavigationFragment"
    android:label="ToolsNavigationFragment">
    <action
        android:id="@+id/action_toolsFragment_to_meetingSignFragment"
        app:destination="@id/meetingSignFragment2" />
    <action
        android:id="@+id/action_toolsFragment_to_requirementsFragment"
        app:destination="@id/requirementsFragment" />
</fragment>
val uiState: ToolsUiState
val meetingSignColors: StateFlow<Pair<Int, Int>> // or LiveData
fun clickRequirements()
fun onDialogDismissed()
fun onDialogConfirmed()
ToolsNavigationFragmentDirections
if the fragment is named `ToolsNavigationFragment` in the nav graph.  
findNavController().navigate(R.id.action_toolsFragment_to_meetingSignFragment)
// # Block 108-2: import android.os.Bundle
class ToolsNavigationFragment : Fragment() {
    private val viewModel: ToolsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ToolsScreen(
                uiState = viewModel.uiState,
                onEvent = { event ->
                    when (event) {
                        ToolsUiEvent.RequirementsClicked ->
                            findNavController().navigate(R.id.action_toolsFragment_to_requirementsFragment)
                        ToolsUiEvent.MeetingSignClicked -> {
                            val colors = viewModel.meetingSignColors.value
                            findNavController().navigate(
                                ToolsNavigationFragmentDirections
                                    .actionToolsFragmentToMeetingSignFragment(
                                        colors.first,
                                        colors.second
                                    )
                            )
                        }
                        ToolsUiEvent.DialogDismissed -> viewModel.onDialogDismissed()
                        ToolsUiEvent.DialogConfirmed -> viewModel.onDialogConfirmed()
                    }
                }
            )
        }
    }
}
