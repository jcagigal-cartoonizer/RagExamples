package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ReceiptRedSysScreen
import ifac.td.taxi.compose.viewModel.ReceiptRedSysComposeViewModel
import ifac.td.taxi.viewModel.ReceiptRedSysViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ReceiptRedSysNavigationFragment : Fragment() {
    private val viewModel: ReceiptRedSysComposeViewModel by viewModels()
    private val args: ReceiptRedSysNavigationFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    ReceiptRedSysScreen(
                        navController = androidx.navigation.findNavController(this),
                        viewModel = viewModel,
                        operations = args.receiptRedSysList.toList()
                    )
                }
            }
        }
    }
}
Your current composable is already fine conceptually, but if you want it to work with the fragment above, make sure the type matches the one you use in the fragment:
@Composable
fun ReceiptRedSysScreen(
    navController: NavController,
    viewModel: ReceiptRedSysComposeViewModel,
    operations: List<RedSysOperation>
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(operations) {
        viewModel.onEvent(ReceiptRedSysUiEvent.ScreenOpened)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ReceiptRedSysUiEffect.ShowDialog -> Unit
                is ReceiptRedSysUiEffect.ShowMessage -> Unit
                is ReceiptRedSysUiEffect.PrintServiceTicket -> {
                    viewModel.printServiceTicket(effect.operation)
                }
                is ReceiptRedSysUiEffect.PrintRefundTicket -> {
                    viewModel.printRefundTicket(effect.operation)
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RedSys Receipts") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.operations.isEmpty()) {
                Text(
                    text = "No operations available",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.operations) { operation ->
                        ReceiptRedSysItem(
                            operation = operation,
                            onClick = {
                                viewModel.onEvent(
                                    ReceiptRedSysUiEvent.OperationClicked(operation)
                                )
                            }
                        )
                    }
                }
            }
            if (uiState.dialogState != null) {
                ReceiptRedSysDialog(
                    state = uiState.dialogState!!,
                    onDismiss = {
                        viewModel.onEvent(ReceiptRedSysUiEvent.DialogDismissed)
                    },
                    onButtonClick = { button ->
                        viewModel.onEvent(
                            ReceiptRedSysUiEvent.DialogButtonClicked(button)
                        )
                    }
                )
            }
        }
    }
}
