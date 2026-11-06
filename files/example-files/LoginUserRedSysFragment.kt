package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentLoginUserRedSysBinding
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.LoginUserRedSysViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginUserRedSysFragment :
    BaseFragment<FragmentLoginUserRedSysBinding, LoginUserRedSysViewModel>(
        R.layout.fragment_login_user_red_sys
    ) {

    private val vModel: LoginUserRedSysViewModel by viewModel()


    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentLoginUserRedSysBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
        vModel.getUserRedSys()

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
                    vModel.loginRedSysFlow.collect {
                        if (it) {
                            iMainActivity.showToast(R.string.toast_red_sys_login_ok)
                            iMainActivity.navigateBack()
                        } else {
                            vBinding.apply {
                                edUser.error = context?.getString(R.string.incorrect_login)
                                edUser.requestFocus()
                                vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                vBinding.btnAccept.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            }
                        }
                    }
                }

                launch {
                    vModel.userRedSysFlow.collect {
                        vBinding.edUser.setText(it ?: "")
                        vBinding.edPassword.setText(TemporalData.passwordRedSys)
                    }

                }

            }
        }
    }

    private fun clickOnAccept() {
        vBinding.apply {
            if (edUser.isValid(FieldType.USERNAME) && edPassword.isValid(FieldType.PASSWORD)
            ) {
                vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.LOADING)
                vModel.loginRedSys(
                    user = edUser.text.toString(),
                    password = edPassword.text.toString()
                )
            }
        }
    }

}