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
// # Block 355-5: import androidx.compose.foundation.BorderStroke
data class CustomButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color = containerColor.copy(alpha = 0.4f),
    val disabledContentColor: Color = contentColor.copy(alpha = 0.4f),
    val borderColor: Color? = null,
)
@Composable
fun CustomFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CustomButtonStyle,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.disabledContainerColor,
            disabledContentColor = style.disabledContentColor
        )
    ) {
        content()
    }
}
@Composable
fun CustomOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CustomButtonStyle,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = style.contentColor,
            disabledContentColor = style.disabledContentColor
        ),
        border = BorderStroke(1.dp, style.borderColor ?: style.containerColor)
    ) {
        content()
    }
}
@Composable
fun InformationMessageBottomBar(
    buttonsState: InformationMessageButtonsState,
    onCancel: () -> Unit,
) {
    if (!buttonsState.cancelVisible) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CustomFilledButton(
            onClick = onCancel,
            enabled = buttonsState.cancelEnabled,
            style = ifac.td.taxi.ui.screen.compose.style.CustomButtonStyle(
                containerColor = buttonsState.cancelContainerColor,
                contentColor = buttonsState.cancelContentColor
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "CANCEL")
        }
    }
}
@Composable
fun InformationMessageRoute(
    viewModel: InformationMessageComposeViewModel,
    navigateBack: () -> Unit,
    autoNavigateBack: () -> Unit
) {
    InformationMessageScreen(
        viewModel = viewModel,
        onBack = navigateBack,
        onNavigateBackAfterSend = autoNavigateBack
    )
}
To get exact parity with your old custom views:
1. Replace the placeholder colors in `InformationMessageButtonsState`
2. Replace the `RoundedCornerShape(12.dp/16.dp)` values with the same corner radius as your XML
3. If your `custom_dialog.xml` has:
   then add those into `InformationMessageDialog`
