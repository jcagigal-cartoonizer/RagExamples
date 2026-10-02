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
// # Block 555-4: import androidx.compose.foundation.background
data class AddAmountComposeFragmentCustomDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonType> = listOf(ButtonType.ACCEPT),
    val onAccept: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null
)
@Composable
fun AddAmountComposeFragmentAddAmountCustomDialog(
    state: AddAmountComposeFragmentCustomDialogState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    if (state.buttons.contains(ButtonType.CANCEL)) {
                        ComposeCustomButton(
                            state = AddAmountComposeButtonState(
                                text = "Cancel",
                                type = ButtonType.CANCEL,
                                visible = true,
                                enabled = true
                            ),
                            onClick = {
                                state.onCancel?.invoke()
                                onDismiss()
                            }
                        )
                    }
                    if (state.buttons.contains(ButtonType.ACCEPT)) {
                        ComposeCustomButton(
                            state = AddAmountComposeButtonState(
                                text = "Accept",
                                type = ButtonType.ACCEPT,
                                visible = true,
                                enabled = true
                            ),
                            onClick = {
                                state.onAccept?.invoke()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
To preserve your existing Fragment navigation, use a fragment wrapper around the Compose screen.
