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
import  androidx.compose.ui.window.Dialog
// # Block 526-5: import androidx.compose.foundation.background
data class MacroZoningCustomActionButtonCustomDialogState(
    val visible: Boolean = false,
    val title: String,
    val description: String,
    val buttons: List<MacroZoningCustomActionButtonDialogButtonSpec> = listOf(MacroZoningCustomActionButtonDialogButtonSpec.Accept)
)
sealed interface MacroZoningCustomActionButtonDialogButtonSpec {
    data object Accept : MacroZoningCustomActionButtonDialogButtonSpec
    data object Cancel : MacroZoningCustomActionButtonDialogButtonSpec
}
@Composable
fun MacroZoningCustomActionButtonCustomDialogComposable(
    state: MacroZoningCustomActionButtonCustomDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!state.visible) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.buttons.forEach { button ->
                        when (button) {
                            MacroZoningCustomActionButtonDialogButtonSpec.Cancel -> OutlinedButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            MacroZoningCustomActionButtonDialogButtonSpec.Accept -> Button(onClick = onConfirm) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
        }
    }
}
