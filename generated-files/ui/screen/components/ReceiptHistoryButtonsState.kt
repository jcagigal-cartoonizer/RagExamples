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
// # Block 207-3: import androidx.compose.ui.graphics.Color
data class ReceiptHistoryButtonsState(
    val print: ButtonUiState = ButtonUiState.Disabled(text = "Print"),
    val previous: ButtonUiState = ButtonUiState.Enabled(text = "Previous"),
    val partials: ButtonUiState = ButtonUiState.Enabled(text = "Partials"),
    val card: ButtonUiState = ButtonUiState.Empty(text = "Card"),
    val bill: ButtonUiState = ButtonUiState.Empty(text = "Factura"),
    val foto: ButtonUiState = ButtonUiState.Disabled(text = "Foto")
) {
    companion object {
        fun from(
            trip: Trip?,
            isReceiptEmpty: Boolean,
            canGenerateInvoice: Boolean,
            isVoucherEnabled: Boolean,
            isSubscriberAndFromDispatch: Boolean,
            shouldPrint: Boolean,
            isCardLoading: Boolean = false
        ): ReceiptHistoryButtonsState {
            val printState = when {
                isReceiptEmpty || trip == null -> ButtonUiState.Disabled("Print")
                isCardLoading -> ButtonUiState.Loading("Print")
                shouldPrint -> ButtonUiState.Enabled("Print")
                else -> ButtonUiState.Disabled("Print")
            }
            val billState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Empty("Factura")
                canGenerateInvoice -> ButtonUiState.Invoice("Factura")
                else -> ButtonUiState.Empty("Factura")
            }
            val fotoState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Disabled("Foto")
                isVoucherEnabled && isSubscriberAndFromDispatch -> ButtonUiState.Enabled("Foto")
                else -> ButtonUiState.Disabled("Foto")
            }
            val cardState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Empty("Card")
                else -> ButtonUiState.Enabled("Card")
            }
            return ReceiptHistoryButtonsState(
                print = printState,
                previous = ButtonUiState.Enabled("Previous"),
                partials = ButtonUiState.Enabled("Partials"),
                card = cardState,
                bill = billState,
                foto = fotoState
            )
        }
    }
}
sealed class ButtonUiState(
    val text: String,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val background: Color,
    val content: Color,
    val border: Color
) {
    class Enabled(text: String) : ButtonUiState(
        text = text,
        enabled = true,
        background = Color(0xFF1976D2),
        content = Color.White,
        border = Color(0xFF1976D2)
    )
    class Disabled(text: String) : ButtonUiState(
        text = text,
        enabled = false,
        background = Color(0xFFE0E0E0),
        content = Color(0xFF9E9E9E),
        border = Color(0xFFBDBDBD)
    )
    class Loading(text: String) : ButtonUiState(
        text = text,
        enabled = false,
        loading = true,
        background = Color(0xFF1565C0),
        content = Color.White,
        border = Color(0xFF1565C0)
    )
    class Empty(text: String) : ButtonUiState(
        text = text,
        visible = false,
        enabled = false,
        background = Color.Transparent,
        content = Color.Transparent,
        border = Color.Transparent
    )
    class Invoice(text: String) : ButtonUiState(
        text = text,
        enabled = true,
        background = Color(0xFF2E7D32),
        content = Color.White,
        border = Color(0xFF2E7D32)
    )
}
