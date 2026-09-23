package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.RequestStandReinforcementComposeViewModel
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 424-7: import androidx.compose.foundation.layout.*
@Composable
fun RequestStandReinforcementCustomDialog(
    dialogState: RequestStandReinforcementCustomDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!dialogState.isVisible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = dialogState.title) },
        text = { Text(text = dialogState.message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(dialogState.confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dialogState.dismissText)
            }
        }
    )
}
class RequestStandReinforcementComposeFragment : Fragment() {
    private val args: RequestStandReinforcementComposeFragmentArgs by navArgs()
    private val viewModel: RequestStandReinforcementComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            RequestStandReinforcementRoute(
                navController = findNavController(),
                viewModel = viewModel,
                idMacrozone = args.idMacrozone,
                idZone = args.idZone,
                isInFavourites = args.isInFavourites,
                onShowToast = { resId ->
                    Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
Original fragment behavior preserved:
