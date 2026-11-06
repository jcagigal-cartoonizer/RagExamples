package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentInformationMessagesBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.InformationMessagesAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.InformationMessageViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InformationMessageFragment :
    BaseFragment<FragmentInformationMessagesBinding, InformationMessageViewModel>(
        R.layout.fragment_information_messages
    ) {

    private val vModel: InformationMessageViewModel by viewModel()

    private lateinit var mAdapter: InformationMessagesAdapter

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentInformationMessagesBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        vBinding.btnCancel.setAction {
            iMainActivity.navigateBack()
        }
        vModel.initVM()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    vModel.informationMessageFlow.collect { informationMessage ->
                        informationMessage?.let {
                            initAdapter(informationMessage)
                        }
                    }
                }
                launch {
                    vModel.selectedInformationMessageFlow.collect { selected ->
                        selected?.let {
                            openInformationMessageDialog(it)
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter(predefinedMessages: List<String>) {
        val listener: (Pair<Int, String>) -> Unit = {
            vModel.changeSelectedInformationMessage(it)
        }
        mAdapter = InformationMessagesAdapter(requireContext(), predefinedMessages, listener)

        vBinding.rvInformationMessage.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun openInformationMessageDialog(selected: Pair<Int, String>) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                //send Response
                vModel.sendInformationMessage(selected.first)
                iMainActivity.showToast(R.string.datos_enviados)
                iMainActivity.autoNavigate()
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.request_information),
                description = selected.second,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }
}