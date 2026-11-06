package ifac.td.taxi.ui.screen

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentContactCentralBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.ContactCentralViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactCentralFragment :
    BaseFragment<FragmentContactCentralBinding, ContactCentralViewModel>(
        R.layout.fragment_contact_central
    ) {

    private val TAG = "ContactCentralFragment"

    private val vModel: ContactCentralViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentContactCentralBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vModel.checkITopTaximeter()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
            btnShortBreak.setAction {
                if (sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                    try {
                        iMainActivity.showToast(R.string.short_break_forced)
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    return@setAction
                }
                btnShortBreak.setButtonStyle(CustomButton.StyleButton.LOADING)
                iMainActivity.shortBreakButtonPressed()
            }

            btnVoiceCall.setAction {
                if (sharedViewModel.bravoStateFlow.value.voiceValue) {
                    openVoiceRequestDialog()
                } else {
                    cancelVoiceRequest()
                }

            }

            btnMessages.setAction {
                vModel.navigateToPredefinedMessages()
            }

            btnInformation.setAction {
                vModel.navigateToInformationMessages()
            }
        }
    }

    private fun cancelVoiceRequest() {
        vModel.sendVoiceRequest(false)
        iMainActivity.navigateBack()
    }

    private fun openVoiceRequestDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.sendVoiceRequest(true)
                iMainActivity.navigateBack()
            }
        }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.btn_voice_request),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ),
            response = callback,
            fragmentManager = childFragmentManager
        )
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.shortBreakStatus.collect {
                        it?.let { shortBreakStatus ->
                            Logs.d(TAG, "shortBreakStatus collected $shortBreakStatus:  ZONE - ${sharedViewModel.zoneFlow.value}")
                            tryUpdateShortBreakButton(shortBreakStatus, !sharedViewModel.zoneFlow.value.isNullOrBlank())
                        }
                    }
                }
                launch {
                    sharedViewModel.bravoStateFlow.collect { value ->
                        if (value.voiceValue) {
                            vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.DEFAULT)
                            vBinding.btnVoiceCall.changeText(getString(R.string.btn_voice_request).uppercase())
                        } else {
                            vBinding.btnVoiceCall.changeBackground(CustomButton.BackgroundButtonColor.RED)
                            vBinding.btnVoiceCall.changeText(getString(R.string.btn_cancel_voice_request).uppercase())
                        }
                    }
                }

                launch {
                    vModel.hasPredefinedMessagesFlow.collect {
                        if (it) {
                            vBinding.btnMessages.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        } //TODO: else { send a FreeTextMessage instead of navigate to predefinedMessage screen }
                    }
                }
                launch {
                    vModel.hasShortBreak.collect {
                        sharedViewModel.shiftStatusFlow.value?.currentStatus?.let { status ->
                            if (!vModel.isHired(status) && it) {
                                vBinding.btnShortBreak.visibility = View.VISIBLE
                                vBinding.btnCancel.visibility = View.GONE
                            } else {
                                vBinding.btnShortBreak.visibility = View.GONE
                                vBinding.btnCancel.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                launch {
                    vModel.isITopTaximeter.collect {
                        Logs.d(TAG, "isITopTaximeter collect: $it")
                        if (it) {
                            vBinding.btnVoiceCall.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }
                launch {
                    sharedViewModel.locationType.collect {
                        Logs.d(TAG, "locationTypeFlow: $it")
                        updateShortBreakButton(it)
                    }
                }
            }
        }
    }

    private fun updateShortBreakButton(it: String?) {
        Logs.d(TAG, "updateShortBreakButton: $it")
        if (it != "Z") {
            vBinding.btnShortBreak.setButtonStyle(CustomButton.StyleButton.DISABLE)
        } else {
            sharedViewModel.shortBreakStatus.value?.let {
                tryUpdateShortBreakButton(it, true)
            }
        }
    }

    private fun tryUpdateShortBreakButton(shortBreakStatus: ShortBreakStatus, hasLocation: Boolean) {
        //Disable short break button when is in Soon In Zone
        if (hasLocation) {
            when(shortBreakStatus) {
                ShortBreakStatus.IN_SHORT_BREAK,
                ShortBreakStatus.IN_SHORT_BREAK_FORCED -> {
                    vBinding.btnShortBreak.changeText(getString(R.string.btn_end_short_break).uppercase())
                    vBinding.btnShortBreak.changeBackground(CustomButton.BackgroundButtonColor.RED)
                    vBinding.btnShortBreak.setButtonStyle(CustomButton.StyleButton.ENABLE)
                }

                ShortBreakStatus.CAN_START_SHORT_BREAK -> {
                    vBinding.btnShortBreak.changeText(getString(R.string.btn_short_break).uppercase())
                    vBinding.btnShortBreak.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                    vBinding.btnShortBreak.setButtonStyle(CustomButton.StyleButton.ENABLE)
                }
                ShortBreakStatus.SHORT_BREAK_DISABLED -> {
                    vBinding.btnShortBreak.changeText(getString(R.string.btn_short_break).uppercase())
                    vBinding.btnShortBreak.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                    vBinding.btnShortBreak.setButtonStyle(CustomButton.StyleButton.DISABLE)
                }
            }
        } else {
            if (shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK && shortBreakStatus != ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                vBinding.btnShortBreak.changeText(getString(R.string.btn_short_break).uppercase())
                vBinding.btnShortBreak.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                vBinding.btnShortBreak.setButtonStyle(CustomButton.StyleButton.DISABLE)
            }
        }
    }
}