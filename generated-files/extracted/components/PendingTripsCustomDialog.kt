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
// # Block 452-5: import androidx.compose.foundation.layout.*
@Composable
fun PendingTripsCustomDialog(
    title: String,
    description: String,
    buttonsState: PendingTripsDialogButtonsState,
    onDismissRequest: () -> Unit,
    onButtonClick: (PendingTripsDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = description) },
        confirmButton = {
            if (buttonsState.acceptVisible) {
                DialogButton(
                    text = "Accept",
                    enabled = buttonsState.acceptEnabled,
                    containerColor = buttonsState.acceptContainerColor,
                    contentColor = buttonsState.acceptContentColor,
                    onClick = { onButtonClick(PendingTripsDialogButton.ACCEPT) }
                )
            }
        },
        dismissButton = {
            if (buttonsState.cancelVisible) {
                DialogButton(
                    text = "Cancel",
                    enabled = buttonsState.cancelEnabled,
                    containerColor = buttonsState.cancelContainerColor,
                    contentColor = buttonsState.cancelContentColor,
                    onClick = { onButtonClick(PendingTripsDialogButton.CANCEL) }
                )
            }
        }
    )
}
@Composable
fun DialogButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Text(text)
    }
}
