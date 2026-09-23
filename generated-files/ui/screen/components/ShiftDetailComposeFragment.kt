package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ShiftDetailComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 475-8: import android.os.Bundle
class ShiftDetailComposeFragment : Fragment() {
    private val viewModel: ShiftDetailComposeViewModel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val shiftId = arguments?.getLong("shiftId") ?: 0L
        view.findViewById<ComposeView>(android.R.id.content)?.setContent {
            ShiftDetailScreen(
                navController = findNavController(),
                viewModel = viewModel,
                shiftId = shiftId,
                onShowToast = { /* host-side toast */ }
            )
        }
    }
}
Your XML behavior includes:
That logic is preserved in:
Because I only used Android + Jetpack Compose references, a few project-specific items need alignment:
