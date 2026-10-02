package ifac.td.taxi.ui.screen
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
// # Block 254-3: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsScreen(
    uiState: PortugalSettingsUiState,
    onEvent: (PortugalSettingsUiEvent) -> Unit,
    dialogState: PortugalDialogState?,
    onDismissDialog: () -> Unit
) {
    val buttonsState = remember(uiState.resetHashEnabled) {
        PortugalSettingsButtonsState.fromUiState(uiState)
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.atcud,
            onValueChange = { onEvent(PortugalSettingsUiEvent.AtcudChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ATCUD") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.sequenceNumber,
            onValueChange = { onEvent(PortugalSettingsUiEvent.SequenceNumberChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Serie") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.document,
            onValueChange = { onEvent(PortugalSettingsUiEvent.DocumentChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Documento") }
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.resetHashEnabled,
                onCheckedChange = { onEvent(PortugalSettingsUiEvent.ResetHashChanged(it)) }
            )
            Spacer(Modifier.width(8.dp))
            Text("Reset hash")
        }
        Spacer(Modifier.height(20.dp))
        PortugalSettingsButtons(
            state = buttonsState,
            onAccept = { onEvent(PortugalSettingsUiEvent.ClickAccept) },
            onCancel = { onEvent(PortugalSettingsUiEvent.ClickCancel) },
            onChangePin = { onEvent(PortugalSettingsUiEvent.ClickChangePin) }
        )
    }
}
