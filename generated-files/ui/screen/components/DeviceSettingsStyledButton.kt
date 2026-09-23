package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 552-7: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun DeviceSettingsStyledButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f))
    ) {
        Text(text = text, color = contentColor)
    }
}
To preserve your existing navigation flow:
class DeviceSettingsComposeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            val navController = rememberNavController()
            // obtain viewModel from DI or factory
            // DeviceSettingsScreen(navController, viewModel)
        }
    }
}
Your original XML fragment had a lot of behavior tied to custom views and possibly more styling from XML:
I converted those into Compose-friendly equivalents, but to make it *pixel-identical*, I would need the actual XML and `CustomButton`/`CustomSettingsSlider` sources.
1. a **fully self-contained Compose implementation** with a `Scaffold`, top app bar, and all sliders/buttons laid out similarly to the fragment, or  
2. a **1:1 migration plan** where I map each XML/custom view property to its Compose equivalent.
