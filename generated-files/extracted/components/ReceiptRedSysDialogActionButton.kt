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
// # Block 519-9: import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun ReceiptRedSysDialogActionButton(
    text: String,
    colors: androidx.compose.material3.ButtonColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = colors,
        shape = RoundedCornerShape(10.dp),
        contentPadding = ReceiptRedSysButtonStyleHelpers.padding()
    ) {
        Text(text = text)
    }
}
A few details from your original fragment are worth calling out:
That logic is preserved in `handleOperationClicked()`.
That is mirrored in the ViewModel event handling.
In a real project, I’d recommend this separation:
That makes testing much easier and avoids mixing Compose view logic with business logic.
In my sample `handleDialogButton()`, I used `currentOperation = _uiState.value.operations.firstOrNull()`, but your original fragment passes the clicked `operation` directly into the dialog callback.
For a perfect implementation, store the selected operation in state:
val selectedOperation: RedSysOperation? = null
and then update it on item click:
_uiState.update { it.copy(selectedOperation = operation, dialogState = ...) }
Then use that selected operation in dialog action handlers.
That is the exact Compose-safe replacement for the fragment closure behavior.
