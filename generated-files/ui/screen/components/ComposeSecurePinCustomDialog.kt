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
import  androidx.compose.ui.window.Dialog
// # Block 435-7: import androidx.compose.foundation.layout.*
@Composable
fun ComposeSecurePinStyledButtonSecurePinCustomDialog(
    title: String,
    description: String?,
    buttons: List<ComposeDialogButton>,
    onDismiss: () -> Unit,
    onAccept: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (description != null) {
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (ComposeDialogButton.Cancel in buttons) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                    if (ComposeDialogButton.Accept in buttons) {
                        TextButton(onClick = onAccept) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}
data class SecurePinButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color = Color.Unspecified
)
@Composable
fun securePinButtonColors(style: SecurePinButtonStyle): SecurePinButtonColors {
    return when (style) {
        SecurePinButtonStyle.Primary -> SecurePinButtonColors(
            container = BrandPrimary,
            content = Color.White,
            disabledContainer = BrandPrimary.copy(alpha = 0.4f),
            disabledContent = Color.White.copy(alpha = 0.7f)
        )
        SecurePinButtonStyle.Secondary -> SecurePinButtonColors(
            container = Color.Transparent,
            content = BrandPrimary,
            disabledContainer = Color.Transparent,
            disabledContent = BrandPrimary.copy(alpha = 0.4f),
            border = BrandPrimary
        )
        SecurePinButtonStyle.Danger -> SecurePinButtonColors(
            container = ErrorRed,
            content = Color.White,
            disabledContainer = ErrorRed.copy(alpha = 0.4f),
            disabledContent = Color.White.copy(alpha = 0.7f)
        )
        SecurePinButtonStyle.Disabled -> SecurePinButtonColors(
            container = Color.LightGray,
            content = Color.DarkGray,
            disabledContainer = Color.LightGray,
            disabledContent = Color.DarkGray
        )
    }
}
