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
// # Block 340-2: import androidx.compose.runtime.Immutable
@Immutable
data class FixedPriceButtonsState(
    val taxi: FixedPriceButtonState = FixedPriceButtonState(),
    val van: FixedPriceButtonState = FixedPriceButtonState(),
    val business: FixedPriceButtonState = FixedPriceButtonState(),
)
@Immutable
data class FixedPriceButtonState(
    val visible: Boolean = true,
    val loading: Boolean = true,
    val error: Boolean = false,
    val label: String = "",
    val priceText: String = "",
    val containerColor: Color = FixedPriceButtonStyleDefaults.enabledContainer,
    val contentColor: Color = FixedPriceButtonStyleDefaults.enabledContent,
    val errorTint: Color = FixedPriceButtonStyleDefaults.errorTint,
)
object FixedPriceButtonStyleDefaults {
    val enabledContainer = Color(0xFFFFFFFF)
    val enabledContent = Color(0xFF111111)
    val disabledContainer = Color(0xFFEFEFEF)
    val disabledContent = Color(0xFF8E8E8E)
    val errorTint = Color(0xFFE53935)
    val progressTint = Color(0xFF4A4A4A)
}
fun FixedPricePriceState.toButtonsState(): FixedPriceButtonsState {
    return FixedPriceButtonsState(
        taxi = FixedPriceButtonState(
            visible = true,
            loading = showTaxiLoading,
            error = taxiError,
            priceText = taxiPrice.orEmpty(),
            containerColor = if (taxiError) FixedPriceButtonStyleDefaults.disabledContainer else FixedPriceButtonStyleDefaults.enabledContainer,
            contentColor = if (taxiError) FixedPriceButtonStyleDefaults.disabledContent else FixedPriceButtonStyleDefaults.enabledContent
        ),
        van = FixedPriceButtonState(
            visible = true,
            loading = showVanLoading,
            error = vanError,
            priceText = vanPrice.orEmpty(),
            containerColor = if (vanError) FixedPriceButtonStyleDefaults.disabledContainer else FixedPriceButtonStyleDefaults.enabledContainer,
            contentColor = if (vanError) FixedPriceButtonStyleDefaults.disabledContent else FixedPriceButtonStyleDefaults.enabledContent
        ),
        business = FixedPriceButtonState(
            visible = true,
            loading = showBusinessLoading,
            error = businessError,
            priceText = businessPrice.orEmpty(),
            containerColor = if (businessError) FixedPriceButtonStyleDefaults.disabledContainer else FixedPriceButtonStyleDefaults.enabledContainer,
            contentColor = if (businessError) FixedPriceButtonStyleDefaults.disabledContent else FixedPriceButtonStyleDefaults.enabledContent
        )
    )
}
