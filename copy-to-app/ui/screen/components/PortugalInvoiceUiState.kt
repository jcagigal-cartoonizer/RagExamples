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
