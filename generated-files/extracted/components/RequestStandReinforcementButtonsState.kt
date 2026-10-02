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
// # Block 276-4: import androidx.compose.runtime.Immutable
@Immutable
data class RequestStandReinforcementButtonsState(
    val cancel: ComposeButtonStyle = ComposeButtonStyle.neutral(),
    val addFavourites: RequestStandReinforcementButtonState = RequestStandReinforcementButtonState(
        text = "Add favourites",
        visible = true,
        style = ComposeButtonStyle.primary()
    ),
    val removeFavourites: RequestStandReinforcementButtonState = RequestStandReinforcementButtonState(
        text = "Remove favourites",
        visible = false,
        style = ComposeButtonStyle.danger()
    ),
    val reinforcement0: RequestStandReinforcementButtonState = reinforcement("0", 0),
    val reinforcement1: RequestStandReinforcementButtonState = reinforcement("1", 1),
    val reinforcement2: RequestStandReinforcementButtonState = reinforcement("2", 2),
    val reinforcement3: RequestStandReinforcementButtonState = reinforcement("3", 3),
    val reinforcement4: RequestStandReinforcementButtonState = reinforcement("4", 4),
    val reinforcement5: RequestStandReinforcementButtonState = reinforcement("5", 5),
    val reinforcement10: RequestStandReinforcementButtonState = reinforcement("10", 10),
    val reinforcement15: RequestStandReinforcementButtonState = reinforcement("15", 15),
    val reinforcement20: RequestStandReinforcementButtonState = reinforcement("20", 20),
    val reinforcement25: RequestStandReinforcementButtonState = reinforcement("25", 25),
) {
    companion object {
        fun from(isInFavourites: Boolean): RequestStandReinforcementButtonsState {
            return RequestStandReinforcementButtonsState(
                addFavourites = RequestStandReinforcementButtonState(
                    text = "Add favourites",
                    visible = !isInFavourites,
                    style = ComposeButtonStyle.primary()
                ),
                removeFavourites = RequestStandReinforcementButtonState(
                    text = "Remove favourites",
                    visible = isInFavourites,
                    style = ComposeButtonStyle.danger()
                )
            )
        }
        fun reinforcement(text: String, value: Int): RequestStandReinforcementButtonState {
            return RequestStandReinforcementButtonState(
                text = text,
                value = value,
                visible = true,
                style = ComposeButtonStyle.reinforcement()
            )
        }
    }
}
@Immutable
data class RequestStandReinforcementButtonState(
    val text: String,
    val value: Int = 0,
    val visible: Boolean = true,
    val style: ComposeButtonStyle
)
@Immutable
data class ComposeButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val enabled: Boolean = true,
    val cornerRadiusDp: Int = 12,
    val minHeightDp: Int = 48,
) {
    companion object {
        fun primary() = ComposeButtonStyle(
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White
        )
        fun danger() = ComposeButtonStyle(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White
        )
        fun neutral() = ComposeButtonStyle(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color(0xFF111111),
            borderColor = Color(0xFFBDBDBD)
        )
        fun reinforcement() = ComposeButtonStyle(
            containerColor = Color(0xFF455A64),
            contentColor = Color.White
        )
    }
}
