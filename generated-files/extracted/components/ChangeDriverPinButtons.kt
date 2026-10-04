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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.ChangeDriverPinScreen
// # Block 381-7: import androidx.compose.foundation.BorderStroke
@Composable
fun ChangeDriverPinButtons(
    buttonsState: ChangeDriverPinButtonsState,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (buttonsState.cancel.visible) {
            StyledCustomButton(
                spec = buttonsState.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
        }
        if (buttonsState.accept.visible) {
            StyledCustomButton(
                spec = buttonsState.accept,
                modifier = Modifier.weight(1f),
                onClick = onAcceptClick
            )
        }
    }
}
@Composable
fun StyledCustomButton(
    spec: ButtonSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (spec.borderColor.value == 0f) {
        Button(
            onClick = onClick,
            enabled = spec.enabled,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = spec.containerColor,
                contentColor = spec.contentColor,
                disabledContainerColor = spec.containerColor.copy(alpha = 0.5f),
                disabledContentColor = spec.contentColor.copy(alpha = 0.5f)
            )
        ) {
            Text(spec.text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = spec.enabled,
            modifier = modifier.height(48.dp),
            border = BorderStroke(1.dp, spec.borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = spec.containerColor,
                contentColor = spec.contentColor,
                disabledContainerColor = spec.containerColor.copy(alpha = 0.5f),
                disabledContentColor = spec.contentColor.copy(alpha = 0.5f)
            )
        ) {
            Text(spec.text)
        }
    }
}
data class ChangeDriverPinCustomDialogStateCustomDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<ButtonType> = listOf(ButtonType.ACCEPT),
    val cancelable: Boolean = true
)
@Composable
fun ChangeDriverPinCustomDialogStateCustomDialog(
    state: ChangeDriverPinCustomDialogStateCustomDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (ButtonType) -> Unit
) {
    Dialog(
        onDismissRequest = { if (state.cancelable) onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!state.description.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.buttons.forEach { type ->
                        val label = when (type) {
                            ButtonType.ACCEPT -> "ACEPTAR"
                            ButtonType.CANCEL -> "CANCELAR"
                            else -> type.name
                        }
                        Button(
                            onClick = { onButtonClick(type) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}
In the fragment version, navigation was:
In Compose, preserve that by injecting a callback from the hosting Activity / NavController / fragment host:
ChangeDriverPinScreen(
    viewModel = viewModel,
    onNavigateBack = { /* activity.onBackPressedDispatcher.onBackPressed() or navController.popBackStack() */ }
)
class ChangeDriverPinComposeFragment : Fragment() {
    private val viewModel: ChangeDriverPinViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChangeDriverPinScreen(
                    viewModel = viewModel,
                    onNavigateBack = { parentFragmentManager.popBackStack() }
                )
            }
        }
    }
}
1. a version that uses `NavController` instead of a callback,  
2. a more exact Material replication of your `CustomButton` XML styling, or  
3. a full `custom_dialog.xml`-like Compose dialog with icon, header, divider, and button row matching your app theme.
