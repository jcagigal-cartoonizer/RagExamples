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
// # Block 583-5: import androidx.compose.foundation.layout.*
@Composable
fun PointsOfInterestCustomDialogCustomDialog(
    state: PointsOfInterestDialogState,
    onDismissRequest: () -> Unit,
    onButtonClick: (ButtonTypeUi) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = state.description)
            }
        },
        confirmButton = {
            DialogButtons(
                buttons = state.buttons,
                onButtonClick = onButtonClick
            )
        }
    )
}
@Composable
fun DialogButtons(
    buttons: List<ButtonTypeUi>,
    onButtonClick: (ButtonTypeUi) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { button ->
            when (button) {
                ButtonTypeUi.CANCEL -> TextButton(onClick = { onButtonClick(ButtonTypeUi.CANCEL) }) {
                    Text("Cancel")
                }
                ButtonTypeUi.ACCEPT -> TextButton(onClick = { onButtonClick(ButtonTypeUi.ACCEPT) }) {
                    Text("Accept")
                }
                ButtonTypeUi.UBICAR_DESTINO -> TextButton(onClick = { onButtonClick(ButtonTypeUi.UBICAR_DESTINO) }) {
                    Text("Locate destination")
                }
                ButtonTypeUi.NAVEGAR -> TextButton(onClick = { onButtonClick(ButtonTypeUi.NAVEGAR) }) {
                    Text("Navigate")
                }
                ButtonTypeUi.UBICAR_DESTINO_NAVEGAR -> TextButton(onClick = { onButtonClick(ButtonTypeUi.UBICAR_DESTINO_NAVEGAR) }) {
                    Text("Locate + Navigate")
                }
            }
        }
    }
}
In a Fragment-hosted Compose setup, you’d keep the old navigation behavior by passing the same host callbacks:
