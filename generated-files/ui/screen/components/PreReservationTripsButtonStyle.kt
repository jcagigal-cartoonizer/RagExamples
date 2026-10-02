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
// # Block 537-7: import androidx.compose.ui.graphics.Color
data class PreReservationTripsButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val enabled: Boolean = true
)
fun PreReservationTripsButtonState.toStyle(
    primaryColor: Color,
    secondaryColor: Color,
    warningColor: Color,
    disabledColor: Color,
    onPrimary: Color,
    onSecondary: Color,
    onWarning: Color,
    onDisabled: Color
): PreReservationTripsButtonStyle {
    return when (kind) {
        PreReservationTripsButtonKind.Primary -> PreReservationTripsButtonStyle(
            backgroundColor = primaryColor,
            contentColor = onPrimary,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Secondary -> PreReservationTripsButtonStyle(
            backgroundColor = secondaryColor,
            contentColor = onSecondary,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Warning -> PreReservationTripsButtonStyle(
            backgroundColor = warningColor,
            contentColor = onWarning,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Disabled -> PreReservationTripsButtonStyle(
            backgroundColor = disabledColor,
            contentColor = onDisabled,
            enabled = false
        )
    }
}
In Compose, the equivalent of your `repeatOnLifecycle(STARTED)` is:
@Composable
fun PreReservationTripsDestination(
    viewModel: PreReservationTripsComposeViewModel,
    onBack: () -> Unit
) {
    PreReservationTripsRoute(
        viewModel = viewModel,
        onNavigateBack = onBack
    )
}
To fully mirror the original ViewModel, keep these calls:
But in Compose, emit only:
That fully replaces multiple flows and fragment-side callbacks.
1. a **complete ViewModel implementation with selected-trip support**,  
2. a **Compose version of the list item matching the RecyclerView row**, and  
3. a **drop-in Fragment hosting a ComposeView** so you can migrate gradually.
