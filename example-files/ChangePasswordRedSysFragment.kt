package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentChangePasswordRedSysBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.ChangePasswordRedSysViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePasswordRedSysFragment :
    BaseFragment<FragmentChangePasswordRedSysBinding, ChangePasswordRedSysViewModel>(
        R.layout.fragment_change_password_red_sys
    ) {

    private val vModel: ChangePasswordRedSysViewModel by viewModel()


    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentChangePasswordRedSysBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
            btnAccept.setAction {
                clickOnAccept()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.changePasswordRedSysFlow.collect { it ->
                        val success = it.first
                        val text = it.second

                        val callback: (CustomDialog.CustomDialogResponse) -> Unit =
                            { response ->
                                when (response.buttonPressed) {
                                    ButtonType.ACCEPT -> {}

                                    else -> {}
                                }
                            }

                        /**iMainActivity.openDialog(
                            model = CustomDialog.CustomDialogModel(
                                title = context?.getString(R.string.red_sys_title),
                                description = if (success) { context?.getString(R.string.dialog_change_password) } else { text ?: getString(R.string.no_response_error) },
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            ),

                            response = callback
                        )*/

                        //if (success) {
                            iMainActivity.navigateBack()
                        //} else {
                        //    vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
//                            Toast.makeText(context, getString(R.string.no_response_error), Toast.LENGTH_LONG).show()
                        //}
                    }
                }
            }
        }
    }

    private fun clickOnAccept() {
        vBinding.apply {
            if (edUser.isValid(FieldType.USERNAME) && edPassword.isValid(FieldType.PASSWORD)
            ) {
                if (edNewPassword.isValid(FieldType.RED_SYS_PASSWORD) && edRepeatNewPassword.isValid(FieldType.REPEAT_PASSWORD, edNewPassword.text.toString())) {
                    vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)

                    vModel.changePassword(
                        user = edUser.text.toString(),
                        password = edPassword.text.toString(),
                        newPassword = edNewPassword.text.toString()
                    )
                }
            }
        }
    }
}