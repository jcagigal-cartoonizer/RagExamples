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
// # Block 518-3: import androidx.compose.ui.graphics.Color
data class PaymentMoneiUiState(
    val isLoading: Boolean = false,
    val showQrLoading: Boolean = true,
    val showInfoQr: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val showAmount: Boolean = true,
    val showStatus: Boolean = true,
    val showDialog: Boolean = false,
    val dialogTitle: String = "",
    val dialogMessage: String = "",
    val dialogConfirmText: String = "",
    val dialogDismissText: String = "",
    val amountText: String = "",
    val orderId: String = "",
    val status: PaymentMoneiComposeViewModel.StatusPayments = PaymentMoneiComposeViewModel.StatusPayments.PENDING,
    val statusText: String = PaymentMoneiComposeViewModel.StatusPayments.PENDING.name,
    val statusBackgroundColor: Color = Color(0xFFFFC107),
    val paymentId: String? = null,
    val qrBitmap: android.graphics.Bitmap? = null,
    val qrUrl: String? = null
)
sealed interface PaymentMoneiUiEvent {
    data object CancelClicked : PaymentMoneiUiEvent
    data object PrintClicked : PaymentMoneiUiEvent
    data object DialogConfirm : PaymentMoneiUiEvent
    data object DialogDismiss : PaymentMoneiUiEvent
}
sealed interface PaymentMoneiUiEffect {
    data object NavigateBack : PaymentMoneiUiEffect
    data object NavigateHome : PaymentMoneiUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : PaymentMoneiUiEffect
    data class PrintTicket(val ticketBody: String) : PaymentMoneiUiEffect
    data class ShowDialog(
        val title: String,
        val message: String,
        val confirmText: String,
        val dismissText: String
    ) : PaymentMoneiUiEffect
    data object HideDialog : PaymentMoneiUiEffect
}
data class PaymentMoneiButtonsState(
    val cancel: ButtonState = ButtonState.cancel(),
    val print: ButtonState = ButtonState.print(hidden = true)
) {
    data class ButtonState(
        val text: String,
        val visible: Boolean,
        val enabled: Boolean,
        val colors: ButtonColors,
        val contentColor: Color,
    ) {
        companion object {
            @Composable
            fun cancel(
                text: String = "Cancel",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
            @Composable
            fun finish(
                text: String = "Finish",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
            @Composable
            fun print(
                text: String = "Print",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
        }
    }
}
@Composable
fun PaymentMoneiMainActivitySharedUiModelPaymentMoneiCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = Color.White
    )
}
