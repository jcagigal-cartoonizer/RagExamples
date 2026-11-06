package ifac.td.taxi.ui.screen

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.interfacom.sdk.taximeter.bravocomm.StatusConnection
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentLoginUserBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.LoginUserViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginUserFragment : BaseFragment<FragmentLoginUserBinding, LoginUserViewModel>(
    R.layout.fragment_login_user
) {
    private val TAG = "LoginUserFragment"
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val vModel: LoginUserViewModel by viewModel()

    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentLoginUserBinding.inflate(layoutInflater)
    private val safeArgs: LoginUserFragmentArgs by navArgs()


    override fun setupComponents() {
        iMainActivity.showHeader(false)
        // iMainActivity.showToast("LoginUserFragment")
        setButtons()

        vModel.checkSavedData()
        vModel.checkHasSettingsPassword()
    }

    private fun setButtons() {
        vBinding.apply {
            btnChangeUser.visibility = View.GONE

            btnCancel.setAction {
                hideKeyboard()
                iMainActivity.navigateBack()
            }
            btnAccept.setAction {
                hideKeyboard()
                clickOnAccept()
            }

            tvChangePassword.setOnClickListener {
                Logs.d(TAG, "tvChangePassword onClick")
                hideKeyboard()
                vModel.clickChangePassword()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.progressBarFlow.collect {
                        vBinding.lytPbDownload.visibility = it.first
                        if (it.first == View.VISIBLE) {
                            vBinding.lytPbDownload.requestFocus()
                        }
                        vBinding.pbDownload.progress = it.second
                    }
                }

                launch {
                    sharedViewModel.correctLoginFlow.collect {
                        if (it.first) {
                            iMainActivity.startBravoService()
                            iMainActivity.saveSharedPreferencesValue("User", vBinding.edUser.text.toString())
                            iMainActivity.saveSharedPreferencesValue("Password", vBinding.edPassword.text.toString())

                            sharedViewModel.xmppStatusFlow.collect { xmpp ->
                                if (xmpp == StatusConnection.CONNECTED) {
                                    vModel.downloadBravoConfiguration()
                                }
                            }
                        } else {
                            vBinding.apply {
                                edUser.error = resources.getString(R.string.incorrect_login)
                                edUser.requestFocus()
                                vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                vBinding.btnAccept.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.downloadedInitialBravoconfigurationFlow.collect {
                        iMainActivity.navigateBack()
                        sharedViewModel.updateProgressBarFlow(View.GONE, 0)
                    }
                }

                launch {
                    vModel.loginDataFlow.collect { (user, pass) ->
                        if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                            vBinding.apply {
                                edUser.setText(user)
                                edPassword.setText(pass)
                                tvChangePassword.visibility = View.VISIBLE
                            }

                            val autoDownload = safeArgs.autoDownloadConfigurationFromMigration
                            if (autoDownload) {
                                clickOnAccept()
                                vModel.migrateConfigsUser()
                                vModel.migrateShiftsAndTrips()
                                vModel.migratePartials()
                                vModel.migratePortugal()
                            }
                        } else {
                            vBinding.tvChangePassword.visibility = View.INVISIBLE
                        }
                    }
                }

                launch {
                    vModel.configurationPasswordDialogFlow.collect { hasPassword ->
                        hasPassword?.let {
                            if (it) {
                                vBinding.apply {
                                    btnChangeUser.setAction {
                                        openConfigurationPasswordDialog()
                                    }
                                    btnChangeUser.visibility = View.VISIBLE
                                    enableEditTexts(false)
                                }
                            } else {
                                vBinding.apply {
                                    btnChangeUser.setAction { }
                                    btnChangeUser.visibility = View.GONE
                                    enableEditTexts(true)
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.correctPasswordFlow.collect { correctPassword ->
                        if (correctPassword) {
                            enableEditTexts(true)
                            vBinding.btnChangeUser.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            iMainActivity.showToast(R.string.pin_incorrecto)
                        }
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
                vModel.loginUser(
                    user = edUser.text.toString().trim(),
                    password = edPassword.text.toString(),
                    fromMigration = safeArgs.autoDownloadConfigurationFromMigration
                )
            }
        }
    }

    private fun enableEditTexts(enable: Boolean) {
        if (enable) {
            vBinding.apply {
                edUser.isEnabled = true
                edUser.isFocusable = true
                edUser.isFocusableInTouchMode = true
                context?.getColor(R.color.white)?.let { edUser.setTextColor(it) }

                edPassword.isEnabled = true
                edPassword.isFocusable = true
                edPassword.isFocusableInTouchMode = true
                context?.getColor(R.color.white)?.let { edPassword.setTextColor(it) }
            }
        } else {
            vBinding.apply {
                edUser.isEnabled = false
                edUser.isFocusable = false
                context?.getColor(R.color.grey)?.let { edUser.setTextColor(it) }

                edPassword.isEnabled = false
                edPassword.isFocusable = false
                context?.getColor(R.color.grey)?.let { edPassword.setTextColor(it) }
            }
        }
    }

    private fun openConfigurationPasswordDialog() {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                val password = response.editTextString
                if (!password.isNullOrEmpty()) {
                    vModel.checkSettingsPassword(password)
                }
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.pin_actual),
                description = getString(R.string.pin_actual_hint),
                editText = "",
                editTextTypePin = true,
                editTextMaxLength = 4,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ), callBack,
            fragmentManager = childFragmentManager
        )
    }
}