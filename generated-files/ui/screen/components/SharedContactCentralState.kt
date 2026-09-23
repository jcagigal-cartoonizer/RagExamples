package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 421-4: import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
data class SharedContactCentralState(
    val shortBreakStatus: ShortBreakStatus? = null,
    val voiceValue: Boolean = false,
    val zone: String? = null,
)
