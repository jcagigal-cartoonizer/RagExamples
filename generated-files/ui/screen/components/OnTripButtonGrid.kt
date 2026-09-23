package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 584-5: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
@Composable
fun OnTripButtonGrid(
    buttons: OnTripButtonsState,
    locationAllowedByCentral: Boolean,
    hiredZoneExists: Boolean,
    shiftStatusCurrentStatus: Int?,
    dispatch: InfoDispatchModel?,
    roofLight: Boolean?,
    onFixedPrice: () -> Unit,
    onNotifications: () -> Unit,
    onZoning: () -> Unit,
    onNavigate: () -> Unit,
    onDispatchInfo: () -> Unit,
    onReceipts: () -> Unit,
    onMessages: () -> Unit,
    onCentral: () -> Unit,
    onClient: () -> Unit,
    onRoofLight: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OnTripActionButton(buttons.fixedPrice, "Fixed Price", onFixedPrice)
        OnTripActionButton(buttons.notifications, "Notifications", onNotifications)
        OnTripActionButton(buttons.zoning, "Zoning", onZoning)
        OnTripActionButton(buttons.navigate, "Navigate", onNavigate)
        OnTripActionButton(buttons.dispatchInfo, "Dispatch Info", onDispatchInfo)
        OnTripActionButton(buttons.receipts, "Receipts", onReceipts)
        OnTripActionButton(buttons.messages, "Messages", onMessages)
        OnTripActionButton(buttons.central, "Central", onCentral)
        OnTripActionButton(buttons.client, "Client", onClient)
        OnTripActionButton(buttons.roofLight, "Roof Light", onRoofLight)
    }
}
@Composable
fun OnTripActionButton(
    state: OnTripButtonState,
    fallbackLabel: String,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = OnTripButtonStyles.backgroundColor(state.background)
    val enabled = state.style == ButtonStyle.Enabled
    val alpha = OnTripButtonStyles.alpha(state.style)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(alpha),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = bg,
            contentColor = OnTripButtonStyles.white,
            disabledContentColor = OnTripButtonStyles.disabledTint
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = if (state.text.isNotBlank()) state.text else fallbackLabel)
    }
}
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
@Composable
fun OnTripCustomDialog(
    state: OnTripDialogState,
    onDismiss: () -> Unit,
    onButtonClicked: (OnTripDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start
            )
        },
        text = {
            Column {
                state.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    state.buttons.forEach { button ->
                        val label = when (button) {
                            OnTripDialogButton.CANCEL -> "Cancel"
                            OnTripDialogButton.ACCEPT -> "Accept"
                            OnTripDialogButton.AT_DOOR -> "At door"
                            OnTripDialogButton.RIDER_IN_CAB -> "Rider in cab"
                        }
                        Button(
                            onClick = { onButtonClicked(button) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (button) {
                                    OnTripDialogButton.ACCEPT -> Color(0xFF2E7D32)
                                    OnTripDialogButton.CANCEL -> Color(0xFF757575)
                                    OnTripDialogButton.AT_DOOR -> Color(0xFF1976D2)
                                    OnTripDialogButton.RIDER_IN_CAB -> Color(0xFFF57C00)
                                }
                            )
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
To preserve the old Fragment navigation behavior:
navController.navigate("android-app://ifac.td.taxi/zoningFragment/$idMacroZone")
or use a `NavDeepLinkRequest` in your navigation host setup.
A few behaviors in the original Fragment depend on external shared state that the Fragment handled via multiple collectors:
In Compose, the cleanest approach is:
That is what the above implementation does.
The original fragment has some very specific UI interactions, such as:
Those are represented above, but if you want a pixel-perfect migration, I’d recommend one more layer:
I can provide that if you want a stricter MVI split.
