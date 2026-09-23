package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DestinationMapCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 425-5: import androidx.compose.foundation.background
@Composable
fun DestinationMapCustomDialog(
    title: String,
    message: String,
    positiveText: String,
    negativeText: String,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onNegativeClick) {
                        Text(negativeText)
                    }
                    Button(onClick = onPositiveClick) {
                        Text(positiveText)
                    }
                }
            }
        }
    }
}
class DestinationMapComposeFragment : Fragment() {
    private val viewModel: DestinationMapViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    DestinationMapRouteScreen(
                        viewModel = viewModel,
                        onLaunchIntent = { intent ->
                            intent?.let { startActivity(it) }
                        },
                        onShowHeader = { show ->
                            (activity as? MainActivityHost)?.showHeader(show)
                        }
                    )
                }
            }
        }
    }
}
Because the actual XML files and original `DestinationMapCustomDialog.kt` are not included, I modeled the behavior in a Compose-equivalent way. To make it exact, you would map:
Your original `DestinationMapViewModel` only emitted navigator intents. In Compose, the more scalable pattern is:
That is exactly what the code above does.
1. a fully type-safe version using your actual dispatch model class,  
2. a `DestinationMapScreenPreview`, or  
3. a direct translation of the original `NewDirectionsAdapter` row into Compose.
