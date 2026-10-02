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
// # Block 379-4: import androidx.compose.foundation.BorderStroke
@Composable
fun PendingTripsButtonsRow(
    state: PendingTripsButtonsState,
    onButtonClick: (PendingTripsScreenButtonAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            PendingTripsButton(
                text = state.cancelText,
                enabled = state.cancelEnabled,
                containerColor = if (state.cancelEnabled) state.cancelContainerColor else state.cancelDisabledContainerColor,
                contentColor = if (state.cancelEnabled) state.cancelContentColor else state.cancelDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.CANCEL) },
                modifier = Modifier.weight(1f)
            )
        }
        if (state.confirmVisible) {
            PendingTripsButton(
                text = state.confirmText,
                enabled = state.confirmEnabled,
                containerColor = if (state.confirmEnabled) state.confirmContainerColor else state.confirmDisabledContainerColor,
                contentColor = if (state.confirmEnabled) state.confirmContentColor else state.confirmDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.ACCEPT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
fun PendingTripsButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, containerColor)
    ) {
        Text(text)
    }
}
