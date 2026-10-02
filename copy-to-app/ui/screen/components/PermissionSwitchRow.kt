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
// # Block 696-4: import androidx.compose.animation.animateColorAsState
@Composable
fun PermissionSwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    visible: Boolean,
    highlighted: Boolean,
    onCheckedChange: () -> Unit,
) {
    if (!visible) return
    val containerColor by animateColorAsState(
        targetValue = when {
            highlighted -> Color(0xFFFFF3CD)
            checked -> Color(0xFFE8F5E9)
            else -> Color(0xFFFFFFFF)
        },
        label = "permissionRowColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            highlighted -> Color(0xFFFFB300)
            checked -> Color(0xFF2E7D32)
            else -> Color(0xFFE0E0E0)
        },
        label = "permissionRowBorder"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors()
        )
    }
}
