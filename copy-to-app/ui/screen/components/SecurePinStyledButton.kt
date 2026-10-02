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
// # Block 535-9: import androidx.compose.material3.*
@Composable
fun SecurePinStyledButton(
    state: SecurePinButtonState,
    onClick: () -> Unit
) {
    val colors = securePinButtonColors(state.style)
    if (state.style == SecurePinButtonStyle.Secondary) {
        OutlinedButton(
            onClick = onClick,
            enabled = state.enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.container,
                contentColor = colors.content,
                disabledContentColor = colors.disabledContent
            )
        ) {
            Text(text = stringResource(state.textRes))
        }
    } else {
        Button(
            onClick = onClick,
            enabled = state.enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.container,
                contentColor = colors.content,
                disabledContainerColor = colors.disabledContainer,
                disabledContentColor = colors.disabledContent
            )
        ) {
            Text(text = stringResource(state.textRes))
        }
    }
}
In the fragment version, navigation is handled by:
In Compose, that is preserved via:
composable("secure_pin") {
    val viewModel: SecurePinComposeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    SecurePinScreen(
        navController = navController,
        viewModel = viewModel,
        onShowBottomBar = { visible -> /* call host */ },
        onShowHeader = { visible -> /* call host */ }
    )
}
1. a **more exact Material 2 / Material 3 custom button recreation**
2. a **fully themed dialog matching your XML margins, corners, and colors**
3. a **Navigation Compose version with argument support**
4. a **Koin module for the Compose ViewModel**
5. a **migration version that keeps your existing domain/usecase classes unchanged**
