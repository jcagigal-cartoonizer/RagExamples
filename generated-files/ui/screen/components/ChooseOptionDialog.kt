package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ChooseOptionCustomDialogCompose
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 377-4: import androidx.compose.foundation.background
@Composable
fun ChooseOptionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF212121)
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242)
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
@Composable
fun ChooseOptionCustomDialogCompose(
    title: String,
    description: String,
    acceptText: String = "ACCEPT",
    cancelText: String = "CANCEL",
    onAccept: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) { Text(cancelText) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) { Text(acceptText) }
                }
            }
        }
    }
}
class ChooseOptionComposeFragment : Fragment() {
    private val viewModel: ChooseOptionViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChooseOptionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { findNavController().navigateUp() },
                    onShowBottomBar = { visible -> (activity as? MainActivityHost)?.showBottomBar(visible) },
                    onShowHeader = { visible -> (activity as? MainActivityHost)?.showHeader(visible) },
                    onSetChooseOptionFragmentActive = { active -> (activity as? MainActivityHost)?.setChooseOptionFragmentActive(active) }
                )
            }
        }
    }
}
The most important behavior from the fragment is preserved:
1. a `ClassicAdapter`-to-Compose item conversion,
2. a fragment wrapper that fully mirrors your current `BaseFragment` integration,
3. or a more exact `CustomButton` style implementation with colors/borders/shapes mapped from XML.
