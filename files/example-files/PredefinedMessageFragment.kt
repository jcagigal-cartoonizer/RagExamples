package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPredefinedMessagesBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.PredefinedMessagesAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PredefinedMessageViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PredefinedMessageFragment :
    BaseFragment<FragmentPredefinedMessagesBinding, PredefinedMessageViewModel>(
        R.layout.fragment_predefined_messages
    ) {

    private val messageArgs: PredefinedMessageFragmentArgs by navArgs()

    private val vModel: PredefinedMessageViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private lateinit var mAdapter: PredefinedMessagesAdapter


    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPredefinedMessagesBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
        vModel.initVM(messageArgs.messageId)
    }

    private fun setButtons() {
        vBinding.apply {

            btnNewMessage.setAction {
                openEditableDialog()
            }
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.predefinedMessagesFlow.collect { predefinedMessages ->
                        if (predefinedMessages != null) {
                            vBinding.apply {
                                tvPredefinedMessages.visibility = View.GONE
                                rvPredefinedMessage.visibility = View.VISIBLE
                            }

                            initAdapter(predefinedMessages)
                        } else {
                            vBinding.apply {
                                tvPredefinedMessages.visibility = View.VISIBLE
                                rvPredefinedMessage.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    vModel.selectedPredefinedMessageFlow.collect {
                        it?.let { selected ->
                            openPredefinedMessageDialog(selected)
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter(predefinedMessages: List<String>) {
        val listener: (Pair<Int, String>) -> Unit = {
            vModel.changeSelectedPredefinedMessage(it)
        }
        mAdapter = PredefinedMessagesAdapter(requireContext(), predefinedMessages, listener)

        vBinding.rvPredefinedMessage.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun openPredefinedMessageDialog(selected: Pair<Int, String>) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.SEND) {
                //send Response
                vModel.sendPredefinedMessage(selected.first)
                iMainActivity.navigateBack()
            } else if (response.buttonPressed == ButtonType.EDIT) {
                openEditableDialog(selected)
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.predefined_message),
                description = selected.second,
                buttons = arrayListOf(
                    ButtonType.EDIT,
                    ButtonType.SEND
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun openEditableDialog(selected: Pair<Int, String>? = null) {
        // Capturamos el valor antes de abrir el diálogo para evitar acceder al sharedViewModel
        // cuando el fragment ya no está adjunto a la actividad (crash en onDismiss)
        val dispatchNumber = sharedViewModel.dispatchFlow.value?.dispatchNumber
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.SEND) {
                //send Response
                response.editTextString?.let {
                    vModel.sendMessage(it, dispatchNumber)
                    iMainActivity.navigateBack()
                }
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.write_message),
                editText = selected?.second,
                hint = getString(R.string.message),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.SEND
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }
    /*
        override fun createScreenMenu(): List<ButtonModel>? {
            return listOf(
                menuNewMessage,
                menuCancel,
            )
        }

     */
}