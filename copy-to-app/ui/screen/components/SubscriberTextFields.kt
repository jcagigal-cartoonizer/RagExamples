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
// # Block 135-3: import androidx.compose.foundation.layout.*
@Composable
fun SubscriberTextFields(
    state: SubscriberPaymentUiState,
    onEvent: (SubscriberPaymentUiEvent) -> Unit,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.subscriber,
            onValueChange = { onEvent(SubscriberPaymentUiEvent.SubscriberChanged(it)) },
            label = { Text("Subscriber") },
            enabled = state.fieldsEnabled.subscriberEnabled,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.user,
            onValueChange = { onEvent(SubscriberPaymentUiEvent.UserChanged(it)) },
            label = { Text("User") },
            enabled = state.fieldsEnabled.userEnabled,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.authorization,
            onValueChange = { onEvent(SubscriberPaymentUiEvent.AuthorizationChanged(it)) },
            label = { Text("Authorization") },
            enabled = state.fieldsEnabled.authorizationEnabled,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.pin,
            onValueChange = { onEvent(SubscriberPaymentUiEvent.PinChanged(it)) },
            label = { Text("PIN") },
            enabled = state.fieldsEnabled.pinEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
