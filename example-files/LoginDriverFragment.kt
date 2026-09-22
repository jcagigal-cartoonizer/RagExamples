package ifac.td.taxi.ui.screen

import android.content.res.Configuration
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentLoginDriverBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.LoginDriverViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDriverFragment : BaseFragment<FragmentLoginDriverBinding, LoginDriverViewModel>(
    R.layout.fragment_login_driver
) {

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: LoginDriverViewModel by viewModel()
    private val safeArgs: LoginDriverFragmentArgs by navArgs()
    private val TAG = "LoginDriverFragment"

    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentLoginDriverBinding.inflate(layoutInflater)

    private var funConCentral = false
    private var funSinCentral = false
    private var funConrefuerzo = false


    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()

        vBinding.tvChangePin.setOnClickListener {
            Logs.d("LoginDriverFragment", "tvChangePin onClick")
            hideKeyboard()
            vModel.clickChangePin()
        }
    }

    private fun setButtons() {
        val connectionMode = safeArgs.connectionMode
        val startTurnMode = safeArgs.startTurnMode

        funConCentral = (connectionMode and 0x01) != 0
        funSinCentral = (connectionMode and 0x02) != 0
        funConrefuerzo = (connectionMode and 0x04) != 0

        vBinding.apply {
            btnCancel.setAction {
                hideKeyboard()
                iMainActivity.navigateBack()
            }

            var btnCnt = 1

            if (funConCentral) {
                btnCnt++
                vBinding.btnConCentral.visibility = View.VISIBLE
            } else {
                //vBinding.btnConCentral.visibility = View.GONE
            }
            //sinCentral
            if (funSinCentral) {
                btnCnt++
                vBinding.btnSinCentral.visibility = View.VISIBLE
            } else {
                vBinding.btnSinCentral.visibility = View.GONE
            }
            //refuerzo
            if (funConrefuerzo) {
                btnCnt++
                vBinding.btnRefuerzo.visibility = View.VISIBLE
            } else {
                vBinding.btnRefuerzo.visibility = View.GONE
            }

            if (startTurnMode == 0) {
                vBinding.container.visibility = View.GONE
                vBinding.edDriver.setText("0000")
                vBinding.edPassword.setText("")
            }

            val params = lytContainerFlowMenu.layoutParams as ConstraintLayout.LayoutParams
            if (btnCnt > 2) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    params.dimensionRatio = "1:1"
                } else {
                    params.dimensionRatio = "3:2"
                }
            } else {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    params.dimensionRatio = "1:1"
                } else {
                    if (funConCentral) {
                        params.dimensionRatio = "5:2"
                    } else {
                        params.dimensionRatio = "3:2"
                    }
                }
            }
            lytContainerFlowMenu.layoutParams = params

            btnConCentral.setAction {
                hideKeyboard()
                clickOnWithCentral()
            }

            btnRefuerzo.setAction {
                hideKeyboard()
                clickOnReinforcement()
            }

            btnSinCentral.setAction {
                hideKeyboard()
                clickOnWithOutCentral()
            }

            if (btnCnt == 2 && (connectionMode and 0x02) != 0) {
                hideKeyboard()
                clickOnWithOutCentral()
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
                    vModel.loginWithoutCentralFlow.collect {
                        loginWithOutCentralFinish()
                    }
                }

                launch {
                    vModel.lastSessionFlow.collect {
                        vBinding.edDriver.setText(it)
                    }
                }

                launch {
                    sharedViewModel.correctLoginFlow.collect {
                        if (safeArgs.startTurnMode == 0) {
                            try {
                                iMainActivity.showToast(
                                    R.string.incorrect_login_no_credentials,
                                    Toast.LENGTH_LONG
                                )
                            } catch (e: IllegalStateException) {
                                Logs.d(TAG, "Couldn't show toast: ${e.message}")
                            }
                        } else {
                            if (!it.first) {
                                errorFields(it.second)
                            }
                        }

                    }
                }

                launch {
                    vModel.noLoginConnectionFlow.collect {
                        vBinding.btnConCentral.setButtonStyle(CustomButton.StyleButton.ENABLE)
                        if (it) {
                            try {
                                iMainActivity.showToast(R.string.error_login)
                            } catch (e: IllegalStateException) {
                                Logs.d(TAG, "Couldn't show toast: ${e.message}")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun clickOnWithCentral() {
        vBinding.apply {
            if (edDriver.isValid(FieldType.USERNAME) && edPassword.isValid(FieldType.PASSWORD)) {
                vBinding.btnConCentral.setButtonStyle(CustomButton.StyleButton.LOADING)

                vModel.loginDriver(
                    driverID = edDriver.text.toString(),
                    password = edPassword.text.toString()
                )


            }
        }
    }

    private fun clickOnReinforcement() {
        vBinding.apply {
            if (edDriver.isValid(FieldType.USERNAME) && edPassword.isValid(FieldType.PASSWORD)) {
                vBinding.btnRefuerzo.setButtonStyle(CustomButton.StyleButton.LOADING)
                vModel.loginDriver(
                    driverID = edDriver.text.toString(),
                    password = edPassword.text.toString(),
                    isReinforcement = true
                )
            }
        }
    }

    private fun clickOnWithOutCentral() {
        vModel.loginWithoutCentral()
    }

    private fun errorFields(showIncorrectCredentials : Boolean) {
        vBinding.apply {
            if (showIncorrectCredentials) {
                edDriver.error = resources.getString(R.string.incorrect_login)
                edDriver.requestFocus()
            } else {
                edDriver.error = null
                edDriver.requestFocus()
            }

            vBinding.apply {
                btnConCentral.setButtonStyle(CustomButton.StyleButton.ENABLE)
                btnConCentral.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                btnRefuerzo.setButtonStyle(CustomButton.StyleButton.ENABLE)
                btnRefuerzo.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                btnSinCentral.setButtonStyle(CustomButton.StyleButton.ENABLE)
                btnSinCentral.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
            }
        }
    }

    private fun loginWithOutCentralFinish() {
        iMainActivity.connectTaximeter()
        iMainActivity.navigateTo(R.id.action_loginDriverFragment_to_homeFragment)
    }
}