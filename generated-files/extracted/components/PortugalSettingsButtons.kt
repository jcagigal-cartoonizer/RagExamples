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
// # Block 367-5: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsButtons(
    state: PortugalSettingsButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onChangePin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.acceptContainerColor,
                    contentColor = state.acceptContentColor
                )
            ) {
                Text("ACEPTAR")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.cancelContainerColor,
                    contentColor = state.cancelContentColor
                )
            ) {
                Text("CANCELAR")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.changePinVisible) {
            Button(
                onClick = onChangePin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.changePinContainerColor,
                    contentColor = state.changePinContentColor
                )
            ) {
                Text("CAMBIAR PIN")
            }
        }
    }
}
