package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SettingsButtonStyle and handleConfigurationClick
import ifac.td.taxi.ui.screen.components.SettingsUiEvent
import ifac.td.taxi.compose.viewmodel.SettingsComposeViewModel
import ifac.td.taxi.ui.screen.components.SettingsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 579-6: import androidx.compose.foundation.background
@Composable
fun SettingsCustomDialog(
    dialog: SettingsDialogState,
    onDismiss: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    when (dialog) {
        is SettingsDialogState.Password -> {
            Dialog(onDismissRequest = onDismiss) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 280.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = dialog.title, color = Color.Black)
                    Text(text = dialog.description, color = Color.DarkGray)
                    OutlinedTextField(
                        value = dialog.value,
                        onValueChange = { onEvent(SettingsUiEvent.DialogPasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL")
                        }
                        TextButton(
                            onClick = { onEvent(SettingsUiEvent.DialogPasswordConfirmed) }
                        ) {
                            Text("ACCEPT")
                        }
                    }
                }
            }
        }
    }
}
class SettingsComposeFragment : Fragment() {
    private val viewModel: SettingsComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
                SettingsRoute(
                    navController = navController,
                    viewModel = viewModel,
                    showHeader = { /* iMainActivity.showHeader(it) */ }
                )
            }
        }
    }
}
A few behaviors from the legacy fragment are preserved conceptually, but you’ll likely want to adapt them slightly depending on your app architecture:
  The Compose model preserves this via `SettingsButtonStyle` and `handleConfigurationClick()`.
In the sample above, I used `"PIN actual"` and `"Ingrese el PIN actual"` directly to keep the code self-contained. In your app, switch them to `stringResource(...)` or pass them from the ViewModel as resource IDs to keep localization clean.
1. a fully working **`@HiltViewModel` / Koin** implementation,
2. a **proper permission launcher for Bluetooth and write-settings permissions**,
3. and a **more exact Material 2 / Material 3 styled recreation of `CustomButton` and `SettingsCustomDialog`**.
