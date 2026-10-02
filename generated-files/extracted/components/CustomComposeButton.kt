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
// # Block 491-4: import androidx.compose.foundation.BorderStroke
@Composable
fun CustomComposeButton(
    text: String,
    style: ComposeButtonStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = Color(style.containerColor)
    val content = Color(style.contentColor)
    val disabledBg = Color(style.disabledContainerColor)
    val disabledContent = Color(style.disabledContentColor)
    if (style.borderColor != null) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (enabled) bg else disabledBg,
                contentColor = if (enabled) content else disabledContent,
                disabledContentColor = disabledContent,
                disabledContainerColor = disabledBg
            ),
            border = BorderStroke(
                style.strokeWidthDp.dp,
                Color(style.borderColor)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = disabledBg,
                disabledContentColor = disabledContent
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text)
        }
    }
}
@Composable
fun PointsOfInterestButtons(
    state: ifac.td.taxi.ui.screen.state.PointsOfInterestButtonsState,
    onCancel: () -> Unit,
    onSearch: () -> Unit
) {
    if (state.cancelVisible || state.searchVisible) {
        androidx.compose.foundation.layout.Row {
            if (state.cancelVisible) {
                CustomComposeButton(
                    text = state.cancelText,
                    style = state.cancelStyle,
                    enabled = state.cancelEnabled,
                    onClick = onCancel
                )
            }
            if (state.searchVisible) {
                CustomComposeButton(
                    text = state.searchText,
                    style = state.searchStyle,
                    enabled = state.searchEnabled,
                    onClick = onSearch
                )
            }
        }
    }
}
