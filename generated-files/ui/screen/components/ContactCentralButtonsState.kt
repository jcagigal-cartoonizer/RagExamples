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
import ifac.td.taxi.ui.screen.ContactCentralScreen
// # Block 62-2: import androidx.compose.runtime.Immutable
@Immutable
data class ContactCentralButtonsState(
    val cancel: ButtonUiState = ButtonUiState(),
    val shortBreak: ButtonUiState = ButtonUiState(),
    val voiceCall: ButtonUiState = ButtonUiState(),
    val messages: ButtonUiState = ButtonUiState(),
    val information: ButtonUiState = ButtonUiState(),
) {
    companion object {
        fun from(
            hasPredefinedMessages: Boolean,
            hasShortBreak: Boolean,
            isITopTaximeter: Boolean,
            shortBreakStatus: ShortBreakStatus?,
            voiceValue: Boolean,
            zone: String?,
            currentShiftStatus: Int?,
            isHired: (Int) -> Boolean,
        ): ContactCentralButtonsState {
            val hasLocation = !zone.isNullOrBlank() && zone == "Z"
            val cancel = ButtonUiState(
                visible = true,
                enabled = true,
                textRes = R.string.cancel
            )
            val voiceCall = if (voiceValue) {
                ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_voice_request,
                    style = CustomButtonStyle.default()
                )
            } else {
                ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_cancel_voice_request,
                    style = CustomButtonStyle.red()
                )
            }
            val messages = ButtonUiState(
                visible = true,
                enabled = hasPredefinedMessages,
                textRes = R.string.btn_messages
            )
            val information = ButtonUiState(
                visible = true,
                enabled = true,
                textRes = R.string.btn_information
            )
            val shortBreakVisible = hasShortBreak && currentShiftStatus != null && !isHired(currentShiftStatus)
            val shortBreak = buildShortBreakButton(
                shortBreakVisible = shortBreakVisible,
                shortBreakStatus = shortBreakStatus,
                hasLocation = hasLocation,
                zone = zone
            )
            val voiceDisabledForITop = isITopTaximeter
            val voiceCallFinal = if (voiceDisabledForITop) {
                voiceCall.copy(enabled = false)
            } else voiceCall
            return ContactCentralButtonsState(
                cancel = cancel,
                shortBreak = shortBreak,
                voiceCall = voiceCallFinal,
                messages = messages,
                information = information,
            )
        }
        fun buildShortBreakButton(
            shortBreakVisible: Boolean,
            shortBreakStatus: ShortBreakStatus?,
            hasLocation: Boolean,
            zone: String?,
        ): ButtonUiState {
            if (!shortBreakVisible) {
                return ButtonUiState(visible = false)
            }
            if (!hasLocation) {
                return when (shortBreakStatus) {
                    ShortBreakStatus.IN_SHORT_BREAK,
                    ShortBreakStatus.IN_SHORT_BREAK_FORCED -> ButtonUiState(
                        visible = true,
                        enabled = true,
                        textRes = R.string.btn_end_short_break,
                        style = CustomButtonStyle.red()
                    )
                    else -> ButtonUiState(
                        visible = true,
                        enabled = false,
                        textRes = R.string.btn_short_break,
                        style = CustomButtonStyle.green(disabled = true)
                    )
                }
            }
            return when (shortBreakStatus) {
                ShortBreakStatus.IN_SHORT_BREAK,
                ShortBreakStatus.IN_SHORT_BREAK_FORCED -> ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_end_short_break,
                    style = CustomButtonStyle.red()
                )
                ShortBreakStatus.CAN_START_SHORT_BREAK -> ButtonUiState(
                    visible = true,
                    enabled = true,
                    textRes = R.string.btn_short_break,
                    style = CustomButtonStyle.green()
                )
                ShortBreakStatus.SHORT_BREAK_DISABLED -> ButtonUiState(
                    visible = true,
                    enabled = false,
                    textRes = R.string.btn_short_break,
                    style = CustomButtonStyle.green(disabled = true)
                )
                else -> ButtonUiState(
                    visible = true,
                    enabled = false,
                    textRes = R.string.btn_short_break,
                    style = CustomButtonStyle.green(disabled = true)
                )
            }
        }
    }
}
@Immutable
data class ButtonUiState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val textRes: Int? = null,
    val loading: Boolean = false,
    val style: CustomButtonStyle = CustomButtonStyle.default(),
)
@Immutable
data class CustomButtonStyle(
    val background: Color,
    val content: Color,
    val disabledBackground: Color,
    val disabledContent: Color,
    val border: Color? = null
) {
    companion object {
        fun default() = CustomButtonStyle(
            background = Color(0xFF2E7DFF),
            content = Color.White,
            disabledBackground = Color(0xFFB0B0B0),
            disabledContent = Color.White
        )
        fun green(disabled: Boolean = false) = CustomButtonStyle(
            background = Color(0xFF2EAD4B),
            content = Color.White,
            disabledBackground = if (disabled) Color(0xFFB0B0B0) else Color(0xFF2EAD4B),
            disabledContent = Color.White
        )
        fun red(disabled: Boolean = false) = CustomButtonStyle(
            background = Color(0xFFD32F2F),
            content = Color.White,
            disabledBackground = if (disabled) Color(0xFFB0B0B0) else Color(0xFFD32F2F),
            disabledContent = Color.White
        )
    }
}
