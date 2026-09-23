package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 684-6: import androidx.compose.foundation.clickable
@Composable
fun SuggestionList(
    items: List<SuggestModel>,
    onClick: (SuggestModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 16.dp)
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item) }
                    .padding(vertical = 12.dp)
            ) {
                Text(item.getPrintableStreetTextShort())
            }
        }
    }
}
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    return ComposeView(requireContext()).apply {
        setContent {
            val navController = findNavController()
            FixedPriceMapScreen(
                navController = navController,
                viewModel = vModelCompose
            )
        }
    }
}
Because the map is from a non-Compose SDK, wrap it in `AndroidView`:
@Composable
fun FixedPriceMapView(
    modifier: Modifier = Modifier,
    onMapReady: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            com.nexusgeographics.cercalia.maps.CercaliaMapView(context).apply {
                // initialize map here
            }
        },
        update = { /* update map state */ }
    )
}
Then in your screen:
FixedPriceMapView(
    modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
)
Here’s the direct Compose equivalent of the original behaviors:
To match the XML behavior exactly, wire these from your resources:
For closer parity, update `FixedPriceButtonStyle` with your exact resource values.
1. a **complete `@Composable` screen with an `AndroidView` Cercalia map integration**,  
2. a **`FixedPriceComposeFragment` wrapper**, and  
3. a **more faithful re-creation of the original bottom sheet + custom button XML styling** using your actual resource names.
