package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 326-4: import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun DestinationMapButtons(
    state: DestinationMapButtonsState,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            DestinationMapButton(
                text = state.cancelText,
                style = state.cancelStyle,
                enabled = state.cancelEnabled,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
        }
        if (state.secondaryVisible) {
            DestinationMapButton(
                text = state.secondaryText,
                style = state.secondaryStyle,
                enabled = state.secondaryEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onSecondaryClick?.invoke() }
            )
        }
        if (state.acceptVisible) {
            DestinationMapButton(
                text = state.acceptText,
                style = state.acceptStyle,
                enabled = state.acceptEnabled,
                modifier = Modifier.weight(1f),
                onClick = onAcceptClick
            )
        }
    }
}
@Composable
fun DestinationMapButton(
    text: String,
    style: DestinationMapButtonStyle,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = when (style) {
        DestinationMapButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF90A4AE),
            disabledContentColor = Color.White
        )
        DestinationMapButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1E88E5),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFF90A4AE)
        )
        DestinationMapButtonStyle.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFECEFF1),
            contentColor = Color(0xFF263238),
            disabledContainerColor = Color(0xFFECEFF1),
            disabledContentColor = Color(0xFF90A4AE)
        )
    }
    val border = when (style) {
        DestinationMapButtonStyle.Secondary -> BorderStroke(1.dp, Color(0xFF1E88E5))
        else -> null
    }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = colors,
        border = border,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text = text)
    }
}
// // # Block 425-5: import androidx.compose.foundation.background
// import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
@Composable
fun DestinationMapDestinationMapCustomDialog(
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
Because the actual XML files and original `CustomDialog.kt` are not included, I modeled the behavior in a Compose-equivalent way. To make it exact, you would map:
Your original `DestinationMapViewModel` only emitted navigator intents. In Compose, the more scalable pattern is:
That is exactly what the code above does.
1. a fully type-safe version using your actual dispatch model class,  
2. a `DestinationMapScreenPreview`, or  
3. a direct translation of the original `NewDirectionsAdapter` row into Compose.
