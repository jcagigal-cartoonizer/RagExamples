package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentChangeUserPasswordBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.ChangeUserPasswordViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeUserPasswordFragment :
    BaseFragment<FragmentChangeUserPasswordBinding, ChangeUserPasswordViewModel>(R.layout.fragment_change_user_password) {

    private val vModel: ChangeUserPasswordViewModel by viewModel()

    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentChangeUserPasswordBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                if (checkParameters()) {
                    val userPresenter = object : ChangePasswordPinView {
                        override fun updateSuccess() {
                            val dialogSuccess = CustomDialog.CustomDialogModel(
                                title = resources.getString(R.string.dialog_change_password),
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
                                title = resources.getString(R.string.dialog_error_change_password),
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            )

                            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response -> }
                            iMainActivity.openDialog(dialogFailed, callBack)
                        }

                    }


                    vModel.getUserPresenter(userPresenter)
                }
            }
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
        }
    }

    private fun checkParameters(): Boolean {
        val currentUserPwd: String = vBinding.etActualPwd.text.toString()
        val newUserPwd: String = vBinding.etNewPwd.text.toString()
        val repeatedUserPwd: String = vBinding.etRepeatPwd.text.toString()

        val dialogAllInputs = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_fill_inputs),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val dialogInvalidNewPin = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_no_match_password),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val dialogSameAndOldNewPin = CustomDialog.CustomDialogModel(
            title = resources.getString(R.string.dialog_equal_password),
            buttons = arrayListOf(ButtonType.ACCEPT)
        )

        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                iMainActivity.showToast(R.string.toast_aceptado)
            }
        }

        if (currentUserPwd.isEmpty() ||
            newUserPwd.isEmpty() ||
            repeatedUserPwd.isEmpty()
        ) {
            iMainActivity.openDialog(dialogAllInputs, callBack)
            return false
        } else if (newUserPwd != repeatedUserPwd) {
            iMainActivity.openDialog(dialogInvalidNewPin, callBack)
            return false

        } else if (currentUserPwd == newUserPwd) {
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
                            vModel.changeUserPassword(userPresenter, vBinding.etNewPwd.text.toString().trim())
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