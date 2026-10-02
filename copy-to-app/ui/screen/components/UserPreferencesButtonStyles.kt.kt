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
// # Block 773-5: import androidx.compose.foundation.shape.RoundedCornerShape
object UserPreferencesButtonStyles {
    @Composable
    fun primary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2D6CDF),
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFB0B0B0),
        disabledContentColor = Color(0xFFFFFFFF)
    )
    @Composable
    fun cancel(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF757575),
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFB0B0B0),
        disabledContentColor = Color.White
    )
    @Composable
    fun warning(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFE67E22),
        contentColor = Color.White
    )
    @Composable
    fun success(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF1E8E3E),
        contentColor = Color.White
    )
    val shape = RoundedCornerShape(12.dp)
}
Button(
    onClick = ...,
    shape = UserPreferencesButtonStyles.shape,
    colors = UserPreferencesButtonStyles.primary()
) { ... }
I preserved the behavior using:
Examples:
Your request said:
> Use dialog state, lifecycle collection of state/events for dialog handling
That’s why the screen uses:
So the dialog is driven from `UiEffect.OpenDialog` and dismissed via state reset.
