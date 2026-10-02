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
// # Block 234-3: import androidx.compose.runtime.Immutable
@Immutable
data class CropImageButtonsState(
    val acceptEnabled: Boolean,
    val cropEnabled: Boolean,
    val selectImageEnabled: Boolean,
    val acceptVisible: Boolean = true,
    val cropVisible: Boolean = true,
    val selectImageVisible: Boolean = true,
    val acceptContainerColor: Color,
    val cropContainerColor: Color,
    val selectContainerColor: Color,
    val acceptContentColor: Color,
    val cropContentColor: Color,
    val selectContentColor: Color
) {
    companion object {
        fun from(hasImage: Boolean): CropImageButtonsState {
            return CropImageButtonsState(
                acceptEnabled = hasImage,
                cropEnabled = hasImage,
                selectImageEnabled = true,
                acceptVisible = true,
                cropVisible = true,
                selectImageVisible = true,
                acceptContainerColor = if (hasImage) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                cropContainerColor = if (hasImage) Color(0xFF1565C0) else Color(0xFF9E9E9E),
                selectContainerColor = Color(0xFF455A64),
                acceptContentColor = Color.White,
                cropContentColor = Color.White,
                selectContentColor = Color.White
            )
        }
    }
}
