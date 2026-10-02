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
// # Block 251-3: import androidx.compose.ui.graphics.Color
data class ShiftsButtonsState(
    val isOpen: Boolean = false,
    val isSelectionMode: Boolean = false,
    val mainRotation: Float = 0f,
    val exportAlpha: Float = 0f,
    val trashAlpha: Float = 0f,
    val exportTranslationY: Float = 100f,
    val trashTranslationY: Float = 100f,
    val mainIconTint: Color = Color.Unspecified,
    val exportVisible: Boolean = false,
    val trashVisible: Boolean = false,
    val sortIdArrow: ArrowState = ArrowState.Hidden,
    val sortAmountArrow: ArrowState = ArrowState.Hidden
) {
    companion object {
        fun closed() = ShiftsButtonsState(
            isOpen = false,
            isSelectionMode = false,
            mainRotation = 0f,
            exportAlpha = 0f,
            trashAlpha = 0f,
            exportTranslationY = 100f,
            trashTranslationY = 100f,
            exportVisible = false,
            trashVisible = false
        )
        fun opened() = ShiftsButtonsState(
            isOpen = true,
            isSelectionMode = true,
            mainRotation = 180f,
            exportAlpha = 1f,
            trashAlpha = 1f,
            exportTranslationY = 0f,
            trashTranslationY = 0f,
            exportVisible = true,
            trashVisible = true
        )
    }
}
