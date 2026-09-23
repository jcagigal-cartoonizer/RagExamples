package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 526-5: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
data class MacroZoningCustomDialogState(
    val visible: Boolean = false,
    val title: String,
    val description: String,
    val buttons: List<MacroZoningDialogButtonSpec> = listOf(MacroZoningDialogButtonSpec.Accept)
)
sealed interface MacroZoningDialogButtonSpec {
    data object Accept : MacroZoningDialogButtonSpec
    data object Cancel : MacroZoningDialogButtonSpec
}
@Composable
fun MacroZoningCustomDialogComposable(
    state: CustomDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!state.visible) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.buttons.forEach { button ->
                        when (button) {
                            MacroZoningDialogButtonSpec.Cancel -> OutlinedButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            MacroZoningDialogButtonSpec.Accept -> Button(onClick = onConfirm) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
        }
    }
}
import ifac.td.taxi.ui.screen.state.MacroZoningActionButtonState
@Composable
fun CustomActionButton(
    text: String,
    state: MacroZoningActionButtonState,
    onClick: () -> Unit
) {
    val bg = if (state.enabled) state.backgroundColor else state.disabledBackgroundColor
    val fg = if (state.enabled) state.contentColor else state.disabledContentColor
    Button(
        onClick = onClick,
        enabled = state.enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg,
            disabledContentColor = fg
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(48.dp)
    ) {
        Text(text)
    }
}
@Composable
fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFE3F2FD) else Color.Transparent
    val fg = if (selected) Color(0xFF1565C0) else Color(0xFF424242)
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(36.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .then(Modifier)
                .padding(vertical = 8.dp),
        ) {
            Text(text = text, color = fg, modifier = Modifier.clickable(onClick = onClick))
        }
    }
}
@Composable
fun HeaderSortItem(
    title: String,
    visible: Boolean,
    active: Boolean,
    onClick: () -> Unit
) {
    if (!visible) return
    Text(
        text = title,
        modifier = Modifier.clickable(onClick = onClick),
        color = if (active) Color(0xFF1565C0) else Color(0xFF424242)
    )
}
Keep navigation in the Fragment or Navigation layer by:
Example:
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is MacroZoningUiEffect.NavigateToZoning -> {
                    findNavController().navigate(
                        MacroZoningFragmentDirections.actionMacroZoningFragmentToZoningFragment(effect.macroZoneId)
                    )
                }
                MacroZoningUiEffect.NavigateToPendingTrips -> {
                    findNavController().navigate(R.id.action_macroZoningFragment_to_pendingTripsFragment)
                }
                MacroZoningUiEffect.NavigateToPreReservationTrips -> {
                    findNavController().navigate(R.id.action_macroZoningFragment_to_preReservationTripsFragment)
                }
                is MacroZoningUiEffect.ShowToast -> {
                    Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                }
                is MacroZoningUiEffect.ShowDialog -> {
                    // show Compose dialog state or bridge to legacy dialog
                }
            }
        }
    }
}
The following original behaviors are preserved conceptually in the Compose version:
A practical migration path is:
1. keep existing `MacroZoningFragment`
2. host `MacroZoningScreen()` inside a `ComposeView`
3. move state/effects to `MacroZoningComposeViewModel`
4. slowly replace the old XML + adapter pieces
Example host:
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View = ComposeView(requireContext()).apply {
    setContent {
        val state by viewModel.uiState.collectAsState()
        MacroZoningScreen(
            uiState = state,
            onEvent = viewModel::onEvent,
            uiEffects = viewModel.uiEffects,
            onNavigateToZoning = { id -> /* nav */ },
            onNavigateToPendingTrips = { /* nav */ },
            onNavigateToPreReservationTrips = { /* nav */ },
            onShowToast = { /* toast */ },
            onShowDialog = { /* dialog */ }
        )
    }
}
