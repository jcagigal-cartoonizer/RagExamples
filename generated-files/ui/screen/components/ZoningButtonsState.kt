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
// # Block 95-3: import androidx.annotation.DrawableRes
data class ZoningButtonsState(
    val locateOnHired: ComposeActionButtonState = ComposeActionButtonState.Hidden,
    val soonInZone: ComposeActionButtonState = ComposeActionButtonState.Hidden,
    val pending: ComposeActionButtonState = ComposeActionButtonState.Disabled(
        textRes = R.string.pending,
        iconRes = R.drawable.ic_pending,
        color = ComposeButtonColor.Blue
    ),
    val trips: ComposeActionButtonState = ComposeActionButtonState.Enabled(
        textRes = R.string.trips,
        iconRes = R.drawable.ic_trips,
        color = ComposeButtonColor.Blue
    ),
    val cars: ComposeActionButtonState = ComposeActionButtonState.Enabled(
        textRes = R.string.cars,
        iconRes = R.drawable.ic_cars,
        color = ComposeButtonColor.Blue
    ),
) {
    fun hasVisibleActions(): Boolean =
        locateOnHired.isVisible() || soonInZone.isVisible() || pending.isVisible() || trips.isVisible() || cars.isVisible()
}
sealed class ComposeActionButtonState {
    abstract val textRes: Int
    abstract val iconRes: Int
    abstract val color: ComposeButtonColor
    data class Hidden(
        override val textRes: Int = 0,
        override val iconRes: Int = 0,
        override val color: ComposeButtonColor = ComposeButtonColor.Blue
    ) : ComposeActionButtonState()
    data class Disabled(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
    data class Enabled(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
    data class Loading(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
}
fun ComposeActionButtonState.isVisible(): Boolean = this !is ComposeActionButtonState.Hidden
enum class ComposeButtonColor {
    Blue,
    Red,
    Orange,
    Green,
    Gray,
}
