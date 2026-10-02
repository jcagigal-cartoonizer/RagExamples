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
// # Block 432-5: import androidx.compose.foundation.layout.*
@Composable
fun ZoningServicesButtonsRow(
    state: ZoningServicesButtonsState,
    onShowAllClick: () -> Unit,
    onShowRecentClick: () -> Unit,
    onCancelClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (state.showAllVisible) {
            Button(
                onClick = onShowAllClick,
                enabled = state.showAllEnabled,
                colors = zoningServicesButtonColors(state.showAllEnabled),
                shape = zoningServicesShape(),
                contentPadding = zoningServicesPadding(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show all")
            }
        }
        if (state.showRecentVisible) {
            Button(
                onClick = onShowRecentClick,
                enabled = state.showRecentEnabled,
                colors = zoningServicesButtonColors(state.showRecentEnabled),
                shape = zoningServicesShape(),
                contentPadding = zoningServicesPadding(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show recent")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.cancelVisible) {
                OutlinedButton(
                    onClick = onCancelClick,
                    colors = zoningServicesOutlinedColors(true),
                    border = zoningServicesBorder(true),
                    shape = zoningServicesShape(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
            if (state.closeVisible) {
                Button(
                    onClick = onCloseClick,
                    colors = zoningServicesButtonColors(true),
                    shape = zoningServicesShape(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
