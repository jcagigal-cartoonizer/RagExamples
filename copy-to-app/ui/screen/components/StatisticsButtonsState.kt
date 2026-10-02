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
// # Block 278-5: import androidx.compose.ui.graphics.Color
data class StatisticsButtonsState(
    val billing: StatisticsButtonState = StatisticsButtonState(),
    val time: StatisticsButtonState = StatisticsButtonState(),
    val back: StatisticsButtonState = StatisticsButtonState(),
) {
    companion object {
        fun forTab(tab: StatisticsTab): StatisticsButtonsState {
            return when (tab) {
                StatisticsTab.Week -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = true,
                        backgroundColor = Color(0xFF1E88E5),
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E88E5),
                    ),
                    time = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = false,
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF1E88E5),
                        borderColor = Color(0xFF1E88E5),
                    )
                )
                StatisticsTab.Month -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = false,
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF1E88E5),
                        borderColor = Color(0xFF1E88E5),
                    ),
                    time = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = true,
                        backgroundColor = Color(0xFF1E88E5),
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E88E5),
                    )
                )
                StatisticsTab.Year -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = false,
                        enabled = false
                    ),
                    time = StatisticsButtonState(
                        visible = false,
                        enabled = false
                    )
                )
            }
        }
    }
}
data class StatisticsButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val backgroundColor: Color = Color.Transparent,
    val contentColor: Color = Color.Black,
    val borderColor: Color = Color.Gray,
)
> If you need exact XML matching, you can plug in your project’s actual colors/dimens instead of these defaults.
