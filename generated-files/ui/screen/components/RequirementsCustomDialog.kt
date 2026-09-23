package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.RequirementsComposeViewModel
import ifac.td.taxi.ui.screen.components.RequirementsButtonVisualState
import ifac.td.taxi.ui.screen.components.RequirementsButtonsState
import ifac.td.taxi.ui.screen.components.RequirementsButtonStyleHelper
import ifac.td.taxi.ui.screen.components.RequirementsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 387-5: import androidx.compose.foundation.background
@Composable
fun RequirementsCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF212121)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissText != null) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E0E0),
                                contentColor = Color(0xFF212121)
                            )
                        ) {
                            Text(dismissText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
object RequirementsButtonStyleHelper {
    val PrimaryEnabledBackground = Color(0xFF1E88E5)
    val PrimaryDisabledBackground = Color(0xFFE0E0E0)
    val PrimaryEnabledContent = Color.White
    val PrimaryDisabledContent = Color(0xFF9E9E9E)
    fun primary(enabled: Boolean): RequirementsButtonVisualState {
        return RequirementsButtonVisualState(
            visible = true,
            enabled = enabled,
            background = if (enabled) PrimaryEnabledBackground else PrimaryDisabledBackground,
            contentColor = if (enabled) PrimaryEnabledContent else PrimaryDisabledContent
        )
    }
}
Then inside your state builder:
buttonsState = RequirementsButtonsState(
    driverPrimary = RequirementsButtonStyleHelper.primary(driver.isNotEmpty()),
    vehiclePrimary = RequirementsButtonStyleHelper.primary(vehicle.isNotEmpty())
)
In the Fragment version, navigation/UI actions were likely handled by the hosting activity or fragment manager.
In Compose, to preserve that style:
That keeps the ViewModel testable and the screen still works with your existing app-level navigation setup.
class RequirementsComposeFragment : Fragment() {
    private val viewModel: RequirementsComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RequirementsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                    onShowToast = { /* Toast.makeText(...) */ },
                    onShowHeader = { /* iMainActivity.showHeader(it) */ },
                    onShowBottomBar = { /* iMainActivity.showBottomBar(it) */ }
                )
            }
        }
    }
}
