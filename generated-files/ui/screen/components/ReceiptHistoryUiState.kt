package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptHistoryUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 189-2: import ifac.td.taxi.domain.model.Trip
data class ReceiptHistoryUiState(
    val trip: Trip? = null,
    val isLastTrip: Boolean = false,
    val isReceiptEmpty: Boolean = true,
    val invoiceConfig: InvoiceConfig = InvoiceConfig(),
    val licensingFiscal: Boolean = false
)
data class InvoiceConfig(
    val isInvoiceEnabled: Boolean = false,
    val isShowInvoiceButton: Boolean = false
)
