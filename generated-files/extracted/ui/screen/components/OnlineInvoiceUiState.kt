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
import ifac.td.taxi.ui.screen.OnlineInvoiceScreen
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
