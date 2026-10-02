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
// # Block 661-6: import androidx.compose.foundation.background
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
A few behaviors in the original Fragment depend on external shared state that the Fragment handled via multiple collectors:
In Compose, the cleanest approach is:
That is what the above implementation does.
Those are represented above, but if you want a pixel-perfect migration, I’d recommend one more layer:
I can provide that if you want a stricter MVI split.
