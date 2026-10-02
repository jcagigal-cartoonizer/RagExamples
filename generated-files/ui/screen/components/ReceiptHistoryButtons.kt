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
// # Block 712-9: import androidx.compose.foundation.border
@Composable
fun ReceiptHistoryButtons(
    state: ReceiptHistoryButtonsState,
    onPrint: () -> Unit,
    onPrevious: () -> Unit,
    onPartials: () -> Unit,
    onCard: () -> Unit,
    onBill: () -> Unit,
    onFoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.print, onPrint, modifier = Modifier.weight(1f))
            ReceiptButton(state.previous, onPrevious, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.partials, onPartials, modifier = Modifier.weight(1f))
            ReceiptButton(state.card, onCard, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceiptButton(state.bill, onBill, modifier = Modifier.weight(1f))
            ReceiptButton(state.foto, onFoto, modifier = Modifier.weight(1f))
        }
    }
}
@Composable
fun ReceiptButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.background,
            contentColor = state.content,
            disabledContainerColor = state.background,
            disabledContentColor = state.content
        )
    ) {
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(state.text)
    }
}
1. a **fully compiling version** with missing imports filled in,
2. a **Koin module** for the Compose ViewModel,
3. or a **more exact recreation** of the XML `ReceiptHistoryButtonsCustomDialog` and `CustomButton` visuals using Material 3 + custom shapes/colors.
