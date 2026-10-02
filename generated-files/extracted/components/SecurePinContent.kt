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
// # Block 312-5: import androidx.compose.foundation.layout.*
@Composable
fun SecurePinContent(
    uiState: SecurePinUiState,
    onEvent: (SecurePinUiEvent) -> Unit
) {
    val titlePin = when (uiState.hasSecurePin) {
        true -> stringResource(R.string.title_update_pin)
        false -> stringResource(R.string.title_insert_pin)
        null -> stringResource(R.string.title_insert_pin)
    }
    val titleRepeat = when (uiState.hasSecurePin) {
        true -> stringResource(R.string.title_update_repeat_pin)
        false -> stringResource(R.string.title_insert_repeat_pin)
        null -> stringResource(R.string.title_insert_repeat_pin)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = titlePin, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.pin,
            onValueChange = { onEvent(SecurePinUiEvent.PinChanged(it)) },
            label = { Text(stringResource(R.string.pin)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = titleRepeat, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.pinRepeat,
            onValueChange = { onEvent(SecurePinUiEvent.PinRepeatChanged(it)) },
            label = { Text(stringResource(R.string.repeat_pin)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onEvent(SecurePinUiEvent.AcceptClicked) },
                enabled = uiState.buttonsState.accept.enabled
            ) {
                Text(stringResource(R.string.accept))
            }
            OutlinedButton(
                onClick = { onEvent(SecurePinUiEvent.CancelClicked) },
                enabled = uiState.buttonsState.cancel.enabled
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
