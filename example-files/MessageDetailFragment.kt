package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentMessageDetailBinding
import ifac.td.taxi.repository.room.entities.message.MessageType
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.notification.NotificationManager
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.MessageDetailViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessageDetailFragment :
    BaseFragment<FragmentMessageDetailBinding, MessageDetailViewModel>(
        R.layout.fragment_message_detail
    ) {

    private val messageArgs: MessageDetailFragmentArgs by navArgs()

    private val vModel: MessageDetailViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentMessageDetailBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        vModel.initVM(messageArgs.messageId, messageArgs.skipAutoClose)
        setButtons()
        //cancel notification
        context?.let { context ->
            NotificationManager(context).cancelMessageNotification()
        }
        /*
        val flowMenu = IncludeMenuBinding.bind(vBinding.root).flowMenu
        val guideline = IncludeMenuBinding.bind(vBinding.root).guidelineMenu
        flowMenu.setMaxElementsWrap(2)
        guideline.apply {
            val rows = 2
            this.setGuidelinePercent(1.0f - (rows * 0.2).toFloat())
        }

         */

    }

    private fun setButtons() {
        vBinding.apply {
            btnAnswer.setAction {
                //refresh menu
                vModel.stopAutoCloseMessage()
                onClickAnswer()
            }

            btnPrint.setAction {
                vModel.stopAutoCloseMessage()
                vModel.printMessage(messageArgs.messageId)
            }

            btnDelete.setAction {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        vModel.stopAutoCloseMessage()
                        vModel.deleteMessage(messageArgs.messageId)
                        iMainActivity.navigateBack(shouldAutoNavigate = true)
                    }
                }
                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = context?.getString(R.string.dialog_delete_message_title),
                        description = context?.getString(R.string.dialog_delete_message_desc),
                        buttons = arrayListOf(
                            ButtonType.CANCEL,
                            ButtonType.ACCEPT
                        )
                    ),
                    callback,
                    fragmentManager = childFragmentManager
                )
            }

            btnAccept.setAction {
                vModel.stopAutoCloseMessage()
                iMainActivity.navigateBack(shouldAutoNavigate = true)
            }

            btnPredefinedMessage.setAction {
                vModel.stopAutoCloseMessage()
                vModel.messageFlow.value?.let {
                    iMainActivity.navigateTo(
                        MessageDetailFragmentDirections.actionMessageDetailFragmentToPredefinedMessageFragment(
                            it.id,
                            true
                        )
                    )
                }
            }

            btnNewMessage.setAction {
                vModel.stopAutoCloseMessage()
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.SEND && isAdded) {
                        response.editTextString?.let {
                            vModel.sendMessage(response = it, sharedViewModel.dispatchFlow.value?.dispatchNumber)
                        }
                    }
                }
                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = getString(R.string.write_message),
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
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            iMainActivity.navigateBack(shouldAutoNavigate = true)
        }
    }

    private fun onClickAnswer() {
        vBinding.apply {
            btnPredefinedMessage.visibility = View.VISIBLE
            btnNewMessage.visibility = View.VISIBLE

            btnAnswer.visibility = View.GONE
            btnPrint.visibility = View.GONE
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.messageFlow.collect { message ->
                        message?.let {
                            vBinding.apply {
                                val messageTypeText = when (it.messageType) {
                                    MessageType.MESSAGE -> context?.getString(R.string.message_receive)?.uppercase()
                                    MessageType.DISPATCH -> context?.getString(R.string.dispatch_receive)
                                    MessageType.PREDISPATCH -> context?.getString(R.string.predispatch_receive)
                                    else -> null
                                }

                                val allText = buildString {
                                    messageTypeText?.let { appendLine(it) }
                                    appendLine(it.defaultFormatted)
                                    appendLine(it.text)
                                }

                                ticketContent.text = allText
                            }

                            if (message.answers != null) {
                                vBinding.btnAnswer.setAction {
                                    val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                        { response ->
                                            if (response.buttonPressed == ButtonType.ACCEPT && isAdded) {
                                                response.editTextString?.let { responseString ->
                                                    vModel.sendMessage(
                                                        responseString, sharedViewModel.dispatchFlow.value?.dispatchNumber
                                                    )
                                                }
                                            }
                                        }

                                    iMainActivity.openDialog(
                                        model = CustomDialog.CustomDialogModel(
                                            title = getString(R.string.write_message),
                                            messageOptions = message.answers,
                                            buttons = arrayListOf(
                                                ButtonType.CANCEL,
                                                ButtonType.ACCEPT
                                            ),
                                            hint = getString(R.string.dialog_write_custom_message_hint),
                                        ), response = callBack,
                                        fragmentManager = childFragmentManager
                                    )
                                }
                            } else {
                                vBinding.btnAnswer.setAction {
                                    onClickAnswer()
                                }
                            }


                            if (!message.isUrgent) {
                                vBinding.btnAnswer.setButtonStyle(CustomButton.StyleButton.DISABLE)
                            }
                        }
                    }
                }

                launch {
                    vModel.hasPredefinedMessagesFlow.collect {
                        it?.let { hasPredefinedMessages ->
                            if (hasPredefinedMessages) {
                                vBinding.btnPredefinedMessage.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
                        }
                    }
                }

                launch {
                    vModel.closeMessagesFlow.collect {
                        if (it == true) {
                            iMainActivity.navigateBack(shouldAutoNavigate = true)
                        }
                    }
                }

                launch {
                    vModel.buttonTimerState.collect { timerState ->
                        if (timerState != null) {
                            vBinding.btnAccept.showTimerButton(
                                maxSeconds = timerState.maxSeconds,
                                leftSeconds = timerState.remainingSeconds,
                                countdownStartTime = timerState.startTime
                            )
                        } else {
                            vBinding.btnAccept.stopTimer()
                            vBinding.btnAccept.hideTimerButton()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        vBinding.btnAccept.stopTimer()
    }

    override fun onDestroyView() {
        vBinding.btnAccept.stopTimer()
        super.onDestroyView()
    }
}