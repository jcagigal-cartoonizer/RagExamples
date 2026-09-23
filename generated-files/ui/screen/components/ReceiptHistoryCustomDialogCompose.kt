package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptHistoryCustomDialogCompose
import ifac.td.taxi.ui.screen.components.ReceiptHistoryButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 645-8: import androidx.compose.foundation.background
@Composable
fun ReceiptHistoryCustomDialogCompose(
    state: ReceiptHistoryDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (ButtonType) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = { Text(text = state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = state.description,
                    textAlign = TextAlign.Start
                )
                if (state.isRedSysDialog) {
                    // Keep simple; can be expanded to TextFields as needed
                    Text(
                        text = "RedSys login dialog",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            DialogButtonsRow(
                buttons = state.buttons,
                onButtonClick = onButtonClick
            )
        },
        dismissButton = {}
    )
}
@Composable
fun DialogButtonsRow(
    buttons: List<ButtonType>,
    onButtonClick: (ButtonType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { btn ->
            Button(onClick = { onButtonClick(btn) }) {
                Text(btn.name)
            }
        }
    }
}
@Composable
fun ReceiptHistoryButtons(
    state: ReceiptHistoryButtonsState,
    onPrint: () -> Unit,
    onPrevious: () -> Unit,
    onPartials: () -> Unit,
    onCard: () -> Unit,
    onBill: () -> Unit,
    onFoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.print, onPrint, modifier = Modifier.weight(1f))
            ReceiptButton(state.previous, onPrevious, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.partials, onPartials, modifier = Modifier.weight(1f))
            ReceiptButton(state.card, onCard, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.bill, onBill, modifier = Modifier.weight(1f))
            ReceiptButton(state.foto, onFoto, modifier = Modifier.weight(1f))
        }
    }
}
@Composable
fun ReceiptButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.background,
            contentColor = state.content,
            disabledContainerColor = state.background,
            disabledContentColor = state.content
        )
    ) {
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(state.text)
    }
}
To preserve your current Navigation graph behavior:
Example fragment wrapper:
class ReceiptHistoryComposeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            ReceiptHistoryRoute(
                ticketId = requireArguments().getLong("ticketId"),
                navController = findNavController(),
                viewModel = /* inject */
            )
        }
    }
}
1. a **fully compiling version** with missing imports filled in,
2. a **Koin module** for the Compose ViewModel,
3. or a **more exact recreation** of the XML `ReceiptHistoryCustomDialog` and `CustomButton` visuals using Material 3 + custom shapes/colors.
