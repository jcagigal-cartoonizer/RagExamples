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
// # Block 312-5: import androidx.compose.foundation.background
@Composable
fun ShiftDetailButtonsRow(
    buttons: ShiftDetailButtonsState,
    onExport: () -> Unit,
    onEmail: () -> Unit,
    onPrint: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ShiftActionButton(buttons.export, onClick = onExport, modifier = Modifier.weight(1f))
        ShiftActionButton(buttons.email, onClick = onEmail, modifier = Modifier.weight(1f))
        ShiftActionButton(buttons.print, onClick = onPrint, modifier = Modifier.weight(1f))
    }
}
@Composable
fun ShiftSortRow(
    buttons: ShiftDetailButtonsState,
    onSortById: () -> Unit,
    onSortByAmount: () -> Unit,
    onSortByInitHour: () -> Unit,
    onSortByDistance: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortHeaderButton(buttons.idSort, onSortById, Modifier.weight(1f))
        SortHeaderButton(buttons.amountSort, onSortByAmount, Modifier.weight(1f))
        SortHeaderButton(buttons.initHourSort, onSortByInitHour, Modifier.weight(1f))
        SortHeaderButton(buttons.distanceSort, onSortByDistance, Modifier.weight(1f))
    }
}
@Composable
fun ShiftActionButton(
    state: ButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(state.backgroundColor)
    val content = Color(state.contentColor)
    val border = state.borderColor?.let { Color(it) }
    Surface(
        modifier = modifier
            .height(48.dp)
            .then(
                if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(enabled = state.enabled, onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = state.label, color = content)
        }
    }
}
@Composable
fun SortHeaderButton(
    state: SortButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color(0xFFEFEFEF)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = state.label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
            when (state.ascending) {
                true -> Icon(Icons.Default.ArrowDropUp, contentDescription = null)
                false -> Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                null -> Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}
