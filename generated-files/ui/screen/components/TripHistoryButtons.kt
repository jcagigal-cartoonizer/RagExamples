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
// # Block 440-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun TripHistoryButtons(
    state: TripHistoryButtonsState,
    onBackClick: () -> Unit,
    onAllClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.backVisible) {
            CustomStyledButton(
                text = "Back",
                enabled = state.backEnabled,
                colors = state.backColors,
                modifier = Modifier.weight(1f),
                onClick = onBackClick
            )
        }
        if (state.allVisible) {
            CustomStyledButton(
                text = "All",
                enabled = state.allEnabled,
                colors = state.allColors,
                modifier = Modifier.weight(1f),
                onClick = onAllClick
            )
        }
        if (state.deleteVisible) {
            CustomStyledButton(
                text = "Delete",
                enabled = state.deleteEnabled,
                colors = state.deleteColors,
                modifier = Modifier.weight(1f),
                onClick = onDeleteClick
            )
        }
    }
}
@Composable
fun CustomStyledButton(
    text: String,
    enabled: Boolean,
    colors: ButtonColorTokens,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) colors.container else colors.disabledContainer,
            contentColor = if (enabled) colors.content else colors.disabledContent,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent
        )
    ) {
        Text(text = text)
    }
}
