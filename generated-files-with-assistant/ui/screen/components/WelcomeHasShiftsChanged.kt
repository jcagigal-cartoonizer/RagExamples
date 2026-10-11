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
import ifac.td.taxi.ui.screen.WelcomeScreen
// # Block 832-5: import ifac.td.taxi.R
object WelcomeButtonsReducer {
    fun reduce(
        current: WelcomeButtonsState,
        action: WelcomeButtonsAction
    ): WelcomeButtonsState {
        return when (action) {
            is WelcomeButtonsAction.HasShiftsChanged -> {
                current.copy(
                    turnos = current.turnos.copy(
                        enabled = action.hasShifts,
                        background = if (action.hasShifts) {
                            WelcomeButtonBackground.BLUE
                        } else {
                            WelcomeButtonBackground.ORANGE
                        }
                    )
                )
            }
            is WelcomeButtonsAction.PartialButtonChanged -> {
                current.copy(
                    parciales = current.parciales.copy(
                        enabled = action.enabled,
                        background = if (action.enabled) {
                            WelcomeButtonBackground.BLUE
                        } else {
                            WelcomeButtonBackground.ORANGE
                        }
                    )
                )
            }
            is WelcomeButtonsAction.UserLoggedInChanged -> {
                if (action.loggedIn) {
                    current.copy(
                        parciales = current.parciales.copy(
                            enabled = true,
                            background = WelcomeButtonBackground.BLUE
                        )
                    )
                } else {
                    current.copy(
                        parciales = current.parciales.copy(
                            enabled = false,
                            background = WelcomeButtonBackground.ORANGE
                        ),
                        turnos = current.turnos.copy(
                            enabled = false,
                            background = WelcomeButtonBackground.ORANGE
                        ),
                        estadisticas = current.estadisticas.copy(
                            enabled = true,
                            background = WelcomeButtonBackground.BLUE
                        ),
                        inicio = current.inicio.copy(
                            enabled = true,
                            loading = false
                        )
                    )
                }
            }
            is WelcomeButtonsAction.DownloadingBravoConfigurationChanged -> {
                if (action.downloading) {
                    current.copy(
                        inicio = current.inicio.copy(
                            loading = true,
                            enabled = false
                        )
                    )
                } else {
                    current.copy(
                        inicio = current.inicio.copy(
                            loading = false,
                            enabled = action.hasITopMeterPaired.not()
                        )
                    )
                }
            }
            is WelcomeButtonsAction.HasAllPermissionsChanged -> {
                current.copy(
                    permisos = current.permisos.copy(
                        background = if (action.hasAllPermissions) {
                            WelcomeButtonBackground.BLUE
                        } else {
                            WelcomeButtonBackground.ORANGE
                        }
                    )
                )
            }
            is WelcomeButtonsAction.LastITopStateReceived -> {
                if (!action.hasITopMeterPaired) return current
                when (action.taximeterState) {
                    null,
                    ITopMeterStateEnum.DISCONNECTED.state -> {
                        current.copy(
                            inicio = current.inicio.copy(
                                enabled = false,
                                loading = false
                            )
                        )
                    }
                    else -> {
                        current.copy(
                            inicio = current.inicio.copy(
                                enabled = true,
                                loading = false
                            )
                        )
                    }
                }
            }
            is WelcomeButtonsAction.CorrectLoginChanged -> {
                if (action.success) current
                else {
                    val inicioEnabled = if (
                        action.hasITopMeterPaired &&
                        (action.lastTaximeterState == null ||
                         action.lastTaximeterState == ITopMeterStateEnum.DISCONNECTED.state)
                    ) {
                        false
                    } else {
                        true
                    }
                    current.copy(
                        inicio = current.inicio.copy(
                            enabled = inicioEnabled,
                            loading = false
                        )
                    )
                }
            }
        }
    }
}
sealed interface WelcomeButtonsAction {
    data class HasShiftsChanged(val hasShifts: Boolean) : WelcomeButtonsAction
    data class PartialButtonChanged(val enabled: Boolean) : WelcomeButtonsAction
    data class UserLoggedInChanged(val loggedIn: Boolean) : WelcomeButtonsAction
    data class DownloadingBravoConfigurationChanged(
        val downloading: Boolean,
        val hasITopMeterPaired: Boolean
    ) : WelcomeButtonsAction
    data class HasAllPermissionsChanged(val hasAllPermissions: Boolean) : WelcomeButtonsAction
    data class LastITopStateReceived(
        val hasITopMeterPaired: Boolean,
        val taximeterState: Int?
    ) : WelcomeButtonsAction
    data class CorrectLoginChanged(
        val success: Boolean,
        val hasITopMeterPaired: Boolean,
        val lastTaximeterState: Int?
    ) : WelcomeButtonsAction
}
That means your Compose screen can now apply the same logic the fragment did, but in a single state pipeline.
So the Compose bridge should expose those as:
and provide a single `bindSharedState(...)` method that mirrors the fragment collectors.
