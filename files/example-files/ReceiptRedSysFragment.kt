package ifac.td.taxi.ui.screen

import ifac.td.taxi.framework.util.Logs
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentReceiptRedSysBinding
import ifac.td.taxi.domain.model.RedSysOperation.Companion.AUTHORIZATION
import ifac.td.taxi.domain.model.RedSysOperation.Companion.DENIED
import ifac.td.taxi.domain.model.RedSysOperation.Companion.REFUND
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.ReceiptRedSysAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.ReceiptRedSysViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReceiptRedSysFragment :
    BaseFragment<FragmentReceiptRedSysBinding, ReceiptRedSysViewModel>(R.layout.fragment_receipt_red_sys) {

    private val vModel: ReceiptRedSysViewModel by viewModel()

    private val safeArgs: ReceiptRedSysFragmentArgs by navArgs()

    private lateinit var adapter: ReceiptRedSysAdapter

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentReceiptRedSysBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        Logs.d("ReceiptRedSysFragment", "Array<RedSysOperation> = \n$safeArgs")

        setUpAdapter()
    }

    private fun setUpAdapter() {
        val receiptList = safeArgs.receiptRedSysList.toList()
        Logs.d("ReceiptRedSysFragment", "receiptList = \n$receiptList")
        if (receiptList.isEmpty()) {
            Logs.d("ReceiptRedSysFragment", "The list is empty")
        } else {
            context?.let {
               adapter = ReceiptRedSysAdapter(it, receiptList) { operation ->
                   val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                       when (response.buttonPressed) {
                           ButtonType.DEVOLVER -> {
                               vModel.doRefund(operation) { redsysOperation ->
                                   TemporalData.lastRefundTicket = redsysOperation
                               }
                           }

                           ButtonType.IMPRIMIR -> {
                               if (operation.result != DENIED && operation.operationType == AUTHORIZATION) {
                                   vModel.printServiceTicket(operation)
                               } else if (operation.result != DENIED && operation.operationType == REFUND) {
                                   vModel.printRefundTicket(operation)
                               }
                           }

                           else -> {

                           }
                       }
                   }

                   if ((operation.operationType == REFUND && operation.refundResponse != null) ||
                       operation.operationType == AUTHORIZATION && operation.result != DENIED) {
                       iMainActivity.openDialog(
                           model = CustomDialog.CustomDialogModel(
                               title = it.getString(R.string.red_sys_operation_title),
                               description = it.getString(R.string.red_sys_operation_description),
                               buttons = if (operation.result != DENIED && operation.operationType == AUTHORIZATION) arrayListOf(
                                   ButtonType.DEVOLVER,
                                   ButtonType.IMPRIMIR
                               ) else arrayListOf(ButtonType.IMPRIMIR)
                           ), response = callback,
                           fragmentManager = childFragmentManager
                       )
                   }
                }

                val llm = LinearLayoutManager(context)
                llm.orientation = LinearLayoutManager.VERTICAL

                vBinding.rvReceiptRedSys.layoutManager = llm
                vBinding.rvReceiptRedSys.adapter = adapter
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.refundResultFlow.collect { success ->
                        vModel.updateList()

                        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                            when (response.buttonPressed) {
                                ButtonType.ACCEPT -> {
                                }

                                else -> {}
                            }
                        }

                        iMainActivity.openDialog(
                            model = CustomDialog.CustomDialogModel(
                                title = getString(R.string.red_sys_operation_title),
                                description = if (success) getString(R.string.red_sys_refund_success) else getString(
                                    R.string.red_sys_refund_failure
                                ),
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            ), response = callback,
                            fragmentManager = childFragmentManager
                        )
                    }
                }

                launch {
                    vModel.updateListFlow.collect { list ->
                        if (list != null && adapter.getData() != list) {
                            adapter.updateData(list)
                        }
                    }
                }
            }
        }
    }

}
