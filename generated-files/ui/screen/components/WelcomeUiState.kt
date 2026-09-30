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
// # Block 493-4: import androidx.annotation.ColorInt
@Immutable
data class WelcomeUiState(
    val isLoggedIn: Boolean = false,
    val hasShifts: Boolean? = null,
    val hasClosuresButton: Boolean? = null,
    val hasAllPermissions: Boolean = false,
    val isDownloadingBravoConfig: Boolean = false,
    val lastItopState: ITopMeterStatus? = null,
    val correctLogin: Boolean = true,
    val buttons: WelcomeButtonsState = WelcomeButtonsState.default(),
)
sealed interface WelcomeUiAction {
    data object StartPressed : WelcomeUiAction
    data object SettingsPressed : WelcomeUiAction
    data object ShiftsPressed : WelcomeUiAction
    data object StatisticsPressed : WelcomeUiAction
    data object ConfigurationPressed : WelcomeUiAction
    data object PermissionsPressed : WelcomeUiAction
    data object PartialsPressed : WelcomeUiAction
    data object ExitPressed : WelcomeUiAction
    data object ConfirmExit : WelcomeUiAction
    data object ConfirmBatteryOptimization : WelcomeUiAction
    data class SubmitSecurePin(
        val pin: String,
        val destination: WelcomeUiEffect.SecurePinDestination
    ) : WelcomeUiAction
}
sealed interface WelcomeUiEffect {
    data class Navigate(val directions: Any) : WelcomeUiEffect
    data object NavigateBack : WelcomeUiEffect
    data object ExitApp : WelcomeUiEffect
    data class Toast(@StringRes val messageRes: Int) : WelcomeUiEffect
    data object ShowExitDialog : WelcomeUiEffect
    data object ShowBatteryOptimizationDialog : WelcomeUiEffect
    data class ShowSecurePinDialog(val destination: SecurePinDestination) : WelcomeUiEffect
    data object ClearDialog : WelcomeUiEffect
    data class OpenPrivacyPolicy(val url: String) : WelcomeUiEffect
    data object OpenBatteryOptimizationSettings : WelcomeUiEffect
    enum class SecurePinDestination {
        SHIFTS, STATISTICS
    }
}
@Immutable
data class WelcomeButtonsState(
    val start: WelcomeButtonState,
    val settings: WelcomeButtonState,
    val shifts: WelcomeButtonState,
    val statistics: WelcomeButtonState,
    val configuration: WelcomeButtonState,
    val permissions: WelcomeButtonState,
    val partials: WelcomeButtonState,
    val exit: WelcomeButtonState,
) {
    companion object {
        fun default() = WelcomeButtonsState(
            start = WelcomeButtonState.textOnly(R.string.btn_inicio),
            settings = WelcomeButtonState.textOnly(R.string.btn_ajustes),
            shifts = WelcomeButtonState.textOnly(R.string.btn_turnos),
            statistics = WelcomeButtonState.textOnly(R.string.btn_estadisticas),
            configuration = WelcomeButtonState.textOnly(R.string.btn_configuracion),
            permissions = WelcomeButtonState.textOnly(R.string.btn_permisos),
            partials = WelcomeButtonState.textOnly(R.string.btn_parciales),
            exit = WelcomeButtonState.textOnly(R.string.btn_salir),
        )
        fun from(
            isLoggedIn: Boolean,
            hasShifts: Boolean?,
            hasClosuresButton: Boolean?,
            hasAllPermissions: Boolean,
            isDownloadingBravoConfig: Boolean,
            isITopPaired: Boolean,
            lastItopState: ITopMeterStatus?,
            correctLogin: Boolean,
        ): WelcomeButtonsState {
            val startEnabled =
                if (isITopPaired) {
                    when (lastItopState?.taximeterState) {
                        null -> false
                        else -> lastItopState.taximeterState != ifac.td.taxi.domain.model.itop.ITopMeterStateEnum.DISCONNECTED.state
                    }
                } else {
                    !isDownloadingBravoConfig
                }
            val start = WelcomeButtonState(
                textRes = R.string.btn_inicio,
                enabled = startEnabled,
                visible = true,
                loading = isDownloadingBravoConfig,
                color = if (!hasAllPermissions) WelcomeButtonColor.ORANGE else WelcomeButtonColor.BLUE,
            )
            val shifts = WelcomeButtonState(
                textRes = R.string.btn_turnos,
                enabled = hasShifts == true,
                visible = true,
                loading = false,
                color = if (hasShifts == true) WelcomeButtonColor.BLUE else WelcomeButtonColor.GRAY
            )
            val partials = WelcomeButtonState(
                textRes = R.string.btn_parciales,
                enabled = hasClosuresButton == true,
                visible = true,
                loading = false,
                color = if (hasClosuresButton == true) WelcomeButtonColor.BLUE else WelcomeButtonColor.GRAY
            )
            val permissions = WelcomeButtonState(
                textRes = R.string.btn_permisos,
                enabled = true,
                visible = true,
                loading = false,
                color = if (hasAllPermissions) WelcomeButtonColor.BLUE else WelcomeButtonColor.ORANGE
            )
            return WelcomeButtonsState(
                start = start,
                settings = WelcomeButtonState.textOnly(R.string.btn_ajustes),
                shifts = shifts,
                statistics = WelcomeButtonState.textOnly(R.string.btn_estadisticas),
                configuration = WelcomeButtonState.textOnly(R.string.btn_configuracion),
                permissions = permissions,
                partials = partials,
                exit = WelcomeButtonState.textOnly(R.string.btn_salir),
            )
        }
    }
}
@Immutable
data class WelcomeButtonState(
    @StringRes val textRes: Int,
    val enabled: Boolean,
    val visible: Boolean,
    val loading: Boolean,
    val color: WelcomeButtonColor,
) {
    companion object {
        fun textOnly(@StringRes textRes: Int) = WelcomeButtonState(
            textRes = textRes,
            enabled = true,
            visible = true,
            loading = false,
            color = WelcomeButtonColor.BLUE
        )
    }
}
enum class WelcomeButtonColor {
    BLUE, ORANGE, GRAY
}
