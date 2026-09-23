package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.AddAmountComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 599-7: import androidx.compose.runtime.Composable
@Composable
fun stringResourceCompat(resId: Int): String {
    return LocalContext.current.getString(resId)
}
class AddAmountComposeFragment : Fragment() {
    private val viewModel: AddAmountComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            AddAmountRoute(
                viewModel = viewModel,
                onNavigateBack = {
                    findNavController().navigateUp()
                },
                onShowDialog = { dialogState ->
                    // optional external dialog handling if needed
                }
            )
        }
    }
}
To match the fragment behavior:
Your original fragment relies on:
1. a **complete fully styled Compose `AddAmountScreen` with Material3 theme tokens**, or  
2. a **1:1 migration mapping from your XML IDs to Compose state**, or  
3. a **fully compilable Koin-ready version** of the Compose ViewModel and screen.
