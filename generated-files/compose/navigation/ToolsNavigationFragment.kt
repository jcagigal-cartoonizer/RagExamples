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
