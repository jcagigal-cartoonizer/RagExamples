package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 314-4: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.compose.state.DispatchReceivedButtonStyles
import ifac.td.taxi.ui.screen.compose.state.DispatchReceivedButtonsState
@Composable
fun DispatchReceivedButtons(
    state: DispatchReceivedButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onRequestRejectConfirmation: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.acceptEnabled)
                        state.acceptContainerColor else DispatchReceivedButtonStyles.DisabledContainer,
                    contentColor = if (state.acceptEnabled)
                        state.acceptContentColor else DispatchReceivedButtonStyles.DisabledContent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Aceptar")
            }
        }
        if (state.cancelVisible) {
            Button(
                onClick = {
                    if (state.useCancelConfirmationDialog) onRequestRejectConfirmation() else onCancel()
                },
                enabled = state.cancelEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.cancelEnabled)
                        state.cancelContainerColor else DispatchReceivedButtonStyles.DisabledContainer,
                    contentColor = if (state.cancelEnabled)
                        state.cancelContentColor else DispatchReceivedButtonStyles.DisabledContent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar")
            }
        }
    }
}
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
data class DispatchReceivedDialogModel(
    val title: String,
    val description: String,
    val buttons: List<DispatchReceivedDialogButtonSpec> = listOf(DispatchReceivedDialogButtonSpec.Cancel, DispatchReceivedDialogButtonSpec.Accept)
)
sealed interface DispatchReceivedDialogButtonSpec {
    data object Cancel : DispatchReceivedDialogButtonSpec
    data object Accept : DispatchReceivedDialogButtonSpec
    data object CancelAccept : DispatchReceivedDialogButtonSpec
}
data class DispatchReceivedDialogResult(
    val buttonPressed: DispatchReceivedDialogButtonSpec
)
@Composable
fun DispatchReceivedCustomDialog(
    model: DispatchReceivedDialogModel,
    onDismiss: () -> Unit,
    onResult: (DispatchReceivedDialogResult) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = model.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = model.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (model.buttons.contains(DispatchReceivedDialogButtonSpec.Cancel) || model.buttons.contains(DispatchReceivedDialogButtonSpec.CancelAccept)) {
                        TextButton(onClick = {
                            onResult(DispatchReceivedDialogResult(DispatchReceivedDialogButtonSpec.Cancel))
                            onDismiss()
                        }) {
                            Text("Cancelar")
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (model.buttons.contains(DispatchReceivedDialogButtonSpec.Accept) || model.buttons.contains(DispatchReceivedDialogButtonSpec.CancelAccept)) {
                        Button(onClick = {
                            onResult(DispatchReceivedDialogResult(DispatchReceivedDialogButtonSpec.Accept))
                            onDismiss()
                        }) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }
}
Example of showing the dialog from the route:
@Composable
fun DispatchReceivedDialogHost(
    dialog: DispatchReceivedDialogModel?,
    onDismiss: () -> Unit,
    onResult: (DispatchReceivedDialogResult) -> Unit
) {
    if (dialog != null) {
        DispatchReceivedCustomDialog(
            model = dialog,
            onDismiss = onDismiss,
            onResult = onResult
        )
    }
}
To preserve your existing navigation style, keep navigation as callbacks from Compose to the Fragment/Activity layer.
Example Fragment hosting Compose:
class DispatchReceivedComposeFragment : Fragment() {
    private val args: DispatchReceivedComposeFragmentArgs by navArgs()
    private val viewModel: DispatchReceivedComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            DispatchReceivedRoute(
                viewModel = viewModel,
                idDispatch = args.idDispatch,
                onNavigateToInfoDispatch = { tripId ->
                    findNavController().navigate(
                        HomeDirections.goToInfoDispatchFragment(tripId)
                    )
                },
                onCloseDialog = { tag ->
                    // bridge to your existing activity/dialog system
                    (activity as? YourMainActivityInterface)?.closeDialog(tag)
                },
                onOpenDialog = { dialog ->
                    // bridge to your existing custom dialog manager
                    // open dialog in Compose or forward to activity
                },
                onRequestPhonePermission = {
                    // bridge to permission request flow
                }
            )
        }
    }
}
To match the original fragment closely:
Example helper:
@Composable
fun dispatchReceivedButtonColors(
    enabled: Boolean,
    container: Color,
    content: Color
): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = if (enabled) container else DispatchReceivedButtonStyles.DisabledContainer,
        contentColor = if (enabled) content else DispatchReceivedButtonStyles.DisabledContent,
        disabledContainerColor = DispatchReceivedButtonStyles.DisabledContainer,
        disabledContentColor = DispatchReceivedButtonStyles.DisabledContent
    )
}
1. a **fully compilable single-file version** of the whole Compose screen, or  
2. a **more exact migration** that includes the missing `acceptDispatch()`, `rejectManualDispatch()`, `tripIdFlow`, and phone permission flow based on your current backend/viewmodel implementation.
