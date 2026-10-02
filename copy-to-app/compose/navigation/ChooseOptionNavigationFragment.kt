package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ChooseOptionScreen
import ifac.td.taxi.compose.viewModel.ChooseOptionComposeViewModel
import ifac.td.taxi.viewModel.ChooseOptionViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import androidx.compose.runtime.Composable
class ChooseOptionNavigationFragment : Fragment() {
    private val viewModel: ChooseOptionComposeViewModel by viewModels()
    private val args: ChooseOptionNavigationFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): android.view.View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        ChooseOptionNavigationContent(
                            viewModel = viewModel,
                            onNavigateBack = {
                                findNavController().navigateUp()
                            },
                            onCloseScreenAndGoBack = {
                                findNavController().navigateUp()
                            },
                            onSetFragmentActive = { active ->
                                // If you need to notify MainActivity, do it here.
                                // Example:
                                // (activity as? MainActivity)?.setChooseOptionFragmentActive(active)
                            },
                            title = "Choose Option"
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun ChooseOptionNavigationContent(
        viewModel: ChooseOptionComposeViewModel,
        onNavigateBack: () -> Unit,
        onCloseScreenAndGoBack: () -> Unit,
        onSetFragmentActive: (Boolean) -> Unit,
        title: String
    ) {
        ChooseOptionScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onCloseScreenAndGoBack = onCloseScreenAndGoBack,
            onSetFragmentActive = onSetFragmentActive,
            title = title
        )
    }
}
Replace the old fragment destination:
<fragment
    android:id="@+id/chooseOptionFragment"
    android:name="ifac.td.taxi.ui.screen.ChooseOptionNavigationFragment"
    android:label="fragment_choose_option"
    tools:layout="@layout/fragment_choose_option">
    <argument
        android:name="BravoListIDs"
        app:argType="string[]" />
    <argument
        android:name="BravoListStrings"
        app:argType="string[]" />
</fragment>
private val viewModel: ChooseOptionComposeViewModel by viewModels()
Your current composable `ChooseOptionScreen(...)` does not receive the nav args directly.  
If the `ChooseOptionComposeViewModel` needs the args (`BravoListIDs`, `BravoListStrings`), you should pass them into the ViewModel in one of these ways:
For example, if your ViewModel supports initialization:
viewModel.setOptions(
    ids = args.bravoListIDs.toList(),
    labels = args.bravoListStrings.toList()
)
then those responsibilities should either:
1. be moved into the `ChooseOptionComposeViewModel`, or
2. be handled by the hosting activity through callbacks passed into the composable.
it is a good fit for bridging the old fragment behavior.
// # Block 108-2: import androidx.compose.material3.MaterialTheme
class ChooseOptionNavigationFragment : Fragment() {
    private val viewModel: ChooseOptionComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                Surface {
                    ChooseOptionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { findNavController().navigateUp() },
                        onCloseScreenAndGoBack = { findNavController().navigateUp() },
                        onSetFragmentActive = { },
                    )
                }
            }
        }
    }
}
