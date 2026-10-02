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
// # Block 302-4: import androidx.compose.runtime.Immutable
@Immutable
data class MessagesButtonsState(
    val showBack: Boolean = true,
    val showDelete: Boolean = false,
    val showSelect: Boolean = true,
    val showMarkRead: Boolean = true,
    val backEnabled: Boolean = true,
    val deleteEnabled: Boolean = false,
    val selectEnabled: Boolean = true,
    val markReadEnabled: Boolean = true,
    val backColor: Color = Color(0xFF607D8B),
    val deleteColor: Color = Color(0xFFD32F2F),
    val selectColor: Color = Color(0xFF1976D2),
    val markReadColor: Color = Color(0xFF388E3C)
) {
    companion object {
        fun from(uiState: MessagesUiState): MessagesButtonsState {
            return MessagesButtonsState(
                showDelete = uiState.showDeleteButton,
                showSelect = uiState.showSelectButton,
                showMarkRead = uiState.showMarkReadAction,
                deleteEnabled = uiState.selectedMessageIds.isNotEmpty(),
                selectEnabled = true,
                markReadEnabled = true
            )
        }
    }
}
