package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentMessageBinding
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.MessagesAdapter
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.item.DateItem
import ifac.td.taxi.ui.custom.item.GeneralItem
import ifac.td.taxi.ui.custom.item.ListItem
import ifac.td.taxi.viewmodel.MessagesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MessageFragment : BaseFragment<FragmentMessageBinding, MessagesViewModel>(
    R.layout.fragment_message
) {

    private val vModel: MessagesViewModel by viewModel()
    private lateinit var mAdapter: MessagesAdapter

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentMessageBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vModel.loadMessagesByDriverId()
    }

    override fun updateTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.MARK_READ_MESSAGES, true) {
            val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    vModel.markAllMessagesRead()
                }
            }

            iMainActivity.openDialog(
                model = CustomDialog.CustomDialogModel(
                    title = getString(R.string.warning),
                    description = getString(R.string.strMarcarMensajesComoLeidos),
                    buttons = arrayListOf(
                        ButtonType.CANCEL,
                        ButtonType.ACCEPT
                    )
                ),
                response = callback,
                fragmentManager = childFragmentManager
            )
        }
    }

    private fun setButtons() {
        vBinding.apply {
            btnBack.setAction {
                iMainActivity.navigateBack()
            }

            btnDelete.setAction {
                val toDelete = mAdapter.deleteSelected()
                if (toDelete.isNotEmpty()) {
                    vModel.deleteMessages(toDelete)
                }
            }

            btnSelect.setAction {
                mAdapter.activateAndSelect()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.messagesFlow.collect { messageList ->
                        if (messageList != null) {
                            initRecyclerView(messageList)
                        }
                    }
                }
            }
        }
    }

    private fun getListToMutableList(messageList: List<MessageEntity>): MutableList<ListItem> {
        val dateSet = mutableSetOf<String>()
        val sortedList = mutableListOf<ListItem>()

        for (message in messageList) {
            val formattedDate = message.dateFormatted
            if (dateSet.add(formattedDate)) {
                sortedList.add(DateItem(formattedDate = formattedDate))
            }
            sortedList.add(GeneralItem(message = message))
        }

        return sortedList
    }


    private fun initRecyclerView(messages: List<MessageEntity>) {
        mAdapter = MessagesAdapter(
            requireContext(),
            messageList = getListToMutableList(messages),
            listenerOnClick = { message ->
                val action =
                    MessageFragmentDirections.actionMessageFragmentToMessageDetailFragment(
                        message.id,
                        true
                    )
                iMainActivity.navigateTo(action)
            }) {
            mAdapter.activateSelectionMode()
        }

        vBinding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}
