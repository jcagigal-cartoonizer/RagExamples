package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.LoginUserRedSysComposeViewModel
import ifac.td.taxi.ui.screen.components.LoginUserRedSysCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 390-6: import androidx.compose.foundation.layout.*
@Composable
fun LoginUserRedSysCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
To preserve the original fragment navigation exactly:
  `R.id.action_loginUserRedSysFragment_to_changePasswordRedSysFragment`
class LoginUserRedSysFragment : Fragment() {
    private val viewModel: LoginUserRedSysComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            LoginUserRedSysScreen(
                viewModel = viewModel,
                navController = findNavController(),
                onBack = { requireActivity().onBackPressedDispatcher.onBackPressed() }
            )
        }
    }
}
