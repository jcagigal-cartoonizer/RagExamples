package ifac.td.taxi.compose.navigation
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.compose.ui.Modifier
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ifac.td.taxi.compose.viewmodel.CropImageComposeViewModel
import ifac.td.taxi.ui.screen.CropImageScreen
class CropImageNavigationFragment : Fragment() {

    private val viewModel: CropImageComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
val uiState = viewModel.uiState.value
val buttonsState = viewModel.buttonsState
val dialogState = viewModel.dialogState
        return ComposeView(requireContext()).apply {
            setContent {
                CropImageScreen(
onRequestCameraPermission = {},
sharedViewModel = {},
onOpenImageSelect = {},
onOpenCropImage = {},
serviceId = {},
vModel = {},

                )
            }
        }
    }
}
