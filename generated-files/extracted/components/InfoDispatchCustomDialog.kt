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
import  androidx.compose.ui.window.Dialog
// # Block 477-5: import androidx.compose.foundation.layout.*
@Composable
fun InfoDispatchCustomDialog(
    dialog: InfoDispatchDialogState,
    onDismissRequest: () -> Unit,
    onButtonClicked: (InfoDispatchDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = if (dialog.dismissOnOutsideTap) onDismissRequest else {},
        title = { Text(dialog.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dialog.description?.let { Text(it) }
            }
        },
        confirmButton = {
            DialogButtonsRow(
                buttons = dialog.buttons,
                onButtonClicked = onButtonClicked
            )
        }
    )
}
@Composable
fun DialogButtonsRow(
    buttons: List<InfoDispatchDialogButton>,
    onButtonClicked: (InfoDispatchDialogButton) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { button ->
            Spacer(Modifier.width(8.dp))
            when (button) {
                InfoDispatchDialogButton.Cancel -> TextButton(onClick = { onButtonClicked(button) }) { Text("Cancel") }
                InfoDispatchDialogButton.Accept -> Button(onClick = { onButtonClicked(button) }) { Text("Accept") }
                InfoDispatchDialogButton.AtDoor -> Button(onClick = { onButtonClicked(button) }) { Text("At door") }
                InfoDispatchDialogButton.RiderInCab -> Button(onClick = { onButtonClicked(button) }) { Text("Rider in cab") }
            }
        }
    }
}
To get even closer to the XML/custom views behavior, I’d recommend adding:
1. **Full dispatch content area**
2. **Exact custom button visuals**
3. **Exact dialog button mapping**
4. **A host-side effect handler**
