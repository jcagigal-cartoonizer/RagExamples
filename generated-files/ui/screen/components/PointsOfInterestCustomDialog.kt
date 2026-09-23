package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PointsOfInterestCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 583-5: import androidx.compose.foundation.layout.*
@Composable
fun PointsOfInterestCustomDialog(
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
Your original fragment logic has two nested dialog flows:
1. Clicking a POI opens a details dialog.
2. If the POI has a zone, there are three buttons:
3. For `UBICAR_DESTINO` / `UBICAR_DESTINO_NAVEGAR`, a second confirmation dialog is shown.
4. On confirmation, zone is saved and the fragment navigates back twice.
That logic is preserved conceptually above, but for a full production implementation you should:
sealed class PointsOfInterestDialogState {
    data class PoiDetails(val poi: Poi) : PointsOfInterestDialogState()
    data class ConfirmLocate(val poi: Poi, val navigateAfter: Boolean) : PointsOfInterestDialogState()
}
To preserve the original fragment behavior:
Those remain host responsibilities and should be passed into the composable.
1. a fully working `@Composable` screen,
2. a fully working `ViewModel`,
3. a navigation integration snippet using `NavController`,
4. and a refined dialog state machine that exactly handles `UBICAR_DESTINO` vs `UBICAR_DESTINO_NAVEGAR`.
