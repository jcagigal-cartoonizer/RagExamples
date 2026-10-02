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
// # Block 440-6: import androidx.compose.foundation.BorderStroke
@Composable
fun PreReservationTripsButtons(
    state: PreReservationTripsButtonsState,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        if (state.assign.visibility == PreReservationTripsButtonVisibility.Visible) {
            CustomComposeButton(
                text = "Assign",
                kind = state.assign.kind,
                enabled = state.assign.enabled
            ) {}
        }
        if (state.cancel.visibility == PreReservationTripsButtonVisibility.Visible) {
            CustomComposeButton(
                text = "Cancel",
                kind = state.cancel.kind,
                enabled = state.cancel.enabled
            ) {}
        }
    }
}
@Composable
fun CustomComposeButton(
    text: String,
    kind: PreReservationTripsButtonKind,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when (kind) {
        PreReservationTripsButtonKind.Primary -> MaterialTheme.colorScheme.primary
        PreReservationTripsButtonKind.Secondary -> MaterialTheme.colorScheme.secondary
        PreReservationTripsButtonKind.Disabled -> MaterialTheme.colorScheme.surfaceVariant
        PreReservationTripsButtonKind.Warning -> MaterialTheme.colorScheme.error
    }
    val contentColor = when (kind) {
        PreReservationTripsButtonKind.Disabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Text(text = text)
    }
}
@Composable
fun DialogActionButton(
    text: String,
    destructive: Boolean,
    secondary: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        destructive -> MaterialTheme.colorScheme.error
        secondary -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when {
        secondary -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text)
    }
}
