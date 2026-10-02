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
// # Block 261-3: import androidx.compose.foundation.background
@Composable
fun OfflineInvoiceActionButton(
    text: String,
    enabled: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.65f)
            .clickable(enabled = enabled) { onClick() }
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
// # Block 307-4: import androidx.compose.foundation.background
enum class OfflineInvoiceRouteDialogButtonType { ACCEPT, CANCEL }
data class ComposeOfflineInvoiceRouteCustomDialogModel(
    val title: String,
    val description: String,
    val isCancellable: Boolean = false,
    val buttons: List<OfflineInvoiceRouteDialogButtonType> = listOf(OfflineInvoiceRouteDialogButtonType.CANCEL, OfflineInvoiceRouteDialogButtonType.ACCEPT)
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeOfflineInvoiceRouteOfflineInvoiceCustomDialog(
    model: ComposeOfflineInvoiceRouteCustomDialogModel,
    onAction: (OfflineInvoiceRouteDialogButtonType) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = { if (model.isCancellable) onDismissRequest?.invoke() },
        title = { Text(text = model.title) },
        text = { Text(text = model.description) },
        confirmButton = {
            if (model.buttons.contains(OfflineInvoiceRouteDialogButtonType.ACCEPT)) {
                Button(onClick = { onAction(OfflineInvoiceRouteDialogButtonType.ACCEPT) }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (model.buttons.contains(OfflineInvoiceRouteDialogButtonType.CANCEL)) {
                Button(
                    onClick = { onAction(OfflineInvoiceRouteDialogButtonType.CANCEL) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Cancel", color = Color.Black)
                }
            }
        }
    )
}
