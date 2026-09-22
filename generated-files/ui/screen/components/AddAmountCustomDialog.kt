package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.state.AddAmountDialogState
@Composable
fun AddAmountCustomDialog(
    dialog: AddAmountDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
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
                    text = dialog.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResourceCompat(dialog.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAccept) {
                        Text(dialog.acceptText)
                    }
                }
            }
        }
    }
}
import androidx.compose.ui.platform.LocalContext
@Composable
fun stringResourceCompat(resId: Int): String {
    return LocalContext.current.getString(resId)
}
class AddAmountComposeFragment : Fragment() {
    private val viewModel: AddAmountComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            AddAmountRoute(
                viewModel = viewModel,
                onNavigateBack = {
                    findNavController().navigateUp()
                },
                onShowDialog = { dialogState ->
                }
            )
        }
    }
}
To match the fragment behavior:
Your original fragment relies on:
1. a **complete fully styled Compose `AddAmountScreen` with Material3 theme tokens**, or  
2. a **1:1 migration mapping from your XML IDs to Compose state**, or  
3. a **fully compilable Koin-ready version** of the Compose ViewModel and screen.
