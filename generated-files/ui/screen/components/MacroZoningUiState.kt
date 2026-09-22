package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 436-3: import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
// import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.viewmodel.model.OrderOptions
data class MacroZoningUiState(
    val macroZones: List<MacroZoneModelUi> = emptyList(),
    val listOrder: OrderOptions = OrderOptions.NONE,
    val dataTypes: String? = null,
    val scrollMode: ScrollModeEnum = ScrollModeEnum.FOLLOW_SELECTED,
    val refreshProgress: Int = 0,
    val selectedZoneId: Int? = null,
    val shortBreakStatus: ShortBreakStatus? = null,
    val currentZone: String? = null,
    val hiredZone: String? = null,
    val buttonsState: MacroZoningButtonsState = MacroZoningButtonsState(),
    val dialogState: CustomDialogState? = null
)
sealed interface MacroZoningUiEvent {
    data object OnResume : MacroZoningUiEvent
    data object OnPause : MacroZoningUiEvent
    data object OnFilterClick : MacroZoningUiEvent
    data class OnOrderSelected(val order: OrderOptions) : MacroZoningUiEvent
    data object OnPendingClick : MacroZoningUiEvent
    data object OnPreReservationClick : MacroZoningUiEvent
    data class OnMacroZoneClick(val macroZoneId: Int) : MacroZoningUiEvent
    data object OnDialogDismiss : MacroZoningUiEvent
    data object OnDialogConfirm : MacroZoningUiEvent
}
sealed interface MacroZoningUiEffect {
    data class NavigateToZoning(val macroZoneId: Int) : MacroZoningUiEffect
    data object NavigateToPendingTrips : MacroZoningUiEffect
    data object NavigateToPreReservationTrips : MacroZoningUiEffect
    data class ShowToast(val message: String) : MacroZoningUiEffect
    data class ShowDialog(val dialogState: CustomDialogState) : MacroZoningUiEffect
}
// // # Block 477-4: import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.Color
data class MacroZoningButtonsState(
    val onStopVisible: Boolean = false,
    val onZoneVisible: Boolean = false,
    val hiredVisible: Boolean = false,
    val tripsVisible: Boolean = false,
    val hiredLabel: String = "Hired",
    val tripsLabel: String = "Trips",
    val placeholderText: String = "Sort by",
    val activeOrder: OrderOptions = OrderOptions.NONE,
    val pendingButton: MacroZoningActionButtonState = MacroZoningActionButtonState.Pending(),
    val preReservationButton: MacroZoningActionButtonState = MacroZoningActionButtonState.PreReservation()
)
sealed class MacroZoningActionButtonState(
    open val enabled: Boolean,
    open val visible: Boolean,
    open val backgroundColor: Color,
    open val contentColor: Color,
    open val disabledBackgroundColor: Color,
    open val disabledContentColor: Color
) {
    data class Pending(
        override val enabled: Boolean = false,
        override val visible: Boolean = true,
        override val backgroundColor: Color = Color(0xFF1976D2), // blue
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
    data class PreReservation(
        override val enabled: Boolean = false,
        override val visible: Boolean = false,
        override val backgroundColor: Color = Color(0xFF1976D2),
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
}
// // # Block 526-5: import androidx.compose.foundation.background
// // // import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color
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
// // // import androidx.compose.foundation.background
// import androidx.compose.ui.graphics.Color
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
