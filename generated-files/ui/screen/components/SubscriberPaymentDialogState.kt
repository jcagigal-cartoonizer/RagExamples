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
// # Block 690-9: import androidx.compose.foundation.background
data class SubscriberPaymentDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<SubscriberPaymentDialogButton>,
    val cancellable: Boolean = true
)
enum class SubscriberPaymentDialogButton {
    Accept,
    FrontCamera,
    BackCamera
}
@Composable
fun SubscriberPaymentDialog(
    state: SubscriberPaymentDialogState?,
    onDismiss: () -> Unit,
    onButtonClick: (SubscriberPaymentDialogButton) -> Unit
) {
    if (state == null) return
    AlertDialog(
        onDismissRequest = if (state.cancellable) onDismiss else {},
        title = {
            Text(text = state.title)
        },
        text = {
            Column {
                state.description?.let { Text(text = it) }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.buttons.forEach { button ->
                    TextButton(onClick = {
                        onButtonClick(button)
                    }) {
                        Text(
                            text = when (button) {
                                SubscriberPaymentDialogButton.Accept -> "Accept"
                                SubscriberPaymentDialogButton.FrontCamera -> "Front camera"
                                SubscriberPaymentDialogButton.BackCamera -> "Back camera"
                            }
                        )
                    }
                }
            }
        }
    )
}
To preserve your current Fragment navigation behavior in Compose:
Here is how the old fragment logic maps to Compose:
1. a **full Compose screen with TextField validation** equivalent to `FieldType.SUBSCRIBER`
2. a **Navigation Compose graph** for the same routes
3. a **Koin module** for the new Compose ViewModel
4. a **more exact XML-like SubscriberPaymentDialogStateCustomDialog with camera selection buttons** matching `SubscriberPaymentDialogStateCustomDialog.kt` behavior more closely
