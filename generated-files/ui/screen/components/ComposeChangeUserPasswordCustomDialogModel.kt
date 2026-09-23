package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ChangeUserPasswordComposeViewModel
import ifac.td.taxi.ui.screen.components.ChangeUserPasswordCustomDialogModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 428-8: import androidx.compose.foundation.layout.*
@Composable
fun ChangeUserPasswordDialog(
    state: ChangeUserPasswordDialogState,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
            state.description?.let { Text(text = it) }
        },
        confirmButton = {
            if (state.buttons.contains(ChangeUserPasswordDialogButton.Accept)) {
                TextButton(onClick = onAccept) {
                    Text("ACCEPT")
                }
            }
        }
    )
}
data class ComposeChangeUserPasswordCustomDialogModel(
    val title: String,
    val description: String? = null,
    val buttons: List<ChangeUserPasswordDialogButton> = listOf(ChangeUserPasswordDialogButton.Accept)
)
Then the dialog composable can render it directly.
class ChangeUserPasswordComposeFragment : Fragment() {
    private val viewModel: ChangeUserPasswordComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ChangeUserPasswordRoute(
                viewModel = viewModel,
                onNavigateBack = {
                    findNavController().navigateUp()
                }
            )
        }
    }
}
Your fragment logic maps like this:
Your original code emits a `UserPresenter?` via `MutableSharedFlow<UserPresenter?>`. In Compose, a cleaner approach is to:
1. validate input
2. create presenter
3. invoke password change
4. receive callback success/failure
5. emit dialog effect
That avoids having to store a presenter in state.
