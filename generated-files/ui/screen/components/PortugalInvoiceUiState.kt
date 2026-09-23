package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PortugalInvoiceUiState
import ifac.td.taxi.ui.screen.components.PortugalInvoiceButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 11-1: import androidx.compose.runtime.Immutable
@Immutable
data class PortugalInvoiceUiState(
    val countryOptions: List<String> = emptyList(),
    val selectedCountryIndex: Int = 0,
    val nif: String = "",
    val name: String = "",
    val localidade: String = "",
    val externalCustomer: Boolean = false,
    val isAcceptLoading: Boolean = false,
    val showExternalCustomerDialog: Boolean = false,
) {
    val selectedCountry: String
        get() = countryOptions.getOrNull(selectedCountryIndex).orEmpty()
    val isPortugalSelected: Boolean
        get() = selectedCountryIndex == 0
}
sealed interface PortugalInvoiceUiEffect {
    data object NavigateBack : PortugalInvoiceUiEffect
    data class NavigateToReceiptHistory(val tripId: Long = -1L) : PortugalInvoiceUiEffect
    data object ShowExternalCustomerDialog : PortugalInvoiceUiEffect
    data object HideExternalCustomerDialog : PortugalInvoiceUiEffect
    data object EnableAcceptButton : PortugalInvoiceUiEffect
}
@Immutable
data class PortugalInvoiceButtonsState(
    val cancel: ButtonUiState = ButtonUiState(),
    val accept: ButtonUiState = ButtonUiState(),
)
@Immutable
data class ButtonUiState(
    val text: String = "",
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val visible: Boolean = true,
    val backgroundColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val disabledBackgroundColor: Color = Color.Unspecified,
    val disabledContentColor: Color = Color.Unspecified,
)
