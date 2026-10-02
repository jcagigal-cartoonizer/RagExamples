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
// # Block 214-2: import androidx.compose.runtime.Composable
@Stable
data class OfflineInvoiceButtonsState(
    val acceptEnabled: Boolean,
    val cancelEnabled: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptBackgroundColor: Color,
    val acceptContentColor: Color,
    val cancelBackgroundColor: Color,
    val cancelContentColor: Color
) {
    companion object {
        @Composable
        fun fromFormState(
            isValidToSubmit: Boolean,
            isBusy: Boolean = false
        ): OfflineInvoiceButtonsState {
            return OfflineInvoiceButtonsState(
                acceptEnabled = isValidToSubmit && !isBusy,
                cancelEnabled = !isBusy,
                acceptVisible = true,
                cancelVisible = true,
                acceptBackgroundColor = if (isValidToSubmit && !isBusy) ButtonColors.AcceptEnabledBg else ButtonColors.AcceptDisabledBg,
                acceptContentColor = if (isValidToSubmit && !isBusy) ButtonColors.AcceptEnabledText else ButtonColors.AcceptDisabledText,
                cancelBackgroundColor = ButtonColors.CancelBg,
                cancelContentColor = ButtonColors.CancelText
            )
        }
    }
}
object ButtonColors {
    val AcceptEnabledBg = Color(0xFF2E7D32)
    val AcceptDisabledBg = Color(0xFFBDBDBD)
    val AcceptEnabledText = Color.White
    val AcceptDisabledText = Color(0xFF616161)
    val CancelBg = Color(0xFFE0E0E0)
    val CancelText = Color(0xFF212121)
}
