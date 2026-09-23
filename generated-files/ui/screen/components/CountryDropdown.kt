package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PortugalInvoiceUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 422-5: import androidx.compose.material3.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDropdown(
    countries: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = countries.getOrNull(selectedIndex).orEmpty()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("País") },
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countries.forEachIndexed { index, country ->
                DropdownMenuItem(
                    text = { Text(country) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
fun PortugalExternalCustomerDialog(
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cliente externo",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Este cliente parece ser externo. Deseja continuar?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text("Aceitar")
                    }
                }
            }
        }
    }
}
onNavigateToReceiptHistory = { tripId ->
    navController.navigate("receiptHistory/$tripId")
}
when (effect) {
    is PortugalInvoiceUiEffect.NavigateToReceiptHistory -> {
        navController.navigate(
            PortugalInvoiceFragmentDirections
                .actionPortugalInvoiceFragmentToReceiptHistoryFragment(effect.tripId)
        )
    }
}
class PortugalInvoiceComposeFragment : Fragment() {
    private val viewModel: PortugalInvoiceViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            PortugalInvoiceScreen(
                viewModel = viewModel,
                onNavigateBack = { findNavController().popBackStack() },
                onNavigateToReceiptHistory = { tripId ->
                    findNavController().navigate(
                        PortugalInvoiceFragmentDirections
                            .actionPortugalInvoiceFragmentToReceiptHistoryFragment(tripId)
                    )
                }
            )
        }
    }
}
1. a more exact XML-to-Compose port of `fragment_portugal_invoice.xml`,
2. a Material3 `CustomButton` matching your old `CustomButton` API more closely,
3. a fully typed `PortugalInvoiceContract` file split into `State`, `Effect`, `Action`, and `Reducer`.
