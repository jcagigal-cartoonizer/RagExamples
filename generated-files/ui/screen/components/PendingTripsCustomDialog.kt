package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 453-5: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.PendingTripsDialogButton
import ifac.td.taxi.viewmodel.PendingTripsDialogButtonsState
@Composable
fun PendingTripsCustomDialog(
    title: String,
    description: String,
    buttonsState: PendingTripsDialogButtonsState,
    onDismissRequest: () -> Unit,
    onButtonClick: (PendingTripsDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = description) },
        confirmButton = {
            if (buttonsState.acceptVisible) {
                DialogButton(
                    text = "Accept",
                    enabled = buttonsState.acceptEnabled,
                    containerColor = buttonsState.acceptContainerColor,
                    contentColor = buttonsState.acceptContentColor,
                    onClick = { onButtonClick(PendingTripsDialogButton.ACCEPT) }
                )
            }
        },
        dismissButton = {
            if (buttonsState.cancelVisible) {
                DialogButton(
                    text = "Cancel",
                    enabled = buttonsState.cancelEnabled,
                    containerColor = buttonsState.cancelContainerColor,
                    contentColor = buttonsState.cancelContentColor,
                    onClick = { onButtonClick(PendingTripsDialogButton.CANCEL) }
                )
            }
        }
    )
}
@Composable
fun DialogButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Text(text)
    }
}
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
data class PendingTripsDialogState(
    val title: String,
    val description: String,
    val pendingTrip: PendingTrip
)
To preserve Jetpack Views navigation semantics, use callbacks from the host `Fragment` or `Activity` when embedding Compose:
@Composable
fun PendingTripsRoute(
    viewModel: PendingTripsComposeViewModel,
    navigateBack: () -> Unit,
    showHeader: (Boolean) -> Unit
) {
    PendingTripsScreen(
        viewModel = viewModel,
        onNavigateBack = navigateBack,
        onShowHeader = showHeader
    )
}
class PendingTripsComposeFragment : Fragment() {
    private val viewModel: PendingTripsViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            PendingTripsRoute(
                viewModel = viewModel,
                navigateBack = { findNavController().navigateUp() },
                showHeader = { /* iMainActivity.showHeader(it) */ }
            )
        }
    }
}
Original fragment responsibilities now mapped to:
1. a **fully integrated `NavHost` example** for this screen,  
2. a **Material3 theme-matching custom button component**, or  
3. a **more exact XML-to-Compose port** if you paste `custom_dialog.xml` and `CustomButton` implementation.
