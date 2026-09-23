package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ClosedPartialComposeViewModel
import ifac.td.taxi.ui.screen.components.ClosedPartialCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 396-7: import androidx.compose.foundation.background
@Composable
fun ClosedPartialCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissText != null) {
                        TextButton(onClick = onDismiss) {
                            Text(dismissText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
Example fragment host:
class ClosedPartialComposeFragment : Fragment() {
    private val viewModel: ClosedPartialComposeViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val safeArgs: ClosedPartialFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ClosedPartialRoute(
                    viewModel = viewModel,
                    justClosed = safeArgs.justClosed,
                    navController = findNavController(),
                    sharedViewModel = sharedViewModel
                )
            }
        }
    }
}
