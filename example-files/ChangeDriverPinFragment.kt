package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentChangeDriverPinBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.ChangeDriverPinViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeDriverPinFragment :
    BaseFragment<FragmentChangeDriverPinBinding, ChangeDriverPinViewModel>(R.layout.fragment_change_driver_pin) {

    private val vModel: ChangeDriverPinViewModel by viewModel()


    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentChangeDriverPinBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (checkParameters()) {
                    changePin()
                }
            }

            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
        }
    }

    private fun changePin() {
        val userPresenter = object : ChangePasswordPinView {
            override fun updateSuccess() {
                val dialogSuccess = CustomDialog.CustomDialogModel(
                    title = resources.getString(R.string.dialog_change_pin),
                    buttons = arrayListOf(ButtonType.ACCEPT)
                )

                val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        iMainActivity.navigateBack()
                    }
                }

                iMainActivity.openDialog(dialogSuccess, callBack, fragmentManager = childFragmentManager)

            }

            override fun updateFailure() {
                val dialogFailed = CustomDialog.CustomDialogModel(
                    title = resources.getString(R.string.dialog_error_change_pin),
                    buttons = arrayListOf(ButtonType.ACCEPT)
                )

                val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response -> }
                iMainActivity.openDialog(dialogFailed, callBack)
            }
        }

        vModel.getUserPresenter(userPresenter)
    }

    private fun checkParameters(): Boolean {
        val currentDriverNumber: String = vBinding.etNumeroConductor?.text.toString()
        val currentDriverPwd: String = vBinding.etPinActual?.text.toString()
        val newDriverPwd: String = vBinding.etNuevoPin?.text.toString()
        val repeatedDriverPwd: String = vBinding.etRepeatPin?.text.toString()

        val dialogAllInputs = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_fill_inputs),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val dialogInvalidNewPin = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_no_match_pin),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val dialogSameAndOldNewPin = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_equal_pin),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                iMainActivity.showToast(R.string.toast_aceptado)
            }
        }

        if (currentDriverNumber.isEmpty() ||
            currentDriverPwd.isEmpty() ||
            newDriverPwd.isEmpty() ||
            repeatedDriverPwd.isEmpty()
        ) {
            iMainActivity.openDialog(dialogAllInputs, callBack)
            return false
        } else if (newDriverPwd != repeatedDriverPwd) {
            iMainActivity.openDialog(dialogInvalidNewPin, callBack)
            return false

        } else if (currentDriverPwd == newDriverPwd) {
            iMainActivity.openDialog(dialogSameAndOldNewPin, callBack)
            return false
        } else {
            return true
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.userCredentialsFlow.collect { userPresenter ->
                        if (userPresenter != null) {
                            vModel.changeDriverPin(
                                userPresenter,
                                vBinding.etNumeroConductor.text.toString().trim(),
                                vBinding.etPinActual.text.toString().trim(),
                                vBinding.etNuevoPin.text.toString().trim()
                            )
                        } else {
                            val dialogNoUser = CustomDialog.CustomDialogModel(
                                title = resources.getString(R.string.dialog_user_error),
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            )

                            val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                { response ->
                                    if (response.buttonPressed == ButtonType.ACCEPT) {
                                        iMainActivity.navigateBack()
                                    }
                                }

                            iMainActivity.openDialog(dialogNoUser, callBack, fragmentManager = childFragmentManager)
                        }
                    }
                }
            }
        }
    }
}