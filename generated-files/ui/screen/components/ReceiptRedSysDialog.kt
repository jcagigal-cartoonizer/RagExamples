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
// # Block 428-7: import androidx.compose.foundation.layout.*
@Composable
fun ReceiptRedSysDialog(
    state: ReceiptRedSysDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (ReceiptRedSysDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = { Text(text = state.description) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.buttons.forEach { button ->
                    when (button) {
                        ReceiptRedSysDialogButton.DEVOLVER -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Devolver",
                                colors = ReceiptRedSysButtonStyleHelpers.refundColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                        ReceiptRedSysDialogButton.IMPRIMIR -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Imprimir",
                                colors = ReceiptRedSysButtonStyleHelpers.printColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                        ReceiptRedSysDialogButton.ACCEPT -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Aceptar",
                                colors = ReceiptRedSysButtonStyleHelpers.acceptColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
object ReceiptRedSysButtonStyleHelpers {
    @Composable
    fun refundColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFFE53935),
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFFFCDD2),
        disabledContentColor = androidx.compose.ui.graphics.Color(0xFF8D6E63)
    )
    @Composable
    fun printColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFBBDEFB),
        disabledContentColor = androidx.compose.ui.graphics.Color(0xFF607D8B)
    )
    @Composable
    fun acceptColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
        contentColor = androidx.compose.ui.graphics.Color.White
    )
    fun padding() = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    fun border() = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Transparent)
}
Custom action button:
