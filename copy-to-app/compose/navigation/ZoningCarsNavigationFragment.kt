package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ZoningCarsScreen
import ifac.td.taxi.compose.viewModel.ZoningCarsComposeViewModel
import ifac.td.taxi.viewModel.ZoningCarsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ZoningCarsNavigationFragment : Fragment() {
    private val viewModel: ZoningCarsViewModel by viewModels()
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
                MaterialTheme {
                    val navController = findNavController()
                    // If your ViewModel exposes StateFlow/Flow state, collect it here.
                    // Replace these with your real state properties.
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
                    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
                    ZoningCarsScreen(
                        uiState = uiState,
                        buttonsState = buttonsState,
                        dialogState = dialogState,
                        onEvent = { event ->
                            viewModel.onEvent(event)
                        },
                        onDismissDialog = {
                            viewModel.onDismissDialog()
                        },
                        onDialogButton = { buttonId ->
                            viewModel.onDialogButton(buttonId)
                        }
                    )
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val args = requireArguments()
        val idMacroZone = args.getInt("idMacroZone")
        val idZone = args.getInt("idZone")
        viewModel.initViewModelData(idMacroZone, idZone)
        viewModel.getPOIsValue()
        viewModel.checkPendingServiceOnForHirePermission()
        viewModel.checkPendingServiceOnHiredPermission()
    }
}
Update the destination class:
<fragment
    android:id="@+id/zoningCarsFragment"
    android:name="ifac.td.taxi.ui.screen.ZoningCarsNavigationFragment"
    android:label="ZoningCarsFragment">
Keep the same arguments:
<argument
    android:name="idMacroZone"
    app:argType="integer" />
<argument
    android:name="idZone"
    app:argType="integer" />
Your original `ZoningCarsFragment` performs navigation actions like:
Those should now be handled through the ViewModel or passed as callbacks into the composable.
onEvent = { event ->
    when (event) {
        ZoningCarsUiEvent.BackClicked -> navController.popBackStack()
        ZoningCarsUiEvent.CloseClicked -> navController.navigate(R.id.action_zoningCarsFragment_to_homeFragment)
        ZoningCarsUiEvent.PendingClicked -> navController.navigate(R.id.action_zoningCarsFragment_to_pendingTripsFragment)
    }
}
Then you need to adapt the wrapper to your actual state API.
For example, if your ViewModel has a single `uiState` only, use that and derive the rest or expose them from the ViewModel.
// # Block 114-2: import android.os.Bundle
class ZoningCarsNavigationFragment : Fragment() {
    private val viewModel: ZoningCarsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            MaterialTheme {
                val navController = findNavController()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                val buttonsState = viewModel.buttonsState.collectAsStateWithLifecycle().value
                val dialogState = viewModel.dialogState.collectAsStateWithLifecycle().value
                ZoningCarsScreen(
                    uiState = uiState,
                    buttonsState = buttonsState,
                    dialogState = dialogState,
                    onEvent = { event ->
                        when (event) {
                            ZoningCarsUiEvent.BackClicked ->
                                navController.popBackStack()
                            ZoningCarsUiEvent.CloseClicked ->
                                navController.navigate(R.id.action_zoningCarsFragment_to_homeFragment)
                            ZoningCarsUiEvent.PendingClicked ->
                                navController.navigate(R.id.action_zoningCarsFragment_to_pendingTripsFragment)
                        }
                    },
                    onDismissDialog = { viewModel.onDismissDialog() },
                    onDialogButton = { viewModel.onDialogButton(it) }
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val args = requireArguments()
        viewModel.initViewModelData(
            args.getInt("idMacroZone"),
            args.getInt("idZone")
        )
    }
}
