package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 595-6: import androidx.compose.foundation.background
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
A practical migration path is:
1. keep existing `MacroZoningFragment`
2. host `MacroZoningScreen()` inside a `ComposeView`
3. move state/effects to `MacroZoningComposeViewModel`
4. slowly replace the old XML + adapter pieces
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
