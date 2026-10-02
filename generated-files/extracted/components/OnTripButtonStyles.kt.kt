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
// # Block 86-2: import androidx.compose.ui.graphics.Color
object OnTripButtonStyles {
    val red = Color(0xFFD32F2F)
    val green = Color(0xFF2E7D32)
    val blue = Color(0xFF1976D2)
    val orange = Color(0xFFF57C00)
    val gray = Color(0xFF9E9E9E)
    val white = Color.White
    val disabledTint = Color(0xFFBDBDBD)
    fun backgroundColor(background: ButtonBackground): Color = when (background) {
        ButtonBackground.Red -> red
        ButtonBackground.Green -> green
        ButtonBackground.Blue -> blue
        ButtonBackground.Orange -> orange
        ButtonBackground.Gray -> gray
        ButtonBackground.Default -> green
    }
    fun textColor(style: ButtonStyle): Color = when (style) {
        ButtonStyle.Enabled -> white
        ButtonStyle.Loading -> white
        ButtonStyle.Disabled -> disabledTint
    }
    fun alpha(style: ButtonStyle): Float = when (style) {
        ButtonStyle.Enabled -> 1f
        ButtonStyle.Loading -> 0.75f
        ButtonStyle.Disabled -> 0.38f
    }
}
