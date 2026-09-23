package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.OnlineInvoiceButtonUi
import ifac.td.taxi.ui.screen.components.OnlineInvoiceDialogState
import ifac.td.taxi.ui.screen.components.OnlineInvoiceUiState
import ifac.td.taxi.ui.screen.components.OnlineInvoiceButtonStyle
import ifac.td.taxi.ui.screen.components.OnlineInvoiceButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 10-1: import androidx.annotation.StringRes
data class OnlineInvoiceUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,
    val fiscalId: String = "",
    val companyName: String = "",
    val streetName: String = "",
    val number: String = "",
    val city: String = "",
    val postalCode: String = "",
    val province: String = "",
    val country: String = "",
    val email: String = "",
    val areFiscalFieldsVisible: Boolean = false,
    val dialogState: OnlineInvoiceDialogState? = null
) {
    val hasNif: Boolean get() = fiscalId.isNotBlank()
    val areRequiredFieldsEmpty: Boolean
        get() = companyName.isBlank() ||
            streetName.isBlank() ||
            number.isBlank() ||
            city.isBlank() ||
            postalCode.isBlank() ||
            province.isBlank() ||
            fiscalId.isBlank() ||
            email.isBlank() ||
            country.isBlank()
    fun toFiscalData() = FiscalData(
        fiscalID = fiscalId,
        companyName = companyName,
        streetName = streetName,
        number = number,
        city = city,
        postalCode = postalCode,
        province = province,
        country = country,
        email = email
    )
}
sealed interface OnlineInvoiceUiEffect {
    data class ShowToast(@StringRes val messageRes: Int) : OnlineInvoiceUiEffect
    data object NavigateBack : OnlineInvoiceUiEffect
    data class OpenDialog(val dialogState: OnlineInvoiceDialogState) : OnlineInvoiceUiEffect
}
data class OnlineInvoiceDialogState(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val buttons: List<OnlineInvoiceDialogButton> = listOf(OnlineInvoiceDialogButton.Accept),
    val onAccept: (() -> Unit)? = null
)
sealed interface OnlineInvoiceDialogButton {
    data object Accept : OnlineInvoiceDialogButton
}
enum class OnlineInvoiceButtonType {
    ACCEPT,
    CANCEL
}
data class OnlineInvoiceButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val loadingContainerColor: Color,
    val loadingContentColor: Color,
)
data class OnlineInvoiceButtonUi(
    val type: OnlineInvoiceButtonType,
    val textRes: Int,
    val enabled: Boolean,
    val isVisible: Boolean = true,
    val isLoading: Boolean = false,
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)
data class OnlineInvoiceButtonsState(
    val accept: OnlineInvoiceButtonUi,
    val cancel: OnlineInvoiceButtonUi
) {
    companion object {
        fun from(
            isLoading: Boolean,
            canGenerateInvoice: Boolean,
        ): OnlineInvoiceButtonsState {
            val acceptStyle = OnlineInvoiceButtonStyle(
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBDBDBD),
                disabledContentColor = Color(0xFF757575),
                loadingContainerColor = Color(0xFF2E7D32),
                loadingContentColor = Color.White
            )
            val cancelStyle = OnlineInvoiceButtonStyle(
                containerColor = Color(0xFFE0E0E0),
                contentColor = Color(0xFF212121),
                disabledContainerColor = Color(0xFFE0E0E0),
                disabledContentColor = Color(0xFF9E9E9E),
                loadingContainerColor = Color(0xFFE0E0E0),
                loadingContentColor = Color(0xFF212121)
            )
            return OnlineInvoiceButtonsState(
                accept = OnlineInvoiceButtonUi(
                    type = OnlineInvoiceButtonType.ACCEPT,
                    textRes = R.string.accept,
                    enabled = !isLoading && canGenerateInvoice,
                    isLoading = isLoading,
                    containerColor = acceptStyle.containerColor,
                    contentColor = acceptStyle.contentColor,
                    disabledContainerColor = acceptStyle.disabledContainerColor,
                    disabledContentColor = acceptStyle.disabledContentColor
                ),
                cancel = OnlineInvoiceButtonUi(
                    type = OnlineInvoiceButtonType.CANCEL,
                    textRes = R.string.cancel,
                    enabled = !isLoading,
                    isVisible = true,
                    isLoading = false,
                    containerColor = cancelStyle.containerColor,
                    contentColor = cancelStyle.contentColor,
                    disabledContainerColor = cancelStyle.disabledContainerColor,
                    disabledContentColor = cancelStyle.disabledContentColor
                )
            )
        }
    }
}
