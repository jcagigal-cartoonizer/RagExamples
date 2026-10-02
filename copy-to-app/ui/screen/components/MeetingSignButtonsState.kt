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
// # Block 45-2: import androidx.annotation.DrawableRes
@Immutable
data class MeetingSignButtonsState(
    val options: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.more, // replace with your actual options icon
        visible = true,
        enabled = true,
        alpha = 1f,
        translationY = 0f,
        rotation = 0f,
    ),
    val edit: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.edit, // replace with your actual edit icon
        visible = false,
        enabled = true,
        alpha = 0f,
        translationY = 100f,
        rotation = 0f,
    ),
    val dispatch: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.back,
        visible = false,
        enabled = true,
        alpha = 0f,
        translationY = 100f,
        rotation = 0f,
    ),
)
@Immutable
data class MeetingSignFabButtonState(
    @DrawableRes val iconRes: Int,
    val visible: Boolean,
    val enabled: Boolean,
    val alpha: Float,
    val translationY: Float,
    val rotation: Float,
)
