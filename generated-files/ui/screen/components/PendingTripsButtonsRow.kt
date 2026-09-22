package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 380-4: import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.BorderStroke
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.PendingTripsButtonsState
import ifac.td.taxi.viewmodel.PendingTripsDialogButtonsState
import ifac.td.taxi.viewmodel.PendingTripsScreenButtonAction
@Composable
fun PendingTripsButtonsRow(
    state: PendingTripsButtonsState,
    onButtonClick: (PendingTripsScreenButtonAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            PendingTripsButton(
                text = state.cancelText,
                enabled = state.cancelEnabled,
                containerColor = if (state.cancelEnabled) state.cancelContainerColor else state.cancelDisabledContainerColor,
                contentColor = if (state.cancelEnabled) state.cancelContentColor else state.cancelDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.CANCEL) },
                modifier = Modifier.weight(1f)
            )
        }
        if (state.confirmVisible) {
            PendingTripsButton(
                text = state.confirmText,
                enabled = state.confirmEnabled,
                containerColor = if (state.confirmEnabled) state.confirmContainerColor else state.confirmDisabledContainerColor,
                contentColor = if (state.confirmEnabled) state.confirmContentColor else state.confirmDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.ACCEPT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
fun PendingTripsButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, containerColor)
    ) {
        Text(text)
    }
}
// // # Block 453-5: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import ifac.td.taxi.viewmodel.PendingTripsDialogButton
@Composable
fun PendingTripsPendingTripsCustomDialog(
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
// import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
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
